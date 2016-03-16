package dataProcessor

interface Lemmatizer {
  fun tryFindLemma(form: String): String?
  fun normalize(lemma: String): String
}