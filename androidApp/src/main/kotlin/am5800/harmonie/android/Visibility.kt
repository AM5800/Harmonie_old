package am5800.harmonie.android

import android.view.View

enum class Visibility {
  Visible,
  Invisible,
  Collapsed
}

fun Visibility.toAndroidVisibility(): Int {
  return when (this) {
    Visibility.Visible -> View.VISIBLE
    Visibility.Invisible -> View.INVISIBLE
    Visibility.Collapsed -> View.GONE
  }
}
