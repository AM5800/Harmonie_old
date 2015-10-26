package am5800.harmonie

import am5800.harmonie.logging.AndroidLoggerProvider
import am5800.harmonie.controllers.*
import am5800.harmonie.model.*
import am5800.harmonie.model.repetition.BucketRepetitionAlgorithm
import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream


public class HarmonieApplication : Application() {
    var lifetime: Lifetime? = null
    var controllerRegistry: ControllerRegistry? = null
    override fun onTerminate() {
        lifetime?.terminate()
    }

    override fun onCreate() {
        val loggerProvider = AndroidLoggerProvider()
        val logger = loggerProvider.getLogger(this.javaClass)

        try {
            val lt = Lifetime()
            lifetime = lt

            val env = AndroidEnvironment(assets, this)

            val settingsDb = SettingsDb(this)
            val harmonieDb = HarmonieDb(this, lt, settingsDb, loggerProvider)



            val database = harmonieDb.readableDatabase!!
            val settings = AppSettings()
            val germanEntityManager = GermanEntityManager(harmonieDb.readableDatabase)
            val deserializers = listOf(germanEntityManager)
            val textsProvider = TextsProvider(loggerProvider, database, deserializers)
            val historyManager = AttemptsHistoryManagerImpl(env, settings, deserializers)
            val scheduler = EntitySchedulerImpl(settings, env, deserializers)
            val examplesManager = ExamplesRenderer()
            val bucketsAlg = BucketRepetitionAlgorithm()
            val newEntitiesSource = NewEntitiesSource(textsProvider, historyManager)
            val flowController = FlowManager(loggerProvider, lt, historyManager, scheduler, bucketsAlg, newEntitiesSource)
            val registry = ControllerRegistry()
            controllerRegistry = registry
            DbSynchronizer(historyManager, scheduler, deserializers, harmonieDb, lt)

            settingsDb.initialize()
            harmonieDb.initialize()

            // Creating VMs
            val statsVm = StatsController(historyManager, registry, bucketsAlg)
            val textVm = TextController(textsProvider, lt, TextPartScoreCalculator(bucketsAlg, historyManager), TextProgress(env), registry)
            val startVm = StartScreenController(flowController, textVm, statsVm, scheduler)
            val markErrorHelper = MarkErrorHelper(settingsDb.writableDatabase, loggerProvider)
            val flowVm = FlowController(lt, flowController, listOf(WordsContentManagerController(examplesManager, germanEntityManager, markErrorHelper)), registry)
            //Start
            registry.registerKnownController(startVm, lt)
            registry.registerKnownController(textVm, lt)
            registry.registerKnownController(statsVm, lt)
            registry.registerKnownController(flowVm, lt)

            registry.setDefaultVm(startVm)

        } catch (e: Exception) {
            logger.exception(e)
            throw e
        }

        super.onCreate()
    }
}

class AndroidEnvironment(private val assets: AssetManager, private val context : Context) : FileEnvironment {
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
        }
        catch(e : FileNotFoundException) {
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
