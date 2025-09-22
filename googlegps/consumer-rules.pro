# 保留 GPS 框架核心类
-keep class com.casic.otitan.googlegps.** { *; }
-dontwarn com.casic.otitan.googlegps.**

# 保留定位服务
-keep class com.casic.otitan.googlegps.service.** { *; }
-keepclassmembers class com.casic.otitan.googlegps.service.** {
    public * onStartCommand(...);
    public * onBind(...);
}

# 保留 Socket 通信
-keep class com.casic.otitan.googlegps.socket.** { *; }
-keepclassmembers class com.casic.otitan.googlegps.socket.** {
    public * connect*(...);
    public * send*(...);
}

# 保留工具类
-keep class com.casic.otitan.googlegps.utils.** { *; }

# 保留监听器接口
-keep interface com.casic.otitan.googlegps.listener.** { *; }

# 保留位置数据处理方法
-keepclassmembers class * {
    public * getLocation*(...);
    public * onLocation*(...);
    public * format*Gps*(...);
}