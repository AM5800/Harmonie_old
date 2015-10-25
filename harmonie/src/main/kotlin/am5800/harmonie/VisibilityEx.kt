package am5800.harmonie

import android.view.View
import am5800.harmonie.controllers.Visibility

public fun Visibility.toVisible() : Int {
    return when(this) {
        Visibility.Visible -> View.VISIBLE
        Visibility.Invisible -> View.INVISIBLE
        Visibility.Collapsed -> View.GONE
    }
}
