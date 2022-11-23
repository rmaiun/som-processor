package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import dev.rmaiun.somprocessor.dtos.Event.GenerateInputDocumentEvent

import java.util.UUID
import scala.util.Random

case class SomInputFileGenerator[F[_]: Sync]() {
  def generateInputDocument(event: GenerateInputDocumentEvent): F[Unit] = {
    val uuId      = UUID.randomUUID().toString
    val randomInt = Random.nextInt(100)
    if (randomInt <= 10) {
      // todo impossible to create input file.
      //  mark optimization as failed
    } else {
      // todo set message id
      // create connection with SOM
      // update message id
    }
  }
}
