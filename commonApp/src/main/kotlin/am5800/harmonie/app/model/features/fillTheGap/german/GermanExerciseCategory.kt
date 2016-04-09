package am5800.harmonie.app.model.features.fillTheGap.german

import am5800.common.Language
import am5800.harmonie.app.model.features.flow.LanguageBasedCategory

class GermanExerciseCategory() : LanguageBasedCategory {
  override val questionLanguage = Language.German
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as GermanExerciseCategory

    if (questionLanguage != other.questionLanguage) return false

    return true
  }

  override fun hashCode(): Int {
    return questionLanguage.hashCode()
  }
}