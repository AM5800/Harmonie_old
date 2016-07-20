import am5800.common.Language
import org.junit.Assert
import org.junit.Test

class LanguageCodeTests {
  @Test
  fun deserializationEn() {
    Assert.assertEquals(Language.English, Language.parse("En"))
  }

  @Test
  fun serializationEn() {
    Assert.assertEquals("en", Language.English.code)
  }

  @Test(expected = Exception::class)
  fun failAtWrongLanguageCode() {
    Language.parse("NonLanguageCode")
  }
}