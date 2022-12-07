package dev.rmaiun.somprocessor.repositories

import cats.effect.Sync
import dev.rmaiun.somprocessor.domains.AlgorithmLock

case class AlgorithmLockRepository[F[_]: Sync]() {
  var storage: List[AlgorithmLock]         = Nil
  def loadLocked(): F[List[AlgorithmLock]] = Sync[F].pure(storage)
  def create(algorithmLock: AlgorithmLock): F[Long] = {
    storage = algorithmLock :: storage
    Sync[F].pure(algorithmLock.id)
  }
  def delete(code: String): F[Unit] = {
    storage = storage.filter(_.code != code)
    Sync[F].pure(())
  }
}
