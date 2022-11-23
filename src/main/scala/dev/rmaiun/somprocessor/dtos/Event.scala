package dev.rmaiun.somprocessor.dtos

import dev.rmaiun.somprocessor.domains.OptimizationRunRequest

trait Event

object Event {
  case class GenerateInputDocumentEvent(optimizationRunId: Long)              extends Event
  case class StartRequestProcessingEvent(request: OptimizationRunRequest)     extends Event
  case class CreateSomConnection(optimizationId: Long, algorithmCode: String) extends Event
  case class SendSomRequest(optimizationId: Long, algorithmCode: String)      extends Event
}
