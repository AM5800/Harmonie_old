package am5800.harmonie.android

import am5800.common.componentContainer.ComponentContainer
import am5800.common.utils.Lifetime
import am5800.harmonie.android.controllers.DefaultFlowControllerOwner
import am5800.harmonie.android.controllers.EmptyFlowContentController
import am5800.harmonie.android.controllers.ParallelSentenceController
import am5800.harmonie.android.controllers.wordsList.WordsListController
import am5800.harmonie.android.controllers.workspace.WorkspaceController
import am5800.harmonie.android.dbAccess.AndroidContentDb
import am5800.harmonie.android.dbAccess.AndroidUserDb
import am5800.harmonie.android.dbAccess.KeyValueDatabaseImpl
import am5800.harmonie.android.logging.AndroidLoggerProvider
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.feedback.impl.ErrorReportingServiceImpl
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.flow.SqlLemmasOrderer
import am5800.harmonie.app.model.languageCompetence.LanguageCompetenceManagerStub
import am5800.harmonie.app.model.lemmasMeaning.SqlLemmaMeaningsProvider
import am5800.harmonie.app.model.parallelSentence.ParallelSentenceFlowManager
import am5800.harmonie.app.model.parallelSentence.ParallelSentenceSelectorImpl
import am5800.harmonie.app.model.parallelSentence.SentenceSelectionStrategyImpl
import am5800.harmonie.app.model.parallelSentence.sql.SqlSentenceScoreStorage
import am5800.harmonie.app.model.repetition.BucketRepetitionAlgorithm
import am5800.harmonie.app.model.repetition.LemmaRepetitionServiceImpl
import am5800.harmonie.app.model.repetition.SqlRepetitionService
import am5800.harmonie.app.model.sentencesAndLemmas.SqlSentenceAndLemmasProvider
import am5800.harmonie.app.model.statistics.LanguageTagStatisticsProvider
import am5800.harmonie.app.vm.DefaultFlowControllerOwnerViewModel
import am5800.harmonie.app.vm.ParallelSentenceViewModel
import am5800.harmonie.app.vm.wordsList.WordsListViewModel
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
      val lemmasRepetitionService = LemmaRepetitionServiceImpl(repetitionService, lt, sentenceAndLemmasProvider)

      val sentenceSelectionStrategy = ParallelSentenceSelectorImpl(lemmasRepetitionService,
          debugOptions,
          loggerProvider,
          sentenceAndLemmasProvider,
          SqlSentenceScoreStorage(userDb),
          SentenceSelectionStrategyImpl())

      val languageCompetenceManager = LanguageCompetenceManagerStub()
      val parallelSentenceFlowManager = ParallelSentenceFlowManager(lt, sentenceAndLemmasProvider, lemmasRepetitionService, sentenceSelectionStrategy, languageCompetenceManager)
      val flowItemProviders = listOf(parallelSentenceFlowManager)

      val flowManager = FlowManager(lt, flowItemProviders, debugOptions)

      val localizationService = AndroidLocalizationService.create(resources, keyValueDb, lt)
      val feedbackService = AndroidFeedbackService(userDb)
      val reportingService = ErrorReportingServiceImpl(userDb)
      val orderer = SqlLemmasOrderer(userDb, sentenceAndLemmasProvider)

      // ViewModels
      val parallelSentenceViewModel = ParallelSentenceViewModel(lt, parallelSentenceFlowManager, flowManager, localizationService, SqlLemmaMeaningsProvider(), keyValueDb, reportingService)
      val defaultFlowControllerOwnerViewModel = DefaultFlowControllerOwnerViewModel(flowManager, lt)
      val wordsListViewModel = WordsListViewModel(lt, sentenceAndLemmasProvider, lemmasRepetitionService, orderer)
      val workspaceViewModel = WorkspaceViewModel(lt, flowManager, LanguageTagStatisticsProvider(lemmasRepetitionService, sentenceAndLemmasProvider), feedbackService, wordsListViewModel, localizationService)

      // View components
      val controllerStack = ControllerStack()
      val defaultFlowController = DefaultFlowControllerOwner(controllerStack, lt, defaultFlowControllerOwnerViewModel)
      WorkspaceController(workspaceViewModel, lt, controllerStack, localizationService)
      EmptyFlowContentController(defaultFlowController, flowManager, lt, localizationService)
      val parallelSentenceController = ParallelSentenceController(lt, defaultFlowController, parallelSentenceViewModel)
      WordsListController(wordsListViewModel, controllerStack, lt)

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