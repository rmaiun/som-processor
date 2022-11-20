package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import dev.rmaiun.somprocessor.dtos.GenerateInputDocumentEvent

case class SomInputFileGenerator[F[_]:Sync]() {
  def generateInputDocument(event:GenerateInputDocumentEvent):F[Unit] = {
  ???
  }
}
