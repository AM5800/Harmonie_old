package am5800.harmonie.model.logging


interface Logger {
  fun info(message: String)

  fun verbose(message: String)

  fun catch(function: () -> Unit)

  fun exception(e: Exception)
}