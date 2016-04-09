package am5800.harmonie.android

import am5800.common.componentContainer.getComponent
import am5800.common.componentContainer.getComponents
import am5800.common.utils.Lifetime
import am5800.harmonie.android.viewBinding.ActivityConsumer
import am5800.harmonie.app.vm.StartScreenViewModel
import android.os.Bundle
import android.support.v7.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
  var mainActivityLifetime: Lifetime? = null
  var controllerStack: ControllerStack? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    val app = application as HarmonieApplication
    val modelContainer = app.modelContainer!!

    val lt = Lifetime(modelContainer.lifetime)
    mainActivityLifetime = lt

    for (consumer in modelContainer.getComponents<ActivityConsumer>()) {
      consumer.setActivity(this, lt)
    }

    val stack = modelContainer.getComponent<ControllerStack>()
    controllerStack = stack

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    stack.initialize(supportFragmentManager)
    if (savedInstanceState == null) {
      val startScreen = modelContainer.getComponent<StartScreenViewModel>()
      startScreen.activationRequested.fire(Unit)
    }
  }

  override fun onDestroy() {
    mainActivityLifetime?.terminate()
    mainActivityLifetime = null
    super.onDestroy()
  }

  override fun onBackPressed() {
    if (!controllerStack!!.back()) super.onBackPressed()
  }
}