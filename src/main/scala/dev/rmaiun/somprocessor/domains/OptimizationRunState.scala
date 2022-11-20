package dev.rmaiun.somprocessor.domains

sealed trait OptimizationRunState {}
object OptimizationRunState {
  final case object Init       extends OptimizationRunState
  final case object Optimizing extends OptimizationRunState
  final case object Results    extends OptimizationRunState
  final case object Finished   extends OptimizationRunState
}
