package ie.liammaloney.streams

import org.scalatest._
import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.stream.ActorMaterializer
import ie.liammaloney.streams.messages._
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.scaladsl.{Keep, Sink, Source}
import ie.liammaloney.streams.blueprints.LoginSubSystem
import ie.liammaloney.streams.blueprints.LoggedInUsers.WhoIsLoggedIn
import ie.liammaloney.streams.MockedLoggedInUsers.SetInitialTestState

class LoginStreamTest extends FeatureSpec with GivenWhenThen {

  implicit val system = ActorSystem("testSystem")
  implicit val materializer = ActorMaterializer()
  implicit val t: akka.util.Timeout = new FiniteDuration(1, SECONDS)

  feature("Ability to log a user in.") {

    scenario("Request comes into the stream to join chat") {
      Given("a system with no logged in users")

      val mockedLoginState = system.actorOf(MockedLoggedInUsers.props)
      val graphUnderTest = LoginSubSystem(mockedLoginState).loginSubSystemGraph

      When("a request is made to log a user in")

      val request = JoinChat("Liam")
      Await.result(Source.single(request).via(graphUnderTest).toMat(Sink.queue())(Keep.right).run().pull(), 10 second).get

      Then("the users name should be stored in the state of logged in users")

      val result = Await.result(mockedLoginState ? WhoIsLoggedIn, 1 second).asInstanceOf[Vector[String]]
      assert(result == Vector("Liam"))

    }

    scenario("A new joiner needs to know the other initial users of the chat.") {
      Given("a system with some logged in users")

      val mockedLoginState = system.actorOf(MockedLoggedInUsers.props)
      val graphUnderTest = LoginSubSystem(mockedLoginState).loginSubSystemGraph
      Await.result(mockedLoginState ? SetInitialTestState(Vector("Tom", "Dick", "Harry")), 1 second)

      When("a request is made to log a new user in")

      val request = JoinChat("Liam")
      val runnable = Source.single(request).via(graphUnderTest).toMat(Sink.queue())(Keep.right).run()
      val emittedValue = Await.result(runnable.pull(), 10 second) getOrElse
          assert(false, "There was an error running the test graph.")

      Then("the users name should be stored in the state of logged in users, and the existing users should be emitted" +
        "as an event on the other side")

      val result = Await.result(mockedLoginState ? WhoIsLoggedIn, 1 second).asInstanceOf[Vector[String]]
      val expected = Vector("Tom", "Dick", "Harry", "Liam")
      assert((result == expected) && (emittedValue == JoinAccepted(expected)))

    }

  }

  feature("Ability to log a user out.") {
    scenario("a notification is sent that a user has left the chat.") {
      Given("a system with existing users")

      val mockedLoginState = system.actorOf(MockedLoggedInUsers.props)
      val graphUnderTest = LoginSubSystem(mockedLoginState).loginSubSystemGraph
      Await.result(mockedLoginState ? SetInitialTestState(Vector("Liam", "Laura")), 1 second)

      When("a request is made to log a user out")

      val request = LogUserOut("Liam")
      val runnable = Source.single(request).via(graphUnderTest).toMat(Sink.queue())(Keep.right).run()
      val emittedValue = Await.result(runnable.pull(), 10 second) getOrElse
        assert(false, "There was an error running the test graph.")

      Then("the users name should be removed from the state of logged in users, and the existing users should be emitted" +
        "as an event on the other side")

      val remainingUsers = Await.result(mockedLoginState ? WhoIsLoggedIn, 1 second).asInstanceOf[Vector[String]]

      assert(emittedValue == UserLeft("Liam") && remainingUsers == Vector("Laura"))

    }
  }

}

class MockedLoggedInUsers extends Actor {
  import ie.liammaloney.streams.blueprints.LoggedInUsers._

  var loggedInUsers = Vector.empty[String]

  override def receive = {
    case WhoIsLoggedIn          => sender() ! loggedInUsers
    case RegisterNewUser(name)  => loggedInUsers = loggedInUsers :+ name; sender() ! loggedInUsers
    case RemoveUser(name)       => loggedInUsers = loggedInUsers.filterNot(_ equals name)
    case SetInitialTestState(u) => loggedInUsers = u ; sender() ! "continue"
  }
}

object MockedLoggedInUsers {
  def props: Props = Props(new MockedLoggedInUsers())
  case class SetInitialTestState(loggedInUsers: Vector[String])
}
