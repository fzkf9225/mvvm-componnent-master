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