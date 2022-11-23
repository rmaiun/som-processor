package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import cats.implicits._
import dev.profunktor.fs2rabbit.model.AmqpEnvelope
import dev.rmaiun.somprocessor.dtos.EventProducers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
case class SomResultReceiver[F[_]: Sync](eventProducers: EventProducers[F], logger: Logger[F]) {
  def receiveSomMessage(envelope: AmqpEnvelope[String]): F[Unit] = ???
}
object SomResultReceiver {
  def apply[F[_]](implicit ev: SomResultReceiver[F]): SomResultReceiver[F] = ev

  def impl[F[_]: Sync](eventProducers: EventProducers[F]): F[SomResultReceiver[F]] =
    Slf4jLogger.create[F].map(new SomResultReceiver(eventProducers, _))
}
