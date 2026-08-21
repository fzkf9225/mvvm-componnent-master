# core-log

统一的 Android 日志模块：PrettyLogger 初始化与本地 Logcat 落盘由本模块集中管理；各业务/框架模块只保留自己的控制台 debug 开关。

## 坐标

```gradle
implementation 'io.coderf.arklab.core:log:1.0.0'
```

发布命令（需环境变量 `ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`）：

```bash
./gradlew :core-log:publish
```

## 为什么需要它

多模块各自复制 `LogUtil` / `LogcatHelper` / `FileLogLevel` / `DebugUtil` 时会出现：

1. 实现漂移（一边改了另一边没改）
2. 各自 `Logger.clearLogAdapters()` 互相覆盖
3. 宿主 Application 要给每个框架单独写一套开关与落盘逻辑

`core-log` 解决：

| 能力 | 谁负责 |
|------|--------|
| PrettyLogger 格式、全局 tag | `ArkLog.init` / `LogConfig` |
| 本地文件读写层级与目录 | `ArkLog.startFileLog` / `LogConfig.fileLogLevel` |
| 控制台 debug 开关 | 各模块自己的 `LogChannel.setEnableDebug` |

## 宿主接入（推荐）

```java
@Override
public void onCreate() {
    super.onCreate();

    // 1) 只初始化一次：格式 +（可选）本地日志
    ArkLog.init(this, LogConfig.builder()
            .showThreadInfo(true)
            .methodCount(0)
            .methodOffset(0)
            .globalTag("ArkLab_Log")
            .fileLogLevel(BuildConfig.LOG_DEBUG ? FileLogLevel.DEBUG : FileLogLevel.NONE)
            .fileNamePrefix("log")
            .fileDirName("log")
            .build());

    if (BuildConfig.LOG_DEBUG) {
        // 2) 各模块只开自己的控制台开关（不再各自起 Logcat）
        ArkLog.base().setEnableDebug(true);
        ArkLog.mqtt().setEnableDebug(true);
        ArkLog.gps().setEnableDebug(true);
        ArkLog.media().setEnableDebug(true);
        // 或：Config / Mqtt.enableDebug / Gps DebugUtil 等模块封装
    }
}
```

运行时切换落盘：

```java
ArkLog.startFileLog(this, FileLogLevel.TEST);
ArkLog.stopFileLog();
```

## 模块内用法

```java
// 获取本模块通道（同 moduleId 全局单例）
LogChannel log = ArkLog.channel("gps"); // 或 ArkLog.gps()

log.setEnableDebug(true);
log.loggerI("GpsService", "位置已接受");
log.e("GpsService", "bootstrap failed", t);
```

预置通道 id：

- `ArkLog.MODULE_BASE` / `ArkLog.base()`
- `ArkLog.MODULE_MQTT` / `ArkLog.mqtt()`
- `ArkLog.MODULE_GPS` / `ArkLog.gps()`
- `ArkLog.MODULE_MEDIA` / `ArkLog.media()`
- 其它模块：`ArkLog.channel("your-module-id", "DefaultTag")`

## 行为约定

- **V / D / I / json / xml**：受该通道 `enableDebug` 控制
- **W / E**：始终输出（与原先各模块一致）
- 本地落盘按 `FileLogLevel` 过滤当前进程 PID 的 logcat 行，写入  
  `{externalCacheDir}/{fileDirName}/{fileNamePrefix}-yyyy-MM-dd.log`

## 本仓库模块依赖

本工程内 `:core-base` / `:core-mqtt` / `:googlegps` / `:commonmedia` 已依赖 `:core-log`，删除了各自重复的 Log 实现。
