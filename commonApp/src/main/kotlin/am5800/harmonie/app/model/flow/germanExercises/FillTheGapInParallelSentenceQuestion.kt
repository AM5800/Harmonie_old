package am5800.harmonie.app.model.flow.germanExercises

import am5800.common.Sentence
import am5800.common.WordOccurrence

class FillTheGapInParallelSentenceQuestion(val sentence: Sentence,
                                           val translation: Sentence,
                                           val occurrence: WordOccurrence,
                                           val wrongVariants: List<String>,
                                           val correctAnswer: String)