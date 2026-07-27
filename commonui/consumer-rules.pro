# 保留 CommonUI 框架核心类
-keep class io.coderf.arklab.ui.** { *; }
-dontwarn io.coderf.arklab.ui.**

# 确保数据模型不被混淆
-keep class io.coderf.arklab.ui.bean.** { *; }
-keepclassmembers class io.coderf.arklab.ui.bean.** {
    void set*(***);
    *** get*();
}

# 保留自定义 Widget 组件
-keep class io.coderf.arklab.ui.widget.** { *; }
-keepclassmembers class io.coderf.arklab.ui.widget.** {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
}

# 保留 Fragment
-keep class io.coderf.arklab.ui.fragment.** { *; }

# 保留适配器
-keep class io.coderf.arklab.ui.adapter.** { *; }

# 保留表单相关功能
-keep class io.coderf.arklab.ui.form.** { *; }

# 保留接口和回调
-keep interface io.coderf.arklab.ui.inter.** { *; }
-keep class io.coderf.arklab.ui.impl.** { *; }

# 保留数据绑定相关类
-keep class *Binding { *; }
-keep class *BindingImpl { *; }

# ---- 依赖库（须放在 consumer，发布 AAR 后 App minify 才能生效）----
# Retrofit2
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepattributes Signature,Exceptions,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp3
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class okio.** { *; }
-dontwarn okio.**

# Gson
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-keepattributes Signature
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# RxJava3
-keep class io.reactivex.rxjava3.** { *; }
-dontwarn io.reactivex.rxjava3.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao
-keepclassmembers class * extends androidx.room.Dao {
    *;
}
