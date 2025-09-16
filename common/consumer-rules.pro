# 保留 MVVM 框架核心类
-keep class pers.fz.mvvm.** { *; }
-dontwarn pers.fz.mvvm.**

# 保留数据绑定相关类
-keep class pers.fz.mvvm.bean.** { *; }
-keepclassmembers class pers.fz.mvvm.bean.** {
    void set*(***);
    *** get*();
}

# 保留 BaseResponse 和 ViewModel
-keep class pers.fz.mvvm.base.BaseResponse { *; }
-keep class pers.fz.mvvm.viewmodel.** { *; }

# 保留注解处理器生成的类
-keep class * extends pers.fz.mvvm.base.** { *; }

# 保留资源绑定相关类（如果有自动生成的绑定类）
-keep class *Binding { *; }
-keep class *BindingImpl { *; }