package dev.rmaiun.somprocessor.services

import cats.effect.Sync

case class SomRequestSender[F[_]: Sync]() {
  def distributeSomRequest(): F[Unit] = ???
}
