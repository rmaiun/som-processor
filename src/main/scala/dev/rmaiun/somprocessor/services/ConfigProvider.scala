package dev.rmaiun.somprocessor.services

import dev.rmaiun.somprocessor.dtos.configuration.AppConfiguration
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object ConfigProvider {

  def provideConfig: AppConfiguration =
    ConfigSource.default.loadOrThrow[AppConfiguration]
}
