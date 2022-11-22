package dev.rmaiun.somprocessor.domains

import java.time.ZonedDateTime

case class OptimizationRun(
  id: Long,
  state: OptimizationRunState,
  algorithmCode: String,
  algorithmNodesQty: Int,
  algorithmReleased: Boolean,
  criticalEndTime: ZonedDateTime,
  resultReceived: Int,
  finalLogsReceivedQty: Int,
  successfulResultReceived: Boolean,
  assignedRequest: Long
)
