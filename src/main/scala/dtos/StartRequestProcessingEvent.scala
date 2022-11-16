package dtos

import domains.OptimizationRunRequest

case class StartRequestProcessingEvent(request: OptimizationRunRequest) extends Event("START_REQUEST_PROCESSING")
