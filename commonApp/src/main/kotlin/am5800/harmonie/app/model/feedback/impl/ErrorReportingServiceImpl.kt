package am5800.harmonie.app.model.feedback.impl

import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.feedback.ErrorReportingService
import am5800.harmonie.app.model.sql.UserDb


class ErrorReportingServiceImpl(private val userDb: UserDb, debugOptions: DebugOptions) : ErrorReportingService {
  override fun report(issueCategory: String, issueDescription: String) {
    userDb.execute("INSERT INTO errorReports VALUES(?, ?)", issueCategory, issueDescription)
  }

  init {
    if (debugOptions.dropErrorReportsOnStart) userDb.execute("DROP TABLE IF EXISTS errorReports")
    userDb.execute("CREATE TABLE IF NOT EXISTS errorReports(category TEXT, description TEXT)")
  }
}