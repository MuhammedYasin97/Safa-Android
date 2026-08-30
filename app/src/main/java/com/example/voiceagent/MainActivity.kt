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
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var speech: TextToSpeech
    private lateinit var statusText: TextView

    // سنضع رابط الخادم هنا بعد إنشائه
    private val API_URL = "ضع_رابط_الخادم_هنا"

    private val requestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        speech = TextToSpeech(this) {
            speech.language = Locale("ar")
        }

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                requestCode
            )
        }

        val button = Button(this)
        button.text = "🎙️ تحدث مع الوكيل"

        statusText = TextView(this)
        statusText.text = "اضغط الزر وتحدث"
        statusText.textSize = 18f
        statusText.setPadding(20, 30, 20, 30)

        button.setOnClickListener {
            startVoiceInput()
        }

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 80, 40, 40)

        layout.addView(button)
        layout.addView(statusText)

        setContentView(layout)
    }

    private fun startVoiceInput() {

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "ar-SA"
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "تحدث الآن..."
        )

        startActivityForResult(intent, 200)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == 200 &&
            resultCode == RESULT_OK &&
            data != null
        ) {

            val results =
                data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

            val text = results?.firstOrNull()

            if (!text.isNullOrEmpty()) {

                statusText.text =
                    "أنت: $text\n\nجاري التفكير..."

                askAI(text)
            }
        }
    }

    private fun askAI(message: String) {

        thread {

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

                val json =
                    """
                    {
                      "message": ${jsonEscape(message)}
                    }
                    """.trimIndent()

                OutputStreamWriter(
                    connection.outputStream
                ).use { writer ->
                    writer.write(json)
                    writer.flush()
                }

                val reader =
                    BufferedReader(
                        InputStreamReader(
                            connection.inputStream
                        )
                    )

                val response =
                    reader.readText()

                reader.close()

                connection.disconnect()

                val answer =
                    extractAnswer(response)

                runOnUiThread {

                    statusText.text =
                        "الوكيل:\n$answer"

                    speak(answer)
                }

            } catch (e: Exception) {

                runOnUiThread {

                    statusText.text =
                        "حدث خطأ في الاتصال:\n${e.message}"

                    speak(
                        "حدث خطأ أثناء الاتصال بالخادم"
                    )
                }
            }
        }
    }

    private fun extractAnswer(json: String): String {

        val marker = "\"reply\":\""

        val start = json.indexOf(marker)

        if (start == -1) {
            return "لم أفهم رد الخادم"
        }

        val beginning =
            start + marker.length

        val end =
            json.indexOf(
                "\"",
                beginning
            )

        if (end == -1) {
            return "تعذر قراءة الرد"
        }

        return json
            .substring(beginning, end)
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    private fun jsonEscape(text: String): String {

        return "\"" +
                text
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r") +
                "\""
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
