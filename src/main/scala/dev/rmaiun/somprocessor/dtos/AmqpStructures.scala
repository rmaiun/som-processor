package dev.rmaiun.somprocessor.dtos

import cats.effect.MonadCancel
import dev.rmaiun.somprocessor.services.RabbitInitializer.{AmqpConsumer, AmqpPublisher}

case class AmqpStructures[F[_]](
                                 botInPublisher: AmqpPublisher[F],
                                 botInConsumer: AmqpConsumer[F]
                               )(implicit
                                 MC: MonadCancel[F, Throwable] )