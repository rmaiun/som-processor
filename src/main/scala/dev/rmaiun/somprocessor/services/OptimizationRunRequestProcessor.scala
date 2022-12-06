package dev.rmaiun.somprocessor.services

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import dev.rmaiun.somprocessor.domains.OptimizationRun
import dev.rmaiun.somprocessor.domains.OptimizationRun._
import dev.rmaiun.somprocessor.dtos.ProcessingEvent.{ GenerateInputDocumentProcessingEvent, StartRequestProcessingProcessingEvent }
import dev.rmaiun.somprocessor.dtos.EventProducers
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent.{ BindAlgorithm, PairRequest }
import dev.rmaiun.somprocessor.repositories.{ AlgorithmLockRepository, AlgorithmRepository, OptimizationRunRepository }
import fs2.Chunk
import fs2.kafka.{ ProducerRecord, ProducerRecords }
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.{ ZoneOffset, ZonedDateTime }
case class OptimizationRunRequestProcessor[F[_]](
  algorithmRepository: AlgorithmRepository[F],
  algorithmLockRepository: AlgorithmLockRepository[F],
  optimizationRunRepository: OptimizationRunRepository[F],
  eventProducers: EventProducers[F],
  logger: Logger[F]
)(implicit S: Sync[F]) {
  def startRequestProcessing(event: StartRequestProcessingProcessingEvent): F[Unit] =
    for {
      allAlgorithms    <- algorithmRepository.loadAllAlgorithms()
      lockedAlgorithms <- algorithmLockRepository.loadLocked()
      lockedCodes       = lockedAlgorithms.map(_.code)
      freeCodes         = allAlgorithms.filter(lockedCodes.contains(_))
      _                <- startOptimizationRuns(event.request.id, freeCodes)
    } yield ()

  private def startOptimizationRuns(request: Long, algorithmCodes: List[String]): F[Unit] =
    if (algorithmCodes.isEmpty) {
      logger.info("All algorithms are busy")
    } else {
      for {
        optRunList <- optimizationRunRepository.listReadyForProcessing()
        _          <- updateOptimizationData(request, optRunList, algorithmCodes)
      } yield ()
    }

  private def updateOptimizationData(request: Long, foundOptimizationRuns: List[OptimizationRun], algorithmCodes: List[String]): F[Unit] =
    if (foundOptimizationRuns.isEmpty) {
      logger.info("No Initialized Optimization Runs found to process")
    } else {
      val pairedData = pairAlgorithmWithOptimizationRun(foundOptimizationRuns, algorithmCodes)
      pairedData.map(t2 => updateOptimizationRun(request, t2._1, t2._2).flatMap(t => invokeFileSending(t._1, t._2))).sequence_
    }
  private def pairAlgorithmWithOptimizationRun(foundOptimizationRuns: List[OptimizationRun], algorithmCodes: List[String]): List[(OptimizationRun, String)] = {
    val end = if (foundOptimizationRuns.size > algorithmCodes.size) foundOptimizationRuns.size else algorithmCodes.size
    (0 until end).map(i => (foundOptimizationRuns(i), algorithmCodes(i))).toList
  }

  private def updateOptimizationRun(request: Long, optRun: OptimizationRun, algorithm: String): F[(OptimizationRun, String)] = {
    val endTime = ZonedDateTime.now(ZoneOffset.UTC)
    val record1 = ProducerRecord(updateOptimizationRunTopic, optRun.id.toString, BindAlgorithm(optRun.id, algorithm, endTime))
    val record2 = ProducerRecord(updateOptimizationRunTopic, optRun.id.toString, PairRequest(optRun.id, request))
    val chunk   = Chunk.seq(Seq(record1, record2))
    eventProducers.optimizationRunUpdateProducer.produce(ProducerRecords.chunk(chunk)).flatten.map(_ => optRun.copy(algorithmCode = algorithm)) *>
      Monad[F].pure((optRun, algorithm))
  }
  private def invokeFileSending(optRun: OptimizationRun, algorithm: String): F[Unit] = {
    val record = ProducerRecord(generateInputFileTopic, optRun.id.toString, GenerateInputDocumentProcessingEvent(optRun.id, algorithm))
    eventProducers.somInputProducer.produce(ProducerRecords.one(record)).flatten.map(_ => ())
  }
}

object OptimizationRunRequestProcessor {
  def apply[F[_]](implicit ev: OptimizationRunRequestProcessor[F]): OptimizationRunRequestProcessor[F] = ev

  def impl[F[_]: Sync](
    algorithmRepository: AlgorithmRepository[F],
    algorithmLockRepository: AlgorithmLockRepository[F],
    optimizationRunRepository: OptimizationRunRepository[F],
    eventProducers: EventProducers[F]
  ): F[OptimizationRunRequestProcessor[F]] =
    Slf4jLogger.create[F].map(new OptimizationRunRequestProcessor(algorithmRepository, algorithmLockRepository, optimizationRunRepository, eventProducers, _))
}
