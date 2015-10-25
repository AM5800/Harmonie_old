package am5800.harmonie

import am5800.harmonie.HarmonieApplication
import am5800.harmonie.R
import am5800.harmonie.ControllerRegistry
import am5800.harmonie.model.Lifetime
import am5800.harmonie.tryReadFile
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import java.io.File
import java.io.FileOutputStream


public class MainActivity : AppCompatActivity() {
    public var mainActivityLifetime: Lifetime? = null
    public var controllerRegistry: ControllerRegistry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = getApplication() as HarmonieApplication

        val lt = Lifetime()
        mainActivityLifetime = lt

        val registry = app.controllerRegistry!!
        controllerRegistry = registry

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            registry.restore(getSupportFragmentManager())
        } else registry.start(getSupportFragmentManager())
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
        val id = item!!.getItemId()

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}