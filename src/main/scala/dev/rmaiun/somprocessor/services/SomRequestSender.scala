package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import cats.implicits._
import dev.profunktor.fs2rabbit.model.{ AmqpMessage, AmqpProperties }
import dev.rmaiun.somprocessor.dtos.ProcessingEvent.SendSomRequest
import dev.rmaiun.somprocessor.services.RabbitInitializer.OpenedConnections
import io.circe._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.io.Source

case class SomRequestSender[F[_]: Sync](rabbitConnections: OpenedConnections[F], logger: Logger[F]) {
  def sendSomRequest(event: SendSomRequest): F[Unit] =
    for {
      payloadString    <- Sync[F].delay(Source.fromResource("som_request_example.json").mkString)
      payload           = parser.parse(payloadString).fold(_ => Json.Null, json => json)
      updJson          <- Sync[F].pure(payload.hcursor.downField("optimizationId").withFocus(_.mapString(_ => event.optimizationId.toString)).top.getOrElse(Json.Null))
      connections      <- rabbitConnections.get
      currentConnection = connections(event.algorithmCode)
      _                <- currentConnection.requestPublisher(AmqpMessage(updJson.toString(), AmqpProperties()))
      _                <- logger.info(s"Request message ${event.messageId} is successfully sent for optimization ${event.optimizationId} and algorithm ${event.algorithmCode}")
    } yield ()
}

object SomRequestSender {
  def apply[F[_]](implicit ev: SomRequestSender[F]): SomRequestSender[F] = ev

  def impl[F[_]: Sync](rabbitConnections: OpenedConnections[F]): F[SomRequestSender[F]] =
    Slf4jLogger.create[F].map(new SomRequestSender(rabbitConnections: OpenedConnections[F], _))
}
