package dev.rmaiun.somprocessor

import cats.effect.{ ExitCode, IO, IOApp, Ref }
import dev.rmaiun.somprocessor.domains.OptimizationRunRequest
import dev.rmaiun.somprocessor.events.ProcessingEvent.StartRequestProcessingProcessingEvent
import dev.rmaiun.somprocessor.repositories.{ AlgorithmLockRepository, AlgorithmRepository, OptimizationRunRepository }
import dev.rmaiun.somprocessor.services.RabbitInitializer.{ AlgorithmCode, AmqpStructures }
import dev.rmaiun.somprocessor.services._
import fs2.concurrent.SignallingRef
import fs2.kafka.vulcan.{ AvroSettings, SchemaRegistryClientSettings }
object Boot extends IOApp {

  val avroSettings: AvroSettings[IO] =
    AvroSettings {
      SchemaRegistryClientSettings[IO]("http://localhost:8081")
    }

  override def run(args: List[String]): IO[ExitCode] = {
    val cfg = ConfigProvider.provideConfig
    for {
      // kafka publishers
      eventProducers <- EventProducersProvider.provide
      // rabbitmq data structures
      rabbitConnections <- Ref.of[IO, Map[AlgorithmCode, AmqpStructures[IO]]](Map())
      somAlg4Signal     <- SignallingRef.of[IO, Boolean](false)
      map                = Map("ALG_SOM4_DEV" -> somAlg4Signal)
      // repositories
      algorithmRepo     <- IO(AlgorithmRepository[IO]())
      algorithmLockRepo <- IO(AlgorithmLockRepository[IO]())
      optimizationRepo  <- IO(OptimizationRunRepository[IO]())
      // services
      optModifier        <- OptimizationRunModifier.impl[IO](optimizationRepo, eventProducers)
      optReqProcessor    <- OptimizationRunRequestProcessor.impl[IO](algorithmRepo, algorithmLockRepo, optimizationRepo, eventProducers)
      somConnProvider    <- SomConnectionProvider.impl[IO](cfg, rabbitConnections, eventProducers, map, algorithmLockRepo)
      somInFileGenerator <- SomInputFileGenerator.impl[IO](eventProducers)
      somReqSender       <- SomRequestSender.impl[IO](rabbitConnections)
      optFinalizer       <- OptimizationFinalizer.impl[IO](algorithmLockRepo, eventProducers)
      // kafka consumers
      _ <- EventConsumersProvider.updateOptimizationRunEventConsumer(optModifier).start
      _ <- EventConsumersProvider.generateInputDocumentProcessingEventConsumer(somInFileGenerator).start
      _ <- EventConsumersProvider.createSomConnectionEventConsumer(somConnProvider).start
      _ <- EventConsumersProvider.sendSomRequestEventConsumer(somReqSender).start
      _ <- EventConsumersProvider.finalizeOptimizationEventConsumer(optFinalizer).start
      // infinite execution
      _ <- optReqProcessor.startRequestProcessing(StartRequestProcessingProcessingEvent(OptimizationRunRequest(1L, "2")))
      _ <- IO.never
    } yield ExitCode.Success
  }

}
