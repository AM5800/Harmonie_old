package am5800.harmonie.app.model.features.fillTheGap

import am5800.common.Sentence

class FillTheGapInParallelSentenceQuestion(val sentence: Sentence,
                                           val translation: Sentence,
                                           val occurrenceStart: Int,
                                           val occurrenceEnd: Int,
                                           val wrongVariants: List<String>,
                                           val correctAnswer: String)