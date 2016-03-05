package am5800.harmonie.android

interface ContentDbConsumer {
  fun dbMigrationPhase1(oldDb: ContentDb)
  fun dbMigrationPhase2(newDb: ContentDb)
  fun dbInitialized(db: ContentDb)
}