package ie.liammaloney.streams.messages

import play.api.http.websocket.{Message, TextMessage}
import play.api.libs.json.{JsValue, Json}


// Requests which may act as inputs to the stream.


trait Request

trait LoginRequest extends Request
case class JoinChat(name: String) extends LoginRequest
case class LogUserOut(name: String) extends LoginRequest

trait ChatRequest extends Request
case class PostNewChatMessage(author: String, time: String, content: String) extends ChatRequest

trait ErrorNotification extends Request
case class BadRequest(reason: String) extends ErrorNotification


// Responses which materialize from the end of the stream.


trait Response {
  def asBroadcastOrSingleReply: Response
}

case class ResponseToRequester(msg: Message) extends Response {
  override def asBroadcastOrSingleReply = this
}

case class ResponseToAllClients(msg: Message) extends Response {
  override def asBroadcastOrSingleReply = this
}

case class JoinAccepted(loggedInUsers: Vector[String]) extends Response {

  override def asBroadcastOrSingleReply = ResponseToAllClients(TextMessage(toJson.toString()))

  def toJson: JsValue = {
    Json.parse(
      s"""
         |{
         |  "type": "JOIN_ACCEPTED",
         |  "loggedInUsers": ${asJsonArray(loggedInUsers)}
         |}
         |
       |""".stripMargin)
  }

  private def asJsonArray(data: Vector[String]) = {
    loggedInUsers.foldLeft("[")((acc, currentName) => acc + s"""{"name": "$currentName"},""").dropRight(1) + "]"
  }

}

case object LoginNameTaken extends Response {

  override def asBroadcastOrSingleReply = ResponseToRequester(TextMessage(toJson.toString()))

  def toJson: JsValue = {
    Json.parse(
      s"""
         |{
         |  "type": "LOGIN_NAME_TAKEN"
         |}
         |
       |""".stripMargin)
  }
}

case class UserLeft(name: String) extends Response {

  override def asBroadcastOrSingleReply = ResponseToAllClients(TextMessage(toJson.toString()))

  def toJson: JsValue = {
    Json.parse(
      s"""
         |{
         |  "type": "USER_LEFT",
         |  "name": "$name"
         |}
         |
       |""".stripMargin)
  }

}

case class NewChatMessage(author: String, time: String, content: String) extends Response {

  override def asBroadcastOrSingleReply = ResponseToAllClients(TextMessage(toJson.toString()))

  def toJson: JsValue = {
    Json.parse(
      s"""
         |{
         |  "type": "NEW_CHAT_MESSAGE",
         |  "author": "$author",
         |  "time": "$time",
         |  "content": "$content"
         |}
         |
       |""".stripMargin)
  }

}

case class NotifyBadRequest(reason: String) extends Response {

  override def asBroadcastOrSingleReply = ResponseToRequester(TextMessage(toJson.toString()))

  def toJson: JsValue = {
    Json.parse(
      s"""
         |{
         |  "type": "BAD_REQUEST",
         |  "reason": "$reason"
         |}
         |
       |""".stripMargin)
  }

}