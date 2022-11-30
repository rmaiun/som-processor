package dev.rmaiun.somprocessor.repositories

import cats.effect.Sync

case class AlgorithmRepository[F[_]: Sync]() {
  def loadAllAlgorithms(): F[List[String]] = ???
}
