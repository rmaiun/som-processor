package dev.rmaiun.somprocessor.repositories

import cats.effect.Sync
import dev.rmaiun.somprocessor.domains.AlgorithmLock

case class AlgorithmLockRepository[F[_]: Sync]() {
  def loadLocked(): F[List[AlgorithmLock]]          = ???
  def create(algorithmLock: AlgorithmLock): F[Long] = ???
  def delete(code: String): F[Long]                 = ???
}
