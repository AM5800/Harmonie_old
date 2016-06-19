package dataProcessor

import am5800.common.Language
import am5800.common.Sentence
import am5800.common.Word
import am5800.common.WordOccurrence
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.LinkedListMultimap
import com.google.common.collect.Multimap
import java.util.*

data class UnlockInfo(val word: Word, val sentences: List<Sentence>)

class WordsOrderCreator {
  companion object {
    fun createWordsOrder(counts: Map<Word, Int>, occurrences: Collection<WordOccurrence>): Multimap<Language, UnlockInfo> {
      val byLanguage = occurrences.groupBy { it.word.language }
      val result = LinkedHashMultimap.create<Language, UnlockInfo>()
      for ((language, languageOccurrences) in byLanguage) {
        result.putAll(language, createWordsOrder(counts, language, languageOccurrences))
      }

      return result
    }

    fun createWordsOrder(counts: Map<Word, Int>, language: Language, occurrences: Collection<WordOccurrence>): List<UnlockInfo> {
      val thisLangOccurrences = occurrences.filter { it.word.language == language }
      val sentences = LinkedListMultimap.create<Sentence, Word>()
      for (occurrence in thisLangOccurrences) {
        sentences.put(occurrence.sentence, occurrence.word)
      }

      val result = LinkedHashSet<UnlockInfo>()
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

        result.addAll(intersection.filter { it != word }.map { UnlockInfo(it, emptyList()) })
        result.add(UnlockInfo(word, unlockedSentences))

        for (notThisTimeWord in batch.minus(intersection).reversed()) {
          queue.addFirst(notThisTimeWord)
        }

        for ((sentence, words) in sentences.asMap()) {
          for (w in intersection) sentences.remove(sentence, w)
        }

        batch.clear()
      }

      return result.toList()
    }
  }
}



