package dev.rmaiun.somprocessor.dtos

import dev.rmaiun.somprocessor.domains.OptimizationRunRequest

case class StartRequestProcessingEvent(request: OptimizationRunRequest) extends Event("START_REQUEST_PROCESSING")
