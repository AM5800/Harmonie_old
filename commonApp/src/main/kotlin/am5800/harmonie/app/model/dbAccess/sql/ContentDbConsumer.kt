package am5800.harmonie.app.model.dbAccess.sql

interface ContentDbConsumer {
  fun dbMigrationPhase1(oldDb: ContentDb)
  fun dbMigrationPhase2(newDb: ContentDb)
  fun dbInitialized(db: ContentDb)
}