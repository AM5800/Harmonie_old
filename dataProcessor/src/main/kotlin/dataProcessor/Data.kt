package dataProcessor

import am5800.common.db.Sentence
import am5800.common.db.Word
import am5800.common.db.WordOccurrence

class Data(val sentenceTranslations: Map<Sentence, Sentence>,
           val wordOccurrences: List<WordOccurrence>,
           val difficulties: Map<Sentence, Int>,
           val realWorldWordsCount: Map<Word, Int>)