package am5800.harmonie.app.model.features.localization

interface FormatString {
  fun build(args: List<String>): String
}