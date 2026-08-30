package com.example.voiceagent

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationReaderService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // المرحلة الأولى: استقبال إشعارات الهاتف.
        // في المرحلة التالية سنضيف سياسة قراءة آمنة، سؤال المستخدم قبل القراءة،
        // وربط الإشعارات بالوكيل الصوتي.
    }
}
