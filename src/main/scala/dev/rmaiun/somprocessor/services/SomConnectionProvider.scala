package dev.rmaiun.somprocessor.services

import cats.effect.Sync

case class SomConnectionProvider[F[_]:Sync]() {
  def createSomConnection(): F[Unit] = ???
  // todo need to copy everything related to rabbit here
  def disconnectFromSom():F[Unit] = ???
  // todo need to copy everything related to rabbit here
  // need to finalize
}
