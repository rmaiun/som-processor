package dev.rmaiun.somprocessor

import cats.effect.{ExitCode, IO, IOApp}
import dev.rmaiun.somprocessor.domains.OptimizationRunState
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent.ChangeState
import org.apache.kafka.clients.producer.ProducerRecord

object Main extends IOApp {



  override def run(args: List[String]): IO[ExitCode] = {
    for{

    }yield ExitCode.Success
  }
}
