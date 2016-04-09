package am5800.harmonie.app.model.features.feedback.impl

import am5800.harmonie.app.model.features.feedback.ErrorReportingService
import am5800.harmonie.app.model.services.PermanentDb


class ErrorReportingServiceImpl(private val permanentDb: PermanentDb) : ErrorReportingService {
  override fun report(issueCategory: String, issueDescription: String) {
    permanentDb.execute("INSERT INTO errorReports VALUES(?, ?)", issueCategory, issueDescription)
  }

  init {
    permanentDb.execute("CREATE TABLE IF NOT EXISTS errorReports(category TEXT, description TEXT)")
  }
}