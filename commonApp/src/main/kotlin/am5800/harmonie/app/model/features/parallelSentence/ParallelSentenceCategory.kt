package am5800.harmonie.app.model.features.parallelSentence

import am5800.common.Language
import am5800.harmonie.app.model.features.flow.LanguageCategory

data class ParallelSentenceCategory(override val learnLanguage: Language) : LanguageCategory {
  override fun toString(): String {
    return "${javaClass.name}, learn: ${learnLanguage.code}"
  }
}