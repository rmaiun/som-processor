package dev.rmaiun.somprocessor.events

import dev.rmaiun.somprocessor.domains.OptimizationRunState
import dev.rmaiun.somprocessor.domains.OptimizationRunState.findValues
import enumeratum.{Enum, EnumEntry, VulcanEnum}
import vulcan.generic.AvroNamespace

sealed trait OptimizationRunUpdateEvent  extends Product with Serializable {}
object OptimizationRunUpdateEvent {
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class IncrementResult(id: Long)                          extends OptimizationRunUpdateEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class IncrementFinalLog(id: Long)                        extends OptimizationRunUpdateEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class ChangeState(id: Long, state: OptimizationRunState) extends OptimizationRunUpdateEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class SuccessfulResultReceived(id: Long)                 extends OptimizationRunUpdateEvent
  @AvroNamespace("dev.rmaiun.somprocessor")
  final case class BindAlgorithm(id: Long, algorithmId: Long)         extends OptimizationRunUpdateEvent
}
