package am5800.harmonie.app.model

import am5800.common.Language
import am5800.common.db.Word

interface WordSelector {
  fun findBestWord(language: Language): Word?
}