package am5800.harmonie.android

import am5800.harmonie.app.vm.Visibility
import android.view.View

fun Visibility.toVisible(): Int {
  return when (this) {
    Visibility.Visible -> View.VISIBLE
    Visibility.Invisible -> View.INVISIBLE
    Visibility.Collapsed -> View.GONE
  }
}
