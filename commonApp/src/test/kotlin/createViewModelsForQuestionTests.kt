import am5800.common.Language
import am5800.common.db.Sentence
import am5800.common.db.Word
import am5800.common.utils.Lifetime
import am5800.common.utils.TextRange
import am5800.harmonie.app.model.flow.ParallelSentenceQuestion
import am5800.harmonie.app.vm.ToggleableWordViewModel
import am5800.harmonie.app.vm.createViewModelsForQuestion
import com.google.common.collect.LinkedHashMultimap
import org.junit.Assert
import org.junit.Test


class createViewModelsForQuestionTests {
  @Test
  fun wordInTheMiddle() {
    Lifetime().use {
      val question = "Hello world!"
      val lemmas = LinkedHashMultimap.create<Word, TextRange>()
      lemmas.put(Word(Language.English, "ell"), TextRange(1, 4))
      val vms = createViewModelsForQuestion(ParallelSentenceQuestion(Sentence(Language.English, question), Sentence(Language.English, ""), lemmas), it)
      Assert.assertEquals(4, vms.size)
      val h = vms[0]
      val ell = vms[1]
      val o = vms[2]
      val world = vms[3]

      Assert.assertTrue(h !is ToggleableWordViewModel)
      Assert.assertTrue(ell is ToggleableWordViewModel)
      Assert.assertTrue(o !is ToggleableWordViewModel)
      Assert.assertTrue(world !is ToggleableWordViewModel)

      Assert.assertFalse(h.needSpaceBefore)
      Assert.assertFalse(ell.needSpaceBefore)
      Assert.assertFalse(o.needSpaceBefore)
      Assert.assertTrue(world.needSpaceBefore)

      Assert.assertEquals("H", h.text)
      Assert.assertEquals("ell", ell.text)
      Assert.assertEquals("o", o.text)
      Assert.assertEquals("world!", world.text)
    }
  }

  @Test
  fun wordInTheMiddleSurroundedBySpaces() {
    Lifetime().use {
      val question = "Hello  my   beautiful   world!"
      val lemmas = LinkedHashMultimap.create<Word, TextRange>()
      lemmas.put(Word(Language.English, "my"), TextRange(7, 9))
      val vms = createViewModelsForQuestion(ParallelSentenceQuestion(Sentence(Language.English, question), Sentence(Language.English, ""), lemmas), it)
      Assert.assertEquals(4, vms.size)

      val hello = vms[0]
      val my = vms[1]
      val beautiful = vms[2]
      val world = vms[3]

      Assert.assertTrue(hello !is ToggleableWordViewModel)
      Assert.assertTrue(my is ToggleableWordViewModel)
      Assert.assertTrue(beautiful !is ToggleableWordViewModel)
      Assert.assertTrue(world !is ToggleableWordViewModel)

      Assert.assertFalse(hello.needSpaceBefore)
      Assert.assertTrue(my.needSpaceBefore)
      Assert.assertTrue(beautiful.needSpaceBefore)
      Assert.assertTrue(world.needSpaceBefore)

      Assert.assertEquals("Hello", hello.text)
      Assert.assertEquals("my", my.text)
      Assert.assertEquals("beautiful", beautiful.text)
      Assert.assertEquals("world!", world.text)
    }
  }
}
