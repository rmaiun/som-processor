package dev.rmaiun.somprocessor.dtos

import dev.rmaiun.somprocessor.dtos.ProcessingEvent.{ CreateSomConnection, GenerateInputDocumentProcessingEvent, SendSomRequest }
import dev.rmaiun.somprocessor.dtos.EventProducers.{ OptimizationRunUpdateProducer, SomConnectionProducer, SomInputProducer, SomRequestSenderProducer }
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent
import fs2.kafka.KafkaProducer

case class EventProducers[F[_]](
  somInputProducer: SomInputProducer[F],
  somConnectionProducer: SomConnectionProducer[F],
  somRequestSenderProducer: SomRequestSenderProducer[F],
  optimizationRunUpdateProducer: OptimizationRunUpdateProducer[F]
)

object EventProducers {
  type SomInputProducer[F[_]]              = KafkaProducer.Metrics[F, String, GenerateInputDocumentProcessingEvent]
  type SomConnectionProducer[F[_]]         = KafkaProducer.Metrics[F, String, CreateSomConnection]
  type SomRequestSenderProducer[F[_]]      = KafkaProducer.Metrics[F, String, SendSomRequest]
  type OptimizationRunUpdateProducer[F[_]] = KafkaProducer.Metrics[F, String, OptimizationRunUpdateEvent]
}
