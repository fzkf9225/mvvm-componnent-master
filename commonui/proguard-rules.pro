# ==================== 基础配置 ====================
# 不做预校验
-dontpreverify
# 忽略警告
-ignorewarnings

# 保留源文件和行号信息（用于调试）
-keepattributes SourceFile,LineNumberTable,LocalVariableTable
-keepattributes Exceptions,Signature,InnerClasses,EnclosingMethod
# 保护注解和注释
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

# 保留自定义 View 和 Widget
-keep public class * extends android.view.ViewGroup
-keep public class * extends android.widget.**

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

# ==================== 网络请求相关 ====================
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

# ==================== 图片加载 ====================
# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# ==================== 数据库相关 ====================
# Room
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao
-keepclassmembers class * extends androidx.room.Dao {
    *;
}

# ==================== 项目特定类 ====================
# 保留 CommonUI 框架相关类
-keep class com.casic.titan.commonui.** { *; }
-dontwarn com.casic.titan.commonui.**

# 保留数据模型类（Bean）
-keep class com.casic.titan.commonui.bean.** { *; }
-keepclassmembers class com.casic.titan.commonui.bean.** {
    void set*(***);
    *** get*();
}

# 保留适配器类
-keep class com.casic.titan.commonui.adapter.** { *; }
-keepclassmembers class com.casic.titan.commonui.adapter.** {
    public <init>(...);
}

# 保留 Fragment 类
-keep class com.casic.titan.commonui.fragment.** { *; }

# 保留自定义 Widget 组件
-keep class com.casic.titan.commonui.widget.** { *; }
-keepclassmembers class com.casic.titan.commonui.widget.** {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留表单相关类
-keep class com.casic.titan.commonui.form.** { *; }

# 保留接口和实现类
-keep interface com.casic.titan.commonui.inter.** { *; }
-keep class com.casic.titan.commonui.impl.** { *; }

# 保留帮助类和工具类
-keep class com.casic.titan.commonui.helper.** { *; }
-keep class com.casic.titan.commonui.api.** { *; }

# 保留枚举类
-keep class com.casic.titan.commonui.enums.** { *; }

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

# ==================== 数据绑定相关 ====================
# 保留数据绑定生成的类
-keep class *Binding { *; }
-keep class *BindingImpl { *; }

# ==================== 依赖库相关 ====================
# 保留 common 和 commonmedia 依赖的类
-keep class pers.fz.mvvm.** { *; }
-dontwarn pers.fz.mvvm.**
-keep class pers.fz.media.** { *; }
-dontwarn pers.fz.media.**

# ==================== RecyclerView 相关 ====================
# 保留 ViewHolder
-keepclassmembers class * extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
    public <init>(android.view.View);
}

# 保留 Adapter 的重要方法
-keepclassmembers class * extends androidx.recyclerview.widget.RecyclerView.Adapter {
    public int getItemCount();
    public int getItemViewType(int);
    public long getItemId(int);
}

# ==================== ViewPager2 相关 ====================
# 保留 ViewPager2 适配器
-keep class * extends androidx.viewpager2.adapter.FragmentStateAdapter
-keep class * extends androidx.viewpager2.widget.ViewPager2

# ==================== 表单验证相关 ====================
# 保留表单验证方法
-keepclassmembers class * {
    public * validate*(...);
    public * isValid*(...);
    public * getError*(...);
    public * setError*(...);
}

-keep class * extends androidx.databinding.ViewDataBinding
-keep class * implements androidx.databinding.DataBindingComponent

-keepclassmembers class * {
    public * validate*(...);
    public * isValid*(...);
}