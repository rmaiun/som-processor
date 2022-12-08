package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import cats.implicits._
import dev.rmaiun.somprocessor.dtos.EventProducers
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent.ReleaseAlgorithm
import dev.rmaiun.somprocessor.events.ProcessingEvent.FinalizeOptimization
import dev.rmaiun.somprocessor.repositories.AlgorithmLockRepository
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
class OptimizationFinalizer[F[_]: Sync](algorithmLockRepository: AlgorithmLockRepository[F], eventProducers: EventProducers[F], logger: Logger[F]) {
  def finalizeOptimization(event: FinalizeOptimization): F[Unit] =
    for {
      _ <- logger.info(s"Removing algorithmLock for ${event.algorithmCode}")
      _ <- algorithmLockRepository.delete(event.algorithmCode)
      _ <- eventProducers.optimizationRunUpdateProducer.publish(event.optimizationId.toString, ReleaseAlgorithm(event.optimizationId))
    } yield ()
}

object OptimizationFinalizer {
  def apply[F[_]](implicit ev: OptimizationFinalizer[F]): OptimizationFinalizer[F] = ev
  def impl[F[_]: Sync](algorithmLockRepository: AlgorithmLockRepository[F], eventProducers: EventProducers[F]): F[OptimizationFinalizer[F]] =
    Slf4jLogger.create[F].map(new OptimizationFinalizer(algorithmLockRepository, eventProducers, _))
}
