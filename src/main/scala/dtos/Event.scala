package dtos

import dtos.Event.EventCode

class Event(val code: EventCode) {}
object Event {
  type EventCode = String
}
