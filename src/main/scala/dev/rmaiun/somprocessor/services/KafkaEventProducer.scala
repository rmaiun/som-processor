package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import cats.implicits._
import fs2.kafka.{ KafkaProducer, ProducerRecord, ProducerRecords }
case class KafkaEventProducer[F[_]: Sync, T](topic: String, producer: KafkaProducer.Metrics[F, String, T]) {

  def publish(key: String, data: T): F[Unit] = {
    val record = ProducerRecord(topic, key, data)
    producer.produce(ProducerRecords.one(record)).flatten.map(_ => ())
  }
}
