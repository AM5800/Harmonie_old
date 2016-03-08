package am5800.harmonie.android

import am5800.common.componentContainer.ComponentContainer
import am5800.common.utils.Lifetime
import am5800.harmonie.android.logging.AndroidLoggerProvider
import am5800.harmonie.android.model.FileEnvironment
import am5800.harmonie.android.model.dbAccess.*
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
      modelContainer = container

      val env = AndroidEnvironment(assets, this)
      container.register(env)

      val keyValueDb = KeyValueDatabaseImpl()


      val sentenceProvider = SentenceProviderImpl()

      PermanentDb(this, listOf(keyValueDb))
      ContentDb(this, keyValueDb, loggerProvider, listOf(sentenceProvider))

      val flowManager = FlowManager(lt, loggerProvider)
      val attempts = AttemptsServiceImpl()
      val parallelSentenceFlowManager = ParallelSentenceFlowManager(lt, sentenceProvider, loggerProvider, attempts)
      val flowItemProviderRegistrar = FlowItemProviderRegistrar(parallelSentenceFlowManager)

      container.register(flowItemProviderRegistrar)
      container.register(flowManager)
      container.register(parallelSentenceFlowManager)
      container.register(ControllerStack())
      container.register(loggerProvider)
      container.register(sentenceProvider)
      container.register(keyValueDb)
      container.register(attempts)

      container.register(ParallelSentenceViewModel(lt, parallelSentenceFlowManager, flowManager))
      container.register(StartScreenViewModel(flowManager, flowItemProviderRegistrar))
      container.register(DefaultFlowControllerOwnerViewModel(flowManager, lt))
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
