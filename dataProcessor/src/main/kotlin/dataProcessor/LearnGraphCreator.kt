package dataProcessor

import am5800.common.*
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.LinkedListMultimap
import com.google.common.collect.Multimap
import java.util.*


class LearnGraphCreator {
  companion object {
    fun createGraph(counts: Map<Word, Int>, occurrences: Collection<WordOccurrence>): Multimap<Language, LearnGraphNode> {
      val byLanguage = occurrences.groupBy { it.word.language }
      val result = LinkedHashMultimap.create<Language, LearnGraphNode>()
      for ((language, languageOccurrences) in byLanguage) {
        result.putAll(language, createGraph(counts, language, languageOccurrences))
      }

      return result
    }

    fun createGraph(counts: Map<Word, Int>, language: Language, occurrences: Collection<WordOccurrence>): List<LearnGraphNode> {
      val thisLangOccurrences = occurrences.filter { it.word.language == language }
      val sentences = LinkedListMultimap.create<Sentence, Word>()
      for (occurrence in thisLangOccurrences) {
        sentences.put(occurrence.sentence, occurrence.word)
      }

      val result = LinkedHashSet<LearnGraphNode>()
      val batch = LinkedHashSet<Word>()
      val queue = LinkedList<Word>(counts.toList().sortedByDescending { it.second }.map { it.first })

      while (queue.isNotEmpty()) {
        val word = queue.pop()
        val unlockedSentences = mutableListOf<Sentence>()

        batch.add(word)

        for ((sentence, words) in sentences.asMap()) {
          if (batch.containsAll(words))
            unlockedSentences.add(sentence)
        }

        if (unlockedSentences.isEmpty()) continue

        val wordsInUnlockingSentences = unlockedSentences.flatMap { sentences[it] }.distinct()
        val intersection = batch.intersect(wordsInUnlockingSentences) // TODO: check if order is not changed

        result.addAll(intersection.filter { it != word }.map { LearnGraphNode(it, emptyList()) })
        result.add(LearnGraphNode(word, unlockedSentences))

        for ((sentence, words) in sentences.asMap()) {
          for (w in intersection) sentences.remove(sentence, w)
        }

        batch.removeAll(intersection)
      }

      return result.toList()
    }
  }
}



