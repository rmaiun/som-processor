package dev.rmaiun.somprocessor.services

import cats.effect.Sync

case class SomResultReceiver[F[_]:Sync]() {
  def receiveSomMessage():F[Unit] = ???
}
