package dataProcessor

import am5800.common.Sentence
import am5800.common.Word
import am5800.common.WordOccurrence

class Data(val sentenceTranslations: Map<Sentence, Sentence>,
           val wordOccurrences: List<WordOccurrence>,
           val difficulties: Map<Sentence, Int>,
           val realWorldWordsCount: Map<Word, Int>,
           val fillTheGapOccurrences: List<FormOccurrence>)

