package dev.rmaiun.somprocessor.dtos

import fs2.concurrent.SignallingRef

case class RabbitSwitch[F[_]] (code:String, signal: SignallingRef[F, Boolean])
