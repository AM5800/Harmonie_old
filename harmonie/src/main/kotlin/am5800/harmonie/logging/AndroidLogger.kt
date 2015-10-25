package am5800.harmonie.logging

import am5800.harmonie.model.Logger
import android.util.Log


public class AndroidLogger(private val tag: String) : Logger {
    override fun catch(function: () -> Unit) {
        try {
            function()
        } catch(e: Exception) {
            exception(e)
        }
    }

    override fun exception(e: Exception) {
        Log.e(tag, "EXCEPTION ${e.getMessage()}", e)
    }

    override fun verbose(message: String) {
        Log.v(tag, message)
    }

    override fun info(message: String) {
        Log.i(tag, message)
    }
}