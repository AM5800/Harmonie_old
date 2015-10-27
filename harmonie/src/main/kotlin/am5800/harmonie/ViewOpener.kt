package am5800.harmonie

import am5800.harmonie.controllers.StatsController
import am5800.harmonie.viewBinding.BindableController

interface ViewOpener {
    public fun bringToFront(controller: BindableController)
}