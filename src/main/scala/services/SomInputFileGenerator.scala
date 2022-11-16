package services

import dtos.GenerateInputDocumentEvent
import zio.Task

case class SomInputFileGenerator() {
  def generateInputDocument(event:GenerateInputDocumentEvent):Task[Unit] = {
  ???
  }
}
