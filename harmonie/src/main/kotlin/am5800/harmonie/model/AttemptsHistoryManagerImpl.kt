package am5800.harmonie.model

import com.google.common.collect.HashMultimap
import org.joda.time.DateTime

public class AttemptsHistoryManagerImpl(private val env: FileEnvironment,
                                        private val settings: AppSettings,
                                        private val deserializers : List<EntityIdDeserializer>) : AttemptsHistoryManager {
    private final val file = "attempts.dat"

    override fun getKeys(): List<EntityId> {
        return map.keys().distinct().toList()
    }

    override fun remove(items: List<EntityId>) {
        var removed = false
        for (item in items) {
            if (!removed) removed = map.containsKey(item)
            map.removeAll(item)
        }
        if (removed) save()
    }

    private val map = HashMultimap.create<EntityId, Attempt>()!!

    init {
        read(map)
    }

    protected fun read(reader: (InputStreamWrapper) -> Unit) {
        env.tryReadDataFile(file, {
            reader(InputStreamWrapper(it))
        })
    }
    protected fun write(writer: (OutputStreamWrapper) -> Unit) {
        if (settings.readonlyMode) return
        env.writeDataFile(file, {
            writer(OutputStreamWrapper(it))
        })
    }
    protected fun append(writer: (OutputStreamWrapper) -> Unit) {
        if (settings.readonlyMode) return
        env.appendDataFile(file, {
            writer(OutputStreamWrapper(it))
        })
    }

    private fun read(result: HashMultimap<EntityId, Attempt>) {
        read({ reader ->
            while (reader.available() != 0) {
                val id = deserializers.deserialize(reader.readString())
                val date = DateTime(reader.readLong())
                val note = reader.readString()
                val success = reader.readBool()
                val score = reader.readFloat()

                result.put(id, Attempt(id, date, score, note, success))
            }
        })
    }

    private fun save() {
        write { writer ->
            for (entry in map.asMap()) {
                for (attempt in entry.value) {
                    writeAttempt(attempt, writer)
                }
            }
        }
    }

    override fun getAttempts(entity: EntityId): List<Attempt> {
        val result = map[entity] ?: return emptyList()
        return result.toList().sortedBy { a -> a.date }
    }

    override fun addAttempt(attempt: Attempt) {
        map.put(attempt.entity, attempt)
        append({ writer -> writeAttempt(attempt, writer) })
    }

    private fun writeAttempt(attempt: Attempt, writer: OutputStreamWrapper) {
        writer.writeString(attempt.entity.serialize())
        writer.writeLong(attempt.date.millis)
        writer.writeString(attempt.note)
        writer.writeBool(attempt.success)
        writer.writeFloat(attempt.score)
    }

}



