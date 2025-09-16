# 保留 GPS 框架核心类
-keep class com.casic.titan.googlegps.** { *; }
-dontwarn com.casic.titan.googlegps.**

# 保留定位服务
-keep class com.casic.titan.googlegps.service.** { *; }
-keepclassmembers class com.casic.titan.googlegps.service.** {
    public * onStartCommand(...);
    public * onBind(...);
}

# 保留 Socket 通信
-keep class com.casic.titan.googlegps.socket.** { *; }
-keepclassmembers class com.casic.titan.googlegps.socket.** {
    public * connect*(...);
    public * send*(...);
}

# 保留工具类
-keep class com.casic.titan.googlegps.utils.** { *; }

# 保留监听器接口
-keep interface com.casic.titan.googlegps.listener.** { *; }

# 保留位置数据处理方法
-keepclassmembers class * {
    public * getLocation*(...);
    public * onLocation*(...);
    public * format*Gps*(...);
}