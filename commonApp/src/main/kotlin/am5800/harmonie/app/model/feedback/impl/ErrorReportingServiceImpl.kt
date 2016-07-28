package am5800.harmonie.app.model.feedback.impl

import am5800.harmonie.app.model.UserDb
import am5800.harmonie.app.model.feedback.ErrorReportingService


class ErrorReportingServiceImpl(private val userDb: UserDb) : ErrorReportingService {
  override fun report(issueCategory: String, issueDescription: String) {
    userDb.execute("INSERT INTO errorReports VALUES(?, ?)", issueCategory, issueDescription)
  }

  init {
    userDb.execute("CREATE TABLE IF NOT EXISTS errorReports(category TEXT, description TEXT)")
  }
}