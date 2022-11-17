package events

import domains.OptimizationRunState

sealed trait OptimizationRunUpdateEvent {}
object OptimizationRunUpdateEvent {
  final case class IncrementResult(id: Long)   extends OptimizationRunUpdateEvent
  final case class IncrementFinalLog(id: Long) extends OptimizationRunUpdateEvent
  final case class ChangeState(id: Long, state: OptimizationRunState)
  final case class SuccessfulResultReceived(id: Long)
  final case class BindAlgorithm(id: Long, algorithmId: Long)
}
