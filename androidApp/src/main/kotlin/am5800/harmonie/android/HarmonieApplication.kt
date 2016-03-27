package am5800.harmonie.android

import am5800.common.componentContainer.ComponentContainer
import am5800.common.utils.Lifetime
import am5800.harmonie.android.controllers.DefaultFlowControllerOwner
import am5800.harmonie.android.controllers.EmptyFlowContentController
import am5800.harmonie.android.controllers.ParallelSentenceController
import am5800.harmonie.android.controllers.StartScreenController
import am5800.harmonie.android.logging.AndroidLoggerProvider
import am5800.harmonie.android.model.FileEnvironment
import am5800.harmonie.android.model.dbAccess.AndroidContentDb
import am5800.harmonie.android.model.dbAccess.AndroidPermanentDb
import am5800.harmonie.android.model.dbAccess.KeyValueDatabaseImpl
import am5800.harmonie.app.model.repetition.BucketRepetitionAlgorithm
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.repetition.WordsRepetitionServiceImpl
import am5800.harmonie.app.model.dbAccess.sql.SqlSentenceSelector
import am5800.harmonie.app.model.dbAccess.sql.SqlRepetitionService
import am5800.harmonie.app.model.dbAccess.sql.SqlSentenceProvider
import am5800.harmonie.app.model.dbAccess.sql.SqlWordSelector
import am5800.harmonie.app.model.flow.FlowItemProviderRegistrar
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.flow.ParallelSentenceFlowManager
import am5800.harmonie.app.vm.DefaultFlowControllerOwnerViewModel
import am5800.harmonie.app.vm.ParallelSentenceViewModel
import am5800.harmonie.app.vm.StartScreenViewModel
import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

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

      val env = AndroidEnvironment(assets, this)
      container.register(env)


      val permanentDb = AndroidPermanentDb(this, lt)
      val keyValueDb = KeyValueDatabaseImpl(permanentDb)

      val repetitionService = SqlRepetitionService(BucketRepetitionAlgorithm(), permanentDb, debugOptions)
      val wordsRepetitionService = WordsRepetitionServiceImpl(repetitionService)

      val sentenceProvider = SqlSentenceProvider()
      val wordSelector = SqlWordSelector(wordsRepetitionService, keyValueDb)
      val sentenceSelector = SqlSentenceSelector(wordsRepetitionService, loggerProvider, debugOptions, wordSelector)
      val dbConsumers = listOf(sentenceProvider, sentenceSelector, wordsRepetitionService, wordSelector)
      AndroidContentDb(this, keyValueDb, loggerProvider, dbConsumers, lt)

      val flowManager = FlowManager(lt, loggerProvider)
      val parallelSentenceFlowManager = ParallelSentenceFlowManager(lt, sentenceProvider, loggerProvider, wordsRepetitionService, sentenceSelector)
      val flowItemProviderRegistrar = FlowItemProviderRegistrar(parallelSentenceFlowManager)

      // ViewModels
      val parallelSentenceViewModel = ParallelSentenceViewModel(lt, parallelSentenceFlowManager, flowManager)
      val startScreenViewModel = StartScreenViewModel(flowManager, flowItemProviderRegistrar)
      val defaultFlowControllerOwnerViewModel = DefaultFlowControllerOwnerViewModel(flowManager, lt)

      // View components
      val controllerStack = ControllerStack(loggerProvider)
      val defaultFlowController = DefaultFlowControllerOwner(controllerStack, lt, defaultFlowControllerOwnerViewModel, resources)

      EmptyFlowContentController(defaultFlowController, flowManager, lt)
      ParallelSentenceController(lt, defaultFlowController, parallelSentenceViewModel)
      val startScreen = StartScreenController(startScreenViewModel)

      container.register(controllerStack)
      container.register(loggerProvider)
      container.register(startScreen)
    } catch (e: Exception) {
      logger.exception(e)
      throw e
    }

    super.onCreate()
  }
}

class AndroidEnvironment(private val assets: AssetManager, private val context: Context) : FileEnvironment {
  override fun appendDataFile(path: String, func: (OutputStream) -> Unit) {
    val stream = context.openFileOutput(path, Context.MODE_APPEND)
    stream.use { func(it) }
  }


  override fun writeDataFile(path: String, func: (OutputStream) -> Unit) {
    val stream = context.openFileOutput(path, Context.MODE_PRIVATE)
    stream.use { func(it) }
  }

  override fun <T> tryReadDataFile(path: String, func: (InputStream) -> T?): T? {
    try {
      return context.openFileInput(path).use {
        func(it)
      }
    } catch(e: FileNotFoundException) {
      return null
    }
  }

  override fun enumerateAssets(basePath: String): List<String> {
    return assets.list(basePath).toList()
  }

  override fun <T> readAsset(path: String, func: (InputStream) -> T): T? {
    try {
      return assets.open(path).use(func)
    } catch(e: FileNotFoundException) {
      return null
    }
  }
}
