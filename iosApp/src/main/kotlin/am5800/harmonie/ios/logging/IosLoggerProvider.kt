package am5800.harmonie.ios.logging

import am5800.harmonie.app.model.logging.Logger
import am5800.harmonie.app.model.logging.LoggerProvider
import am5800.harmonie.ios.logging.IosLogger

class IosLoggerProvider : LoggerProvider {
  override fun getLogger(name: String): Logger {
    return IosLogger(name)
  }
}