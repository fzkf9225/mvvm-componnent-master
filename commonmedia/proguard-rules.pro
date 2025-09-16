# ==================== 基础配置 ====================
# 不做预校验
-dontpreverify
# 忽略警告
-ignorewarnings

# 保留源文件和行号信息（用于调试）
-keepattributes SourceFile,LineNumberTable,LocalVariableTable
-keepattributes Exceptions,Signature,InnerClasses,EnclosingMethod
# 保护注解
-keepattributes *Annotation*,Comments
# 隐藏源文件名
-renamesourcefileattribute SourceFile

# ==================== Android 系统组件 ====================
# 保留四大组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# 保留 AndroidX 组件
-keep class androidx.** { *; }
-keep public class * extends androidx.**
-keep interface androidx.** { *; }
-dontwarn androidx.**

# ==================== 视图相关 ====================
# 保留 View 及其子类
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    *** get*();
    void set*(***);
}

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

# ==================== 响应式编程 ====================
# RxJava/RxAndroid
-keep class io.reactivex.rxjava3.** { *; }
-dontwarn io.reactivex.rxjava3.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
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

# ==================== 多媒体处理相关 ====================
# AspectJ
-keep class org.aspectj.** { *; }
-dontwarn org.aspectj.**

# MP4Parser/ISO Parser
-keep class com.coremedia.** { *; }
-keep class org.mp4parser.** { *; }
-dontwarn com.coremedia.**
-dontwarn org.mp4parser.**

# Picasso 图片加载
-keep class com.squareup.picasso.** { *; }
-dontwarn com.squareup.picasso.**

# ==================== 项目特定类 ====================
# 保留 Media 框架相关类
-keep class pers.fz.media.** { *; }
-dontwarn pers.fz.media.**

# 保留数据模型类（Bean）
-keep class pers.fz.media.bean.** { *; }
-keepclassmembers class pers.fz.media.bean.** {
    void set*(***);
    *** get*();
}

# 保留回调接口
-keep class pers.fz.media.callback.** { *; }
-keep interface pers.fz.media.callback.** { *; }

# 保留工具类
-keep class pers.fz.media.utils.** { *; }

# 保留 MediaBuilder 和 MediaHelper（核心类）
-keep class pers.fz.media.MediaBuilder { *; }
-keep class pers.fz.media.MediaHelper { *; }
-keepclassmembers class pers.fz.media.MediaBuilder {
    public <init>();
    public *;
}
-keepclassmembers class pers.fz.media.MediaHelper {
    public static *;
}

# 保留处理器和帮助类
-keep class pers.fz.media.handler.** { *; }
-keep class pers.fz.media.helper.** { *; }

# 保留监听器
-keep class pers.fz.media.listener.** { *; }
-keep interface pers.fz.media.listener.** { *; }

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
    void *(**Callback);
}

# ==================== Native 方法 ====================
# 保留 Native 方法名
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

# ==================== 多媒体特定规则 ====================
# 保留多媒体处理相关的方法
-keepclassmembers class * {
    public * compress*(...);
    public * process*(...);
    public * encode*(...);
    public * decode*(...);
}

-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * {
    public * get*Path*(...);
    public * set*Path*(...);
}
-keepclassmembers class * {
    public * runOn*Thread*(...);
    public * execute*Async*(...);
}

# 保留文件操作相关方法
-keepclassmembers class * {
    public * save*(...);
    public * load*(...);
    public * read*(...);
    public * write*(...);
}

