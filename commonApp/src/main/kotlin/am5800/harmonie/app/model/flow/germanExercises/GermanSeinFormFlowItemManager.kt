package am5800.harmonie.app.model.flow.germanExercises

import am5800.common.Language
import am5800.common.Sentence
import am5800.common.WordOccurrence
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.SentenceSelector
import am5800.harmonie.app.model.dbAccess.PreferredLanguagesService
import am5800.harmonie.app.model.dbAccess.SentenceProvider
import am5800.harmonie.app.model.flow.FlowItemCategory
import am5800.harmonie.app.model.flow.FlowItemProvider


class GermanSeinFormQuestion(val question: Sentence, val answer: Sentence, val occurrence: WordOccurrence, variants: List<String>)

class GermanSeinFormFlowItemManager(
    private val preferredLanguagesService: PreferredLanguagesService,
    private val sentenceSelector: SentenceSelector,
    private val sentenceProvider: SentenceProvider,
    lifetime: Lifetime) : FlowItemProvider {
  override val supportedCategories: Set<FlowItemCategory> = setOf(SeinFormQuizCategory(Language.German))

  private val forms = listOf("sein", "bin", "ist", "bist", "seid", "sind")

  val question = Property<GermanSeinFormQuestion>(lifetime, null)

  override fun tryPresentNextItem(category: FlowItemCategory): Boolean {
    if (category !is SeinFormQuizCategory) throw UnsupportedOperationException("Category is not supported")

    val knownLanguages = preferredLanguagesService.knownLanguages.value!!
    val searchResult = sentenceSelector.findSentenceWithLemma(Language.German, knownLanguages, "sein") ?: return false

    val occurrences = sentenceProvider.getOccurrences(searchResult.question)


    return false
  }
}