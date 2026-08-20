# core-base：框架主体（Base* / widget / helper / res）供宿主 R8 消费
-keep class io.coderf.arklab.common.** { *; }
-dontwarn io.coderf.arklab.common.**

-keep class io.coderf.arklab.core.request.** { *; }
-dontwarn io.coderf.arklab.core.request.**

# DataBinding / 子类基类
-keep class * extends io.coderf.arklab.common.base.** { *; }
-keep class *Binding { *; }
-keep class *BindingImpl { *; }

# Bean / Response
-keep class io.coderf.arklab.common.bean.** { *; }
-keepclassmembers class io.coderf.arklab.common.bean.** {
    <fields>;
    <methods>;
}
-keep class io.coderf.arklab.common.base.BaseResponse { *; }
-keep class io.coderf.arklab.common.viewmodel.** { *; }
-keep class io.coderf.arklab.common.helper.bean.** { *; }
-keep class io.coderf.arklab.common.widget.dialog.bean.** { *; }

# Hilt / ViewModel
-keep class androidx.hilt.** { *; }
-keep class dagger.hilt.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Glide / GSY / ZXing / MMKV / AWS（core-base 直接依赖）
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.shuyu.gsyvideoplayer.** { *; }
-keep class tv.danmaku.ijk.** { *; }
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }
-keep class com.tencent.mmkv.** { *; }
-keep class com.amazonaws.** { *; }
-dontwarn com.amazonaws.**
-dontwarn com.shuyu.gsyvideoplayer.**
-dontwarn tv.danmaku.ijk.**
