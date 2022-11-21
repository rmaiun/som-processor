package dev.rmaiun.somprocessor

import cats.effect.{ ExitCode, IO, IOApp }
import cats.syntax.all._
import dev.rmaiun.somprocessor.domains.OptimizationRunState
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent.ChangeState
import vulcan.{ Codec, _ }
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent
import dev.rmaiun.somprocessor.services.OptimizationRunModifier
import fs2.kafka.{
  AutoOffsetReset,
  ConsumerSettings,
  Deserializer,
  KafkaConsumer,
  KafkaProducer,
  ProducerRecord,
  ProducerRecords,
  ProducerSettings,
  RecordDeserializer,
  RecordSerializer,
  Serializer
}
import fs2.kafka.vulcan.{ avroDeserializer, avroSerializer, Auth, AvroSettings, SchemaRegistryClientSettings }
import vulcan.generic._
object Main extends IOApp {

  val avroSettings =
    AvroSettings {
      SchemaRegistryClientSettings[IO]("http://localhost:8081")
//        .withAuth(Auth.Basic("username", "password"))
    }

//  implicit val optimizationRunStateCodec: Codec[OptimizationRunState] = Codec[OptimizationRunState]

//  implicit val changeStateCodec: Codec[ChangeState] =
//    Codec.record(
//      name = "ChangeState",
//      namespace = "dev.rmaiun.somprocessor"
//    ) { field =>
//      (
//        field("id", _.id),
//        field("state", _.state)
//      ).mapN(ChangeState)
//    }

//  implicit val changeStateSerializer: RecordSerializer[IO, ChangeState] =
//    avroSerializer[ChangeState].using(avroSettings)
//
//  implicit val changeStateDeserializer: RecordDeserializer[IO, ChangeState] =
//    avroDeserializer[ChangeState].using(avroSettings)

  override def run(args: List[String]): IO[ExitCode] = {
    import cats.implicits._
    println(Codec[OptimizationRunState].schema)
    implicit val x: Codec[OptimizationRunUpdateEvent] = Codec.derive
    println(x.schema)

    implicit val OptimizationRunUpdateEventSerializer: RecordSerializer[IO, OptimizationRunUpdateEvent] =
      avroSerializer[OptimizationRunUpdateEvent].using(avroSettings)

    implicit val OptimizationRunUpdateEventDeserializer: RecordDeserializer[IO, OptimizationRunUpdateEvent] =
      avroDeserializer[OptimizationRunUpdateEvent].using(avroSettings)

    val consumerSettings: ConsumerSettings[IO, String, OptimizationRunUpdateEvent] =
      ConsumerSettings(
        keyDeserializer = Deserializer[IO, String],
        valueDeserializer = OptimizationRunUpdateEventDeserializer
      )
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group")

    val producerSettings: ProducerSettings[IO, String, OptimizationRunUpdateEvent] = {
      ProducerSettings(
        keySerializer = Serializer[IO, String],
        valueSerializer = OptimizationRunUpdateEventSerializer
      )
        .withBootstrapServers("localhost:9092")
    }
//    IO.pure(ExitCode.Success)

    val processor = OptimizationRunModifier.impl[IO]
    val effectStream = for {
      kp    <- KafkaProducer.stream(producerSettings)
      record = ProducerRecord("opt_run_updates", "12345", ChangeState(12345, OptimizationRunState.Optimizing))
      res   <- fs2.Stream.eval(kp.produce(ProducerRecords.one(record)).flatten)
      _ <- fs2.Stream.eval(
             KafkaConsumer
               .stream(consumerSettings)
               .subscribeTo("opt_run_updates")
               .records
               .evalTap(r => processor.applyUpdate(r.record.value))
               .compile
               .drain
           )
    } yield ExitCode.Success
    effectStream.compile.toList.map(_.head)
  }
}
