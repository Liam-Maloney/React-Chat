package ie.liammaloney.streams

import language._
import akka.stream._
import akka.actor.ActorSystem
import com.google.inject.Inject
import play.api.http.websocket.Message
import ie.liammaloney.streams.messages._
import ie.liammaloney.streams.blueprints._
import akka.stream.scaladsl.{Flow, GraphDSL, Merge}

class StreamProvider @Inject()(appActorSys: ActorSystem) extends BackendInterface {

  override def pluggableRequestStream: Flow[Message, Response, _] = Flow fromGraph linkGraphBlueprints()

  private def linkGraphBlueprints() = GraphDSL.create(
    StreamInputRouter.frontRouterGraph,
    LoginSubSystem(appActorSys.actorOf(LoggedInUsers.props)).loginSubSystemGraph,
    BadRequestsErrorHandler.errorShortCircuitGraph,
    ChatMessagingSubSystem.chatMessageSubSystemGraph)((_,_,_,_)) {

    implicit graphBuilder => (router, logins, errors, chats) => {

      import GraphDSL.Implicits._

      val responder = graphBuilder.add(Merge[Response](3))

      val outputTransformer = graphBuilder.add(Flow[Response].map(_.asBroadcastOrSingleReply))

      router.out0 ~>  logins.in
                      logins.out  ~> responder.in(0)

      router.out1 ~>  chats.in
                      chats.out   ~> responder.in(1)

      router.out2 ~>  errors      ~> responder.in(2)

      responder.out ~> outputTransformer.in

      FlowShape[Message, Response](router.in, outputTransformer.out)
    }
  }

}