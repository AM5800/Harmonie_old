package am5800.harmonie.android

import am5800.common.componentContainer.ComponentContainer
import am5800.common.utils.Lifetime
import am5800.harmonie.android.controllers.DefaultFlowControllerOwner
import am5800.harmonie.android.controllers.EmptyFlowContentController
import am5800.harmonie.android.controllers.ParallelSentenceController
import am5800.harmonie.android.controllers.workspace.WorkspaceController
import am5800.harmonie.android.dbAccess.AndroidContentDb
import am5800.harmonie.android.dbAccess.AndroidUserDb
import am5800.harmonie.android.dbAccess.KeyValueDatabaseImpl
import am5800.harmonie.android.logging.AndroidLoggerProvider
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.feedback.impl.ErrorReportingServiceImpl
import am5800.harmonie.app.model.features.parallelSentence.ParallelSentenceFlowManager
import am5800.harmonie.app.model.features.parallelSentence.ParallelSentenceSelectorImpl
import am5800.harmonie.app.model.features.parallelSentence.SentenceSelectionStrategyImpl
import am5800.harmonie.app.model.features.parallelSentence.sql.SqlSentenceScoreStorage
import am5800.harmonie.app.model.features.repetition.BucketRepetitionAlgorithm
import am5800.harmonie.app.model.features.repetition.LemmaRepetitionServiceImpl
import am5800.harmonie.app.model.features.statistics.LanguageTagStatisticsProvider
import am5800.harmonie.app.model.services.LanguageCompetenceManagerStub
import am5800.harmonie.app.model.services.SqlRepetitionService
import am5800.harmonie.app.model.services.flow.FlowManager
import am5800.harmonie.app.model.services.sentencesAndWords.SqlSentenceAndLemmasProvider
import am5800.harmonie.app.vm.DefaultFlowControllerOwnerViewModel
import am5800.harmonie.app.vm.ParallelSentenceViewModel
import am5800.harmonie.app.vm.workspace.WorkspaceViewModel
import android.app.Application

class HarmonieApplication : Application() {
  var modelContainer: ComponentContainer? = null

  override fun onTerminate() {
    modelContainer?.lifetime?.terminate()
  }

  override fun onCreate() {
    val loggerProvider = AndroidLoggerProvider()
    val logger = loggerProvider.getLogger(this.javaClass)

    try {
      val lt = Lifetime()
      val container = ComponentContainer(lt, null)
      val debugOptions = DebugOptions(false, false, null)
      modelContainer = container

      val userDb = AndroidUserDb(this, lt)
      val keyValueDb = KeyValueDatabaseImpl(userDb)
      val contentDb = AndroidContentDb(this, keyValueDb, loggerProvider, lt)

      val repetitionService = SqlRepetitionService(BucketRepetitionAlgorithm(), userDb, debugOptions)
      val sentenceAndLemmasProvider = SqlSentenceAndLemmasProvider(contentDb)
      val wordsRepetitionService = LemmaRepetitionServiceImpl(repetitionService, lt, sentenceAndLemmasProvider)

      val sentenceSelectionStrategy = ParallelSentenceSelectorImpl(wordsRepetitionService,
          debugOptions,
          loggerProvider,
          sentenceAndLemmasProvider,
          SqlSentenceScoreStorage(userDb),
          SentenceSelectionStrategyImpl())

      val languageCompetenceManager = LanguageCompetenceManagerStub()
      val parallelSentenceFlowManager = ParallelSentenceFlowManager(lt, sentenceAndLemmasProvider, wordsRepetitionService, sentenceSelectionStrategy, languageCompetenceManager)
      val flowItemProviders = listOf(parallelSentenceFlowManager)

      val flowManager = FlowManager(lt, flowItemProviders, debugOptions)

      val localizationService = AndroidLocalizationService.create(resources, keyValueDb, lt)
      val feedbackService = AndroidFeedbackService(userDb)
      val reportingService = ErrorReportingServiceImpl(userDb)

      // ViewModels
      val parallelSentenceViewModel = ParallelSentenceViewModel(lt, parallelSentenceFlowManager, flowManager, localizationService, keyValueDb, reportingService)
      val defaultFlowControllerOwnerViewModel = DefaultFlowControllerOwnerViewModel(flowManager, lt)
      val workspaceViewModel = WorkspaceViewModel(lt, flowManager, LanguageTagStatisticsProvider(wordsRepetitionService, sentenceAndLemmasProvider))

      // View components
      val controllerStack = ControllerStack()
      val defaultFlowController = DefaultFlowControllerOwner(controllerStack, lt, defaultFlowControllerOwnerViewModel)
      WorkspaceController(workspaceViewModel, lt, controllerStack, localizationService)
      EmptyFlowContentController(defaultFlowController, flowManager, lt, localizationService)
      val parallelSentenceController = ParallelSentenceController(lt, defaultFlowController, parallelSentenceViewModel)

      container.register(controllerStack)
      container.register(loggerProvider)
      container.register(workspaceViewModel)
      container.register(localizationService)
      container.register(feedbackService)
      container.register(parallelSentenceController)
    } catch (e: Exception) {
      logger.exception(e)
      throw e
    }

    super.onCreate()
  }
}