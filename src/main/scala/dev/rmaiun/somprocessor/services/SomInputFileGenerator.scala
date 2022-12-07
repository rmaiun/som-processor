package dev.rmaiun.somprocessor.services

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import dev.rmaiun.somprocessor.domains.OptimizationRunState
import dev.rmaiun.somprocessor.dtos.EventProducers
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent.{ AssignMessageId, ChangeState }
import dev.rmaiun.somprocessor.events.ProcessingEvent.{ CreateSomConnection, GenerateInputDocumentProcessingEvent }
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID
import scala.util.Random

case class SomInputFileGenerator[F[_]: Sync](eventProducers: EventProducers[F], logger: Logger[F]) {
  def generateInputDocument(event: GenerateInputDocumentProcessingEvent): F[Unit] = {
    val randomInt = Random.nextInt(100)
    if (randomInt <= 10) {
      for {
        _ <- logger.info("Failed to create SOM Request")
        _ <- markOptimizationAsFailed(event.optimizationRunId)
      } yield ()
    } else {
      for {
        msgId <- generateSomInputFile()
        _     <- logger.info(s"SOM Request file was generated with messageId $msgId")
        _     <- addMessageIdForOptimization(event.optimizationRunId, msgId)
        _     <- createSomConnection(event.optimizationRunId, event.algorithmCode, msgId)
      } yield ()
    }
  }

  private def generateSomInputFile(): F[String] =
    Monad[F].pure(UUID.randomUUID().toString.replaceAll("-", ""))
  private def markOptimizationAsFailed(optRunId: Long): F[Unit] =
    eventProducers.optimizationRunUpdateProducer.publish(optRunId.toString, ChangeState(optRunId, OptimizationRunState.Finished))

  private def addMessageIdForOptimization(optRunId: Long, messageId: String): F[Unit] =
    eventProducers.optimizationRunUpdateProducer.publish(optRunId.toString, AssignMessageId(optRunId, messageId))

  private def createSomConnection(optRunId: Long, algorithmCode: String, messageId: String): F[Unit] =
    eventProducers.somConnectionProducer.publish(optRunId.toString, CreateSomConnection(optRunId, algorithmCode, messageId))
}

object SomInputFileGenerator {
  def apply[F[_]](implicit ev: SomInputFileGenerator[F]): SomInputFileGenerator[F] = ev

  def impl[F[_]: Sync](eventProducers: EventProducers[F]): F[SomInputFileGenerator[F]] =
    Slf4jLogger.create[F].map(new SomInputFileGenerator(eventProducers, _))
}
