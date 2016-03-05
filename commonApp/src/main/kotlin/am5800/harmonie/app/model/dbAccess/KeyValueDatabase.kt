package am5800.harmonie.app.model.dbAccess

interface KeyValueDatabase {
  fun getValue(key: String, defaultValue: String): String
  fun setValue(key: String, value: String)
}