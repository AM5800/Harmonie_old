package am5800.harmonie.model.logging

import am5800.harmonie.model.Logger

interface LoggerProvider {
  fun getLogger(name: String): Logger
  fun <T> getLogger(javaClass: Class<T>): Logger = getLogger(javaClass.name)
}