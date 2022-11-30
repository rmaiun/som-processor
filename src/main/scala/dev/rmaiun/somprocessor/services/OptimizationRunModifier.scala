package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent.{ ChangeState, IncrementResult }

class OptimizationRunModifier[F[_]: Sync] {
  def applyUpdate(event: OptimizationRunUpdateEvent): F[Unit] =
    event match {
      case _: IncrementResult => Sync[F].delay(println("I`m IncrementResult"))
      case _: ChangeState     => Sync[F].delay(println("I`m ChangeState"))
      case _                  => Sync[F].delay(println("Another type"))
    }
}

object OptimizationRunModifier {
  def apply[F[_]](implicit ev: OptimizationRunModifier[F]): OptimizationRunModifier[F] = ev

  def impl[F[_]: Sync]: OptimizationRunModifier[F] =
    new OptimizationRunModifier[F]()
}
