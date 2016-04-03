package am5800.harmonie.android

import am5800.common.componentContainer.ComponentContainer
import am5800.common.utils.Lifetime
import am5800.harmonie.android.controllers.*
import am5800.harmonie.android.dbAccess.AndroidContentDb
import am5800.harmonie.android.dbAccess.AndroidPermanentDb
import am5800.harmonie.android.dbAccess.KeyValueDatabaseImpl
import am5800.harmonie.android.logging.AndroidLoggerProvider
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.dbAccess.WordsRepetitionServiceImpl
import am5800.harmonie.app.model.dbAccess.sql.*
import am5800.harmonie.app.model.flow.FlowItemDistributionService
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.flow.parallelSentence.ParallelSentenceFlowManager
import am5800.harmonie.app.model.repetition.BucketRepetitionAlgorithm
import am5800.harmonie.app.vm.DefaultFlowControllerOwnerViewModel
import am5800.harmonie.app.vm.ParallelSentenceViewModel
import am5800.harmonie.app.vm.StartScreenViewModel
import am5800.harmonie.app.vm.WelcomeScreenViewModel
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
      val debugOptions = DebugOptions(false, true, null)
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
      val flowItemProviders = listOf(parallelSentenceFlowManager)
      val flowManager = FlowManager(lt, loggerProvider, flowItemProviders, debugOptions)
      val distributionService = FlowItemDistributionService(flowItemProviders)

      // ViewModels
      val localizationService = AndroidLocalizationService.create(resources, keyValueDb, lt)

      val parallelSentenceViewModel = ParallelSentenceViewModel(lt, parallelSentenceFlowManager, flowManager, localizationService)
      val startScreenViewModel = StartScreenViewModel(flowManager, lt, localizationService, distributionService)
      val defaultFlowControllerOwnerViewModel = DefaultFlowControllerOwnerViewModel(flowManager, lt, localizationService)

      // View components
      val controllerStack = ControllerStack()
      val defaultFlowController = DefaultFlowControllerOwner(controllerStack, lt, defaultFlowControllerOwnerViewModel)

      EmptyFlowContentController(defaultFlowController, flowManager, lt)
      ParallelSentenceController(lt, defaultFlowController, parallelSentenceViewModel)
      StartScreenController(startScreenViewModel, lt, controllerStack)

      if (languageService.configurationRequired) {
        val welcomeScreenViewModel = WelcomeScreenViewModel(lt, localizationService, languageService, startScreenViewModel)
        WelcomeScreenController(welcomeScreenViewModel, lt, controllerStack)
        container.register(welcomeScreenViewModel)
      }

      container.register(controllerStack)
      container.register(loggerProvider)
      container.register(startScreenViewModel)
      container.register(languageService)
      container.register(localizationService)
    } catch (e: Exception) {
      logger.exception(e)
      throw e
    }

    super.onCreate()
  }
}