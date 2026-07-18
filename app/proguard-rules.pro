# ExamIA ProGuard Rules
-keepattributes JavascriptInterface
-keepclassmembers class com.examia.app.** {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.examia.app.** { *; }
