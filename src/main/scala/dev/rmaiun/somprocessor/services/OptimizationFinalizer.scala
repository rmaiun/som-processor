package dev.rmaiun.somprocessor.services

import cats.effect.Sync

class OptimizationFinalizer[F[_]:Sync] {
  def finalizeOptimization():F[Unit] = ???
}
