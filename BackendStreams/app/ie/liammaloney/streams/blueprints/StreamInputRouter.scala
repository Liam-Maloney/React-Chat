package ie.liammaloney.streams.blueprints

import ie.liammaloney.streams.messages._
import akka.stream.{FanOutShape3, Graph}
import scala.util.{Failure, Success, Try}
import play.api.libs.json.{JsSuccess, JsValue, Json}
import akka.stream.scaladsl.{Flow, GraphDSL, Partition}
import play.api.http.websocket.{CloseMessage, Message, TextMessage}

object StreamInputRouter {

  type MessageRouter = Graph[FanOutShape3[Message, LoginRequest, ChatRequest, ErrorNotification], _]

  private val routeToLoginSystem = 0
  private val routeToChatMessageSystem  = 1
  private val routeToErrorHandler = 2

  val frontRouterGraph: MessageRouter = GraphDSL.create() { implicit graphBuilder =>
    import GraphDSL.Implicits._

    val mapWebsocketTextToCaseClass = graphBuilder.add(Flow[Message].map(
      incomingMessage => webSocketToCaseClass(incomingMessage))
    )

    val router = graphBuilder.add(Partition[Request](3, {
      case _: LoginRequest      => routeToLoginSystem
      case _: ChatRequest       => routeToChatMessageSystem
      case _: ErrorNotification => routeToErrorHandler
    }))

    val toLoginType:  Request => LoginRequest       = { case login: LoginRequest      => login }
    val toChatType:   Request => ChatRequest        = { case chat:  ChatRequest       => chat  }
    val toErrorType:  Request => ErrorNotification  = { case error: ErrorNotification => error }

    val mapToLoginType  = graphBuilder add ( Flow[Request] map toLoginType )
    val mapToChatType   = graphBuilder add ( Flow[Request] map toChatType  )
    val mapToErrorType  = graphBuilder add ( Flow[Request] map toErrorType )

    mapWebsocketTextToCaseClass.out ~> router.in
    router.out(routeToLoginSystem)        ~> mapToLoginType.in
    router.out(routeToChatMessageSystem)  ~> mapToChatType.in
    router.out(routeToErrorHandler)       ~> mapToErrorType.in

    new FanOutShape3(
      mapWebsocketTextToCaseClass.in,
      mapToLoginType.out,
      mapToChatType.out,
      mapToErrorType.out
    )
  }

  private val webSocketToCaseClass: Message => Request = {

    case CloseMessage(_, name) => LogUserOut(name)

    case TextMessage(text) => Try(Json.parse(text).validate[JsValue]) match {
      case Success(value) => {
        val json = value.get
        (json \ "type").validate[String] match {
          case messageType: JsSuccess[String] => messageType.value match {
            case "JOIN_CHAT" => JoinChat((json \ "name").validate[String].get)
            case "POST_NEW_CHAT_MESSAGE" => PostNewChatMessage(
              (json \ "author").validate[String].get,
              (json \ "time").validate[String].get,
              (json \ "content").validate[String].get
            )
            case _ => BadRequest("Unknown request type in request")
          }
          case _ => BadRequest("Requests require a 'type' field to signify the type of request accepted")
        }
      }
      case Failure(_) => BadRequest("Badly formed Json in request")
    }
  }

}
