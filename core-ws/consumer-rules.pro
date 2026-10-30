# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# 对外 API（连接 / 异步门面 / Presence / UI）
-keep public class io.coderf.arklab.ws.** { public *; }
