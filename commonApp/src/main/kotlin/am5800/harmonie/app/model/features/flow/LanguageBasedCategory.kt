package am5800.harmonie.app.model.features.flow

import am5800.common.Language

interface LanguageBasedCategory : FlowItemCategory {
  val questionLanguage: Language
}