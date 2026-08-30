package com.example.voiceagent

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.*
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var status: TextView
    private val requestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 40, 30, 30)
        }
        status = TextView(this).apply {
            text = "وكيلك الصوتي جاهز 🎙️"
            textSize = 22f
        }
        val button = Button(this).apply { text = "🎙️ تحدث" }
        val notificationButton = Button(this).apply { text = "🔔 تفعيل قراءة الإشعارات" }

        layout.addView(status)
        layout.addView(button)
        layout.addView(notificationButton)
        setContentView(layout)

        button.setOnClickListener { listen() }
        notificationButton.setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            requestPermissions(arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE
            ), requestCode)
        }
    }

    private fun listen() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "قل أمرك...")
        }
        startActivityForResult(intent, 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK) {
            val text = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: return
            status.text = "سمعت: $text"
            speak("سمعت أمرك: $text. الربط بالذكاء الاصطناعي سيضاف في المرحلة التالية.")
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "agent")
    }

    override fun onInit(statusCode: Int) {
        tts.language = Locale("ar")
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
