package am5800.harmonie.ios

import am5800.common.Language
import am5800.common.componentContainer.ComponentContainer
import am5800.common.componentContainer.getComponent
import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.flow.FlowSettings
import am5800.harmonie.app.vm.ParallelSentenceViewModel
import am5800.harmonie.ios.logging.IosLoggerProvider
import am5800.harmonie.ios.model.dbAccess.IosPermanentDb
import am5800.harmonie.ios.model.dbAccess.KeyValueDatabaseImpl
import com.intel.inde.moe.natj.general.Pointer
import com.intel.inde.moe.natj.general.ann.Owned
import com.intel.inde.moe.natj.general.ann.RegisterOnStartup
import com.intel.inde.moe.natj.objc.ObjCRuntime
import com.intel.inde.moe.natj.objc.ann.ObjCClassName
import com.intel.inde.moe.natj.objc.ann.Property
import com.intel.inde.moe.natj.objc.ann.Selector
import ios.NSObject
import ios.uikit.UIButton
import ios.uikit.UILabel
import ios.uikit.UIViewController

@Suppress("unused")
@com.intel.inde.moe.natj.general.ann.Runtime(ObjCRuntime::class)
@ObjCClassName("AppViewController")
@RegisterOnStartup
class AppViewController protected constructor(peer: Pointer) : UIViewController(peer) {

  @Selector("init")
  override external fun init(): AppViewController

  private var statusText: UILabel? = null
  private var resultText: UILabel? = null
  private var helloButton: UIButton? = null

  private val viewLifetime: Lifetime by lazy { Lifetime(null) }
  private var container: ComponentContainer? = null

  override fun viewDidUnload() {
    super.viewDidUnload()
    viewLifetime.close()
  }

  override fun viewDidLoad() {
    statusText = getLabel()
    resultText = getResultText()
    helloButton = getHelloButton()

    val loggerProvider = IosLoggerProvider()
    container = initApp(viewLifetime, loggerProvider, loggerProvider.getLogger("view")).let {
      val vm = it.getComponent<ParallelSentenceViewModel>()
      vm.question.bind(viewLifetime, { arg -> statusText!!.setText(arg.newValue!!.joinToString(" ") { it.text }) })
      vm.answer.bind(viewLifetime, { arg -> resultText!!.setText(arg.newValue) })
      it.getComponent<FlowManager>().start(listOf(it.getComponent()), FlowSettings(Language.German, Language.English), null)
      it
    }
  }

  @Selector("statusText")
  @Property
  external fun getLabel(): UILabel

  @Selector("resultText")
  @Property
  external fun getResultText(): UILabel

  @Selector("helloButton")
  @Property
  external fun getHelloButton(): UIButton

  @Selector("BtnPressedCancel_helloButton:")
  fun BtnPressedCancel_button(sender: NSObject) {
    val vm = container!!.getComponent<ParallelSentenceViewModel>()
    vm.next()
  }

  companion object {

    @Owned
    @Selector("alloc")
    @JvmStatic external fun alloc(): AppViewController
  }
}
