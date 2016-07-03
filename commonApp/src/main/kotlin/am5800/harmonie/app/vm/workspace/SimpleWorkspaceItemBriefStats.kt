package am5800.harmonie.app.vm.workspace

import am5800.common.utils.properties.ReadonlyProperty

class SimpleWorkspaceItemBriefStats(val onDue: ReadonlyProperty<Int>,
                                    val onLearning: ReadonlyProperty<Int>,
                                    val total: ReadonlyProperty<Int>)