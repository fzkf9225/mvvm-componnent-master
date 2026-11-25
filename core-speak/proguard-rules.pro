# ==================== core-speak 模块 ProGuard ====================
# 库自身 minifyEnabled=false；本文件供本模块 release 打包与本地调试参考。
# 宿主实际生效规则见 consumer-rules.pro。

-dontpreverify
-ignorewarnings

-keepattributes SourceFile,LineNumberTable,LocalVariableTable
-keepattributes Exceptions,Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*,Comments
-renamesourcefileattribute SourceFile

# 自定义 View
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# 本模块公开 API（与 consumer-rules 对齐）
-keep class io.coderf.arklab.speak.** { *; }
-dontwarn io.coderf.arklab.speak.**

# R
-keep class **.R$* { *; }
-keepclassmembers class **.R$* {
    public static <fields>;
}
