# ============================================================================
# googlegps 模块自身混淆规则
#
# 仅在本模块 minifyEnabled = true 时生效（当前 build.gradle 为 false）。
# 宿主 App 开启 R8 时真正起作用的是 consumer-rules.pro，两份文件中
# 关于 io.coderf.arklab.googlegps.* 的保留范围必须保持一致。
# ============================================================================

# ==================== 基础配置 ====================
-dontpreverify
-ignorewarnings
# 保留行号便于定位崩溃，隐藏真实源文件名
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
# 泛型 / 内部类 / 注解 / 异常签名
-keepattributes Exceptions,Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*

# ==================== Android 系统组件 ====================
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# AndroidX / Material
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ==================== DataBinding（ui/GPSConfirmDialog 使用 DialogGpsConfirmBinding） ====================
-keep class io.coderf.arklab.googlegps.databinding.** { *; }
-keep class io.coderf.arklab.googlegps.DataBinderMapperImpl { *; }
-keep class io.coderf.arklab.googlegps.BR { *; }

# ==================== 资源 R 类 ====================
-keep class io.coderf.arklab.googlegps.R$* {
    public static <fields>;
}

# ==================== 数据序列化 ====================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ==================== 本模块（与 consumer-rules.pro 保持一致） ====================
-dontwarn io.coderf.arklab.googlegps.**

# api        对外公开 API：GpsStarter / GpsCallback / GpsSettingConfig / LocationFilter
-keep class io.coderf.arklab.googlegps.api.** { *; }
-keepnames class * implements io.coderf.arklab.googlegps.api.LocationFilter

# service    GpsService（Manifest 注册）/ GpsBinder / Session
-keep class io.coderf.arklab.googlegps.service.** { *; }

# permission GpsLifecycleObserver / PermissionsChecker / PermissionManager
-keep class io.coderf.arklab.googlegps.permission.** { *; }

# track      IFileLogger / CsvFileLogger / GpxFileLogger / FileLoggerFactory / Files / GpsFileParser
-keep class io.coderf.arklab.googlegps.track.** { *; }

# socket     GPSSocketServer 及回调接口
-keep class io.coderf.arklab.googlegps.socket.** { *; }

# utils      AppUtil / DebugUtil / DeviceIdUtil / EsriUtil / LogUtil
-keep class io.coderf.arklab.googlegps.utils.** { *; }

# location / ui 为内部实现，允许混淆（不添加 keep）

# ==================== 第三方：Netty ====================
-keep class io.netty.** { *; }
-dontwarn io.netty.**
-keepclassmembers class io.netty.channel.** {
    public *;
}
-keepclassmembers class io.netty.buffer.** {
    public *;
}

# ==================== 第三方：Apache Commons Lang3 ====================
-keep class org.apache.commons.lang3.** { *; }
-dontwarn org.apache.commons.lang3.**
