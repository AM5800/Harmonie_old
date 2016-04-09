package am5800.harmonie.app.model.features.feedback


interface ErrorReportingService {
  fun report(issueCategory: String, issueDescription: String)
}