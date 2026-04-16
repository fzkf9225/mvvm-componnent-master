# 保留 MVVM 框架核心类
-keep class io.coderf.arklab.common.** { *; }
-dontwarn io.coderf.arklab.common.**

# 保留数据绑定相关类
-keep class io.coderf.arklab.common.bean.** { *; }
-keepclassmembers class io.coderf.arklab.common.bean.** {
    void set*(***);
    *** get*();
}

# 保留 BaseResponse 和 ViewModel
-keep class io.coderf.arklab.common.base.BaseResponse { *; }
-keep class io.coderf.arklab.common.viewmodel.** { *; }

# 保留注解处理器生成的类
-keep class * extends io.coderf.arklab.common.base.** { *; }

# 保留资源绑定相关类（如果有自动生成的绑定类）
-keep class *Binding { *; }
-keep class *BindingImpl { *; }