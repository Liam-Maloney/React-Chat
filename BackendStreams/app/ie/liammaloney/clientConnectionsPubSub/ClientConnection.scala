package ie.liammaloney.clientConnectionsPubSub

import akka.stream.Materializer

import scala.util.{Failure, Success}
import play.api.libs.json.{JsValue, Json}
import akka.actor.{Actor, ActorRef, Props}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import ie.liammaloney.clientConnectionsPubSub.BroadcastPool.MessageFromBroadcastPool

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.http.websocket.{CloseMessage, Message, TextMessage}
import ie.liammaloney.streams.messages.{Response, ResponseToAllClients, ResponseToRequester}

class ClientConnection(clientReference: ActorRef, broadcastPool: ActorRef,
                       requestHandler: (Materializer, Flow[Message, Response, _])) extends Actor {

  implicit private val mat = requestHandler._1
  private val requestStream = requestHandler._2

  private var myName = ""

  override def preStart(): Unit = broadcastPool ! BroadcastPool.AddConnection(self)
  override def postStop(): Unit = broadcastPool ! BroadcastPool.RemoveConnection(self)

  private def runRequestThroughStream(msg: Message) = {

    Source.single[Message](msg).via(requestStream).toMat(Sink.queue[Response])(Keep.right).run().pull()
      .onComplete({

        case Success(Some(response)) => response match {
          case ResponseToRequester(message)   => clientReference ! message
          case ResponseToAllClients(message)  => broadcastPool ! message
        }

        case Failure(reason) => throw reason

      })
  }

  override def receive = {

    case CloseMessage(_, _) => runRequestThroughStream(CloseMessage(statusCode = None, myName))

    case TextMessage(msg) if msg.contains("POST_NEW_CHAT_MESSAGE") => {
      runRequestThroughStream(injectAuthorNameIntoPost(Json.parse(msg)))
    }

    case TextMessage(msg) if msg.contains("JOIN_CHAT") => {
      myName = (Json.parse(msg) \ "name").validate[String].get
      runRequestThroughStream(TextMessage(msg))
    }

    case MessageFromBroadcastPool(msg)  => Console println s"Got a message from broadcaster $msg"; clientReference ! msg
    case msg: Message                   => runRequestThroughStream(msg)
  }


  private val injectAuthorNameIntoPost: (JsValue) => TextMessage = msg => {
    TextMessage(s"""
       |{
       |  "type": "POST_NEW_CHAT_MESSAGE",
       |  "author": "$myName",
       |  "time": "${(msg \ "time").validate[String].get}",
       |  "content": "${(msg \ "content").validate[String].get}"
       |}
       |""".stripMargin)
  }
}

object ClientConnection {
  def props(clientReference: ActorRef, broadcastPool: ActorRef,
            requestHandler: (Materializer, Flow[Message, Response, _])): Props = {

    Props(new ClientConnection(clientReference, broadcastPool, requestHandler))
  }
}