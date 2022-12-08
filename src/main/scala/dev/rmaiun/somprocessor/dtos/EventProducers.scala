package dev.rmaiun.somprocessor.dtos

import dev.rmaiun.somprocessor.dtos.EventProducers._
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent
import dev.rmaiun.somprocessor.events.ProcessingEvent.{ CreateSomConnection, FinalizeOptimization, GenerateInputDocumentProcessingEvent, SendSomRequest }
import dev.rmaiun.somprocessor.services.KafkaEventProducer

case class EventProducers[F[_]](
  somInputProducer: SomInputProducer[F],
  somConnectionProducer: SomConnectionProducer[F],
  somRequestSenderProducer: SomRequestSenderProducer[F],
  optimizationRunUpdateProducer: OptimizationRunUpdateProducer[F],
  closeRabbitConnectionEventProducer: FinalizeOptimizationEventProducer[F]
)

object EventProducers {
  type SomInputProducer[F[_]]                  = KafkaEventProducer[F, GenerateInputDocumentProcessingEvent]
  type SomConnectionProducer[F[_]]             = KafkaEventProducer[F, CreateSomConnection]
  type SomRequestSenderProducer[F[_]]          = KafkaEventProducer[F, SendSomRequest]
  type OptimizationRunUpdateProducer[F[_]]     = KafkaEventProducer[F, OptimizationRunUpdateEvent]
  type FinalizeOptimizationEventProducer[F[_]] = KafkaEventProducer[F, FinalizeOptimization]
}
