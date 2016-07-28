package am5800.harmonie.app.model.flow

import am5800.common.Lemma


interface LemmasLearnOrderer {
  fun reorder(lemmas: List<Lemma>): List<Lemma>
}