package dataProcessor.german

import dataProcessor.Lemmatizer


interface GermanLemmatizer : Lemmatizer {
  fun looksLikeSeparablePrefix(word: String): Boolean
  fun tryFindVerb(word: String, prefix: String): String?
}