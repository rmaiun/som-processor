package dev.rmaiun.somprocessor.events

import dev.rmaiun.somprocessor.domains.OptimizationRunState
import vulcan.generic.AvroNamespace

sealed trait OptimizationRunUpdateEvent extends Product with Serializable {}

object OptimizationRunUpdateEvent {
  sealed trait OptimizingEvent extends OptimizationRunUpdateEvent
  sealed trait ResultsEvent    extends OptimizationRunUpdateEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class IncrementResult(id: Long) extends ResultsEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class IncrementFinalLog(id: Long) extends ResultsEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class ChangeState(id: Long, state: OptimizationRunState) extends OptimizationRunUpdateEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class SuccessResultReceived(id: Long) extends ResultsEvent
  final case class ErrorResultReceived(id: Long) extends ResultsEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class BindAlgorithm(id: Long, algorithmCode: String, criticalEndTime: Long) extends OptimizingEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class PairRequest(id: Long, requestId: Long) extends OptimizingEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class AssignMessageId(id: Long, messageId: String) extends OptimizingEvent
}
