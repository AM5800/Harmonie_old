package am5800.harmonie.app.model.features.localization

class FormatStringImpl(private val format: String) : FormatString {
  override fun build(args: List<String>): String {
    // TODO: make efficient or use string.Format
    var result = format
    args.toList().forEachIndexed { i, any ->
      val oldValue = "%$i"
      val newValue = any.toString()
      result = result.replace(oldValue, newValue)
    }
    return result
  }
}