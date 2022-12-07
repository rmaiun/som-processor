package dev.rmaiun.somprocessor.repositories

import cats.effect.Sync
import dev.rmaiun.somprocessor.domains.{ OptimizationRun, OptimizationRunState }

case class OptimizationRunRepository[F[_]: Sync]() {
  var storage: List[OptimizationRun] = Nil

  def create(optRun: OptimizationRun): F[Long] = {
    storage = optRun :: storage
    Sync[F].pure(optRun.id)
  }
  def updateMany(optRunList: List[OptimizationRun]): F[Unit] = {
    val ids = optRunList.map(_.id)
    storage = storage.filter(optRun => !ids.contains(optRun.id))
    storage = optRunList ::: storage
    Sync[F].pure(())
  }
  def list(idList: Option[List[Long]] = None): F[List[OptimizationRun]] =
    idList match {
      case Some(ids) => Sync[F].pure(storage.filter(optRun => ids.contains(optRun.id)))
      case None      => Sync[F].pure(storage)
    }
  def listReadyForProcessing(): F[List[OptimizationRun]] =
    Sync[F].pure(storage.filter(_.state == OptimizationRunState.Init))
}
