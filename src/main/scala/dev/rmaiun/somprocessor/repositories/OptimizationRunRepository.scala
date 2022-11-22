package dev.rmaiun.somprocessor.repositories

import cats.effect.Sync
import dev.rmaiun.somprocessor.domains.OptimizationRun

case class OptimizationRunRepository[F[_]: Sync]() {
  def create(optRun: OptimizationRun): F[Long]                          = ???
  def update(optRun: OptimizationRun): F[Long]                          = ???
  def list(idList: Option[List[Long]] = None): F[List[OptimizationRun]] = ???
  def listReadyForProcessing(): F[List[OptimizationRun]]                = ???
  def delete(code: String): F[Long]                                     = ???
}
