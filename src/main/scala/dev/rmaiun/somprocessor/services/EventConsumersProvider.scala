package dev.rmaiun.somprocessor.services

import cats.effect.IO
import dev.rmaiun.somprocessor.domains.OptimizationRun
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent
import dev.rmaiun.somprocessor.events.ProcessingEvent.{
  CreateSomConnectionCodec,
  FinalizeOptimizationCodec,
  GenerateInputDocumentProcessingEventCodec,
  SendSomRequestCodec
}
import fs2.kafka._

object EventConsumersProvider {

  def generateInputDocumentProcessingEventConsumer(processor: SomInputFileGenerator[IO]): IO[Unit] =
    KafkaConsumer
      .stream(consumerSettings(GenerateInputDocumentProcessingEventCodec.deserializer))
      .subscribeTo(OptimizationRun.generateInputFileTopic)
      .records
      .evalTap(r => processor.generateInputDocument(r.record.value))
      .compile
      .drain

  def createSomConnectionEventConsumer(processor: SomConnectionProvider[IO]): IO[Unit] =
    KafkaConsumer
      .stream(consumerSettings(CreateSomConnectionCodec.deserializer))
      .subscribeTo(OptimizationRun.createSomConnectionTopic)
      .records
      .evalTap(r => processor.createSomConnection(r.record.value))
      .compile
      .drain

  def sendSomRequestEventConsumer(processor: SomRequestSender[IO]): IO[Unit] =
    KafkaConsumer
      .stream(consumerSettings(SendSomRequestCodec.deserializer))
      .subscribeTo(OptimizationRun.sendSomInputTopic)
      .records
      .evalTap(r => processor.sendSomRequest(r.record.value))
      .compile
      .drain

  def finalizeOptimizationEventConsumer(processor: OptimizationFinalizer[IO]): IO[Unit] =
    KafkaConsumer
      .stream(consumerSettings(FinalizeOptimizationCodec.deserializer))
      .subscribeTo(OptimizationRun.closeSomConnection)
      .records
      .evalTap(r => processor.finalizeOptimization(r.record.value))
      .compile
      .drain

  def updateOptimizationRunEventConsumer(processor: OptimizationRunModifier[IO]): IO[Unit] =
    KafkaConsumer
      .stream(consumerSettings(OptimizationRunUpdateEvent.codec.deserializer))
      .subscribeTo(OptimizationRun.updateOptimizationRunTopic)
      .partitionedRecords
      .evalTap { chunk =>
        chunk.compile.toList.flatMap(list => processor.applyBatchUpdate(list.map(_.record.value)))
      }
      .compile
      .drain

  private def consumerSettings[T](deserializer: RecordDeserializer[IO, T]): ConsumerSettings[IO, String, T] =
    ConsumerSettings(
      keyDeserializer = Deserializer[IO, String],
      valueDeserializer = deserializer
    )
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("group")

}
