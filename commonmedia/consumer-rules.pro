# 保留 Media 框架核心类
-keep class pers.fz.media.** { *; }
-dontwarn pers.fz.media.**

# 保留数据模型类
-keep class pers.fz.media.bean.** { *; }
-keepclassmembers class pers.fz.media.bean.** {
    void set*(***);
    *** get*();
}

# 保留核心工具类和构建器
-keep class pers.fz.media.MediaBuilder { *; }
-keep class pers.fz.media.MediaHelper { *; }
-keep class pers.fz.media.utils.** { *; }

# 保留回调接口
-keep class pers.fz.media.callback.** { *; }
-keep interface pers.fz.media.callback.** { *; }

# 保留多媒体处理方法
-keepclassmembers class pers.fz.media.** {
    public * compress*(...);
    public * process*(...);
    public * encode*(...);
    public * decode*(...);
}