package am5800.harmonie.model

import org.joda.time.DateTime


public class InfiniteFlowItemsSource(private val scheduler: EntityScheduler,
                                     private val newEntitiesSource: NewEntitiesSource
) : FlowItemsSource {

    override fun getItems(amount: Int, deprecatedItems: Set<EntityId>): List<EntityId> {
        val now = DateTime()
        val scheduled = scheduler.getAllScheduledItems()
                .sortedBy { it.dueDate }
                .filter { !deprecatedItems.contains(it.entity) }

        val result = scheduled.filter { it.dueDate <= now }.map { it.entity }.toArrayList()
        if (result.count() >= amount) return result.take(amount)

        val scheduledForLater = scheduled.filter { it.dueDate > now }.map { it.entity }.shuffle()
        val newWords = newEntitiesSource.getNewEntities(amount, deprecatedItems)

        val first = newWords.iterator()
        val second = scheduledForLater.iterator()
        while (result.size() < amount && (first.hasNext() || second.hasNext())) {
            if (first.hasNext()) {
                val next = first.next()
                if (!deprecatedItems.contains(next)) result.add(next)
            }
            if (result.size() >= amount) break
            if (second.hasNext()) {
                val next = second.next()
                //if (!deprecatedItems.contains(next)) result.add(next)
            }
        }

        return result
    }
}