package am5800.harmonie.android

import am5800.common.componentContainer.ComponentContainer
import am5800.common.utils.Lifetime
import am5800.harmonie.android.controllers.*
import am5800.harmonie.android.dbAccess.AndroidContentDb
import am5800.harmonie.android.dbAccess.AndroidUserDb
import am5800.harmonie.android.dbAccess.KeyValueDatabaseImpl
import am5800.harmonie.android.logging.AndroidLoggerProvider
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.feedback.impl.ErrorReportingServiceImpl
import am5800.harmonie.app.model.features.fillTheGap.FillTheGapFlowItemManagerImpl
import am5800.harmonie.app.model.features.flow.FlowItemDistributionService
import am5800.harmonie.app.model.features.flow.FlowManager
import am5800.harmonie.app.model.features.parallelSentence.ParallelSentenceFlowManager
import am5800.harmonie.app.model.features.repetition.BucketRepetitionAlgorithm
import am5800.harmonie.app.model.features.repetition.WordsRepetitionServiceImpl
import am5800.harmonie.app.model.services.PreferredLanguagesServiceImpl
import am5800.harmonie.app.model.services.SqlRepetitionService
import am5800.harmonie.app.model.services.languagePairs.SqlLanguagePairsProvider
import am5800.harmonie.app.model.services.learnGraph.LearnGraphServiceImpl
import am5800.harmonie.app.model.services.learnGraph.SqlLearnGraphLoader
import am5800.harmonie.app.model.services.sentenceSelection.SentenceSelectionStrategyImpl
import am5800.harmonie.app.model.services.sentencesAndWords.SqlSentenceAndWordsProvider
import am5800.harmonie.app.vm.*
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

      val sentenceAndWordsProvider = SqlSentenceAndWordsProvider(contentDb)
      val learnGraphService = LearnGraphServiceImpl(SqlLearnGraphLoader(contentDb, sentenceAndWordsProvider), keyValueDb, lt)
      val sentenceSelectionStrategy = SentenceSelectionStrategyImpl(wordsRepetitionService, debugOptions, loggerProvider, sentenceAndWordsProvider, learnGraphService)

      val parallelSentenceFlowManager = ParallelSentenceFlowManager(lt, sentenceAndWordsProvider, wordsRepetitionService, sentenceSelectionStrategy, SqlLanguagePairsProvider(contentDb))
      val seinFlowManager = FillTheGapFlowItemManagerImpl(contentDb, lt, debugOptions)
      val flowItemProviders = listOf(parallelSentenceFlowManager, seinFlowManager)
      val languageService = PreferredLanguagesServiceImpl(keyValueDb, lt, flowItemProviders, debugOptions)

      val flowManager = FlowManager(lt, flowItemProviders, debugOptions)
      val distributionService = FlowItemDistributionService(flowItemProviders, languageService)

      val localizationService = AndroidLocalizationService.create(resources, keyValueDb, lt)
      val feedbackService = AndroidFeedbackService(permanentDb)
      val reportingService = ErrorReportingServiceImpl(permanentDb)

      // ViewModels
      val welcomeScreenViewModel = SelectLanguageViewModel(lt, localizationService, languageService)
      val parallelSentenceViewModel = ParallelSentenceViewModel(lt, parallelSentenceFlowManager, flowManager, localizationService, keyValueDb, reportingService)
      val startScreenViewModel = StartScreenViewModel(flowManager, lt, localizationService, distributionService, welcomeScreenViewModel, feedbackService)
      val defaultFlowControllerOwnerViewModel = DefaultFlowControllerOwnerViewModel(flowManager, lt)
      val fillTheGapViewModel = FillTheGapViewModel(lt, listOf(seinFlowManager), flowManager, reportingService, localizationService)

      // View components
      val controllerStack = ControllerStack()
      val defaultFlowController = DefaultFlowControllerOwner(controllerStack, lt, defaultFlowControllerOwnerViewModel)

      EmptyFlowContentController(defaultFlowController, flowManager, lt, localizationService)
      val parallelSentenceController = ParallelSentenceController(lt, defaultFlowController, parallelSentenceViewModel)
      StartScreenController(startScreenViewModel, lt, controllerStack)
      FillTheGapController(fillTheGapViewModel, defaultFlowController, lt)
      SelectLanguageController(welcomeScreenViewModel, lt, controllerStack)

      if (languageService.configurationRequired) {

        container.register(welcomeScreenViewModel)
      }

      container.register(controllerStack)
      container.register(loggerProvider)
      container.register(startScreenViewModel)
      container.register(languageService)
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