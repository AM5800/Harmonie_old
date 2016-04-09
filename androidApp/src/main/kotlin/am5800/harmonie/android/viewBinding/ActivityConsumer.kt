package am5800.harmonie.android.viewBinding

import am5800.common.utils.Lifetime
import android.app.Activity


interface ActivityConsumer {
  fun setActivity(activity: Activity, lifetime: Lifetime)
}