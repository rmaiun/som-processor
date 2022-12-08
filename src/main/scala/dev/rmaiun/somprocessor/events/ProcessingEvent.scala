package dev.rmaiun.somprocessor.events

import cats.effect.IO
import dev.rmaiun.somprocessor.Boot.avroSettings
import dev.rmaiun.somprocessor.domains.OptimizationRunRequest
import fs2.kafka.vulcan.{ avroDeserializer, avroSerializer }
import fs2.kafka.{ RecordDeserializer, RecordSerializer }
import vulcan.Codec
import vulcan.generic.{ AvroNamespace, _ }

trait ProcessingEvent

object ProcessingEvent {
  @AvroNamespace("dev.rmaiun.somprocessor.events")
  case class GenerateInputDocumentProcessingEvent(optimizationRunId: Long, algorithmCode: String) extends ProcessingEvent

  @AvroNamespace("dev.rmaiun.somprocessor.events")
  case class StartRequestProcessingProcessingEvent(request: OptimizationRunRequest) extends ProcessingEvent
  @AvroNamespace("dev.rmaiun.somprocessor.events")
  case class CreateSomConnection(optimizationId: Long, algorithmCode: String, messageId: String) extends ProcessingEvent

  @AvroNamespace("dev.rmaiun.somprocessor.events")
  case class SendSomRequest(optimizationId: Long, algorithmCode: String, messageId: String) extends ProcessingEvent
  @AvroNamespace("dev.rmaiun.somprocessor.events")
  case class DisconnectFromSom(optimizationId: Long, algorithmCode: String) extends ProcessingEvent

  @AvroNamespace("dev.rmaiun.somprocessor.events")
  case class FinalizeOptimization(optimizationId: Long, algorithmCode: String) extends ProcessingEvent

  object GenerateInputDocumentProcessingEventCodec {
    implicit val codec: Codec[GenerateInputDocumentProcessingEvent] = Codec.derive

    implicit val serializer: RecordSerializer[IO, GenerateInputDocumentProcessingEvent] =
      avroSerializer[GenerateInputDocumentProcessingEvent].using(avroSettings)

    implicit val deserializer: RecordDeserializer[IO, GenerateInputDocumentProcessingEvent] =
      avroDeserializer[GenerateInputDocumentProcessingEvent].using(avroSettings)
  }

  object StartRequestProcessingProcessingEventCodec {
    implicit val codec: Codec[StartRequestProcessingProcessingEvent] = Codec.derive

    implicit val serializer: RecordSerializer[IO, StartRequestProcessingProcessingEvent] =
      avroSerializer[StartRequestProcessingProcessingEvent].using(avroSettings)

    implicit val deserializer: RecordDeserializer[IO, StartRequestProcessingProcessingEvent] =
      avroDeserializer[StartRequestProcessingProcessingEvent].using(avroSettings)
  }

  object CreateSomConnectionCodec {
    implicit val codec: Codec[CreateSomConnection] = Codec.derive

    implicit val serializer: RecordSerializer[IO, CreateSomConnection] =
      avroSerializer[CreateSomConnection].using(avroSettings)

    implicit val deserializer: RecordDeserializer[IO, CreateSomConnection] =
      avroDeserializer[CreateSomConnection].using(avroSettings)
  }

  object SendSomRequestCodec {
    implicit val codec: Codec[SendSomRequest] = Codec.derive

    implicit val serializer: RecordSerializer[IO, SendSomRequest] =
      avroSerializer[SendSomRequest].using(avroSettings)

    implicit val deserializer: RecordDeserializer[IO, SendSomRequest] =
      avroDeserializer[SendSomRequest].using(avroSettings)
  }

  object DisconnectFromSomCodec {
    implicit val codec: Codec[DisconnectFromSom] = Codec.derive

    implicit val serializer: RecordSerializer[IO, DisconnectFromSom] =
      avroSerializer[DisconnectFromSom].using(avroSettings)

    implicit val deserializer: RecordDeserializer[IO, DisconnectFromSom] =
      avroDeserializer[DisconnectFromSom].using(avroSettings)
  }

  object FinalizeOptimizationCodec {
    implicit val codec: Codec[FinalizeOptimization] = Codec.derive

    implicit val serializer: RecordSerializer[IO, FinalizeOptimization] =
      avroSerializer[FinalizeOptimization].using(avroSettings)

    implicit val deserializer: RecordDeserializer[IO, FinalizeOptimization] =
      avroDeserializer[FinalizeOptimization].using(avroSettings)
  }
}
