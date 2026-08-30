package com.example.voiceagent

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class NotificationReaderService : NotificationListenerService() {

    private lateinit var tts: TextToSpeech

    override fun onCreate() {
        super.onCreate()

        tts = TextToSpeech(this) {
            tts.language = Locale("ar")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val extras = sbn.notification.extras

        val title =
            extras.getString(Notification.EXTRA_TITLE)
                ?: return

        val text =
            extras.getCharSequence(Notification.EXTRA_TEXT)
                ?.toString()
                ?: return

        if (title.isBlank() || text.isBlank()) return

        val packageName = sbn.packageName

        // في هذه المرحلة نقرأ إشعارات واتساب فقط.
        if (packageName == "com.whatsapp") {

            val message = "رسالة من $title. $text"

            tts.speak(
                message,
                TextToSpeech.QUEUE_ADD,
                null,
                "notification_$title"
            )
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }
}
