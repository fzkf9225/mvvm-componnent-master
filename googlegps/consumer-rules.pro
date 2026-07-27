# 保留 GPS 框架核心类
-keep class io.coderf.arklab.googlegps.** { *; }
-dontwarn io.coderf.arklab.googlegps.**

# 保留定位服务
-keep class io.coderf.arklab.googlegps.service.** { *; }
-keepclassmembers class io.coderf.arklab.googlegps.service.** {
    public * onStartCommand(...);
    public * onBind(...);
}

# 保留 Socket 通信
-keep class io.coderf.arklab.googlegps.socket.** { *; }
-keepclassmembers class io.coderf.arklab.googlegps.socket.** {
    public * connect*(...);
    public * send*(...);
}

# Netty（GPSSocketServer / InboundClientHandler 运行时依赖，必须进入 consumer）
-keep class io.netty.** { *; }
-dontwarn io.netty.**
-keepclassmembers class io.netty.channel.** {
    public *;
}
-keepclassmembers class io.netty.buffer.** {
    public *;
}

# Apache Commons Lang（googlegps 依赖）
-keep class org.apache.commons.lang3.** { *; }
-dontwarn org.apache.commons.lang3.**

# 保留工具类
-keep class io.coderf.arklab.googlegps.utils.** { *; }

# 保留监听器接口
-keep interface io.coderf.arklab.googlegps.listener.** { *; }

# 保留位置数据处理方法
-keepclassmembers class * {
    public * getLocation*(...);
    public * onLocation*(...);
    public * format*Gps*(...);
}
