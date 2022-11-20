package dev.rmaiun.somprocessor.dtos

import dev.rmaiun.somprocessor.dtos.Event.EventCode

class Event(val code: EventCode) {}
object Event {
  type EventCode = String
}
