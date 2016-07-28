package am5800.harmonie.app.model.localization

interface FormatString {
  fun build(args: List<String>): String
}