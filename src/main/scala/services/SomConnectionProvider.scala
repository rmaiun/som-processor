package services

import zio.Task

case class SomConnectionProvider() {
  def createSomConnection(): Task[Unit] = ???
  def disconnectFromSom():Task[Unit] = ???
}
