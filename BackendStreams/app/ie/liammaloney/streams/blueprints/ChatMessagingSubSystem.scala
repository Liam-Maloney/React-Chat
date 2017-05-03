package ie.liammaloney.streams.blueprints

import akka.stream.{FlowShape, Graph}
import akka.stream.scaladsl.{Flow, GraphDSL}
import ie.liammaloney.streams.messages.{ChatRequest, NewChatMessage, PostNewChatMessage, Response}

object ChatMessagingSubSystem {

  val chatMessageSubSystemGraph: Graph[FlowShape[ChatRequest, Response], _] = GraphDSL.create() {
    implicit graphBuilder =>

      val chatHandler = graphBuilder.add(Flow[ChatRequest].map({
        case PostNewChatMessage(name, time, content) => NewChatMessage(name, time, content)
      }))

      FlowShape(chatHandler.in, chatHandler.out)
  }
}
