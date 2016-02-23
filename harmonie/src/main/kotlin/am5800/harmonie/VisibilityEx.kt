package am5800.harmonie

import am5800.harmonie.controllers.Visibility
import android.view.View

fun Visibility.toVisible(): Int {
  return when (this) {
    Visibility.Visible -> View.VISIBLE
    Visibility.Invisible -> View.INVISIBLE
    Visibility.Collapsed -> View.GONE
  }
}
