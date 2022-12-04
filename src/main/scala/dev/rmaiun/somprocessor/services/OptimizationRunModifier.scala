package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import dev.rmaiun.somprocessor.domains.OptimizationRun
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent.{ChangeState, IncrementResult}
import dev.rmaiun.somprocessor.repositories.OptimizationRunRepository
import org.typelevel.log4cats.Logger

import java.time.{ZoneOffset, ZonedDateTime}

class OptimizationRunModifier[F[_]: Sync](optimizationRunRepository: OptimizationRunRepository[F], logger: Logger[F]) {
  def applyUpdate(event: OptimizationRunUpdateEvent): F[Unit] =
    event match {
      case _: IncrementResult => Sync[F].delay(println("I`m IncrementResult"))
      case _: ChangeState     => Sync[F].delay(println("I`m ChangeState"))
      case _                  => Sync[F].delay(println("Another type"))
    }


  private def withCorrectExpirationTime(optRun: OptimizationRun )(action : => F[Unit]): F[Unit] ={
    if (optRun.criticalEndTime.isBefore(ZonedDateTime.now(ZoneOffset.UTC))){
      logger.error(s"Optimization ${optRun.id} is outdated so updates will be skipped")
    }else{
      action
    }
  }
}

object OptimizationRunModifier {
  def apply[F[_]](implicit ev: OptimizationRunModifier[F]): OptimizationRunModifier[F] = ev

  def impl[F[_]: Sync]: OptimizationRunModifier[F] =
    new OptimizationRunModifier[F]()
}
