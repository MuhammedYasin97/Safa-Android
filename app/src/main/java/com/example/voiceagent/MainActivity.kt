package com.example.voiceagent

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var speech: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var statusText: TextView
    private lateinit var button: Button

    private val requestCode = 100
    private val API_URL = "https://example.com/api/chat" // ضع رابط API هنا

    private val client = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // تحويل النص إلى كلام
        speech = TextToSpeech(this) { result ->
            if (result == TextToSpeech.SUCCESS) {
                speech.language = Locale("ar", "SA")
                speech.setSpeechRate(1.0f)
            }
        }

        // التعرف على الكلام
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                statusText.text = "🎤 استمع إليك الآن..."
            }
            override fun onBeginningOfSpeech() {
                statusText.text = "🎤 تحدث الآن..."
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                statusText.text = "⏳ جارٍ معالجة كلامك..."
            }
            override fun onError(error: Int) {
                statusText.text = "❌ خطأ: $error"
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val userText = matches[0]
                    statusText.text = "أنت قلت:\n$userText\n\n⏳ أفكر في الإجابة..."
                    sendToAI(userText)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        // طلب إذن الميكروفون
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), requestCode)
        }

        // واجهة التطبيق
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
        }

        statusText = TextView(this).apply {
            text = "مرحباً 👋\nاضغط الزر وتحدث معي"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(20, 20, 20, 40)
        }

        button = Button(this).apply {
            text = "🎤 تحدث مع وكيل صوتي"
            textSize = 18f
            setOnClickListener { startVoiceInput() }
        }

        layout.addView(statusText)
        layout.addView(button)
        setContentView(layout)
    }

    private fun startVoiceInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText.text = "التعرف على الكلام غير متوفر على هذا الجهاز."
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer.startListening(intent)
    }

    private fun sendToAI(userText: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$API_URL?message=${userText}")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val aiResponse = response.body?.string() ?: "لا توجد إجابة من الخادم"

                withContext(Dispatchers.Main) {
                    showAIResponse(aiResponse)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "تعذر الاتصال بالذكاء الاصطناعي.\n${e.message}"
                }
            }
        }
    }

    private fun showAIResponse(response: String) {
        statusText.text = "🤖 وكيلك الصوتي:\n\n$response"
        speak(response)
    }

    private fun speak(text: String) {
        if (text.isNotBlank()) {
            speech.language = Locale("ar", "SA")
            speech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AI_RESPONSE")
        }
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        speech.stop()
        speech.shutdown()
        super.onDestroy()
    }
}
