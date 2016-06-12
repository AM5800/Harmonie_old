package dataProcessor

import am5800.common.Sentence
import am5800.common.WordOccurrence

class ParseResultImpl(override val occurrencePos: Map<WordOccurrence, PartOfSpeech>,
                      override val occurrences: Set<WordOccurrence>,
                      override val translations: Map<Sentence, Sentence>,
                      override val sentences: List<Sentence>) : ParseResult