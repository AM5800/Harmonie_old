package am5800.harmonie.android

import am5800.common.componentContainer.getComponent
import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.dbAccess.PreferredLanguagesService
import am5800.harmonie.app.vm.StartScreenViewModel
import am5800.harmonie.app.vm.WelcomeScreenViewModel
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

    stack.initialize(supportFragmentManager)
    if (savedInstanceState == null) {
      val preferredLanguages = modelContainer.getComponent<PreferredLanguagesService>()
      if (preferredLanguages.configurationRequired) {
        val welcomeScreen = modelContainer.getComponent<WelcomeScreenViewModel>()
        welcomeScreen.activationRequired.fire(Unit)
      } else {
        val startScreen = modelContainer.getComponent<StartScreenViewModel>()
        startScreen.activationRequired.fire(Unit)
      }
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