package am5800.harmonie.model

import java.util.*


class NewEntitiesSource(private val textsProvider: TextsProvider,
                        private val historyManager: AttemptsHistoryManager) {
  fun getNewEntities(amount: Int, deprecatedItems: Set<EntityId>): List<EntityId> {
    if (amount == 0) return emptyList()
    val used = historyManager.getKeys().plus(deprecatedItems).toSet()

    val result = ArrayList<EntityId>()
    val parts = textsProvider.texts.sortedBy { it.id }.flatMap { it.parts }

    for (part in parts) {
      for (entity in part.entities) {
        if (!used.contains(entity)) {
          result.add(entity)
          if (result.size >= amount) return result
        }
      }
    }

    return result
  }
}