package dev.rmaiun.somprocessor.dtos

import dev.rmaiun.somprocessor.dtos.EventProducers.{ OptimizationRunUpdateProducer, SomConnectionProducer, SomInputProducer, SomRequestSenderProducer }
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent
import dev.rmaiun.somprocessor.events.ProcessingEvent.{ CreateSomConnection, GenerateInputDocumentProcessingEvent, SendSomRequest }
import dev.rmaiun.somprocessor.services.KafkaEventProducer

case class EventProducers[F[_]](
  somInputProducer: SomInputProducer[F],
  somConnectionProducer: SomConnectionProducer[F],
  somRequestSenderProducer: SomRequestSenderProducer[F],
  optimizationRunUpdateProducer: OptimizationRunUpdateProducer[F]
)

object EventProducers {
  type SomInputProducer[F[_]]              = KafkaEventProducer[F, GenerateInputDocumentProcessingEvent]
  type SomConnectionProducer[F[_]]         = KafkaEventProducer[F, CreateSomConnection]
  type SomRequestSenderProducer[F[_]]      = KafkaEventProducer[F, SendSomRequest]
  type OptimizationRunUpdateProducer[F[_]] = KafkaEventProducer[F, OptimizationRunUpdateEvent]
}
