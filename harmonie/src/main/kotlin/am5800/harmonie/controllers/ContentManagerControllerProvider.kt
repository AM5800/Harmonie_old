package am5800.harmonie.controllers

import am5800.harmonie.model.EntityId

interface ContentManagerControllerProvider {
  fun tryGetController(itemId: EntityId): FlowItemController?
}


