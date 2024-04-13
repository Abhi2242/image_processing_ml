package com.smartgeek.imageprocessingtest.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.smartgeek.imageprocessingtest.R
import pl.droidsonroids.gif.GifImageView
import java.util.Locale

class AnimationActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var btnStartTour: Button
    private lateinit var btnSkipTour: Button
    private lateinit var gifDropArrow: GifImageView
    private var isSpeaking = false
    private var isStarted = true
    private var count = 0
    private lateinit var ivRemoveGlass: ImageView
    private lateinit var ivDummyFace: ImageView
    private lateinit var ivFrame: ImageView
    private lateinit var ivSelfie: ImageView
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation)

        checkCameraPermission()

        btnStartTour = findViewById(R.id.btn_start_tour)
        btnSkipTour = findViewById(R.id.btn_skip_tour)
        gifDropArrow = findViewById(R.id.gif_drop_arrow)
        ivRemoveGlass = findViewById(R.id.iv_remove_glass)
        ivDummyFace = findViewById(R.id.iv_dummy_face)
        ivFrame = findViewById(R.id.iv_frame)
        ivSelfie = findViewById(R.id.iv_selfie)

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
            } else {
                Toast.makeText(
                    this, "TextToSpeech initialization failed.", Toast.LENGTH_SHORT
                ).show()
            }
        }
        initView()

    }

//    private fun getScreenWidth(context: Context): Int {
//        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        val displayMetrics = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(displayMetrics)
//
//        val widthPixels = displayMetrics.widthPixels
//        val heightPixels = displayMetrics.heightPixels
//        val density = displayMetrics.density
//
//        val widthInches = widthPixels / density
//        val heightInches = heightPixels / density
//
//        val widthMM = widthInches * 25.4
//        val heightMM = heightInches * 25.4
//
//        Log.v("screen density", density.toString())
//        Log.v("screen width in pixels", widthPixels.toString())
//        Log.v("screen width in MM", widthMM.toString())
//        Log.v("screen height in pixels", heightPixels.toString())
//        Log.v("screen height in MM", heightMM.toString())
//
//        return displayMetrics.widthPixels
//    }

    private fun initView() {
        btnStartTour.setOnClickListener {
            speakText("Starting Tour")
            gifDropArrow.visibility = View.GONE
            isStarted = false
        }

        btnSkipTour.setOnClickListener {
            speakText("Skipping Tour..!")
            gifDropArrow.visibility = View.GONE
            isStarted = true
        }
    }

//    private fun speakText(message: String) {
//        val speed = 0.8f
//        textToSpeech.setSpeechRate(speed)
//        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
//    }

    private fun speakText(message: String) {
        if (!isSpeaking) {
            isSpeaking = true
            val speed = 0.8f
            textToSpeech.setSpeechRate(speed)
            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    // Do nothing
                }

                override fun onDone(utteranceId: String?) {
                    // After the first message is spoken, speak the second message
                    when (utteranceId) {
                        "MESSAGE_ONE" -> {
                            isSpeaking = false
                            startAnimation()
                        }

                        "MESSAGE_TWO" -> {
                            startActivity(Intent(this@AnimationActivity, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                        }

                        "MESSAGE_END" -> {
                            handler.postDelayed({
                                startActivity(Intent(this@AnimationActivity, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            }, 3000)
                        }

                        else -> {
                            isSpeaking = false
                            count++
                            startAnimation()
                        }
                    }
                }

                override fun onError(utteranceId: String?) {
                    // Handle error
                }
            })

            // Use utteranceId to identify each message
            val params = hashMapOf<String, String>()
            when (message) {
                "Starting Tour" -> {
                    params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "MESSAGE_ONE"
                }

                "Skipping Tour..!" -> {
                    params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "MESSAGE_TWO"
                }

                "Keep your phone 25 centimeter away from Face. And Get ready to take selfies..!" -> {
                    params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "MESSAGE_END"
                }

                else -> {
                    params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "animation"
                }
            }
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, params)
        }
    }

    private fun startAnimation() {
        if (!isStarted){
            when (count) {
                0 -> {
                    runOnUiThread {
                        ivRemoveGlass.visibility = View.VISIBLE
                        ivDummyFace.visibility = View.GONE
                        ivFrame.visibility = View.GONE
                        ivSelfie.visibility = View.GONE
                    }
                    speakText("First, remove your glasses, if you have it on")
                }

                1 -> {
                    runOnUiThread {
                        ivRemoveGlass.visibility = View.GONE
                        ivDummyFace.visibility = View.VISIBLE
                        ivFrame.visibility = View.GONE
                        ivSelfie.visibility = View.GONE
                    }
                    speakText("Now You have to place or adjust")
                }

                2 -> {
                    runOnUiThread {
                        ivRemoveGlass.visibility = View.GONE
                        ivDummyFace.visibility = View.VISIBLE
                        ivFrame.visibility = View.VISIBLE
                        ivSelfie.visibility = View.GONE
                    }
                    speakText("your face inside the rectangle")
                }

                3 -> {
                    runOnUiThread {
                        ivRemoveGlass.visibility = View.GONE
                        ivDummyFace.visibility = View.GONE
                        ivFrame.visibility = View.GONE
                        ivSelfie.visibility = View.VISIBLE
                    }
                    speakText("Keep your phone 25 centimeter away from Face. And Get ready to take selfies..!")
                }
            }
        }
        else{
            runOnUiThread {
                ivRemoveGlass.visibility = View.GONE
                ivDummyFace.visibility = View.GONE
                ivFrame.visibility = View.GONE
                ivSelfie.visibility = View.GONE
                gifDropArrow.visibility = View.VISIBLE
            }
            speakText("Skip Tour")
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with camera operations
            } else {
                // Permission denied, show a message or take appropriate action
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object{
        private const val REQUEST_CAMERA_PERMISSION = 100
    }
}