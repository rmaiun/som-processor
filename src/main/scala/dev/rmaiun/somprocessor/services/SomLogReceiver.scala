package dev.rmaiun.somprocessor.services

import cats.effect.Sync

case class SomLogReceiver[F[_]:Sync]() {
  def receiveLog(): F[Unit] = ???
}
