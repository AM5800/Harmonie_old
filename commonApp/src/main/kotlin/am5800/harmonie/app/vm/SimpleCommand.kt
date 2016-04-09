package am5800.harmonie.app.vm

import am5800.common.utils.ReadonlyProperty

class SimpleCommand(val title: ReadonlyProperty<String>, val execute: () -> Unit)