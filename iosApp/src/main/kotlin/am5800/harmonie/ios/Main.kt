package am5800.harmonie.ios

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

  override fun applicationDidFinishLaunchingWithOptions(application: UIApplication?, launchOptions: NSDictionary<*, *>?): Boolean = true

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
