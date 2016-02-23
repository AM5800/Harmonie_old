package am5800.harmonie.model

import am5800.harmonie.HarmonieDb
import am5800.harmonie.model.logging.LoggerProvider
import java.util.*

class TextPart(
        val id: TextPartId,
        val entities: List<EntityId>,
        val text: String
)

data class Text(
        val id: String,
        val title: String,
        val parts: List<TextPart>
)

class TextsProvider(loggerProvider: LoggerProvider,
                    private val database: HarmonieDb,
                    private val entityManagers: List<EntityManager>) {
  private val myTexts = ArrayList<Text>()
  val texts: List<Text> get() = myTexts
  private val logger = loggerProvider.getLogger(this.javaClass)

  init {
    logger.verbose("looking for texts")
    val textsCursor = database.rawQuery("SELECT id,name FROM texts", emptyArray())

    while (textsCursor.moveToNext()) {
      val textId = textsCursor.getString(0)
      val name = textsCursor.getString(1)
      val parts = getParts(textId)

      myTexts.add(Text(textId, name, parts))
    }
  }

  private fun getParts(textId: String): List<TextPart> {
    val result = ArrayList<TextPart>()
    val partsCursor = database.rawQuery("SELECT partText, partNumber FROM textParts WHERE textId = ?", arrayOf(textId))
    while (partsCursor.moveToNext()) {
      val partText = partsCursor.getString(0)
      val partNumber = partsCursor.getInt(1)
      val textPartId = TextPartId(textId, partNumber)

      val entities = entityManagers.flatMap { it.getEntitiesForText(textPartId) }
      result.add(TextPart(textPartId, entities, partText))
    }

    return result
  }
}