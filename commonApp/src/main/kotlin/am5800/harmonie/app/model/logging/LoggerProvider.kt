package am5800.harmonie.app.model.logging

interface LoggerProvider {
  fun getLogger(name: String): Logger
  fun <T> getLogger(javaClass: Class<T>): Logger = getLogger(javaClass.name)
}