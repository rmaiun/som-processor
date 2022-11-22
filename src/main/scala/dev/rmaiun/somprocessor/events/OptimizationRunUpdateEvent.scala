package dev.rmaiun.somprocessor.events

import dev.rmaiun.somprocessor.domains.OptimizationRunState
import vulcan.generic.AvroNamespace

sealed trait OptimizationRunUpdateEvent extends Product with Serializable {}
object OptimizationRunUpdateEvent {
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class IncrementResult(id: Long) extends OptimizationRunUpdateEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class IncrementFinalLog(id: Long) extends OptimizationRunUpdateEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class ChangeState(id: Long, state: OptimizationRunState) extends OptimizationRunUpdateEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class SuccessfulResultReceived(id: Long) extends OptimizationRunUpdateEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class BindAlgorithm(id: Long, algorithmCode: String, criticalEndTime: Long) extends OptimizationRunUpdateEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class PairRequest(id: Long, requestId: Long) extends OptimizationRunUpdateEvent
}