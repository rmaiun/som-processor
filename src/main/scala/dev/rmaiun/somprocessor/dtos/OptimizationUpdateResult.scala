package dev.rmaiun.somprocessor.dtos

import dev.rmaiun.somprocessor.domains.OptimizationRun

case class OptimizationUpdateResult (optimizationRun:OptimizationRun, expired:Boolean = false)
