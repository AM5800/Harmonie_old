package am5800.harmonie.controllers

public enum class Visibility {
    Visible,
    Invisible,
    Collapsed
}

fun Boolean.toVisibility(): Visibility = if (this) Visibility.Visible else Visibility.Invisible
fun Boolean.toVisibilityCollapsed(): Visibility = if (this) Visibility.Visible else Visibility.Collapsed
