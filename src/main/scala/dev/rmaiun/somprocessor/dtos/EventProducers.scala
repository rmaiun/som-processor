package dev.rmaiun.somprocessor.dtos

import dev.rmaiun.somprocessor.dtos.EventProducers.{OptimizationRunUpdateProducer, SomInputProducer}
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent
import fs2.kafka.KafkaProducer

case class EventProducers[F[_]](
  somInputProducer: SomInputProducer[F],
  optimizationRunUpdateProducer: OptimizationRunUpdateProducer[F],
)

object EventProducers {
  type SomInputProducer[F[_]] = KafkaProducer.Metrics[F, String, GenerateInputDocumentEvent]
  type OptimizationRunUpdateProducer[F[_]] = KafkaProducer.Metrics[F, String, OptimizationRunUpdateEvent]
}
