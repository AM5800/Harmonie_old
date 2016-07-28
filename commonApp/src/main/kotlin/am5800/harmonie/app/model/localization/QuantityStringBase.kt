package am5800.harmonie.app.model.localization

abstract class QuantityStringBase : QuantityString {
  protected fun build(value: Int, selectedString: String?): String {
    if (selectedString == null) throw Exception("Matching string not set")
    return selectedString.replace("%i", value.toString())
  }
}