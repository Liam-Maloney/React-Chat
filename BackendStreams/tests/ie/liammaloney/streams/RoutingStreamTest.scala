package ie.liammaloney.streams

import org.scalatest._
import akka.actor.ActorSystem
import scala.concurrent.Await
import scala.concurrent.duration._
import ie.liammaloney.streams.messages._
import akka.stream.{ActorMaterializer, FlowShape}
import ie.liammaloney.streams.blueprints.StreamInputRouter._
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, Merge, Sink, Source}
import play.api.http.websocket.{CloseMessage, Message, TextMessage}

class RoutingStreamTest extends FeatureSpec with GivenWhenThen {

  info("This test suit ensures the correct routing of incoming requests to the correct substreams.")

  implicit val system = ActorSystem("testSystem")
  implicit val materializer = ActorMaterializer()

  private val graphUnderTest: MessageRouter = frontRouterGraph

  private val outputReaderUtil = GraphDSL.create(graphUnderTest) { implicit b =>
    (underTest) =>
      import GraphDSL.Implicits._

      val loginRouteOutput  = b.add(Flow[LoginRequest].map(r => (r, "Came through login route")))
      val errorRouteOutput  = b.add(Flow[ErrorNotification].map(r => (r, "Came through error route")))
      val chatRouteOutput   = b.add(Flow[ChatRequest].map(r => (r, "Came through chat route")))

      val mergeResults = b.add(Merge[(Request, String)](3))

      underTest.out0 ~> loginRouteOutput  ~> mergeResults
      underTest.out1 ~> chatRouteOutput   ~> mergeResults
      underTest.out2 ~> errorRouteOutput  ~> mergeResults


      new FlowShape[Message, (Request, String)](underTest.in, mergeResults.out)
  }

  private def passthroughStream(req: Message): (Request, String) = {
    Await.result(Source.single(req).via(outputReaderUtil).toMat(Sink.queue())(Keep.right).run().pull(), 1 second).get
  }

  feature("Ability to route session related requests to the login stream") {

    scenario("a client wishes to join the chat.") {
      Given("a client requests to join the chat")

      val requestToJoinChat = TextMessage(
        raw"""
           |
           |{"type": "JOIN_CHAT", "name": "mock"}
           |
           """.stripMargin
      )

      When("the request is passed into the router")
      Then("it should be available on the outlet pointing to the login stream")
      assert(passthroughStream(requestToJoinChat)  == (JoinChat("mock"), "Came through login route"))
    }

    scenario("a client has left the chat.") {
      Given("a client connection has closed")
      When("the request is passed into the router")
      Then("it should be available on the outlet pointing to the login stream")
      assert(passthroughStream(CloseMessage(0, "mock"))  == (LogUserOut("mock"), "Came through login route"))
    }

  }

  feature("Ability to route chat related requests to the chat stream") {

    scenario("a client posts a new message to the backend") {
      Given("a request comes in to post a new chat message")

      val requestToPostMessage = TextMessage(
        raw"""
             |
             |{"type": "POST_NEW_CHAT_MESSAGE", "author": "mock1", "time":"mock2", "content": "mock3"}
             |
           """.stripMargin
      )

      When("the request is passed into the router")
      Then("it should appear on the outlet pointing to the messaging subsystem")
      assert(passthroughStream(requestToPostMessage)  == (PostNewChatMessage("mock1", "mock2", "mock3"), "Came through chat route"))
    }
  }

  feature("Ability route invalid requests to the error stream.") {

    scenario("No type field specified in request") {
      Given("a request with no type field")

      val noTypeInRequest = TextMessage(
        raw"""
           |
           |{"author": "mock1", "time":"mock2", "content": "mock3"}
           |
           """.stripMargin)

      When("the request is passed into the router")
      Then("it should appear on the outlet pointing to the error subsystem.")

      assert(
        passthroughStream(noTypeInRequest) ==
          (BadRequest("Requests require a 'type' field to signify the type of request accepted"), "Came through error route")
      )

    }

    scenario("Unsupported type field specified in request") {
      Given("a request with no type field")

      val noTypeInRequest = TextMessage(
        raw"""
           |
           |{"type": "garbage", "author": "mock1", "time":"mock2", "content": "mock3"}
           |
           """.stripMargin)

      When("the request is passed into the router")
      Then("it should appear on the outlet pointing to the error subsystem.")

      assert(
        passthroughStream(noTypeInRequest) ==
          (BadRequest("Unknown request type in request"), "Came through error route")
      )

    }

    scenario("Badly formed Json in request") {
      Given("a request with no type field")

      val badlyFormedJson = TextMessage(
        raw"""
           |
           |{"author": "mock1", "time":{}}}}{}{}"mock2", "content": "mock3"}
           |
           """.stripMargin)

      When("the request is passed into the router")
      Then("it should appear on the outlet pointing to the error subsystem.")

      assert(
        passthroughStream(badlyFormedJson) ==
          (BadRequest("Badly formed Json in request"), "Came through error route")
      )

    }
  }

}
