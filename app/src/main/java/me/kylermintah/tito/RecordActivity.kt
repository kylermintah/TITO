package me.kylermintah.tito

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BlendMode
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_record.*
import me.kylermintah.tito.util.FirestoreUtil
import me.kylermintah.tito.util.StorageUtil
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.FileInputStream
import java.io.IOException

class RecordActivity : AppCompatActivity() {

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        val context = this.applicationContext
        val activity = this
        backButton.setOnClickListener {
            onBackPressed()
        }
        recorder.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {

                            recorder.setColorFilter(
                                ContextCompat.getColor(
                                    context,
                                    R.color.colorAccent
                                ), android.graphics.PorterDuff.Mode.MULTIPLY
                            )
                            startRecording()

                }
                    MotionEvent.ACTION_UP -> {
                        recorder.setColorFilter(null); stopRecording()
                    }


                }

                return v?.onTouchEvent(event) ?: true
            }
        })

        output = Environment.getExternalStorageDirectory().absolutePath + "/titorecording.mp3"
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)
    }

    private fun startRecording() {
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            if (output != null) {
                doAsync {
                    val user = FirebaseAuth.getInstance().currentUser
                    StorageUtil.uploadAudio(FileInputStream(output.toString())) { audioPath ->
                        FirestoreUtil.updateCurrentUser(
                            user!!.displayName.toString(),
                            user.email.toString(),
                            null,
                            null,
                            audioPath
                        )

                    }
                    uiThread {
                        Toast.makeText(
                            it.applicationContext,
                            "Voice Profile Updated!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBackPressed()
                    }
                }
            }
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }
}
