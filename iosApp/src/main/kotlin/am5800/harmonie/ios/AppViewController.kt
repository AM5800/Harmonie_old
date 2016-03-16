package am5800.harmonie.ios

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

  override fun viewDidLoad() {
    statusText = getLabel()
    resultText = getResultText()
    helloButton = getHelloButton()
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
    statusText!!.setText("Hello Intel Multi-OS Engine!")
    val permanentDb = IosPermanentDb()
    val keyValueDb = KeyValueDatabaseImpl(permanentDb)
    IosLoggerProvider().getLogger(this.javaClass).info("Logging to logger")
    resultText!!.setText("kuku ok!")
  }

  companion object {

    @Owned
    @Selector("alloc")
    @JvmStatic external fun alloc(): AppViewController
  }
}
