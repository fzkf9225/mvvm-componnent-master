# ============================================================================
# googlegps consumer 混淆规则（随 AAR 发布，宿主 App 开启 R8 时自动生效）
#
# 包结构（io.coderf.arklab.googlegps.*）：
#   api        对外公开 API：GpsStarter / GpsCallback / GpsSettingConfig / LocationFilter
#   service    前台定位服务 GpsService（Manifest 注册）+ 会话运行态 Session
#   permission GPS 开关 / 前后台定位权限引导（GpsLifecycleObserver 可被宿主直接使用）
#   track      轨迹文件读写：IFileLogger / Csv / Gpx / Files / GpsFileParser
#   socket     Netty TCP 上传通道及其回调接口
#   utils      通用工具（DebugUtil / LogUtil / EsriUtil 等，宿主会直接调用）
#   location   [内部] 系统定位监听与 NMEA 解析 —— 允许混淆
#   ui         [内部] 确认弹窗 —— 允许混淆
#
# 若宿主遇到 NoSuchMethod / ClassNotFound 之类问题，可临时放开全量保留：
#   -keep class io.coderf.arklab.googlegps.** { *; }
# ============================================================================

-dontwarn io.coderf.arklab.googlegps.**

# ---------- 对外 API：宿主会继承 GpsCallback、实现 LocationFilter、链式调用 Config ----------
-keep class io.coderf.arklab.googlegps.api.** { *; }
# 自定义过滤器以 getClass().getSimpleName() 作为日志/回调里的过滤器名，保留实现类名以便排查
-keepnames class * implements io.coderf.arklab.googlegps.api.LocationFilter

# ---------- 定位服务：Manifest 注册的 Service、Binder、Session 均会被宿主引用 ----------
-keep class io.coderf.arklab.googlegps.service.** { *; }

# ---------- 权限引导：GpsLifecycleObserver / PermissionsChecker 可被宿主单独使用 ----------
-keep class io.coderf.arklab.googlegps.permission.** { *; }

# ---------- 轨迹文件：宿主读取 / 解析日志文件 ----------
-keep class io.coderf.arklab.googlegps.track.** { *; }

# ---------- Socket 通道：回调接口由宿主实现 ----------
-keep class io.coderf.arklab.googlegps.socket.** { *; }

# ---------- 通用工具 ----------
-keep class io.coderf.arklab.googlegps.utils.** { *; }

# ---------- Netty（GPSSocketServer / InboundClientHandler 运行时依赖，内部大量反射） ----------
-keep class io.netty.** { *; }
-dontwarn io.netty.**
-keepclassmembers class io.netty.channel.** {
    public *;
}
-keepclassmembers class io.netty.buffer.** {
    public *;
}

# ---------- Apache Commons Lang3（GPSSocketServer 依赖） ----------
-keep class org.apache.commons.lang3.** { *; }
-dontwarn org.apache.commons.lang3.**
