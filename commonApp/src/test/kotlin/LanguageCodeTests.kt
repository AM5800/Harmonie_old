import am5800.common.Language
import am5800.common.LanguageParser
import org.junit.Assert
import org.junit.Test

class LanguageCodeTests {
  @Test
  fun deserializationEn() {
    Assert.assertEquals(Language.English, LanguageParser.parse("En"))
  }

  @Test
  fun serializationEn() {
    Assert.assertEquals("en", Language.English.code)
  }

  @Test(expected = Exception::class)
  fun failAtWrongLanguageCode() {
    LanguageParser.parse("NonLanguageCode")
  }
}