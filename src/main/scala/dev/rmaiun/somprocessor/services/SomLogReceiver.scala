package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import cats.implicits._
import dev.profunktor.fs2rabbit.model.AmqpEnvelope
import dev.rmaiun.somprocessor.domains.OptimizationRun.updateOptimizationRunTopic
import dev.rmaiun.somprocessor.dtos.EventProducers
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent.IncrementFinalLog
import fs2.Chunk
import fs2.kafka.{ ProducerRecord, ProducerRecords }
import io.circe._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
case class SomLogReceiver[F[_]: Sync](eventProducers: EventProducers[F], logger: Logger[F]) {
  def receiveLog(envelope: AmqpEnvelope[String]): F[Unit] = {
    val message        = parser.parse(envelope.payload).flatMap(_.hcursor.downField("message").as[String]).getOrElse("")
    val optimizationId = envelope.properties.headers("optimizationId").toString
    if (message == "Computation completed") {
      logger.info(s"Final Log for optimization $optimizationId is received") *> updateOptimizationRun(optimizationId)
    } else {
      logger.info(s"Non final Log for optimization $optimizationId is received")
    }
  }

  private def updateOptimizationRun(optimizationId: String): F[Unit] = {
    val record1 = ProducerRecord(updateOptimizationRunTopic, optimizationId, IncrementFinalLog(optimizationId.toLong))
    eventProducers.optimizationRunUpdateProducer.produce(ProducerRecords.chunk(Chunk.seq(Seq(record1, record1)))).flatten *> ().pure
  }
}

object SomLogReceiver {
  def apply[F[_]](implicit ev: SomLogReceiver[F]): SomLogReceiver[F] = ev

  def impl[F[_]: Sync](eventProducers: EventProducers[F]): F[SomLogReceiver[F]] =
    Slf4jLogger.create[F].map(new SomLogReceiver(eventProducers, _))
}
