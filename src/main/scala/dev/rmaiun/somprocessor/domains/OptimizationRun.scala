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
  assignedRequest: Long,
  messageId: String
)

object OptimizationRun {
  val updateOptimizationRunTopic = "opt_run_updates"
  val generateInputFileTopic     = "generate_som_input"
  val createSomConnectionTopic   = "som_connection_create"
  val sendSomInputTopic          = "send_som_input"
}
