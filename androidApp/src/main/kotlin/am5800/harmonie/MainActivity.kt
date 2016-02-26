package am5800.harmonie

import Lifetime
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem


class MainActivity : AppCompatActivity() {
  var mainActivityLifetime: Lifetime? = null
  var controllerRegistry: ControllerRegistry? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    val app = application as HarmonieApplication

    val lt = Lifetime()
    mainActivityLifetime = lt

    val registry = app.controllerRegistry!!
    controllerRegistry = registry

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (savedInstanceState != null) {
      registry.restore(supportFragmentManager)
    } else registry.start(supportFragmentManager)
  }

  override fun onDestroy() {
    mainActivityLifetime?.terminate()
    mainActivityLifetime = null
    super.onDestroy()
  }

  override fun onBackPressed() {
    if (!controllerRegistry!!.back()) super.onBackPressed()
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