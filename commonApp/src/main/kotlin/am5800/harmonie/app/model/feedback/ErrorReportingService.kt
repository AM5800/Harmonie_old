package am5800.harmonie.app.model.feedback


interface ErrorReportingService {
  fun report(issueCategory: String, issueDescription: String)
}