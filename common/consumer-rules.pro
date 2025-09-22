# 保留 MVVM 框架核心类
-keep class com.casic.otitan.common.** { *; }
-dontwarn com.casic.otitan.common.**

# 保留数据绑定相关类
-keep class com.casic.otitan.common.bean.** { *; }
-keepclassmembers class com.casic.otitan.common.bean.** {
    void set*(***);
    *** get*();
}

# 保留 BaseResponse 和 ViewModel
-keep class com.casic.otitan.common.base.BaseResponse { *; }
-keep class com.casic.otitan.common.viewmodel.** { *; }

# 保留注解处理器生成的类
-keep class * extends com.casic.otitan.common.base.** { *; }

# 保留资源绑定相关类（如果有自动生成的绑定类）
-keep class *Binding { *; }
-keep class *BindingImpl { *; }