package services

import zio.Task

case class SomConnectionProvider() {
  def createSomConnection(): Task[Unit] = {
  ???
  }
}
