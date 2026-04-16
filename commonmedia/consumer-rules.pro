# 保留 Media 框架核心类
-keep class io.coderf.arklab.media.** { *; }
-dontwarn io.coderf.arklab.media.**

# 保留数据模型类
-keep class io.coderf.arklab.media.bean.** { *; }
-keepclassmembers class io.coderf.arklab.media.bean.** {
    void set*(***);
    *** get*();
}

# 保留核心工具类和构建器
-keep class io.coderf.arklab.media.MediaBuilder { *; }
-keep class io.coderf.arklab.media.MediaHelper { *; }
-keep class io.coderf.arklab.media.utils.** { *; }

# 保留回调接口
-keep class io.coderf.arklab.media.callback.** { *; }
-keep interface io.coderf.arklab.media.callback.** { *; }

# 保留多媒体处理方法
-keepclassmembers class io.coderf.arklab.media.** {
    public * compress*(...);
    public * process*(...);
    public * encode*(...);
    public * decode*(...);
}