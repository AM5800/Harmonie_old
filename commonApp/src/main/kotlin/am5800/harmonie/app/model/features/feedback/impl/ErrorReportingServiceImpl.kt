package am5800.harmonie.app.model.features.feedback.impl

import am5800.harmonie.app.model.features.feedback.ErrorReportingService
import am5800.harmonie.app.model.services.UserDb


class ErrorReportingServiceImpl(private val userDb: UserDb) : ErrorReportingService {
  override fun report(issueCategory: String, issueDescription: String) {
    userDb.execute("INSERT INTO errorReports VALUES(?, ?)", issueCategory, issueDescription)
  }

  init {
    userDb.execute("CREATE TABLE IF NOT EXISTS errorReports(category TEXT, description TEXT)")
  }
}