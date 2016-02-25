package am5800.harmonie.model

import am5800.harmonie.HarmonieDb


class DbSynchronizer(attemptsManager: AttemptsHistoryManager,
                     scheduler: EntityScheduler,
                     entityManagers: List<EntityManager>,
                     harmonieDb: HarmonieDb) {
  init {
    if (harmonieDb.dbIsUpdatedThisLaunch) {
      val list = attemptsManager.getKeys().plus(scheduler.getAllScheduledItems().map { it.entity })
      for (entity in list) {
        if (entityManagers.all { it.getExamples(entity).isEmpty() }) {
          val entityList = listOf(entity)
          attemptsManager.remove(entityList)
          scheduler.remove(entityList)
        }
      }
    }
  }
}