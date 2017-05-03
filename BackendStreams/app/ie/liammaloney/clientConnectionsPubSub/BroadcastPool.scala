package ie.liammaloney.clientConnectionsPubSub

import akka.actor.{Actor, ActorRef, Props}
import play.api.http.websocket.Message

class BroadcastPool extends Actor { import BroadcastPool._

  private var connections: Vector[ActorRef] = Vector()

  override def preStart(): Unit = Console println "Connection Pool Actor Started ... "
  override def postStop(): Unit = Console println "Connection Pool Actor Terminated ... "

  override def receive = {
    case AddConnection(newClient)         => connections = connections :+ newClient
    case RemoveConnection(leavingClient)  => connections = connections.filterNot(_ == leavingClient)
    case msg: Message                     => connections foreach (_ ! MessageFromBroadcastPool(msg))
  }

}

object BroadcastPool {
  case class MessageFromBroadcastPool(msg: Message)
  case class AddConnection(newClient: ActorRef)
  case class RemoveConnection(clientWhichLeft: ActorRef)
  def props: Props = Props(new BroadcastPool())
}