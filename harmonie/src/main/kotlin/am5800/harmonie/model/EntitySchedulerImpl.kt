package am5800.harmonie.model

import org.joda.time.DateTime
import java.util.LinkedHashMap

public class EntitySchedulerImpl(private val settings: AppSettings,
                                 private val env: FileEnvironment,
                                 private val deserializers : List<EntityIdDeserializer>) : EntityScheduler {
    override fun remove(items: List<EntityId>) {
        var removed = false
        for (item in items) {
            if (!removed) removed = map.containsKey(item)
            map.remove(item)
        }
        if (removed) save()
    }

    private val map = LinkedHashMap<EntityId, DateTime>()
    private final val dataFileName = "schedule.dat"

    init {
        env.tryReadDataFile(dataFileName, {
            val reader = InputStreamWrapper(it)
            val n = reader.readInt()
            repeat(n, {
                val id = deserializers.deserialize(reader.readString())
                val date = DateTime(reader.readLong())
                map[id] = date
            })
        })
    }

    override fun scheduleItem(entity: EntityId, dueDate: DateTime) {
        map[entity] = dueDate
        save()
    }

    override fun getAllScheduledItems(): List<EntitySchedule> {
        return map.map({ kvp -> EntitySchedule(kvp.getKey(), kvp.getValue()) })
    }

    private fun save() {
        if (settings.readonlyMode) return
        env.writeDataFile(dataFileName, {
            val writer = OutputStreamWrapper(it)
            writer.writeInt(map.size())
            for (kvp in map) {
                writer.writeString(kvp.getKey().serialize())
                writer.writeLong(kvp.getValue().millis)
            }
        })
    }
}