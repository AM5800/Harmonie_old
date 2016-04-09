package am5800.harmonie.android

import am5800.common.componentContainer.ComponentContainer
import am5800.common.utils.Lifetime
import am5800.harmonie.android.controllers.*
import am5800.harmonie.android.dbAccess.AndroidContentDb
import am5800.harmonie.android.dbAccess.AndroidPermanentDb
import am5800.harmonie.android.dbAccess.KeyValueDatabaseImpl
import am5800.harmonie.android.logging.AndroidLoggerProvider
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.feedback.impl.ErrorReportingServiceImpl
import am5800.harmonie.app.model.features.fillTheGap.german.GermanExerciseFlowItemManager
import am5800.harmonie.app.model.features.flow.FlowItemDistributionService
import am5800.harmonie.app.model.features.flow.FlowManager
import am5800.harmonie.app.model.features.parallelSentence.ParallelSentenceFlowManager
import am5800.harmonie.app.model.features.repetition.BucketRepetitionAlgorithm
import am5800.harmonie.app.model.features.repetition.WordsRepetitionServiceImpl
import am5800.harmonie.app.model.services.impl.*
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

      val permanentDb = AndroidPermanentDb(this, lt)
      val keyValueDb = KeyValueDatabaseImpl(permanentDb)
      val contentDb = AndroidContentDb(this, keyValueDb, loggerProvider, lt)

      val repetitionService = SqlRepetitionService(BucketRepetitionAlgorithm(), permanentDb, debugOptions)
      val wordsRepetitionService = WordsRepetitionServiceImpl(repetitionService, lt, contentDb)

      val sentenceProvider = SqlSentenceProvider(contentDb)
      val wordSelector = SqlWordSelector(wordsRepetitionService, keyValueDb, contentDb, lt, debugOptions)
      val sentenceSelector = SqlSentenceSelector(wordsRepetitionService, loggerProvider, contentDb, debugOptions, wordSelector)
      val languageService = SqlPreferredLanguagesService(keyValueDb, lt, contentDb, debugOptions)


      val parallelSentenceFlowManager = ParallelSentenceFlowManager(lt, languageService, sentenceProvider, wordsRepetitionService, sentenceSelector)
      val seinFlowManager = GermanExerciseFlowItemManager(languageService, contentDb, lt, debugOptions)
      val flowItemProviders = listOf(parallelSentenceFlowManager, seinFlowManager)
      val flowManager = FlowManager(lt, loggerProvider, flowItemProviders, debugOptions)
      val distributionService = FlowItemDistributionService(flowItemProviders)

      val localizationService = AndroidLocalizationService.create(resources, keyValueDb, lt)
      val feedbackService = AndroidFeedbackService(permanentDb)
      val reportingService = ErrorReportingServiceImpl(permanentDb)

      // ViewModels
      val welcomeScreenViewModel = WelcomeScreenViewModel(lt, localizationService, languageService)
      val parallelSentenceViewModel = ParallelSentenceViewModel(lt, parallelSentenceFlowManager, flowManager, localizationService, reportingService)
      val startScreenViewModel = StartScreenViewModel(flowManager, lt, localizationService, distributionService, welcomeScreenViewModel, feedbackService)
      val defaultFlowControllerOwnerViewModel = DefaultFlowControllerOwnerViewModel(flowManager, lt, localizationService)
      val fillTheGapViewModel = FillTheGapInParallelSentenceViewModel(lt, listOf(seinFlowManager), flowManager, reportingService, localizationService)

      // View components
      val controllerStack = ControllerStack()
      val defaultFlowController = DefaultFlowControllerOwner(controllerStack, lt, defaultFlowControllerOwnerViewModel)

      EmptyFlowContentController(defaultFlowController, flowManager, lt, localizationService)
      ParallelSentenceController(lt, defaultFlowController, parallelSentenceViewModel)
      StartScreenController(startScreenViewModel, lt, controllerStack)
      FillTheGapInParallelSentenceController(fillTheGapViewModel, defaultFlowController, lt)
      WelcomeScreenController(welcomeScreenViewModel, lt, controllerStack)

      if (languageService.configurationRequired) {

        container.register(welcomeScreenViewModel)
      }

      container.register(controllerStack)
      container.register(loggerProvider)
      container.register(startScreenViewModel)
      container.register(languageService)
      container.register(localizationService)
      container.register(feedbackService)
    } catch (e: Exception) {
      logger.exception(e)
      throw e
    }

    super.onCreate()
  }
}