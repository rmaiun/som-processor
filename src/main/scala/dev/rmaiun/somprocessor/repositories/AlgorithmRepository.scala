package dev.rmaiun.somprocessor.repositories

import cats.effect.Sync

case class AlgorithmRepository[F[_]: Sync]() {
  val storage: List[String]                = List("ALG_SOM4_DEV")
  def loadAllAlgorithms(): F[List[String]] = Sync[F].pure(storage)
}
