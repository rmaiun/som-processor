package services

import dtos.StartRequestProcessingEvent
import zio.Task

case class OptimizationRunRequestProcessor() {
  def startRequestProcessing(event:StartRequestProcessingEvent):Task[Unit] = {
    ???
  }
}
