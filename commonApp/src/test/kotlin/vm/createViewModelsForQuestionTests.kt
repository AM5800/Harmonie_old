package vm

import am5800.common.Language
import am5800.common.Sentence
import am5800.common.Word
import am5800.common.utils.Lifetime
import am5800.common.utils.TextRange
import am5800.harmonie.app.model.features.parallelSentence.ParallelSentenceQuestion
import am5800.harmonie.app.vm.ToggleableWordViewModel
import am5800.harmonie.app.vm.createViewModelsForQuestion
import com.google.common.collect.LinkedHashMultimap
import org.junit.Assert
import org.junit.Test


class CreateViewModelsForQuestionTests {
  @Test
  fun wordInTheMiddle() {
    Lifetime().use {
      val question = "Hello world!"
      val lemmas = LinkedHashMultimap.create<Word, TextRange>()
      lemmas.put(Word(Language.English, "ell"), TextRange(1, 4))
      val vms = createViewModelsForQuestion(ParallelSentenceQuestion(Sentence(null, Language.English, question), Sentence(null, Language.English, ""), lemmas), it)
      Assert.assertEquals(4, vms.size)
      val h = vms[0]
      val ell = vms[1]
      val o = vms[2]
      val world = vms[3]

      Assert.assertTrue(h !is ToggleableWordViewModel)
      Assert.assertTrue(ell is ToggleableWordViewModel)
      Assert.assertTrue(o !is ToggleableWordViewModel)
      Assert.assertTrue(world !is ToggleableWordViewModel)

      Assert.assertFalse(h.needSpaceAfter)
      Assert.assertFalse(ell.needSpaceAfter)
      Assert.assertTrue(o.needSpaceAfter)
      Assert.assertFalse(world.needSpaceAfter)

      Assert.assertEquals("H", h.text)
      Assert.assertEquals("ell", ell.text)
      Assert.assertEquals("o", o.text)
      Assert.assertEquals("world!", world.text)
    }
  }

  @Test
  fun wordInTheMiddleSurroundedBySpaces() {
    Lifetime().use {
      val question = " Hello  my   beautiful   world!"
      val lemmas = LinkedHashMultimap.create<Word, TextRange>()
      lemmas.put(Word(Language.English, "my"), TextRange(8, 10))
      val vms = createViewModelsForQuestion(ParallelSentenceQuestion(Sentence(null, Language.English, question), Sentence(null, Language.English, ""), lemmas), it)
      Assert.assertEquals(4, vms.size)

      val hello = vms[0]
      val my = vms[1]
      val beautiful = vms[2]
      val world = vms[3]

      Assert.assertTrue(hello !is ToggleableWordViewModel)
      Assert.assertTrue(my is ToggleableWordViewModel)
      Assert.assertTrue(beautiful !is ToggleableWordViewModel)
      Assert.assertTrue(world !is ToggleableWordViewModel)

      Assert.assertTrue(hello.needSpaceAfter)
      Assert.assertTrue(my.needSpaceAfter)
      Assert.assertTrue(beautiful.needSpaceAfter)
      Assert.assertFalse(world.needSpaceAfter)

      Assert.assertEquals("Hello", hello.text)
      Assert.assertEquals("my", my.text)
      Assert.assertEquals("beautiful", beautiful.text)
      Assert.assertEquals("world!", world.text)
    }
  }
}
