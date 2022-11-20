package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent.{ChangeState, IncrementResult}

class OptimizationRunModifier[F[_]:Sync] {
  def applyUpdate(event:OptimizationRunUpdateEvent): F[Unit] = {
    case irEvent:IncrementResult => Sync[F].delay(println("I`m IncrementResult"))
    case csEvent:ChangeState => Sync[F].delay(println("I`m ChangeState"))
    case _ => Sync[F].delay(println("Another type"))
  }
}
