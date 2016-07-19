package am5800.harmonie.android

import am5800.common.componentContainer.ComponentContainer
import am5800.common.utils.Lifetime
import am5800.harmonie.android.controllers.DefaultFlowControllerOwner
import am5800.harmonie.android.controllers.EmptyFlowContentController
import am5800.harmonie.android.controllers.FillTheGapController
import am5800.harmonie.android.controllers.ParallelSentenceController
import am5800.harmonie.android.controllers.workspace.WorkspaceController
import am5800.harmonie.android.dbAccess.AndroidContentDb
import am5800.harmonie.android.dbAccess.AndroidUserDb
import am5800.harmonie.android.dbAccess.KeyValueDatabaseImpl
import am5800.harmonie.android.logging.AndroidLoggerProvider
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.feedback.impl.ErrorReportingServiceImpl
import am5800.harmonie.app.model.features.fillTheGap.FillTheGapFlowItemManagerImpl
import am5800.harmonie.app.model.features.parallelSentence.ParallelSentenceFlowManager
import am5800.harmonie.app.model.features.repetition.BucketRepetitionAlgorithm
import am5800.harmonie.app.model.features.repetition.WordsRepetitionServiceImpl
import am5800.harmonie.app.model.features.statistics.LanguageTagStatisticsProvider
import am5800.harmonie.app.model.services.LanguageCompetenceManagerStub
import am5800.harmonie.app.model.services.SqlRepetitionService
import am5800.harmonie.app.model.services.flow.FlowManager
import am5800.harmonie.app.model.services.languagePairs.SqlLanguagePairsProvider
import am5800.harmonie.app.model.services.sentenceSelection.SentenceSelectionStrategyImpl
import am5800.harmonie.app.model.services.sentencesAndWords.SqlSentenceAndWordsProvider
import am5800.harmonie.app.vm.DefaultFlowControllerOwnerViewModel
import am5800.harmonie.app.vm.FillTheGapViewModel
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

      val permanentDb = AndroidUserDb(this, lt)
      val keyValueDb = KeyValueDatabaseImpl(permanentDb)
      val contentDb = AndroidContentDb(this, keyValueDb, loggerProvider, lt)

      val repetitionService = SqlRepetitionService(BucketRepetitionAlgorithm(), permanentDb, debugOptions)
      val wordsRepetitionService = WordsRepetitionServiceImpl(repetitionService, lt, contentDb)

      val sentenceAndWordsProvider = SqlSentenceAndWordsProvider(contentDb, debugOptions)
      val sentenceSelectionStrategy = SentenceSelectionStrategyImpl(wordsRepetitionService, debugOptions, loggerProvider, sentenceAndWordsProvider)

      val languageCompetenceManager = LanguageCompetenceManagerStub()
      val parallelSentenceFlowManager = ParallelSentenceFlowManager(lt, sentenceAndWordsProvider, wordsRepetitionService, sentenceSelectionStrategy, SqlLanguagePairsProvider(contentDb), languageCompetenceManager)
      val seinFlowManager = FillTheGapFlowItemManagerImpl(contentDb, lt, debugOptions, languageCompetenceManager)
      val flowItemProviders = listOf(parallelSentenceFlowManager, seinFlowManager)

      val flowManager = FlowManager(lt, flowItemProviders, debugOptions)

      val localizationService = AndroidLocalizationService.create(resources, keyValueDb, lt)
      val feedbackService = AndroidFeedbackService(permanentDb)
      val reportingService = ErrorReportingServiceImpl(permanentDb)

      // ViewModels
      val parallelSentenceViewModel = ParallelSentenceViewModel(lt, parallelSentenceFlowManager, flowManager, localizationService, keyValueDb, reportingService)
      val defaultFlowControllerOwnerViewModel = DefaultFlowControllerOwnerViewModel(flowManager, lt)
      val fillTheGapViewModel = FillTheGapViewModel(lt, listOf(seinFlowManager), flowManager, reportingService, localizationService)
      val workspaceViewModel = WorkspaceViewModel(lt, flowManager, LanguageTagStatisticsProvider(wordsRepetitionService, sentenceAndWordsProvider))

      // View components
      val controllerStack = ControllerStack()
      val defaultFlowController = DefaultFlowControllerOwner(controllerStack, lt, defaultFlowControllerOwnerViewModel)
      WorkspaceController(workspaceViewModel, lt, controllerStack, localizationService)
      EmptyFlowContentController(defaultFlowController, flowManager, lt, localizationService)
      val parallelSentenceController = ParallelSentenceController(lt, defaultFlowController, parallelSentenceViewModel)
      FillTheGapController(fillTheGapViewModel, defaultFlowController, lt)

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