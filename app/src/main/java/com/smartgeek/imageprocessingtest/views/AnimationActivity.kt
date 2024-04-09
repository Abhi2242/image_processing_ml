package com.smartgeek.imageprocessingtest.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.smartgeek.imageprocessingtest.R
import pl.droidsonroids.gif.GifImageView
import java.util.Locale

class AnimationActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var btnStartTour: Button
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

        btnStartTour = findViewById(R.id.btn_start_tour)
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

    private fun initView() {
        btnStartTour.setOnClickListener {
            isStarted = if (isStarted) {
                speakText("Starting Tour")
                btnStartTour.alpha = .5f
                btnStartTour.isClickable = false
                gifDropArrow.visibility = View.GONE
                btnStartTour.text = "Skip Tour"
                false
            } else {
                btnStartTour.alpha = .5f
                btnStartTour.isClickable = false
                startAnimation()
                true
            }

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
                            btnStartTour.alpha = 1f
                            btnStartTour.isClickable = true
                            startAnimation()
                        }

                        "MESSAGE_TWO" -> {
                            btnStartTour.alpha = 1f
                            isSpeaking = false
                            btnStartTour.isClickable = true
                            startActivity(Intent(this@AnimationActivity, MainActivity::class.java))
                        }

                        "MESSAGE_END" -> {
                            btnStartTour.alpha = 1f
                            isSpeaking = false
                            btnStartTour.isClickable = true
                            handler.postDelayed({
                                startActivity(Intent(this@AnimationActivity, MainActivity::class.java))
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

                "Skip Tour" -> {
                    params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "MESSAGE_TWO"
                }

                "Get ready to take selfies..!" -> {
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
                    speakText("Get ready to take selfies..!")
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

}