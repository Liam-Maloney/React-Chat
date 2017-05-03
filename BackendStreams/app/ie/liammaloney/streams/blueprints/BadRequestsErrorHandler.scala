package ie.liammaloney.streams.blueprints

import akka.stream.{FlowShape, Graph}
import akka.stream.scaladsl.{Flow, GraphDSL}
import ie.liammaloney.streams.messages.{BadRequest, ErrorNotification, NotifyBadRequest, Response}

object BadRequestsErrorHandler {

  val errorShortCircuitGraph: Graph[FlowShape[ErrorNotification, Response], _] = GraphDSL.create() {
    implicit graphBuilder =>

      val transformErrorToResponse = graphBuilder.add(Flow[ErrorNotification].map({
        case BadRequest(reason) => NotifyBadRequest(reason).asInstanceOf[Response]
      }))

      FlowShape[ErrorNotification, Response](transformErrorToResponse.in, transformErrorToResponse.out)
  }
}
