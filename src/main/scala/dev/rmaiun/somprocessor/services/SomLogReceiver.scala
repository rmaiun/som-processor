package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import cats.implicits._
import dev.profunktor.fs2rabbit.model.AmqpEnvelope
import dev.rmaiun.somprocessor.dtos.EventProducers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
case class SomLogReceiver[F[_]: Sync](eventProducers: EventProducers[F], logger: Logger[F]) {
  def receiveLog(envelope: AmqpEnvelope[String]): F[Unit] = ???
}

object SomLogReceiver {
  def apply[F[_]](implicit ev: SomLogReceiver[F]): SomLogReceiver[F] = ev

  def impl[F[_]: Sync](eventProducers: EventProducers[F]): F[SomLogReceiver[F]] =
    Slf4jLogger.create[F].map(new SomLogReceiver(eventProducers, _))
}
