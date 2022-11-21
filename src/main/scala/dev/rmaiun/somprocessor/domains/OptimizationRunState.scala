package dev.rmaiun.somprocessor.domains

import enumeratum.{ Enum, EnumEntry, VulcanEnum }
import vulcan.generic.AvroNamespace

@AvroNamespace("dev.rmaiun.somprocessor")
sealed trait OptimizationRunState extends EnumEntry {}
object OptimizationRunState extends Enum[OptimizationRunState] with VulcanEnum[OptimizationRunState] {
  final case object Init       extends OptimizationRunState
  final case object Optimizing extends OptimizationRunState
  final case object Results    extends OptimizationRunState
  final case object Finished   extends OptimizationRunState

  val values: IndexedSeq[OptimizationRunState] = findValues
}
