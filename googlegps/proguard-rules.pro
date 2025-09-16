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

# 保留数据绑定相关类
-keep class *Binding { *; }
-keep class *BindingImpl { *; }

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

# ==================== 网络通信相关 ====================
# Netty 网络框架
-keep class io.netty.** { *; }
-dontwarn io.netty.**
-keepclassmembers class io.netty.channel.** {
    public *;
}
-keepclassmembers class io.netty.buffer.** {
    public *;
}

# 保留 Socket 相关类
-keep class * implements java.net.Socket
-keep class * extends java.net.Socket
-keepclassmembers class * {
    public * connect*(...);
    public * send*(...);
    public * receive*(...);
    public * read*(...);
    public * write*(...);
}

# ==================== Apache Commons ====================
# Apache Commons Lang
-keep class org.apache.commons.lang3.** { *; }
-dontwarn org.apache.commons.lang3.**

# ==================== GPS 定位相关 ====================
# 保留定位服务相关类和方法
-keepclassmembers class * {
    public * getLocation*(...);
    public * onLocation*(...);
    public * start*Location*(...);
    public * stop*Location*(...);
    public * request*Location*(...);
}

# 保留位置信息相关类
-keepclassmembers class * {
    public * getLatitude();
    public * getLongitude();
    public * getAltitude();
    public * getAccuracy();
    public * setLatitude(...);
    public * setLongitude(...);
    public * setAltitude(...);
}

# ==================== 项目特定类 ====================
# 保留 Google GPS 框架相关类
-keep class com.casic.titan.googlegps.** { *; }
-dontwarn com.casic.titan.googlegps.**

# 保留服务类（Service）
-keep class com.casic.titan.googlegps.service.** { *; }
-keepclassmembers class com.casic.titan.googlegps.service.** {
    public * onStartCommand(...);
    public * onBind(...);
    public * onUnbind(...);
    public * onDestroy();
}

# 保留 Socket 通信类
-keep class com.casic.titan.googlegps.socket.** { *; }
-keepclassmembers class com.casic.titan.googlegps.socket.** {
    public * connect*(...);
    public * send*(...);
    public * receive*(...);
}

# 保留工具类
-keep class com.casic.titan.googlegps.utils.** { *; }
-keepclassmembers class com.casic.titan.googlegps.utils.** {
    public static *;
}

# 保留对话框类
-keep class com.casic.titan.googlegps.dialog.** { *; }
-keepclassmembers class com.casic.titan.googlegps.dialog.** {
    public <init>(android.content.Context);
    public * show();
    public * dismiss();
}

# 保留监听器接口
-keep class com.casic.titan.googlegps.listener.** { *; }
-keep interface com.casic.titan.googlegps.listener.** { *; }

# 保留帮助类
-keep class com.casic.titan.googlegps.helper.** { *; }
-keepclassmembers class com.casic.titan.googlegps.helper.** {
    public static *;
}

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
    void *(**Handler);
}

# ==================== 位置数据相关 ====================
# 保留位置数据模型
-keepclassmembers class * {
    public * get*Position*(...);
    public * set*Position*(...);
    public * get*Coordinate*(...);
    public * set*Coordinate*(...);
}

# 保留 GPS 数据格式相关方法
-keepclassmembers class * {
    public * format*Gps*(...);
    public * parse*Gps*(...);
    public * convert*Coordinate*(...);
}

# ==================== 网络状态相关 ====================
# 保留网络状态检测方法
-keepclassmembers class * {
    public * is*Network*(...);
    public * check*Connection*(...);
    public * on*Network*(...);
}

# ==================== 后台服务相关 ====================
# 保留后台服务生命周期方法
-keepclassmembers class * extends android.app.Service {
    public * onStartCommand(...);
    public * onBind(...);
    public * onUnbind(...);
    public * onDestroy();
    public * onTaskRemoved(...);
}

# ==================== 线程相关 ====================
# 保留线程池和异步任务方法
-keepclassmembers class * {
    public * execute*Async*(...);
    public * runOn*Thread*(...);
    public * start*Thread*(...);
}

# ==================== 数据格式转换 ====================
# 保留数据格式转换方法
-keepclassmembers class * {
    public * to*Json*(...);
    public * from*Json*(...);
    public * encode*(...);
    public * decode*(...);
    public * serialize*(...);
    public * deserialize*(...);
}

-keepclassmembers class * {
    public * send*Data*(...);
    public * receive*Data*(...);
    public * handle*Packet*(...);
}
-keepclassmembers class * {
    public * convert*Coordinate*(...);
    public * parse*NMEA*(...);
    public * format*Location*(...);
}
-keepclassmembers class * extends android.app.Service {
    public * onCreate();
    public * onStartCommand(...);
    public * onDestroy();
}