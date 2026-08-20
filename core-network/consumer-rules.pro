# core-network：Retrofit / OkHttp / Rx / 新请求 API
-keep class io.coderf.arklab.common.api.** { *; }
-keep class io.coderf.arklab.common.repository.** { *; }
-keep class io.coderf.arklab.common.datasource.** { *; }
-keep class io.coderf.arklab.common.inter.** { *; }
-keep class io.coderf.arklab.core.network.** { *; }
-dontwarn io.coderf.arklab.core.network.**

-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepattributes Signature,Exceptions,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class okio.** { *; }
-dontwarn okio.**

-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keep class io.reactivex.rxjava3.** { *; }
-dontwarn io.reactivex.rxjava3.**

# 业务 Retrofit 接口与实体（宿主侧也会 keep，这里兜底）
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
