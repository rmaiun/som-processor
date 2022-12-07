package dev.rmaiun.somprocessor.services

import cats.effect.IO
import dev.rmaiun.somprocessor.domains.OptimizationRun
import dev.rmaiun.somprocessor.dtos.EventProducers
import dev.rmaiun.somprocessor.dtos.EventProducers.{ OptimizationRunUpdateProducer, SomConnectionProducer, SomInputProducer, SomRequestSenderProducer }
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent
import dev.rmaiun.somprocessor.events.ProcessingEvent._
import fs2.kafka.{ KafkaProducer, ProducerSettings, RecordSerializer, Serializer }

object EventProducersProvider {

  def provide: IO[EventProducers[IO]] =
    for {
      somInputProducer              <- generateInputDocumentProcessingEventSender
      somConnectionProducer         <- createSomConnectionEventSender
      somRequestSenderProducer      <- sendSomRequestEventSender
      optimizationRunUpdateProducer <- optimizationRunUpdateEventProducer
    } yield EventProducers(somInputProducer, somConnectionProducer, somRequestSenderProducer, optimizationRunUpdateProducer)

  private def generateInputDocumentProcessingEventSender: IO[SomInputProducer[IO]] = {
    val producerSettings: ProducerSettings[IO, String, GenerateInputDocumentProcessingEvent] = mkProducerSettings(
      GenerateInputDocumentProcessingEventCodec.serializer
    )
    KafkaProducer.stream(producerSettings).map(KafkaEventProducer(OptimizationRun.generateInputFileTopic, _)).compile.toList.map(_.head)
  }

  private def createSomConnectionEventSender: IO[SomConnectionProducer[IO]] = {
    val producerSettings: ProducerSettings[IO, String, CreateSomConnection] = mkProducerSettings(CreateSomConnectionCodec.serializer)
    KafkaProducer.stream(producerSettings).map(KafkaEventProducer(OptimizationRun.createSomConnectionTopic, _)).compile.toList.map(_.head)
  }

  private def sendSomRequestEventSender: IO[SomRequestSenderProducer[IO]] = {
    val producerSettings: ProducerSettings[IO, String, SendSomRequest] = mkProducerSettings(SendSomRequestCodec.serializer)
    KafkaProducer.stream(producerSettings).map(KafkaEventProducer(OptimizationRun.sendSomInputTopic, _)).compile.toList.map(_.head)
  }

  private def optimizationRunUpdateEventProducer: IO[OptimizationRunUpdateProducer[IO]] = {
    val producerSettings: ProducerSettings[IO, String, OptimizationRunUpdateEvent] = mkProducerSettings(OptimizationRunUpdateEvent.codec.serializer)
    KafkaProducer.stream(producerSettings).map(KafkaEventProducer(OptimizationRun.updateOptimizationRunTopic, _)).compile.toList.map(_.head)
  }

  private def mkProducerSettings[T](serializer: RecordSerializer[IO, T]): ProducerSettings[IO, String, T] =
    ProducerSettings(Serializer[IO, String], serializer)
      .withBootstrapServers("localhost:9092")

}
