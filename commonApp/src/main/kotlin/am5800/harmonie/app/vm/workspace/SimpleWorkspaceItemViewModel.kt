package am5800.harmonie.app.vm.workspace

import am5800.harmonie.app.model.localization.LocalizationTable


class SimpleWorkspaceItemViewModel(
    val header: (LocalizationTable) -> String,
    val description: (LocalizationTable) -> String,
    override val action: () -> Unit) : WorkspaceItemViewModel {
}