package dev.rmaiun.somprocessor

import cats.effect.{ ExitCode, IO, IOApp }
import dev.rmaiun.somprocessor.services.EventProducersProvider
import fs2.kafka.vulcan.{ AvroSettings, SchemaRegistryClientSettings }
object Boot extends IOApp {

  val avroSettings: AvroSettings[IO] =
    AvroSettings {
      SchemaRegistryClientSettings[IO]("http://localhost:8081")
    }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      eventProducers <- EventProducersProvider.provide

    } yield ExitCode.Success

}
