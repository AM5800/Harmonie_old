package am5800.harmonie.app.vm.workspace

import am5800.common.utils.properties.ReadonlyProperty


class SimpleWorkspaceItemViewModel(
    val title: ReadonlyProperty<String>,
    val description: ReadonlyProperty<String>,
    override val action: () -> Unit) : WorkspaceItemViewModel {
}