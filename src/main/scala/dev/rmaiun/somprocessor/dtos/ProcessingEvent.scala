package dev.rmaiun.somprocessor.dtos

import dev.rmaiun.somprocessor.domains.OptimizationRunRequest

trait ProcessingEvent

object ProcessingEvent {
  case class GenerateInputDocumentProcessingEvent(optimizationRunId: Long, algorithmCode: String) extends ProcessingEvent
  case class StartRequestProcessingProcessingEvent(request: OptimizationRunRequest)               extends ProcessingEvent
  case class CreateSomConnection(optimizationId: Long, algorithmCode: String, messageId: String)  extends ProcessingEvent
  case class SendSomRequest(optimizationId: Long, algorithmCode: String, messageId: String)       extends ProcessingEvent
  case class DisconnectFromSom(optimizationId: Long, algorithmCode: String)                       extends ProcessingEvent
}
