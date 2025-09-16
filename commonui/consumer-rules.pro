# 保留 CommonUI 框架核心类
-keep class com.casic.titan.commonui.** { *; }
-dontwarn com.casic.titan.commonui.**

# 确保数据模型不被混淆
-keep class com.casic.titan.commonui.bean.** { *; }
-keepclassmembers class com.casic.titan.commonui.bean.** {
    void set*(***);
    *** get*();
}

# 保留自定义 Widget 组件
-keep class com.casic.titan.commonui.widget.** { *; }
-keepclassmembers class com.casic.titan.commonui.widget.** {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
}

# 保留 Fragment
-keep class com.casic.titan.commonui.fragment.** { *; }

# 保留适配器
-keep class com.casic.titan.commonui.adapter.** { *; }

# 保留表单相关功能
-keep class com.casic.titan.commonui.form.** { *; }

# 保留接口和回调
-keep interface com.casic.titan.commonui.inter.** { *; }
-keep class com.casic.titan.commonui.impl.** { *; }

# 保留数据绑定相关类
-keep class *Binding { *; }
-keep class *BindingImpl { *; }