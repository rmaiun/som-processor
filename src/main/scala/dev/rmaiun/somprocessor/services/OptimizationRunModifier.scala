package dev.rmaiun.somprocessor.services

import cats.effect.Sync
import dev.rmaiun.somprocessor.domains.{OptimizationRun, OptimizationRunState}
import dev.rmaiun.somprocessor.dtos.OptimizationUpdateResult
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent
import dev.rmaiun.somprocessor.events.OptimizationRunUpdateEvent._
import dev.rmaiun.somprocessor.repositories.OptimizationRunRepository
import org.typelevel.log4cats.Logger
import cats.implicits._
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.{ZoneOffset, ZonedDateTime}
import scala.annotation.tailrec

class OptimizationRunModifier[F[_]: Sync](optimizationRunRepository: OptimizationRunRepository[F], logger: Logger[F]) {

  def applyBatchUpdate(events: List[OptimizationRunUpdateEvent]):F[Unit] = {
    val idList = events.map(_.id)
    optimizationRunRepository.list(idList.some).map(optRunList => {
      optRunList.map(optRun => {
        val relatedEvents = events.filter(_.id == optRun.id)
        applyUpdatesRecursively(relatedEvents, OptimizationUpdateResult(optRun))
      })
    }).flatMap(optimizationRunRepository.updateMany)
  }
  @tailrec
  private def applyUpdatesRecursively(events: List[OptimizationRunUpdateEvent], updateResult: OptimizationUpdateResult): OptimizationRun =
    events match {
      case ::(head, next) =>
        val updOptRun = applyUpdate(head, updateResult.optimizationRun)
        if (updOptRun.expired){
          updOptRun.optimizationRun
        }else{
          applyUpdatesRecursively(next, updOptRun)
        }
      case Nil => updateResult.optimizationRun
    }
  private def applyUpdate(event: OptimizationRunUpdateEvent, optimizationRun: OptimizationRun): OptimizationUpdateResult =
    event match {
      case _: IncrementFinalLog =>
        withCorrectExpirationTime(optimizationRun) { optRun =>
          optRun.copy(finalLogsReceivedQty = optRun.finalLogsReceivedQty + 1)
        }
      case _: ChangeOptimizingState =>
        OptimizationUpdateResult(optimizationRun.copy(state = OptimizationRunState.Optimizing))
      case e3: ChangeState =>
        withCorrectExpirationTime(optimizationRun) { optRun =>
          optRun.copy(state = e3.state)
        }
      case _: SuccessResultReceived =>
        withCorrectExpirationTime(optimizationRun) { optRun =>
          val updOptRun = optRun.copy(resultReceived = optRun.resultReceived + 1)
          if (!updOptRun.successfulResultReceived) {
            updOptRun.copy(successfulResultReceived = true)
          } else {
            updOptRun
          }
        }
      case _: ErrorResultReceived =>
        withCorrectExpirationTime(optimizationRun) { optRun =>
          optRun.copy(resultReceived = optimizationRun.resultReceived + 1)
        }
      case BindAlgorithm(_, algorithmCode, criticalEndTime) =>
        OptimizationUpdateResult(optimizationRun.copy(algorithmCode = algorithmCode, criticalEndTime = criticalEndTime))
      case PairRequest(_, requestId: Long) =>
        OptimizationUpdateResult(optimizationRun.copy(assignedRequest = requestId))
      case AssignMessageId(_, messageId) =>
        OptimizationUpdateResult(optimizationRun.copy(messageId = messageId))
    }

  private def withCorrectExpirationTime(optRun: OptimizationRun)(action: => OptimizationRun => OptimizationRun): OptimizationUpdateResult =
    if (optRun.criticalEndTime.isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
      OptimizationUpdateResult(optRun, expired = true)
    } else {
      OptimizationUpdateResult(action(optRun))
    }
}

object OptimizationRunModifier {
  def apply[F[_]](implicit ev: OptimizationRunModifier[F]): OptimizationRunModifier[F] = ev

  def impl[F[_]: Sync](optimizationRunRepository: OptimizationRunRepository[F]): F[OptimizationRunModifier[F]] =
    Slf4jLogger.create[F].map(new OptimizationRunModifier[F](optimizationRunRepository, _))
}
