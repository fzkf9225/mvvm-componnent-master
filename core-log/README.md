# core-log

统一的 Android 日志模块：PrettyLogger 初始化与本地 Logcat 落盘由本模块集中管理；各业务/框架模块只保留自己的控制台 debug 开关。

当前版本：**1.2.0**  
Maven：`io.coderf.arklab.core:log:1.2.0`

## 坐标

```gradle
implementation 'io.coderf.arklab.core:log:1.2.0'
```

发布命令（需环境变量 `ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`）：

```bash
./gradlew :core-log:publish
```

## 1.2.0 变更

- 新增 `ArkLog.getLogDir()`：返回当前本地日志目录，便于分享 / 上传 / 排查
- 落盘目录：优先 `externalCacheDir`，不可用时回落 `cacheDir`（与工程内 CacheUtil 习惯一致）
- 跨天自动换文件：进程过零点后写入新的 `prefix-yyyy-MM-dd.log`
- `stopFileLog` / 切换层级时主动销毁 logcat 子进程，停止更干脆
- `LogChannel` 对 null 消息做空串兜底，避免系统 `Log.*(tag, null)` NPE
- 文档明确：`channel(id, tag)` 的 tag 仅首次创建生效；`FileLogLevel.DEBUG` 不含 V

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
| 日志目录查询 | `ArkLog.getLogDir()` |
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

// 取当前日志目录（分享 / 上传前）
File logDir = ArkLog.getLogDir();
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

> **注意：** 同一 `moduleId` 全局单例，`channel(id, tag)` 的缺省 tag **仅在首次创建时生效**，后续再传不同 tag 不会覆盖。

## API 一览

### ArkLog

| 方法 | 说明 |
|------|------|
| `init(Context)` / `init(Context, LogConfig)` | 初始化 PrettyLogger；配置了非 `NONE` 落盘层级则自动开启 |
| `initFormat(...)` | 仅刷新 PrettyLogger 格式，不改动文件日志 |
| `channel(moduleId)` / `channel(moduleId, defaultTag)` | 获取（或创建）模块通道 |
| `base()` / `mqtt()` / `gps()` / `media()` | 预置模块通道 |
| `startFileLog(Context, FileLogLevel)` | 开启或切换进程级落盘；`NONE` 表示关闭 |
| `applyFileLogFromConfig(Context)` | 按当前 `LogConfig.fileLogLevel` 开启或关闭 |
| `stopFileLog()` | 停止落盘 |
| `getLogDir()` | 当前日志目录；未 `init` 时为 `null` |
| `getFileLogLevel()` / `getConfig()` / `isLoggerReady()` | 状态查询 |

### LogConfig.Builder

| 项 | 默认 | 说明 |
|----|------|------|
| `showThreadInfo` | `true` | 是否显示线程信息 |
| `methodCount` | `0` | 堆栈方法层数 |
| `methodOffset` | `0` | 跳过封装层数 |
| `globalTag` | `"ArkLab"` | 全局默认 tag |
| `fileLogLevel` | `NONE` | 初始化时是否自动落盘 |
| `fileNamePrefix` | `"log"` | 文件名前缀 → `prefix-yyyy-MM-dd.log` |
| `fileDirName` | `"log"` | 相对 cache 根目录的子目录名 |

### LogChannel

| 方法族 | 受 `enableDebug` 控制 | 说明 |
|--------|----------------------|------|
| `loggerV` / `v` | 是 | Verbose |
| `loggerD` / `d` / `logger` / `show` | 是 | Debug（`logger`/`show` 为历史兼容别名） |
| `loggerI` / `i` | 是 | Info |
| `loggerW` / `w` | 否 | Warn，始终输出 |
| `loggerE` / `e` | 否 | Error，始终输出 |
| `json` / `xml` | 是 | PrettyLogger 格式化 |

`logger*` 走 PrettyLogger；短名（`v`/`d`/`i`/`w`/`e`/`show`）走系统 `android.util.Log`。

## 行为约定

- **V / D / I / json / xml**：受该通道 `enableDebug` 控制
- **W / E**：始终输出（与原先各模块一致）
- **消息 null**：通道层转为空串，避免系统 Log NPE；`json`/`xml` 遇 null 直接忽略
- **本地落盘**：按 `FileLogLevel` 过滤当前进程 PID 的 logcat 行，写入  
  `{externalCacheDir|cacheDir}/{fileDirName}/{fileNamePrefix}-yyyy-MM-dd.log`
- **跨天**：同一进程过零点后自动切换到新的日期文件
- **FileLogLevel**：

  | 层级 | 落盘级别 |
  |------|----------|
  | `NONE` | 不落盘 |
  | `DEBUG` | D / I / W / E（**不含 V**） |
  | `TEST` | I / W / E |
  | `RELEASE` | W / E |

## 本仓库模块依赖

本工程内 `:core-base` / `:core-mqtt` / `:core-ws` / `:googlegps` / `:commonmedia` 已依赖 `:core-log`，删除了各自重复的 Log 实现；模块侧通过薄封装委托本库：

| 模块 | 封装 |
|------|------|
| core-base | `LogUtil` → `ArkLog.base()` |
| core-mqtt | `MqttLog` → `ArkLog.mqtt()` |
| core-ws | `WsLog` → `ArkLog.ws()` |
| googlegps | `LogUtil` / `DebugUtil` → `ArkLog.gps()` |
| commonmedia | `LogUtil` / `DebugUtil` → `ArkLog.media()` |
