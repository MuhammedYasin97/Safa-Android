# VoiceAgent Android

مشروع بداية لتطبيق وكيل صوتي Android.

مهم: Android يتطلب بنية مشروع (Gradle) بمجلدات محددة لكي يتم البناء إلى APK.
لذلك لا يمكن أن يكون مشروع Android صالحًا للبناء بدون أي مجلدات على الإطلاق.

هذه الحزمة تحتوي على الملفات الأساسية، لكن ملفات Android يجب وضعها في:
- app/src/main/java/com/example/voiceagent/MainActivity.kt
- app/src/main/java/com/example/voiceagent/NotificationReaderService.kt
- app/src/main/res/values/styles.xml
- app/src/main/AndroidManifest.xml
- app/build.gradle

النسخة الحالية هي هيكل بداية: ميكروفون + تحويل كلام إلى نص + نطق الرد + طلب صلاحيات + نقطة بداية لقراءة الإشعارات.

لا يوجد فيها مفتاح API.
