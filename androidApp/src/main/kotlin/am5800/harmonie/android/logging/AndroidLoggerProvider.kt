package am5800.harmonie.android.logging

import am5800.harmonie.app.model.services.logging.Logger
import am5800.harmonie.app.model.services.logging.LoggerProvider

class AndroidLoggerProvider : LoggerProvider {
  override fun getLogger(name: String): Logger {
    return AndroidLogger(name)
  }
}