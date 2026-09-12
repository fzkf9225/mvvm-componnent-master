# 模块说明与使用指南

> 对应版本：**common 4.6.0 / core-base 1.1.1 / 其余 core-\* 1.1.0 / commonui 3.6.1**（Material3 DayNight，见文末 Maven 坐标）  
> 本文说明各模块职责、依赖关系与日常用法。从 AppCompat 主题线或 4.4.x 升级请看 [UPGRADE.md](./UPGRADE.md)。

---

## 1. 总览

```
app（组装：Hilt / Demo）
  ├─ user（业务）──► userapi（UserService / UserInfo / Router）
  │              └─► base（Gateway、配置、BaseApp* …）  ← case 基建，非框架
  │                      ▲
  │                      │ 实现绑定在 app（适配 media / mqtt）
  ├─ mqttcomponent ──────┘ MessageGateway
  ├─ commonmedia ────────┘ MediaGateway
  ├─ commonui / googlegps / wscomponent / …
  └─ common（facade）── api ──► core-network / core-db / core-ui
                                      │
                                      ▼
                               core-base ← core-utils
```

**业务侧推荐入口：**

```gradle
implementation project(':common')   // 或 Maven: io.coderf.arklab.common:common:4.6.0
implementation project(':base')     // Gateway / AppPropertiesConfig / BaseAppActivity 等
implementation project(':userapi')  // 仅需要用户契约时
```

`common` 是兼容门面：本身几乎无业务代码，通过 `api` 把 `core-*` 透传出去，便于旧工程继续只依赖一个坐标。

> **命名注意：** Gradle 模块 `:base`（case）≠ Maven `io.coderf.arklab.core:base`（框架 `:core-base`）。

---

## 2. 核心分层模块

### `common`（facade）

| 项 | 说明 |
|----|------|
| 职责 | 依赖聚合 + 新旧 API 桥接（`RequestUiBridge`） |
| 包名 / namespace | `io.coderf.arklab.common.facade` |
| 何时用 | 宿主 / 业务模块统一依赖此入口即可 |
| 注意 | Manifest 不要再声明相对类名组件；权限与 `InitProvider` 等在 `core-base` |

### `core-base`

| 项 | 说明 |
|----|------|
| 职责 | `BaseActivity` / `BaseFragment` / `BaseViewModel`、历史 widget、helper、**全部 common 资源与 DataBinding** |
| namespace | `io.coderf.arklab.common`（保留旧 R / 包名，业务 import 基本不用改） |
| 何时用 | 一般通过 `common` 间接依赖；不要在业务里再拆第二份同名 R |
| 现状 | 体积仍大；widget / 重工具二次迁出尚未完成。**4.6.0** 起主题为 Material3，`ActionToolbar` / `TitleBar` 为 `MaterialToolbar`。 |

### `core-network`

| 项 | 说明 |
|----|------|
| 职责 | Retrofit / OkHttp 封装、旧 `RepositoryImpl*`、新 `DefaultNetworkRepository` |
| 新 API | `RequestOptions` / `RequestResult` / `AppError` / `RequestUi` |
| 旧 API | `RepositoryImpl` / `FlowRepositoryImpl` 已 `@Deprecated`，可编译但勿新增调用 |

**新请求示例（Kotlin）：**

```kotlin
class XxxRepository(
    private val api: XxxApi,
    requestUi: RequestUi = NoOpRequestUi
) : DefaultNetworkRepository(requestUi) {

    fun load(): Flow<RequestResult<Xxx>> = request(
        RequestOptions(showLoading = true)
    ) { api.getXxx() }
}
```

Demo 参考：`app/.../SampleCoreNetworkRepository.kt`。

### `core-db`

| 项 | 说明 |
|----|------|
| 职责 | Room DAO / Database / Entity / `RoomRepository*` |
| 何时用 | 本地持久化；经 `common` 或直接 `api project(':core-db')` |

### `core-ui`

| 项 | 说明 |
|----|------|
| 职责 | Activity 委托：`ToolbarDelegate`、`InitDataPolicy` 等 |
| 现状 | 代码量少；控件仍在 `core-base`（受 R/DataBinding 限制） |
| 用法 | `BaseActivity` 已接入 `InitDataPolicy`（默认 `AlwaysInitData`） |

### `core-utils`

| 项 | 说明 |
|----|------|
| 职责 | 无 Android R 依赖的轻量工具与通用扩展（`CoreLogger`、日期/数值/密度/View 等） |
| 包名 | `io.coderf.arklab.core.utils`；扩展统一在 `io.coderf.arklab.core.utils.ext` |
| 现状 | 已从 `core-base` 下沉原 `Extensions.kt`；`DrawableUtil` 等带 R / 重 UI 的仍留在 `core-base` |
| 新代码 | `import io.coderf.arklab.core.utils.ext.*`（勿再依赖 `common.utils.common` 下的旧扩展） |
| 兼容 | `core-base` 的 `Extensions.kt` 仍保留同签名 `@Deprecated` 转发，旧 import 可编译 |

---

## 3. 业务与能力模块

| 模块 | 职责 | 业务应如何依赖 |
|------|------|----------------|
| `base` | **Demo/case 基建**（非框架）：`MediaGateway` / `MessageGateway`、`AppPropertiesConfig`、`BaseAppActivity`、通用枚举等 | `api`/`implementation project(':base')` |
| `userapi` | 用户模块对外契约（`UserService` / `UserInfo` / Router） | `implementation project(':userapi')` |
| `user` | 用户业务 UI / 逻辑 | 依赖 `common` + `base` + `userapi`，**禁止**直接依赖 mqtt / media |
| `mqttcomponent` | MQTT 实现（`MqttClient` 等） | 仅 app 依赖；Gateway 适配写在 app |
| `commonmedia` | 拍照/选图/压缩（`MediaHelper` 等） | 仅 app 依赖；Gateway 适配写在 app |
| `commonui` | 表单等通用 UI 组件 | 按需 |
| `googlegps` | 定位能力 | 按需 |
| `wscomponent` | WebSocket | 按需 |
| `annotation` / `room-processor` | 校验注解与 Room 处理器 | 按需 |
| `app` | Demo 组装：Hilt Module、Gateway 适配与绑定 | 宿主样板 |

---

## 4. Gateway 用法（组件解耦）

业务模块**只依赖接口**，实现由 app 注入。

### MessageGateway（MQTT）

```kotlin
@Inject lateinit var messageGateway: MessageGateway

messageGateway.connect()
messageGateway.subscribe("topic/x") { topic, payload -> /* … */ }
```

绑定示例见 `app/.../di/GatewayModule.kt`、`app/.../mqtt/MqttMessageGateway`。

### MediaGateway（媒体）

```java
@Inject MediaGateway mediaGateway;

mediaGateway.pickImages(1, uris -> { /* 上传头像等 */ });
```

- 接口：`base/.../MediaGateway`（case，非框架）
- 适配与绑定：`app/.../media/MediaHelperGateway`、`app/.../di/MediaGatewayModule`
- 参考：`MeFragment`、`FeedBackActivity`

**禁止：** 在 `user` 等业务 module 的 `build.gradle` 里写 `mqttcomponent` / `commonmedia`。  
**禁止：** `commonmedia` / `mqttcomponent` / `core-*` 依赖 `:base` / `userapi`。

字典类枚举（如业务 `DicTypeEnum`）按项目放在各自的 case `:base`，Demo 不内置业务字典码。

---

## 5. Maven 发布坐标

推送需环境变量 `ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`，例如：

```bash
./gradlew :common:publish
./gradlew :core-base:publish :core-network:publish :core-db:publish :core-ui:publish :core-utils:publish :core-log:publish
```

| 模块 | 坐标 |
|------|------|
| common | `io.coderf.arklab.common:common:4.6.0` |
| core-base | `io.coderf.arklab.core:base:1.1.1` |
| core-network | `io.coderf.arklab.core:network:1.1.0` |
| core-db | `io.coderf.arklab.core:db:1.1.0` |
| core-ui | `io.coderf.arklab.core:ui:1.1.0` |
| core-utils | `io.coderf.arklab.core:utils:1.1.0` |
| core-log | `io.coderf.arklab.core:log:1.1.0` |
| room-processor | `io.coderf.arklab.room:room-processor:1.1.0` |
| core-mqtt | `io.coderf.arklab.mqtt:mqtt:1.6.0` |
| commonmedia | `io.coderf.arklab.media:media:3.4.0` |
| commonui | `io.coderf.arklab.ui:ui:3.6.1`（`api` → core-base / core-network，另依赖 media） |
| googlegps | `io.coderf.arklab.googlegps:googlegps:3.2.0` |
| annotation | `io.coderf.arklab.annotation:annotation:3.3.0` |

宿主若只引 `common`，会通过 POM / `api` 依赖带上对应 `core-*`（以实际发布 POM 为准）。

---

## 6. 新代码约定

1. 业务不直接依赖 mqtt / media 实现模块，只依赖 Gateway。
2. 新网络代码优先 `DefaultNetworkRepository.request`，勿再扩 `sendRequest` 重载。
3. Repository 内不要持有页面；Loading / Toast 只走 `getRequestUi()` / `RequestUi`。
4. `core-*` 禁止依赖 `user` / `app`。
5. 不要把 `APP_SECRET` 明文放进 HTTP Header（仅本地签名）。
6. 主题使用 `AppBaseTheme`（Material3 DayNight）；品牌色底上的字用 `onPrimary`，不要写死白色。
7. `Config.setDynamicColorEnabled` 默认关；若打开须在 `Config.init()` 之前。屏幕适配、崩溃捕获、夜间模式、Edge-to-Edge、默认占位图同样走 `Config`，且须在 `init()` 前设置。
