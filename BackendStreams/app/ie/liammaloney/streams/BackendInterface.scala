package ie.liammaloney.streams

import akka.stream.scaladsl.Flow
import play.api.http.websocket.Message
import com.google.inject.ImplementedBy
import ie.liammaloney.streams.messages.Response

@ImplementedBy(classOf[StreamProvider]) trait BackendInterface {
  def pluggableRequestStream: Flow[Message, Response, _]
}
