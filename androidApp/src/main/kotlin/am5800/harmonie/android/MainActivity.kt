package am5800.harmonie.android

import am5800.common.componentContainer.ComponentContainer
import am5800.common.componentContainer.getComponent
import am5800.common.utils.Lifetime
import am5800.harmonie.android.controllers.DefaultFlowController
import am5800.harmonie.android.controllers.EmptyFlowContentController
import am5800.harmonie.android.controllers.ParallelSentenceController
import am5800.harmonie.android.controllers.StartScreenController
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.vm.ParallelSentenceViewModel
import am5800.harmonie.app.vm.StartScreenViewModel
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem


class MainActivity : AppCompatActivity() {
  var mainActivityLifetime: Lifetime? = null
  var controllerStack: ControllerStack? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    val app = application as HarmonieApplication
    val modelContainer = app.modelContainer!!

    val lt = Lifetime(modelContainer.lifetime)
    mainActivityLifetime = lt

    val stack = modelContainer.getComponent<ControllerStack>()
    controllerStack = stack

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val rootController = initComponents(modelContainer, lt, stack)

    if (savedInstanceState != null) {
      stack.restore(supportFragmentManager)
    } else stack.start(supportFragmentManager, rootController)
  }

  private fun initComponents(container: ComponentContainer, lifetime: Lifetime, stack: ControllerStack): BindableController {
    val defaultFlowController = DefaultFlowController(stack, lifetime)
    val flowManager = container.getComponent<FlowManager>()

    EmptyFlowContentController(defaultFlowController, flowManager, lifetime)
    ParallelSentenceController(lifetime, defaultFlowController, container.getComponent<ParallelSentenceViewModel>())

    val startScreen = StartScreenController(lifetime, container.getComponent<StartScreenViewModel>())

    return startScreen
  }

  override fun onDestroy() {
    mainActivityLifetime?.terminate()
    mainActivityLifetime = null
    super.onDestroy()
  }

  override fun onBackPressed() {
    if (!controllerStack!!.back()) super.onBackPressed()
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    val id = item!!.itemId

    if (id == R.id.action_settings) {
      return true
    }

    return super.onOptionsItemSelected(item)
  }
}