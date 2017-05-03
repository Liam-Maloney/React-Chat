package controllers

import javax.inject._
import play.api.mvc._
import play.api.Configuration
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.http.websocket.Message
import play.api.libs.streams.ActorFlow
import ie.liammaloney.streams.BackendInterface
import ie.liammaloney.clientConnectionsPubSub.{BroadcastPool, ClientConnection}

@Singleton
class HomeController @Inject()(implicit val actorSystem: ActorSystem,
                               implicit val mat: Materializer,
                               streamGraphAssembler: BackendInterface,
                               playconfiguration: Configuration) extends Controller {

  private val serverIP = playconfiguration.getString("serverIP") getOrElse (
    throw new Exception("Could not read server IP from conf file")
  )

  def home = Action { Ok(views.html.home(serverIP)) }

  def socket = WebSocket.accept[Message, Message] { _ =>
    ActorFlow.actorRef (
      responseForwarder => ClientConnection props(
        responseForwarder,
        SingleEventStream.broadcaster,
        (mat, SingleEventStream.stream)
      )
    )
  }

  object SingleEventStream {
    val broadcaster = actorSystem.actorOf(BroadcastPool.props)
    val stream = streamGraphAssembler.pluggableRequestStream
  }

}
