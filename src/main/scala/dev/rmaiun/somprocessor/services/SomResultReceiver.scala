package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import cats.implicits._
import dev.profunktor.fs2rabbit.model.AmqpEnvelope
import dev.rmaiun.somprocessor.domains.OptimizationRun.updateOptimizationRunTopic
import dev.rmaiun.somprocessor.dtos.EventProducers
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent.{ ErrorResultReceived, SuccessResultReceived }
import fs2.Chunk
import fs2.kafka.{ ProducerRecord, ProducerRecords }
import io.circe.parser
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
case class SomResultReceiver[F[_]: Sync](eventProducers: EventProducers[F], logger: Logger[F]) {
  def receiveSomMessage(envelope: AmqpEnvelope[String]): F[Unit] = {
    val optimizationId = parser.parse(envelope.payload).flatMap(_.hcursor.downField("optimizationId").as[String]).getOrElse("")
    val status         = parser.parse(envelope.payload).flatMap(_.hcursor.downField("status").as[String]).getOrElse("")
    for {
      _ <- logger.info(s"Received $status SOM response for $optimizationId")
      _ <- updateOptimizationRun(optimizationId, status)
    } yield ()
  }

  private def updateOptimizationRun(optimizationId: String, status: String): F[Unit] = {
    val event   = if (status == "success") SuccessResultReceived(optimizationId.toLong) else ErrorResultReceived(optimizationId.toLong)
    val record1 = ProducerRecord(updateOptimizationRunTopic, optimizationId, event)
    eventProducers.optimizationRunUpdateProducer.produce(ProducerRecords.chunk(Chunk.seq(Seq(record1, record1)))).flatten *> ().pure
  }
}
object SomResultReceiver {
  def apply[F[_]](implicit ev: SomResultReceiver[F]): SomResultReceiver[F] = ev

  def impl[F[_]: Sync](eventProducers: EventProducers[F]): F[SomResultReceiver[F]] =
    Slf4jLogger.create[F].map(new SomResultReceiver(eventProducers, _))
}
