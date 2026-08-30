package com.example.voiceagent

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var speech: TextToSpeech
    private val requestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        speech = TextToSpeech(this) {
            speech.language = Locale("ar")
        }

        requestPermissions(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE
            ),
            requestCode
        )

        val button = Button(this)
        button.text = "🎙️ تحدث مع الوكيل"

        button.setOnClickListener {
            startVoiceInput()
        }

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 80, 40, 40)
        layout.addView(button)

        setContentView(layout)
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "ar"
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        startActivityForResult(intent, 200)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 &&
            resultCode == RESULT_OK &&
            data != null
        ) {
            val results =
                data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

            val text = results?.firstOrNull()

            if (!text.isNullOrEmpty()) {
                speak("سمعتك تقول: $text")
            }
        }
    }

    private fun speak(text: String) {
        speech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "voice_agent"
        )
    }

    override fun onDestroy() {
        speech.stop()
        speech.shutdown()
        super.onDestroy()
    }
}
