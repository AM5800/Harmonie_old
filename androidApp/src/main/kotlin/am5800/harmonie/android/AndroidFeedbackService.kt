package am5800.harmonie.android

import am5800.common.utils.Lifetime
import am5800.harmonie.android.dbAccess.AndroidUserDb
import am5800.harmonie.android.viewBinding.ActivityConsumer
import am5800.harmonie.app.model.features.feedback.FeedbackService
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class AndroidFeedbackService(private val db: AndroidUserDb) : FeedbackService, ActivityConsumer {
  override fun setActivity(activity: Activity, lifetime: Lifetime) {
    lifetime.execute {
      this.context = activity
      lifetime.addAction {
        this.context = null
        val tempFile = getTempDbFile()
        if (tempFile.exists()) tempFile.delete()
      }
    }
  }

  private var context: Context? = null

  override fun collectAndSendData() {
    val email = Intent(Intent.ACTION_SEND)
    email.type = "message/rfc822"
    email.putExtra(Intent.EXTRA_EMAIL, arrayOf("am5800+harmonie@gmail.com"))
    email.putExtra(Intent.EXTRA_SUBJECT, "Usage statistics")
    email.putExtra(Intent.EXTRA_TEXT, """
This file contains internal database of the application.
It will be used to:
  - Adjust repetition algorithm
  - Check if db is used optimally in a long term
  - Actually submit errors/typos in the application data

File format is sqlite3. And it does not contain any private information
""")

    val inStream = FileInputStream(db.getLocation())
    val outFile = getTempDbFile()
    val outStream = FileOutputStream(outFile)
    outStream.use { outStream ->
      inStream.copyTo(outStream)
    }

    val uri = Uri.fromFile(outFile)
    email.putExtra(Intent.EXTRA_STREAM, uri)
    context!!.startActivity(Intent.createChooser(email, "Send usage statistics"))
  }

  private fun getTempDbFile() = File(Environment.getExternalStorageDirectory(), "harmonie.db")
}