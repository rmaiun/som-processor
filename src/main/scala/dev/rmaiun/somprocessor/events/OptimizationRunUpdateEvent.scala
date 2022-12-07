package dev.rmaiun.somprocessor.events

import cats.effect.IO
import dev.rmaiun.somprocessor.Boot.avroSettings
import dev.rmaiun.somprocessor.domains.OptimizationRunState
import fs2.kafka.vulcan.{ avroDeserializer, avroSerializer }
import fs2.kafka.{ RecordDeserializer, RecordSerializer }
import vulcan.Codec
import vulcan.generic.{ AvroNamespace, _ }

import java.time.ZonedDateTime

sealed trait OptimizationRunUpdateEvent extends Product with Serializable {
  def id: Long
}

object OptimizationRunUpdateEvent {
  sealed trait OptimizingEvent extends OptimizationRunUpdateEvent
  sealed trait ResultsEvent    extends OptimizationRunUpdateEvent
  @AvroNamespace("dev.rmaiun.somprocessor.events")
  final case class IncrementFinalLog(id: Long) extends ResultsEvent
  @AvroNamespace("dev.rmaiun.somprocessor.events")
  final case class ChangeOptimizingState(id: Long)                    extends OptimizingEvent
  final case class ChangeState(id: Long, state: OptimizationRunState) extends ResultsEvent
  @AvroNamespace("dev.rmaiun.somprocessor.events")
  final case class SuccessResultReceived(id: Long) extends ResultsEvent
  final case class ErrorResultReceived(id: Long)   extends ResultsEvent
  @AvroNamespace("dev.rmaiun.somprocessor.events")
  final case class BindAlgorithm(id: Long, algorithmCode: String, criticalEndTime: ZonedDateTime) extends OptimizingEvent
  @AvroNamespace("dev.rmaiun.somprocessor.events")
  final case class PairRequest(id: Long, requestId: Long) extends OptimizingEvent
  @AvroNamespace("dev.rmaiun.somprocessor.events")
  final case class AssignMessageId(id: Long, messageId: String) extends OptimizingEvent

  object codec {
    implicit val codec: Codec[OptimizationRunUpdateEvent] = Codec.derive

    implicit val serializer: RecordSerializer[IO, OptimizationRunUpdateEvent] =
      avroSerializer[OptimizationRunUpdateEvent].using(avroSettings)

    implicit val deserializer: RecordDeserializer[IO, OptimizationRunUpdateEvent] =
      avroDeserializer[OptimizationRunUpdateEvent].using(avroSettings)
  }
}
