package am5800.harmonie.app.model.features.parallelSentence

import am5800.common.Language
import am5800.harmonie.app.model.features.flow.LanguageBasedCategory

data class ParallelSentenceCategory(override val questionLanguage: Language) : LanguageBasedCategory