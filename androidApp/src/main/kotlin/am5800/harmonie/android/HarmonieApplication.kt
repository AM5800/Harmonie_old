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
import am5800.harmonie.app.model.dbAccess.sql.SqlRepetitionService
import am5800.harmonie.app.model.dbAccess.sql.SqlSentenceProvider
import am5800.harmonie.app.model.dbAccess.sql.SqlSentenceSelector
import am5800.harmonie.app.model.dbAccess.sql.SqlWordSelector
import am5800.harmonie.app.model.flow.FlowItemProviderRegistrar
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.flow.ParallelSentenceFlowManager
import am5800.harmonie.app.model.repetition.BucketRepetitionAlgorithm
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
      val debugOptions = DebugOptions(false, null)
      modelContainer = container

      val permanentDb = AndroidPermanentDb(this, lt)
      val keyValueDb = KeyValueDatabaseImpl(permanentDb)

      val repetitionService = SqlRepetitionService(BucketRepetitionAlgorithm(), permanentDb, debugOptions)
      val wordsRepetitionService = WordsRepetitionServiceImpl(repetitionService, lt)

      val sentenceProvider = SqlSentenceProvider()
      val wordSelector = SqlWordSelector(wordsRepetitionService, keyValueDb, lt, debugOptions)
      val sentenceSelector = SqlSentenceSelector(wordsRepetitionService, loggerProvider, debugOptions, wordSelector)
      val dbConsumers = listOf(sentenceProvider, sentenceSelector, wordsRepetitionService, wordSelector)
      AndroidContentDb(this, keyValueDb, loggerProvider, dbConsumers, lt)

      val flowManager = FlowManager(lt, loggerProvider)
      val parallelSentenceFlowManager = ParallelSentenceFlowManager(lt, sentenceProvider, wordsRepetitionService, sentenceSelector)
      val flowItemProviderRegistrar = FlowItemProviderRegistrar(parallelSentenceFlowManager)

      // ViewModels
      val localizationService = AndroidLocalizationService.create(resources, keyValueDb, lt)

      val parallelSentenceViewModel = ParallelSentenceViewModel(lt, parallelSentenceFlowManager, flowManager, localizationService)
      val startScreenViewModel = StartScreenViewModel(flowManager, flowItemProviderRegistrar)
      val defaultFlowControllerOwnerViewModel = DefaultFlowControllerOwnerViewModel(flowManager, lt, localizationService)
      val welcomeScreenViewModel = WelcomeScreenViewModel(lt, localizationService, LanguageAvailability(), loggerProvider)

      // View components
      val controllerStack = ControllerStack(loggerProvider)
      val defaultFlowController = DefaultFlowControllerOwner(controllerStack, lt, defaultFlowControllerOwnerViewModel)

      EmptyFlowContentController(defaultFlowController, flowManager, lt)
      ParallelSentenceController(lt, defaultFlowController, parallelSentenceViewModel)
      val startScreen = StartScreenController(startScreenViewModel)
      val welcomeScreen = WelcomeScreenController(welcomeScreenViewModel)

      container.register(controllerStack)
      container.register(loggerProvider)
      container.register(startScreen)
      container.register(welcomeScreen)
    } catch (e: Exception) {
      logger.exception(e)
      throw e
    }

    super.onCreate()
  }
}