package am5800.harmonie.app.model.services

import am5800.common.Language
import am5800.common.Word

interface WordSelector {
  fun findBestWord(language: Language): Word?
}