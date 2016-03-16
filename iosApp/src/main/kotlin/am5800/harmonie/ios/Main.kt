package am5800.harmonie.ios

import am5800.common.componentContainer.ComponentContainer
import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.BucketRepetitionAlgorithm
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.dbAccess.WordsRepetitionServiceImpl
import am5800.harmonie.app.model.dbAccess.sql.SentenceSelectorImpl
import am5800.harmonie.app.model.dbAccess.sql.SqlRepetitionService
import am5800.harmonie.app.model.dbAccess.sql.SqlSentenceProvider
import am5800.harmonie.app.model.flow.FlowItemProviderRegistrar
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.flow.ParallelSentenceFlowManager
import am5800.harmonie.app.model.logging.Logger
import am5800.harmonie.app.vm.DefaultFlowControllerOwnerViewModel
import am5800.harmonie.app.vm.ParallelSentenceViewModel
import am5800.harmonie.app.vm.StartScreenViewModel
import am5800.harmonie.ios.logging.IosLoggerProvider
import am5800.harmonie.ios.model.dbAccess.IosContentDb
import am5800.harmonie.ios.model.dbAccess.IosPermanentDb
import am5800.harmonie.ios.model.dbAccess.KeyValueDatabaseImpl
import ios.NSObject
import ios.foundation.NSDictionary
import ios.uikit.UIApplication
import ios.uikit.UIWindow
import ios.uikit.c.UIKit
import ios.uikit.protocol.UIApplicationDelegate

import com.intel.inde.moe.natj.general.Pointer
import com.intel.inde.moe.natj.general.ann.RegisterOnStartup
import com.intel.inde.moe.natj.objc.ann.Selector

@RegisterOnStartup
class Main protected constructor(peer: Pointer) : NSObject(peer), UIApplicationDelegate {

  private var window: UIWindow? = null

//  private var modelContainer: ComponentContainer? = null

  override fun applicationDidFinishLaunchingWithOptions(application: UIApplication?, launchOptions: NSDictionary<*, *>?): Boolean {
    val loggerProvider = IosLoggerProvider()
    initApp(Lifetime(), IosLoggerProvider(), loggerProvider.getLogger(javaClass))

    return true
  }

  override fun applicationWillTerminate(application: UIApplication?) {
    super.applicationWillTerminate(application)
  }

  override fun setWindow(value: UIWindow?) {
    window = value
  }

  override fun window(): UIWindow? = window

  companion object {

    @JvmStatic fun main(args: Array<String>) {
      UIKit.UIApplicationMain(0, null, null, Main::class.java.name)
    }

    @Suppress("unused")
    @Selector("alloc")
    @JvmStatic external fun alloc(): Main
  }
}

fun initApp(lifetime: Lifetime, loggerProvider: IosLoggerProvider, logger: Logger): ComponentContainer {

  try {
    val container = ComponentContainer(lifetime, null)
    val debugOptions = DebugOptions(false, false, null)
    //      modelContainer = container

    // TODO: drop then it's dropped from android app
    //      val env = AndroidEnvironment(assets, this)
    //      container.register(env)

    val permanentDb = IosPermanentDb(lifetime)
    val keyValueDb = KeyValueDatabaseImpl(permanentDb)

    val repetitionService = SqlRepetitionService(BucketRepetitionAlgorithm(), permanentDb, debugOptions)
    val wordsRepetitionService = WordsRepetitionServiceImpl(repetitionService)

    val sentenceProvider = SqlSentenceProvider()
    val bestSentenceFinder = SentenceSelectorImpl(wordsRepetitionService, loggerProvider, debugOptions)
    IosContentDb(keyValueDb, loggerProvider, listOf(sentenceProvider, bestSentenceFinder, wordsRepetitionService), lifetime)

    val flowManager = FlowManager(lifetime, loggerProvider)
    container.register(flowManager)
    val parallelSentenceFlowManager = ParallelSentenceFlowManager(lifetime, sentenceProvider, loggerProvider, wordsRepetitionService, bestSentenceFinder)
    container.register(parallelSentenceFlowManager)
    val flowItemProviderRegistrar = FlowItemProviderRegistrar(parallelSentenceFlowManager)

    // ViewModels
    val parallelSentenceViewModel = ParallelSentenceViewModel(lifetime, parallelSentenceFlowManager, flowManager)
    container.register(parallelSentenceViewModel)
    container.register(loggerProvider)

    return container

  } catch (e: Exception) {
    logger.exception(e)
    throw e
  }
}

