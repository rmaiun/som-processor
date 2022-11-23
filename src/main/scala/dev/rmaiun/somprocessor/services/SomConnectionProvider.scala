package dev.rmaiun.somprocessor.services

import cats.Monad
import cats.effect.Async
import cats.implicits._
import dev.rmaiun.somprocessor.dtos.Event.{ CreateSomConnection, SendSomRequest }
import dev.rmaiun.somprocessor.dtos.configuration.AppConfiguration
import dev.rmaiun.somprocessor.dtos.{ EventProducers, RabbitSwitches }
import dev.rmaiun.somprocessor.services.RabbitInitializer.OpenedConnections
import fs2.kafka.{ ProducerRecord, ProducerRecords }
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
case class SomConnectionProvider[F[_]: Async](
  cfg: AppConfiguration,
  rabbitConnections: OpenedConnections[F],
  eventProducers: EventProducers[F],
  rabbitSwitches: RabbitSwitches[F],
  logger: Logger[F]
) {
  def createSomConnection(event: CreateSomConnection): F[Unit] =
    for {
      cfg     <- Monad[F].pure(RabbitInitializer.config(cfg.broker))
      structs <- RabbitInitializer.initConnection(cfg, event.algorithmCode).compile.toList
      // todo bind listeners to consumers
      // todo add signalRefs
      _     <- rabbitConnections.update(connections => connections ++ (event.algorithmCode -> structs.head))
      switch = rabbitSwitches.switches.find(_.code == event.algorithmCode)
      _     <- rabbitSwitches.refreshOptSwitch(switch)
      logsProcessor <- SomLogReceiver.impl(eventProducers)
      resultsProcessor <- SomResultReceiver.impl(eventProducers)
      // todo wrong AmqpStructs structure!
      _     <- invokeSomRequestSending(event.optimizationId, event.algorithmCode)
    } yield ()

  def disconnectFromSom(): F[Unit] = ???
  // todo need to copy everything related to rabbit here
  // need to finalize

  private def invokeSomRequestSending(optRunId: Long, algorithmCode: String): F[Unit] = {
    val record = ProducerRecord("send_som_input", optRunId.toString, SendSomRequest(optRunId, algorithmCode))
    eventProducers.somRequestSenderProducer.produce(ProducerRecords.one(record)).flatten.map(_ => ())
  }
}

object SomConnectionProvider {
  def apply[F[_]](implicit ev: SomConnectionProvider[F]): SomConnectionProvider[F] = ev

  def impl[F[_]: Async](
    cfg: AppConfiguration,
    rabbitConnections: OpenedConnections[F],
    eventProducers: EventProducers[F],
    rabbitSwitches: RabbitSwitches[F]
  ): F[SomConnectionProvider[F]] =
    Slf4jLogger.create[F].map(new SomConnectionProvider(cfg, rabbitConnections, eventProducers, rabbitSwitches, _))
}
