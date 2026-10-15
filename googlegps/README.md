# googlegps

可选定位能力库：GNSS / NMEA、Socket 定位客户端及 GPX/CSV 等记录辅助。

当前版本：**3.3.0**
Maven：`io.coderf.arklab.googlegps:googlegps:3.3.0`

**3.3.0**：按职责重划包结构（`api` / `service` / `permission` / `location` / `track` / `socket` / `ui` / `utils`），旧 `common.*` 等路径废弃（见下方「包结构」）；同步精简 `consumer-rules.pro`。
**3.2.0**：确认框 `colorPrimary` 在 `values-night` 对齐宿主浅色 Primary（`#A9C7FF`），避免深色表面上仍是亮色品牌蓝。
namespace：`io.coderf.arklab.googlegps`

---

## 职责

- GPS / 定位服务与监听回调
- 网络定位 / 日志记录相关封装（按需使用）

---

## 包结构

`io.coderf.arklab.googlegps.*`，按职责分层：

| 包 | 内容 | 说明 |
| --- | --- | --- |
| `api` | `GpsStarter` / `GpsCallback` / `GpsSettingConfig` / `LocationFilter` | **宿主唯一需要依赖的对外 API** |
| `service` | `GpsService` / `Session` | 前台定位服务（Manifest 注册）与会话运行态 |
| `permission` | `GpsLifecycleObserver` / `PermissionsChecker` / `PermissionManager` | GPS 开关、前/后台定位权限引导流程 |
| `location` | `GnssLocationListener` / `NmeaLocationListener` / `NmeaSentence` | 系统定位监听与 NMEA 解析（内部） |
| `track` | `IFileLogger` / `CsvFileLogger` / `GpxFileLogger` / `FileLoggerFactory` / `Files` / `GpsFileParser` / `GpsLoggerThreadExecutor` | 轨迹文件记录与解析 |
| `socket` | `GPSSocketServer` / `InboundClientHandler` / 回调接口 | Netty TCP 上传通道 |
| `ui` | `GPSConfirmDialog` / `UiUtil` | 权限引导确认弹窗（内部） |
| `utils` | `AppUtil` / `DebugUtil` / `DeviceIdUtil` / `EsriUtil` / `LogUtil` | 通用工具 |

> 自 3.3.0 起包路径调整，旧版 `common.*` / `listener.*` / `logger.*` / `dialog.*` 已废弃，升级时只需批量替换 import：
> `common.GpsStarter|GpsCallback|GpsSettingConfig|LocationFilter` → `api.*`，`common.Session` → `service.Session`，
> `common.GpsLifecycleObserver` → `permission.*`，`logger.*` / `utils.GpsFileParser` → `track.*`。

---

## 混淆

`consumer-rules.pro` 随 AAR 发布，宿主无需额外配置：`api` / `service` / `permission` / `track` / `socket` / `utils` 全量保留，
`location` / `ui` 为内部实现允许混淆；`LocationFilter` 实现类保留类名（日志中的过滤器名称可读）。

---

## 依赖

- 无工程内核心 `project` 依赖（独立能力库）

---

## 何时依赖

App 需要定位能力时按需引入：

```gradle
implementation 'io.coderf.arklab.googlegps:googlegps:3.3.0'
// 或
implementation project(':googlegps')
```

业务侧可按项目在 case `:base` 再包一层 Gateway（与 media/mqtt 同样模式），本库本身不强制 Gateway。

---

## 发布

```bash
./gradlew :googlegps:publish
```
