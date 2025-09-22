# 保留 Media 框架核心类
-keep class com.casic.otitan.media.** { *; }
-dontwarn com.casic.otitan.media.**

# 保留数据模型类
-keep class com.casic.otitan.media.bean.** { *; }
-keepclassmembers class com.casic.otitan.media.bean.** {
    void set*(***);
    *** get*();
}

# 保留核心工具类和构建器
-keep class com.casic.otitan.media.MediaBuilder { *; }
-keep class com.casic.otitan.media.MediaHelper { *; }
-keep class com.casic.otitan.media.utils.** { *; }

# 保留回调接口
-keep class com.casic.otitan.media.callback.** { *; }
-keep interface com.casic.otitan.media.callback.** { *; }

# 保留多媒体处理方法
-keepclassmembers class com.casic.otitan.media.** {
    public * compress*(...);
    public * process*(...);
    public * encode*(...);
    public * decode*(...);
}