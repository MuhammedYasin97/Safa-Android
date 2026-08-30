package com.example.voiceagent

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.Executors
import org.json.JSONObject

class MainActivity : Activity() {

    private lateinit var speech: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var statusText: TextView
    private lateinit var button: Button

    /*
     * ضع هنا رابط السيرفر الخاص بك.
     * لا تضع مفتاح API السري هنا.
     */
    private val API_URL = "https://example.com/api/chat"

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // تحويل النص إلى كلام
        speech = TextToSpeech(this) { result ->
            if (result == TextToSpeech.SUCCESS) {
                speech.language = Locale("ar", "SA")
                speech.setSpeechRate(1.0f)
            }
        }

        // واجهة التطبيق
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 50, 50, 50)

        statusText = TextView(this)
        statusText.text = "اضغط على الزر وتحدث معي 🎙️"
        statusText.textSize = 20f

        button = Button(this)
        button.text = "🎙️ تحدث معي"

        layout.addView(
            statusText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        layout.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(layout)

        // طلب إذن الميكروفون
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }

        // إنشاء التعرف على الكلام
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                statusText.text = "🎤 أنا أستمع إليك..."
            }

            override fun onBeginningOfSpeech() {
                statusText.text = "🎙️ تحدث الآن..."
            }

            override fun onRmsChanged(rmsdB: Float) {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onEndOfSpeech() {
                statusText.text = "⏳ جاري معالجة كلامك..."
            }

            override fun onError(error: Int) {
                statusText.text = "❌ حدث خطأ في التعرف على الكلام"
            }

            override fun onResults(results: Bundle?) {

                val matches =
                    results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                if (!matches.isNullOrEmpty()) {

                    val userText = matches[0]

                    statusText.text =
                        "أنت قلت:\n$userText\n\n⏳ أفكر في الإجابة..."

                    sendToAI(userText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
            }

            override fun onEvent(
                eventType: Int,
                params: Bundle?
            ) {
            }
        })

        // زر التحدث
        button.setOnClickListener {
            startListening()
        }
    }

    private fun startListening() {

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "ar-SA"
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PARTIAL_RESULTS,
            true
        )

        speechRecognizer.startListening(intent)
    }

    private fun sendToAI(userText: String) {

        executor.execute {

            try {

                val url = URL(API_URL)

                val connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.doOutput = true
                connection.connectTimeout = 30000
                connection.readTimeout = 30000

                val json = JSONObject()

                json.put("message", userText)

                val writer =
                    OutputStreamWriter(connection.outputStream)

                writer.write(json.toString())
                writer.flush()
                writer.close()

                val responseCode =
                    connection.responseCode

                val inputStream =
                    if (responseCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                val reader =
                    BufferedReader(
                        InputStreamReader(inputStream)
                    )

                val response = StringBuilder()

                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                reader.close()
                connection.disconnect()

                val answer =
                    extractAnswer(response.toString())

                runOnUiThread {

                    statusText.text =
                        "🤖 المساعد:\n$answer"

                    speak(answer)
                }

            } catch (e: Exception) {

                runOnUiThread {

                    statusText.text =
                        "❌ تعذر الاتصال بالخادم\n\n${e.message}"
                }
            }
        }
    }

    private fun extractAnswer(response: String): String {

        return try {

            val json = JSONObject(response)

            when {
                json.has("reply") ->
                    json.getString("reply")

                json.has("response") ->
                    json.getString("response")

                json.has("message") ->
                    json.getString("message")

                json.has("answer") ->
                    json.getString("answer")

                else ->
                    response
            }

        } catch (e: Exception) {

            response
        }
    }

    private fun speak(text: String) {

        speech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "assistant_response"
        )
    }

    override fun onDestroy() {

        super.onDestroy()

        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }

        if (::speech.isInitialized) {
            speech.stop()
            speech.shutdown()
        }

        executor.shutdown()
    }
}
