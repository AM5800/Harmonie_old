package am5800.harmonie.logging

import am5800.harmonie.model.Logger
import am5800.harmonie.model.logging.LoggerProvider

class AndroidLoggerProvider : LoggerProvider {
  override fun getLogger(name: String): Logger {
    return AndroidLogger(name)
  }
}