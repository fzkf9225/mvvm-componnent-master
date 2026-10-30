# 升级与迁移说明

> **当前主干版本（以各模块 `build.gradle` 发布号为准，README 若滞后请以此为准）：**  
> **common `4.7.0`** · **core-base `1.5.9`** · **core-network / core-db `1.6.0`** · **core-ui `1.3.1`** · **core-utils `1.1.1`** · **core-log `1.2.0`** · **core-autosize `1.0.0`** · **room-processor `1.2.0`** · **core-mqtt `1.6.2`** · **core-ws `1.0.0`** · **commonui `3.7.3`** · **commonmedia `3.7.0`** · **googlegps `3.3.1`** · **annotation `3.3.1`**  
>
> **本轮重点：** [§0.3](#03-core-ws-100取代-wscomponent)（新建 `core-ws`）；[§0](#0-160network--db请求-options-完善)（network / db Options）。  
> Room 协程栈落地见 [§0.1](#01-历史core-db-150-协程栈若尚未跟上)；剔除 `BaseView` 见 [§2](#2-120彻底剔除-baseview--mvp-页面绑定)；AppCompat → Material3 见 [§1](#1-460appcompat-主题--material3)。  
> 仍停留在 **common 4.4.x 单体**：先完成 [附录 A](#附录-a4451从-444x-单体拆到-core)。  
> 模块职责见 [MODULES.md](./MODULES.md)；网络迁移细节见 [core-network/MIGRATION.md](./core-network/MIGRATION.md)、[core-network/README.md](./core-network/README.md)；Room 见 [core-db/README.md](./core-db/README.md)。

---

## 当前推荐 Maven 钉版本

业务工程建议**显式**声明下表（勿只升 `common` 指望传递依赖一定最新）：

```gradle
implementation 'io.coderf.arklab.common:common:4.7.0'
implementation 'io.coderf.arklab.core:base:1.5.9'
implementation 'io.coderf.arklab.core:network:1.6.0'
implementation 'io.coderf.arklab.core:db:1.6.0'
implementation 'io.coderf.arklab.core:ui:1.3.1'
implementation 'io.coderf.arklab.core:utils:1.1.1'
implementation 'io.coderf.arklab.core:log:1.2.0'
ksp 'io.coderf.arklab.room:room-processor:1.2.0'   // 使用 @RoomObservedEntity 时
// 按需：
implementation 'io.coderf.arklab.ui:ui:3.7.3'
implementation 'io.coderf.arklab.media:media:3.7.0'
implementation 'io.coderf.arklab.mqtt:mqtt:1.6.2'
implementation 'io.coderf.arklab.ws:ws:1.0.0'
implementation 'io.coderf.arklab.googlegps:googlegps:3.3.1'
```

工程依赖 `implementation project(':common')` 时随源码即可。

| 模块 | 坐标 |
|------|------|
| common | `io.coderf.arklab.common:common:4.7.0` |
| core-base | `io.coderf.arklab.core:base:1.5.9` |
| core-network | `io.coderf.arklab.core:network:1.6.0` |
| core-db | `io.coderf.arklab.core:db:1.6.0` |
| core-ui | `io.coderf.arklab.core:ui:1.3.1` |
| core-utils | `io.coderf.arklab.core:utils:1.1.1` |
| core-log | `io.coderf.arklab.core:log:1.2.0` |
| core-autosize | `io.coderf.arklab.core:autosize:1.0.0` |
| room-processor | `io.coderf.arklab.room:room-processor:1.2.0` |
| core-mqtt | `io.coderf.arklab.mqtt:mqtt:1.6.2` |
| core-ws | `io.coderf.arklab.ws:ws:1.0.0` |
| commonui | `io.coderf.arklab.ui:ui:3.7.3` |
| commonmedia | `io.coderf.arklab.media:media:3.7.0` |
| googlegps | `io.coderf.arklab.googlegps:googlegps:3.3.1` |
| annotation | `io.coderf.arklab.annotation:annotation:3.3.1` |

---

## 0.3 core-ws `1.0.0`：取代 wscomponent

新建与 **core-mqtt** 同构的 WebSocket 框架模块（OkHttp），并**删除**未投入生产的 `:wscomponent`。

| 项 | 说明 |
|----|------|
| 坐标 | `io.coderf.arklab.ws:ws:1.0.0` |
| 入口 | `Ws` / `WsClient` / `WsConfig` / `WsSession` |
| 鉴权 | Header / Query / `requireAuth(false)` 匿名直连；可选 `WsAuthTokenProvider` |
| 频道 | 逻辑频道 + `WsChannelBinder`（宿主编码订退帧）；`WsSession` Lifecycle / Overlay |
| 重连 | 无限退避 **或** 有限固定间隔 + `WsReconnectDialog` |
| Presence | 独立 `PresenceClient` + `HeartbeatScheduler`；宿主实现 `PresenceManager` |
| 边界 | **禁止**业务信封 / bizCode / SubscribeEnum 进入框架 |
| 日志 | `WsLog` → `ArkLog.ws()`（core-log 已加 `MODULE_WS`） |

```gradle
implementation 'io.coderf.arklab.ws:ws:1.0.0'
// 或
implementation project(':core-ws')
```

Demo：`WsDemoActivity`。明细见 [core-ws/README.md](./core-ws/README.md)。

---

## 0. 1.6.0：network / db 请求 Options 完善

本轮 **不强制** 改业务调用点；以补齐 Options、对齐旧栈语义、修隐患为主。明细见各模块 README。

### 0.1 core-network `1.6.0`

| 项 | 说明 |
|----|------|
| Toast 对等 | `RequestOptions` 新增 `showErrorToast` / `errorToastMessage`；工厂 `noToast()` |
| 通用重试 | `RetryPolicy` **仅瞬时网络失败**；业务 `BaseException` 不再误重试；鉴权仍走 `TokenRefresher` |
| 异常转换 | 新栈 `.catch` 接入 `DefaultExceptionConverter`（HTTP errorBody） |
| ApiRetrofit | `o-timestamp`（签名）、`addNetworkInterceptor`、`setLogBodyMaxBytes` |
| 转换工厂 | null `data` 按 `actualType`；成功路径去掉多余序列化 |
| 旧 Options | 修复 `ApiRequestOptions.Builder` 跨请求串实例 |

```kotlin
// 有 Loading，不弹 Toast
request(RequestOptions.noToast()) { api.submit() }

// 登录：关鉴权刷新 + 关通用退避
request(
    RequestOptions.builder()
        .enableAuthRetry(false)
        .retryPolicy(RetryPolicy.None)
        .build()
) { api.login(body) }
```

```gradle
implementation 'io.coderf.arklab.core:network:1.6.0'
```

完整对照与迁移：[core-network/README.md](./core-network/README.md)、[core-network/MIGRATION.md](./core-network/MIGRATION.md)。

### 0.2 core-db `1.6.0`

| 项 | 说明 |
|----|------|
| Toast / 错误交付 | `RoomRequestOptions`：`deliverErrorToUi`、`showErrorToast`、`errorToastMessage`；`noToast()` |
| `silent()` | 对齐网络：无 Loading + 协程栈不交错误 UI（仍可 collect `RequestResult.Error`） |
| 工厂 | `defaults()`、`noTimeout()` |
| Bug | `query(false, …)` 仍保留 `throwOnEmptyList=true`（不再误用 `silent()`） |

```kotlin
execute(RoomRequestOptions.withLoading("保存中...")) { dao.insertSuspend(e) }

// 无 Loading，空列表仍 NOT_FOUND
RoomRequestOptions.query(false, true)
```

```gradle
implementation 'io.coderf.arklab.core:db:1.6.0'
ksp 'io.coderf.arklab.room:room-processor:1.2.0'
```

**升级注意：** `RoomDataRepository` 等默认 `silent()` 时，协程失败**不再自动 Toast**；需要提示请用 `withLoading` / `defaults()` 或 `setDeliverErrorToUi(true)`。Rx 栈错误展示仍由订阅方负责。详见 [core-db/README.md](./core-db/README.md)。

### 0.3 历史：core-db `1.5.0` 协程栈（若尚未跟上）

若宿主仍停在 db `≤1.4.x`，请先升到至少 **1.5.0**（协程 `RoomDataRepository` / Dao `*Suspend` / `RoomPagingSource`），再升 **1.6.0**。  
processor 配套仍为 **`room-processor:1.2.0`**（Bridge suspend RawQuery）。1.6.0 **未**改 Bridge，已用 1.2.0 的工程不必为 Options 再升 processor。

---

## 2. 1.2.0：彻底剔除 BaseView / MVP 页面绑定

这是一次 **架构契约** 大版本（相对当时 core-base **1.1.3** / commonui **3.6.2** / commonmedia **3.4.1**），不是主题变更。目标：ViewModel / Repository **不再持有页面**，请求 Loading / 错误展示只走 **`RequestUi` + `NetworkRequestUiHost`**；业务导航用 LiveData / Flow。后续已进一步删除旧版 `RequestUiCallback`，全链路统一为 `RequestUi`。

> **版本说明：** 下表「当时落点」描述的是 **BaseView 剔除当轮** 的发布号。当前主干已继续演进（见文首矩阵 / 文首 Maven 块），业务工程请钉**当前**坐标，不要只升到表中的 1.2.0。

### 2.1 版本号（BaseView 剔除当轮 → 请再对齐文首当前矩阵）

| 模块 | 剔除前一档 | 剔除当轮落点 | 当前主干（请以此为准） |
|------|------------|--------------|------------------------|
| common（facade） | 4.6.0 | 4.6.0 | **4.7.0** |
| core-base | 1.1.3 | 1.2.0 | **1.5.9** |
| core-network | 1.1.0 | 1.2.0 | **1.6.0** |
| core-db | 1.1.0 | 1.2.0（其后 1.5.0 协程栈） | **1.6.0** |
| core-ui | 1.1.0 | 1.2.0 | **1.3.1** |
| core-utils | 1.1.0 | 1.1.1 | **1.1.1** |
| commonui | 3.6.2 | 3.7.0 | **3.7.3** |
| commonmedia | 3.4.1 | 3.5.0 | **3.7.0** |
| room-processor | — | 1.2.0（配套 db 1.5.0+） | **1.2.0** |

依赖坐标请直接复制文首「当前推荐 Maven 钉版本」。

工程依赖 `implementation project(':common')` 时随源码即可，无需改坐标。

> **说明**：Maven 宿主若只升 `common`，Gradle 可能仍解析到旧 `core-*` POM；请**显式**声明文首坐标，或重新 `publish` 后再 Sync。

### 2.2 架构对照（必读）

| | 旧（MVP 残存） | 现（1.2.0+） |
|--|----------------|-------------|
| 页面契约 | `BaseView` / 业务 `XxxView` | **已删除**；页面实现 `RequestUi`（`BaseActivity` / `BaseFragment` 已实现） |
| ViewModel | `BaseViewModel<IR, BV>`，`createRepository(view)`，`unbindView()` | `BaseViewModel<IR>`，无参 `createRepository()`，`ensureRepository()` |
| Repository | `IRepository<BV>` / `BaseRepository<BV>`，`setBaseView` / `getBaseView` | `IRepository` / `BaseRepository`，仅 `setRequestUi` / `getRequestUi` |
| 工厂 | `RepositoryFactory.create(..., baseView, api)` | **不再传页面**；`create(Class, api)` / `create(Class, dao)` 等 |
| 请求 UI | 仓库直接调 `baseView.showLoading` | 仓库只调 `getRequestUi()`；VM 持有 `NetworkRequestUiHost`，页面 `NetworkRequestUiBinder.bind` |
| 业务导航 | View 接口回调（如 `UserView.openMain()`） | `LiveData` / `SharedFlow`（登录示例：`PostLoginRoute`） |

数据流：

```text
Repository ──写──▶ NetworkRequestUiHost（RequestUi）──Binder──▶ Activity/Fragment（RequestUi）
业务状态 / 导航 ──LiveData / Flow──▶ 页面 observe 后自行处理
```

### 2.3 已删除 / 必须改的 API（编译期 breaking）

按 IDE 报错逐项改即可；典型替换如下。

#### ViewModel

```diff
- class XxxViewModel ... extends BaseViewModel<XxxRepositoryImpl, XxxActivity> {
-     override fun createRepository(view: XxxActivity) =
-         RepositoryFactory.create(XxxRepositoryImpl::class.java, view, api)
- }
+ class XxxViewModel ... extends BaseViewModel<XxxRepositoryImpl> {
+     override fun createRepository() =
+         RepositoryFactory.create(XxxRepositoryImpl::class.java, api)
+ }
```

- 删除对 `unbindView()` 的调用（若有）。
- 删除自定义 `XxxView` 接口；导航改成 VM 暴露 `LiveData` / `SharedFlow`，Activity/Fragment `observe`。

#### Repository

```diff
- class XxxRepositoryImpl(view: Any?, api: XxxApi) :
-     RepositoryImpl<XxxApi, Any>(view, api)
+ class XxxRepositoryImpl(api: XxxApi) :
+     RepositoryImpl<XxxApi>(api)
```

```diff
- baseView?.showLoading(...)
- getBaseView()?.showToast(...)
+ getRequestUi()?.showLoading("加载中…", true)
+ getRequestUi()?.showError(AppError.Unknown("提示文案"))
+ // 或主动 Toast：getNetworkRequestUiHost().showToast("提示文案")
```

分页 / Flow / Room 同理：类型参数里的「页面」位全部去掉，例如：

- `RepositoryImpl<API>` / `FlowRepositoryImpl<API>`
- `PagingRepositoryImpl<API, T, Q>` / `PagingFlowRepositoryImpl<API, T, Q>`
- `NetworkPagingRepository<T, Q>`
- `PagingViewModel<IR, T, Q>` / `FlowPagingViewModel` / `NetworkFlowPagingViewModel`（不再带 View 泛型）
- `RoomRepositoryImpl(dao)`、`AttachmentRepositoryImpl(dao)` —— **不要再传 view**

#### 页面

- 继承 `BaseActivity` / `BaseFragment` 即可：框架会 `ensureRepository()` + `bindNetworkRequestUi()`。
- 不要再 `implements BaseView` / 业务 View 接口。
- 自定义请求 UI：重写 `provideRequestUi()` 或空实现 `bindNetworkRequestUi()` 后自行 observe `getNetworkRequestUiHost()`。

#### commonui 表单

```diff
- formMedia.setBaseView(this)
+ formMedia.setRequestUi(mViewModel.getNetworkRequestUiHost())
```

应传 **ViewModel 的 Host**（或其它 `RequestUi` 实现），不要把 Activity 当仓库 UI 回调长期挂在控件上。

### 2.4 业务工程落地步骤（建议顺序）

1. **升依赖**到文首当前坐标（至少完成 BaseView 剔除当轮 API，再升 network/db 1.6.0），Clean + Sync。
2. **全局搜**（业务工程）：`BaseView`、`setBaseView`、`getBaseView`、`unbindView`、`createRepository(this`、`UserView`、`baseView:`、`BaseViewModel<.+,`。
3. **改签名**：所有 `BaseViewModel` / Repository / Factory / Room 构造按 §2.3 去掉页面类型与参数。
4. **改调用**：`baseView?.…` → `getRequestUi()?.…`（错误用 `showError(AppError)`）；表单 `setRequestUi(host)`。
5. **改导航**：删掉页面 View 接口，改为 LiveData/Flow（可参考本仓库 `LoginViewModel` + `PostLoginRoute` + `LoginActivity`）。
6. **编译**：先 `:app:assembleDebug`，再按模块修报错。
7. **回归**：冷启动请求 Loading/Toast、旋转屏后 Loading 不丢不漏、登录跳转、带 `FormMedia` 的上传页、分页列表、媒体选图/压缩加载框。

### 2.5 验证清单

- [ ] 工程内无 `import …BaseView`、无 `setBaseView` / `getBaseView`
- [ ] 所有 `createRepository()` 无页面参数；页面未再把 `this` 传给 Repository 构造
- [ ] 网络 / Room 请求仍出现 Loading，失败 Toast / 业务码（`showError`）正常
- [ ] 配置变更（旋转）后请求 UI 仍正常（Host 在 VM，页面只重新 bind）
- [ ] 登录或其它「跳转」不再通过 View 接口，而是 observe 事件
- [ ] 使用 commonui `FormMedia` 的页面已改为 `setRequestUi`
- [ ] 使用 commonmedia 时加载框样式正常

```bash
./gradlew :app:assembleDebug
# 可选：在业务工程搜残留
# rg "BaseView|setBaseView|getBaseView|unbindView|RequestUiCallback|onErrorCode|createRepository\\(this" --glob "*.{java,kt}"
```


### 2.6 请求 UI 统一为 RequestUi（删除 RequestUiCallback）

在 1.2.0「去掉 BaseView」之后，请求 UI 曾短暂并存 **`RequestUiCallback`（旧）** 与 **`RequestUi`（新）**。现已**只保留 `RequestUi`**，避免双契约。

| 已删除 | 替代 |
|--------|------|
| `RequestUiCallback` | `io.coderf.arklab.core.request.RequestUi` |
| `RequestUiAdapters` | 不再需要桥接 |
| `RequestUiBridge` | 不再需要桥接 |
| `provideRequestUiCallback()` | `provideRequestUi()` |
| `onErrorCode(BaseResponse)` | `showError(AppError)` / `onBusinessCode(code, message)` |
| `getRequestUi()?.showToast(msg)`（契约方法） | `getRequestUi()?.showError(AppError.Unknown(msg))` 或 `getNetworkRequestUiHost().showToast(msg)` |

数据流不变：

```text
Repository ──RequestUi──▶ NetworkRequestUiHost ──LiveData──▶ NetworkRequestUiBinder ──RequestUi──▶ 页面
```

页面仍实现 `RequestUi`：`BaseActivity` / `BaseFragment` 已实现 `showError`，业务码走登录过期 / 无权限；非业务错误走 Toast。分页基类等若曾 `override onErrorCode`，请改为 `override fun showError(error: AppError)`。


### 2.7 与 §1 / 网络迁移的关系

- 若宿主仍在 **AppCompat 主题线**，先完成 §1（升到 Material3 + 控件补丁），再做本节。
- 旧 `RepositoryImpl.sendRequest` 仍可用但已 `@Deprecated`；新代码优先 `DefaultNetworkRepository`，见 [core-network/MIGRATION.md](./core-network/MIGRATION.md)。**本节不强制迁网络栈**，只强制去掉页面绑定。
- network / db Options 行为见 [§0](#0-160network--db请求-options-完善)。

---

## 1. 4.6.0：AppCompat 主题 → Material3

这是一次 **主题与控件风格** 的大版本，不是再拆模块。官方路线是 **Views + Material Components for Android**，不是 Jetpack Compose。业务 API、页面结构、自定义 Dialog 形态与 AppCompat 工程对齐，观感保持不变。

> 若宿主已经在 Material3 + core-base **≥1.2.0**（无 `BaseView`），可跳过本节；版本请直接对齐文首矩阵，并视需要做 [§0](#0-160network--db请求-options-完善) / [§2](#2-120彻底剔除-baseview--mvp-页面绑定)。

### 1.1 版本号（相对 AppCompat 工程）

中间位 +1、补丁归零。文档里的坐标以 **build.gradle 发布号** 为准（4.5.x 文档曾落后于实际 `core-base 1.0.9` 等）。

| 模块 | AppCompat 线 | 4.6.0 落点（其后继续演进，**当前见文首**） |
|------|----------------|------------------------------------------------|
| common | 4.5.1 | **4.6.0** → 现 **4.7.0** |
| core-base | 1.0.9 | **1.1.0**（控件补丁曾至 1.1.3；BaseView 剔除 → 1.2.0；现 **1.5.9**） |
| core-network | 1.0.3 | **1.1.0** → 1.2.0 → 现 **1.6.0** |
| core-db / core-ui / core-utils | 1.0.2 | **1.1.0**（db 现 **1.6.0**；ui 现 **1.3.1**；utils **1.1.1**） |
| core-log | 1.0.0 | **1.1.0** → 现 **1.2.0** |
| room-processor | 1.0.1 | **1.1.0** → 现 **1.2.0** |
| core-mqtt | 1.5.1 | **1.6.0** → 现 **1.6.2** |
| commonui | 3.5.1 | **3.6.0** → 3.7.0 → 现 **3.7.3** |
| commonmedia | 3.3.2 | **3.4.0** → 3.5.0 → 现 **3.7.0** |
| googlegps | 3.1.7 | **3.2.0** → 现 **3.3.1** |
| annotation | 3.2.0 | **3.3.0** → 现 **3.3.1** |

Maven 示例（仅升主题时的历史坐标；**当前请以文首为准**）：

```gradle
implementation 'io.coderf.arklab.common:common:4.7.0'
implementation 'io.coderf.arklab.core:base:1.5.9'
implementation 'io.coderf.arklab.core:network:1.6.0'
```

### 1.2 主题（必须知悉）

| 项 | AppCompat 线 | 4.6.0 |
|----|----------------|--------|
| `AppBaseTheme` 父类 | `Theme.AppCompat.Light.NoActionBar` | `Theme.Material3.DayNight.NoActionBar` |
| 暗色 | 多为 `forceDarkAllowed=true` 系统强行转暗 | `forceDarkAllowed=false`，走 `values-night` tonal 色板 |
| Primary（亮） | `#1C50B5` | 同种子色，角色拆成完整 M3 token |
| Primary（暗） | 仍偏深品牌蓝 | `#A9C7FF`，字/图标用 `onPrimary`（`#002F67`） |
| Material 库 | 较低 | **1.14.0** |

宿主 Manifest 继续 `android:theme="@style/AppBaseTheme"` 即可，不要改回 `Theme.AppCompat.*`。

### 1.3 行为约定（API 尽量不变）

- **Toolbar**：页面壳为 `CoordinatorLayout > AppBarLayout > MaterialToolbar`（`ActionToolbar` / `TitleBar`），标题仍居中，右侧操作仍是 TextButton。对外 `ToolbarConfig` 未断。
- **圆角控件**：按钮 / 图片直接用 `MaterialButton`、`ShapeableImageView`（见 §1.6）。需要 XML `radius` / `bgColor` / `stroke*` 时用 `CornerTextView` / `CircleTextView` / `CornerConstraintLayout`。
- **`CirclePaddingImageView`**：圆形只做**背景**，`android:padding` 仍内缩图标。不要把它当成裁圆头像（头像用 `ShapeableImageView` + `@style/CircleShapeAppearance`）。
- **自定义确认框 / 底部 ActionSheet**：不改成 `MaterialAlertDialog` / 换 Dialog 父类，链式 API 保持。
- **动态取色（Material You）**：`Config.setDynamicColorEnabled(true)` 须在 `Config.init()` **之前**调用；**默认关**，品牌色不跟壁纸走。
- **屏幕适配 / 崩溃捕获 / 夜间模式 / Edge-to-Edge / 默认占位图**：均在 `Config` 上配置，须在 `init()` 前设置；默认行为与改前一致（适配开、崩溃捕获开且保留 5 天、跟随系统夜间、Edge-to-Edge 开、内置 `ic_default_image`）。接入 Bugly 等请 `setCrashHandlerEnabled(false)`。
- **登录输入框**：仍是自定义圆角底 + `TextInputEditText`，没有强行包 `TextInputLayout`（会改形态）。

### 1.4 宿主要改的（通常很少）

1. 依赖升到文首当前坐标，Clean 后重编译。
2. 自绘「品牌色底 + 写死白字」的地方，暗色下改成 `R.color.onPrimary` / `?attr/colorOnPrimary`（亮色 `onPrimary` 仍是白）。框架内 Tab 选中字、默认日历选中日、GPS 对话框 Primary 已按此处理。
3. 不要把相机 / 视频 / 裁剪叠层上的白字改成语义色。
4. Release 混淆：主题升级**不必改 ProGuard**；`material.**` 与 `io.coderf.arklab.common.**` 已 keep。minify 包请回归登录 JSON（`user.bean` 未单独 keep，属旧债）。

### 1.5 验证建议

- [ ] 亮色 / 暗色：页面底、字色、状态栏图标
- [ ] 普通页 Toolbar、搜索页（品牌底 + onPrimary 字）
- [ ] 登录按钮、圆角输入
- [ ] Tab 选中 pill、默认日历选中日（暗色浅底深字）
- [ ] GPS 确认按钮（暗色浅色 Primary）
- [ ] 相机 / 扫码按钮 padding 与按下态

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease   # minify 时加测登录
```

### 1.6 补丁：core-base 1.1.1 / commonui 3.6.1（历史）

这是 4.6.0 主题之后的 **控件落地**，不是再改主题父类。详细对照见 [core-base/README.md](core-base/README.md)、[commonui/README.md](commonui/README.md)。当前主干版本以文首为准。

| 已删除 | 替代 |
|--------|------|
| `CornerButton` | `MaterialButton`（`cornerRadius` / `backgroundTint` / `stroke*`） |
| `CornerImageView` / `RoundImageView` | `ShapeableImageView` + `shapeAppearanceOverlay`；填色用 **`android:background`**，不要 `backgroundTint` |
| `CornerEditText` / `CounterEditText` | `TextInputLayout` + `TextInputEditText` |

**仍保留**：`CornerTextView`、`CircleTextView`、`CornerConstraintLayout`（官方没有 XML 圆角属性，背景改为 `MaterialShapeDrawable`）；`ClearableEditText` / `PasswordEditText`（官方 TIL 无法同时清 + 眼）。

主题默认按钮 `inset` / `minHeight` 已清零。`commonui` 的 `FormConstraintLayout` / `FormMedia` 改为继承 `CornerConstraintLayout`。

---

## 附录 A：4.5.1（从 4.4.x 单体拆到 core）

> 以下是 **4.4.x → 4.5.1** 的历史说明。若工程已经在 4.5.1 / AppCompat 主题线上，只需看上文 §1（主题）、§2（MVVM）、§0（Options），不必再做附录里的模块拆分。

### A.1 当时升级摘要

| 类别 | 变更 |
|------|------|
| 模块 | 新增 `core-utils` / `core-base` / `core-network` / `core-db` / `core-ui`；`common` 改为 facade |
| 包名 | 业务仍使用 `io.coderf.arklab.common.*`（一般 **import 不用改**） |
| 网络 | 新增 `DefaultNetworkRepository` + `RequestResult`；旧 Repository **已 Deprecated，未删除** |
| 解耦 | `user` 去掉对 `mqttcomponent` / `commonmedia` 的直接依赖，改走 Gateway |
| 发布 | `common` 与各 `core-*` 补齐 `maven-publish`；混淆规则按模块拆到 `consumer-rules.pro` |

### A.2 宿主工程怎么升（4.4 → 4.5）

**仍用工程依赖：**

```gradle
implementation project(':common')
```

确保 `settings.gradle` 已 `include` 全部 `core-*` 与 `:common`。

**当时 Maven 坐标（已被后续版本取代，勿再新接入）：**

```gradle
implementation 'io.coderf.arklab.common:common:4.5.1'
```

必查：Clean 后重装（`InitProvider` 在 `core-base`）；宿主若拷贝过旧 Manifest，删掉错误包名的相对组件。

### A.3 代码迁移清单

**业务模块依赖：** 不要在业务 module 写 `mqttcomponent` / `commonmedia`，改 `@Inject MessageGateway` / `MediaGateway`（接口在 `:base`）。app 侧绑定见 `GatewayModule`、`MediaGatewayModule`。

**网络：** 旧 `sendRequest` 可编译但勿新增；新代码用 `repository.request(RequestOptions) { api.xxx() }`。

**安全：** 请求头不再带 `o-appSecret`；Release 不信任用户安装的 CA。

### A.4 验证（4.5 拆分）

```bash
./gradlew :app:assembleDebug
./gradlew :user:dependencies --configuration debugCompileClasspath
# 输出中不应出现 mqttcomponent、commonmedia
```

- [ ] 冷启动 / 旋转屏后 Loading、Toast 正常
- [ ] 底部 Tab「我的」、选图、登录 / 反馈媒体能力
- [ ] MQTT 连接与收发（若启用）

### A.5 当时已知未完成（后续版本仍部分存在）

| 项 | 说明 |
|----|------|
| `core-base` 二次削片 | widget / 重 utils 仍集中在此 |
| 旧网络 API | 仅 Deprecated，未删除 |
| 库模块自身 R8 | library 仍 `minifyEnabled false`，靠宿主 + consumer-rules |
| user Gson Bean keep | 开启 minify 时登录 JSON 需回归（见 §1.4） |
| 版本文档漂移 | 请以各模块 `build.gradle` 与本文文首矩阵为准，勿死盯过期 README 号 |
