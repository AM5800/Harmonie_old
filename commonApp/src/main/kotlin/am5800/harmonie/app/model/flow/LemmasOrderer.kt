package am5800.harmonie.app.model.flow

import am5800.common.Lemma


interface LemmasOrderer {
  fun reorder(lemmas: List<Lemma>): List<Lemma>
  fun pullUp(lemma: Lemma)
}