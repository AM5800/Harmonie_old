package am5800.harmonie.app.model.logging


interface Logger {
  fun info(message: String)

  fun verbose(message: String)

  fun catch(function: () -> Unit)

  fun exception(e: Exception)
}