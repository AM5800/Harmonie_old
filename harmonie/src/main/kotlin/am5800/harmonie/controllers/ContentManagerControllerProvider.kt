package am5800.harmonie.controllers

import am5800.harmonie.model.EntityId

public interface ContentManagerControllerProvider {
    fun tryGetController(itemId: EntityId): FlowItemController?
}


