package am5800.harmonie.android.viewBinding

interface UIThreadRunner {
  fun runOnUiThread(function: () -> Unit)
}