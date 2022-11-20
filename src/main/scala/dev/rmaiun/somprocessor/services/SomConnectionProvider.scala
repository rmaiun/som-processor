package dev.rmaiun.somprocessor.services

import cats.effect.Sync

case class SomConnectionProvider[F[_]:Sync]() {
  def createSomConnection(): F[Unit] = ???
  def disconnectFromSom():F[Unit] = ???
}
