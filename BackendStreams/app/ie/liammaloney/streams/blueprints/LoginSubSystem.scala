package ie.liammaloney.streams.blueprints

import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.stream.{FlowShape, Graph}
import ie.liammaloney.streams.messages._
import akka.actor.{Actor, ActorRef, Props}
import akka.stream.scaladsl.{Flow, GraphDSL}
import scala.concurrent.ExecutionContext.Implicits.global
import ie.liammaloney.streams.blueprints.LoggedInUsers.{RegisterNewUser, RemoveUser, WhoIsLoggedIn}

case class LoginSubSystem(loggedInUsersHandler: ActorRef) {

  implicit val t: akka.util.Timeout = new FiniteDuration(1, SECONDS)

  val loginSubSystemGraph: Graph[FlowShape[LoginRequest, Response], _] = GraphDSL.create() {
    implicit graphBuilder =>

      val transformRequestToResponse = graphBuilder.add(Flow[LoginRequest].map {

        case JoinChat(user) => {
          if (usernameAlreadyTaken(user)) LoginNameTaken else JoinAccepted(getUsersWithNewUserLoggedIn(user))
        }

        case LogUserOut(name) => {
          loggedInUsersHandler ! RemoveUser(name)
          UserLeft(name)
        }

      })

      FlowShape(transformRequestToResponse.in, transformRequestToResponse.out)
  }

  private def usernameAlreadyTaken(name: String) = {
    getLoggedInUsers.map(_.toLowerCase).contains(name.toLowerCase())
  }

  private def getLoggedInUsers = {
    Await.result(loggedInUsersHandler ? WhoIsLoggedIn, 1 seconds).asInstanceOf[Vector[String]]
  }

  private def getUsersWithNewUserLoggedIn(newUser: String) = {
    Await.result(loggedInUsersHandler ? RegisterNewUser(newUser), 1 seconds).asInstanceOf[Vector[String]]
  }

}

class LoggedInUsers extends Actor {
  import LoggedInUsers._

  var loggedInUsers = Vector.empty[String]

  override def receive = {
    case WhoIsLoggedIn          => sender() ! loggedInUsers
    case RegisterNewUser(name)  => loggedInUsers = loggedInUsers :+ name; sender() ! loggedInUsers
    case RemoveUser(name)       => loggedInUsers = loggedInUsers.filterNot(_ equals name)
  }
}

object LoggedInUsers {
  def props: Props = Props(new LoggedInUsers())
  case object WhoIsLoggedIn
  case class RegisterNewUser(name: String)
  case class RemoveUser(name: String)
}
