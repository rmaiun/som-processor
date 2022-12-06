package dev.rmaiun.somprocessor.services

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import dev.rmaiun.somprocessor.domains.{ OptimizationRun, OptimizationRunState }
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent._
import dev.rmaiun.somprocessor.repositories.OptimizationRunRepository
import org.typelevel.log4cats.Logger

import java.time.{ ZoneOffset, ZonedDateTime }

class OptimizationRunModifier[F[_]: Sync](optimizationRunRepository: OptimizationRunRepository[F], logger: Logger[F]) {
  def applyUpdate(event: OptimizationRunUpdateEvent, optimizationRun: OptimizationRun): F[OptimizationRun] =
    event match {
      case _: IncrementFinalLog =>
        withCorrectExpirationTime(optimizationRun) { optRun =>
          optRun.copy(finalLogsReceivedQty = optRun.finalLogsReceivedQty + 1).pure
        }
      case _: ChangeOptimizingState =>
        optimizationRun.copy(state = OptimizationRunState.Optimizing).pure
      case e3: ChangeState =>
        withCorrectExpirationTime(optimizationRun) { optRun =>
          optRun.copy(state = e3.state).pure
        }
      case _: SuccessResultReceived =>
        withCorrectExpirationTime(optimizationRun) { optRun =>
          val updOptRun = optRun.copy(resultReceived = optRun.resultReceived + 1)
          if (!updOptRun.successfulResultReceived) {
            updOptRun.copy(successfulResultReceived = true).pure
          } else {
            updOptRun.pure
          }
        }
      case _: ErrorResultReceived =>
        withCorrectExpirationTime(optimizationRun) { optRun =>
          optRun.copy(resultReceived = optimizationRun.resultReceived + 1).pure
        }
      case BindAlgorithm(_, algorithmCode, criticalEndTime) =>
        optimizationRun.copy(algorithmCode = algorithmCode, criticalEndTime = criticalEndTime).pure
      case PairRequest(_, requestId: Long) =>
        optimizationRun.copy(assignedRequest = requestId).pure
      case AssignMessageId(_, messageId) =>
        optimizationRun.copy(messageId = messageId).pure
    }

  private def withCorrectExpirationTime(optRun: OptimizationRun)(action: => OptimizationRun => F[OptimizationRun]): F[OptimizationRun] =
    if (optRun.criticalEndTime.isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
      logger.error(s"Optimization ${optRun.id} is outdated so updates will be skipped")
      Applicative[F].pure(optRun)
    } else {
      action(optRun)
    }
}

object OptimizationRunModifier {
  def apply[F[_]](implicit ev: OptimizationRunModifier[F]): OptimizationRunModifier[F] = ev

  def impl[F[_]: Sync]: OptimizationRunModifier[F] =
    new OptimizationRunModifier[F]()
}
