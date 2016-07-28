package am5800.harmonie.app.model.parallelSentence

import am5800.common.Language
import am5800.harmonie.app.model.flow.LanguageTag

data class ParallelSentenceTag(override val learnLanguage: Language) : LanguageTag {
  override fun toString(): String {
    return "${javaClass.name}, learn: ${learnLanguage.code}"
  }
}