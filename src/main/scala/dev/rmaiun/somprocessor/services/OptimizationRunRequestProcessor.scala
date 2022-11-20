package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import dev.rmaiun.somprocessor.dtos.StartRequestProcessingEvent

case class OptimizationRunRequestProcessor[F[_]:Sync]() {
  def startRequestProcessing(event:StartRequestProcessingEvent):F[Unit] = {
    ???
  }
}
