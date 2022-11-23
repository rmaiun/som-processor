package dev.rmaiun.somprocessor.dtos

import cats.Monad
import cats.syntax.apply._
import fs2.concurrent.SignallingRef

case class RabbitSwitches[F[_]: Monad](switches: List[RabbitSwitch[F]]) {
  def refreshSwitch(switch: SignallingRef[F, Boolean]): F[Unit] =
    switch.update(x => !x) *> switch.update(x => !x)

  def refreshOptSwitch(switch: Option[RabbitSwitch[F]]): F[Unit] =
    switch.fold(Monad[F].pure(()))(sw => refreshSwitch(sw.signal))
}
