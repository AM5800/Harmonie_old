package am5800.harmonie

import am5800.harmonie.logging.AndroidLoggerProvider
import am5800.harmonie.model.FileEnvironment
import am5800.harmonie.model.newest.FlowItemProviderRegistrar
import am5800.harmonie.model.newest.FlowManager
import am5800.harmonie.model.newest.ParallelSentenceFlowManager
import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import componentContainer.ComponentContainer
import utils.Lifetime
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


      //val settingsDb = PermanentDb(this)
      //val harmonieDb = ContentDb(this, lt, settingsDb, loggerProvider)

      val flowManager = FlowManager(lt)
      val parallelSentenceFlowManager = ParallelSentenceFlowManager(lt)
      val flowItemProviderRegistrar = FlowItemProviderRegistrar(parallelSentenceFlowManager)

      container.register(flowItemProviderRegistrar)
      container.register(flowManager)
      container.register(parallelSentenceFlowManager)
      container.register(ControllerStack())


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
