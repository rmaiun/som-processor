package dev.rmaiun.somprocessor.services

import cats.Monad
import cats.effect.{ Async, Concurrent }
import cats.implicits._
import dev.rmaiun.somprocessor.dtos.EventProducers
import dev.rmaiun.somprocessor.dtos.configuration.AppConfiguration
import dev.rmaiun.somprocessor.events.ProcessingEvent.{ CreateSomConnection, DisconnectFromSom, SendSomRequest }
import dev.rmaiun.somprocessor.repositories.AlgorithmLockRepository
import dev.rmaiun.somprocessor.services.RabbitInitializer.{ AmqpStructures, OpenedConnections, ShutdownSignals }
import fs2.concurrent.SignallingRef
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
case class SomConnectionProvider[F[_]: Async](
  cfg: AppConfiguration,
  rabbitConnections: OpenedConnections[F],
  eventProducers: EventProducers[F],
  shutdownSignals: ShutdownSignals[F],
  algorithmLockRepository: AlgorithmLockRepository[F],
  logger: Logger[F]
) {
  def createSomConnection(event: CreateSomConnection): F[Unit] =
    for {
      cfg             <- Monad[F].pure(RabbitInitializer.config(cfg.broker))
      structs         <- RabbitInitializer.initConnection(cfg, event.algorithmCode).compile.toList
      resultProcessor <- SomResultReceiver.impl(eventProducers)
      logProcessor    <- SomLogReceiver.impl(eventProducers)
      _               <- shutdownSignals.find(_._1 == event.algorithmCode).fold(Monad[F].pure(()))(signal => signal._2.update(_ => false))
      _               <- forkResultsConsumer(event.algorithmCode, structs.head, resultProcessor)
      _               <- forkLogsConsumer(event.algorithmCode, structs.head, logProcessor)
      _               <- rabbitConnections.update(connections => connections ++ (event.algorithmCode -> structs.head))
      _               <- invokeSomRequestSending(event.optimizationId, event.algorithmCode, event.messageId)
    } yield ()

  def disconnectFromSom(event: DisconnectFromSom): F[Unit] =
    for {
      _ <- refreshSwitch(shutdownSignals(event.algorithmCode))
      _ <- algorithmLockRepository.delete(event.algorithmCode)
    } yield ()

  private def forkLogsConsumer(algorithm: String, structs: AmqpStructures[F], logProcessor: SomLogReceiver[F]): F[Unit] =
    Concurrent[F].start(
      structs.logsConsumer.evalTap(logProcessor.receiveLog).interruptWhen(shutdownSignals(algorithm)).compile.drain
    ) *> ().pure

  private def forkResultsConsumer(algorithm: String, structs: AmqpStructures[F], resultReceiver: SomResultReceiver[F]): F[Unit] =
    Concurrent[F].start(
      structs.logsConsumer.evalTap(resultReceiver.receiveSomMessage).interruptWhen(shutdownSignals(algorithm)).compile.drain
    ) *> ().pure

  private def invokeSomRequestSending(optRunId: Long, algorithmCode: String, messageId: String): F[Unit] =
    eventProducers.somRequestSenderProducer.publish(optRunId.toString, SendSomRequest(optRunId, algorithmCode, messageId))
  private def refreshSwitch(switch: SignallingRef[F, Boolean]): F[Unit] =
    switch.update(x => !x) *> switch.update(x => !x)
}

object SomConnectionProvider {
  def apply[F[_]](implicit ev: SomConnectionProvider[F]): SomConnectionProvider[F] = ev

  def impl[F[_]: Async](
    cfg: AppConfiguration,
    rabbitConnections: OpenedConnections[F],
    eventProducers: EventProducers[F],
    shutdownSignals: ShutdownSignals[F],
    algorithmLockRepository: AlgorithmLockRepository[F]
  ): F[SomConnectionProvider[F]] =
    Slf4jLogger.create[F].map(new SomConnectionProvider(cfg, rabbitConnections, eventProducers, shutdownSignals, algorithmLockRepository, _))
}
