# ==================== 基础配置 ====================
# 不做预校验
-dontpreverify
# 忽略警告
-ignorewarnings

# 保留源文件和行号信息（用于调试）
-keepattributes SourceFile,LineNumberTable,LocalVariableTable
-keepattributes Exceptions,Signature,InnerClasses,EnclosingMethod
# 保护注解
-keepattributes *Annotation*
# 隐藏源文件名
-renamesourcefileattribute SourceFile

# ==================== Android 系统组件 ====================
# 保留四大组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# 保留 AndroidX 组件
-keep class androidx.** { *; }
-keep public class * extends androidx.**
-keep interface androidx.** { *; }
-dontwarn androidx.**

# 保留 Material Design 组件
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ==================== 视图相关 ====================
# 保留 View 及其子类
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    *** get*();
    void set*(***);
}

# 保留 Fragment
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Fragment

# ==================== 数据序列化 ====================
# 保留枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留 Serializable 实现
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ==================== 网络请求相关 ====================
# OkHttp3
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Retrofit2
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepattributes Signature,Exceptions,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# ==================== 响应式编程 ====================
# RxJava/RxAndroid
-keep class io.reactivex.rxjava3.** { *; }
-dontwarn io.reactivex.rxjava3.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}

# ==================== 图片加载 ====================
# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# ==================== 依赖注入 ====================
# Hilt
-keep class androidx.hilt.** { *; }
-keep class dagger.hilt.** { *; }
-keep class dagger.hilt.android.** { *; }
-keepclassmembers class * {
    @dagger.hilt.* <methods>;
    @dagger.hilt.* <fields>;
}

# ==================== 数据库相关 ====================
# Room
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity

# ==================== 视频播放 ====================
# GSYVideoPlayer
-keep class com.shuyu.gsyvideoplayer.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.**
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**

# ==================== 项目特定类 ====================
# 保留 MVVM 框架相关类
-keep class com.casic.otitan.common.** { *; }
-dontwarn com.casic.otitan.common.**

# 保留数据模型类（Bean）
-keep class com.casic.otitan.common.bean.** { *; }
-keepclassmembers class com.casic.otitan.common.bean.** {
    void set*(***);
    *** get*();
}

# 保留 BaseResponse
-keep class com.casic.otitan.common.base.BaseResponse { *; }
-keepclassmembers class com.casic.otitan.common.base.BaseResponse {
    void set*(***);
    *** get*();
}

# 保留 Dialog Bean
-keep class com.casic.otitan.common.widget.dialog.bean.** { *; }
-keepclassmembers class com.casic.otitan.common.widget.dialog.bean.** {
    void set*(***);
    *** get*();
}

# ==================== 第三方库 ====================
# MMKV
-keep class com.tencent.mmkv.** { *; }

# ZXing
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }

# AWS
-keep class com.amazonaws.** { *; }
-dontwarn com.amazonaws.**

# ==================== 资源文件 ====================
# 保留 R 类
-keep class **.R$* { *; }
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ==================== 回调方法 ====================
# 保留回调方法
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}

# ==================== Native 方法 ====================
# 保留 Native 方法名
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}