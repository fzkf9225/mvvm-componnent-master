# 升级与迁移说明

> 当前主干：**core-base / core-network / core-db / core-ui `1.2.0`** + **core-utils `1.1.1`** + **commonui `3.7.0`** + **commonmedia `3.5.0`**（common facade 仍为 **`4.6.0`**，主题仍为 Material3 DayNight）。  
> **本次业务落地重点**：看下面 [§2](#2-120彻底剔除-baseview--mvp-页面绑定)（去掉 `BaseView` / 页面泛型，真正 MVVM）。  
> 从 **AppCompat 主题线**（common 4.5.1 / core-base 1.0.9）升级：先看 [§1](#1-460appcompat-主题--material3)，再看 §2。  
> 控件包装类删除见 §1.6。仍停留在 **common 4.4.x 单体**：先完成 [附录 A](#附录-a4451从-444x-单体拆到-core)。  
> 模块职责与日常用法见 [MODULES.md](./MODULES.md)；网络新 API 见 [core-network/MIGRATION.md](./core-network/MIGRATION.md)。

---

## 2. 1.2.0：彻底剔除 BaseView / MVP 页面绑定

这是一次 **架构契约** 大版本（相对上一档 core-base **1.1.3** / commonui **3.6.2** / commonmedia **3.4.1**），不是主题变更。目标：ViewModel / Repository **不再持有页面**，请求 Loading / 错误展示只走 **`RequestUi` + `NetworkRequestUiHost`**；业务导航用 LiveData / Flow。后续已进一步删除旧版 `RequestUiCallback`，全链路统一为 `RequestUi`。

### 2.1 版本号

| 模块 | 上一档 | 现 |
|------|--------|-----|
| common（facade） | 4.6.0 | **4.6.0**（本轮未改发布号） |
| core-base | 1.1.3 | **1.2.0** |
| core-network | 1.1.0 | **1.2.0** |
| core-db | 1.1.0 | **1.2.0** |
| core-ui | 1.1.0 | **1.2.0** |
| core-utils | 1.1.0 | **1.1.1**（仅注释，无 API 变更） |
| commonui | 3.6.2 | **3.7.0** |
| commonmedia | 3.4.1 | **3.5.0**（加载框样式兼容） |

未在本表内的模块（core-log / room-processor / core-mqtt / googlegps / annotation 等）本轮**未**因 BaseView 剔除而升版，沿用既有坐标即可。

Maven 示例（推荐显式钉住本次升版模块，避免只升 `common:4.6.0` 时传递依赖仍解析到旧 core）：

```gradle
implementation 'io.coderf.arklab.common:common:4.6.0'
implementation 'io.coderf.arklab.core:base:1.2.0'
implementation 'io.coderf.arklab.core:network:1.2.0'
implementation 'io.coderf.arklab.core:db:1.2.0'
implementation 'io.coderf.arklab.core:ui:1.2.0'
implementation 'io.coderf.arklab.core:utils:1.1.1'
// 用到表单 / 媒体时：
implementation 'io.coderf.arklab.ui:ui:3.7.0'
implementation 'io.coderf.arklab.media:media:3.5.0'
```

工程依赖 `implementation project(':common')` 时随源码即可，无需改坐标。

> **说明**：`common` 本轮未升 4.7.0。若私服上仍是旧 POM 的 `common:4.6.0`，Gradle 可能继续解析到旧 `core-*`；业务工程请**显式**声明上表坐标，或重新 `publish` 各 `core-*` / `commonui` / `commonmedia` 后再 Sync。

### 2.2 架构对照（必读）

| | 旧（MVP 残存） | 现（1.2.0） |
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

1. **升依赖**到 §2.1 坐标，Clean + Sync。
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
- [ ] 使用 commonmedia 时加载框样式正常（3.5.0）

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

---

## 1. 4.6.0：AppCompat 主题 → Material3

这是一次 **主题与控件风格** 的大版本，不是再拆模块。官方路线是 **Views + Material Components for Android**，不是 Jetpack Compose。业务 API、页面结构、自定义 Dialog 形态与 AppCompat 工程对齐，观感保持不变。

> 若宿主已经在 Material3 + core-base **1.1.x**，可跳过本节，直接做 [§2](#2-120彻底剔除-baseview--mvp-页面绑定)。

### 1.1 版本号（相对 AppCompat 工程）

中间位 +1、补丁归零。文档里的坐标以 **build.gradle 发布号** 为准（4.5.x 文档曾落后于实际 `core-base 1.0.9` 等）。

| 模块 | AppCompat 线 | 4.6.0 落点（其后还有补丁，见 §1.6；当前主干见文首 / §2.1） |
|------|----------------|--------------------------------------------------------------|
| common | 4.5.1 | **4.6.0** |
| core-base | 1.0.9 | **1.1.0**（控件补丁曾至 **1.1.3**，现见 §2 → **1.2.0**） |
| core-network | 1.0.3 | **1.1.0**（现 **1.2.0**） |
| core-db / core-ui / core-utils | 1.0.2 | **1.1.0**（db/ui 现 **1.2.0**；utils 现 **1.1.1**） |
| core-log | 1.0.0 | **1.1.0** |
| room-processor | 1.0.1 | **1.1.0** |
| core-mqtt | 1.5.1 | **1.6.0** |
| commonui | 3.5.1 | **3.6.0**（补丁 **3.6.x**，现见 §2 → **3.7.0**） |
| commonmedia | 3.3.2 | **3.4.0**（现 **3.5.0**） |
| googlegps | 3.1.7 | **3.2.0** |
| annotation | 3.2.0 | **3.3.0** |

Maven 示例（仅升主题时；当前请以 §2.1 为准）：

```gradle
implementation 'io.coderf.arklab.common:common:4.6.0'
// 一般由 common POM 传递；解析不全时可显式：
// implementation 'io.coderf.arklab.core:base:1.2.0'
// implementation 'io.coderf.arklab.core:network:1.2.0'
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

1. 依赖升到上表坐标，Clean 后重编译。
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

这是 4.6.0 主题之后的 **控件落地**，不是再改主题父类。详细对照见 [core-base/README.md](core-base/README.md)、[commonui/README.md](commonui/README.md)。当前主干版本以 §2.1 为准。

| 已删除 | 替代 |
|--------|------|
| `CornerButton` | `MaterialButton`（`cornerRadius` / `backgroundTint` / `stroke*`） |
| `CornerImageView` / `RoundImageView` | `ShapeableImageView` + `shapeAppearanceOverlay`；填色用 **`android:background`**，不要 `backgroundTint` |
| `CornerEditText` / `CounterEditText` | `TextInputLayout` + `TextInputEditText` |

**仍保留**：`CornerTextView`、`CircleTextView`、`CornerConstraintLayout`（官方没有 XML 圆角属性，背景改为 `MaterialShapeDrawable`）；`ClearableEditText` / `PasswordEditText`（官方 TIL 无法同时清 + 眼）。

主题默认按钮 `inset` / `minHeight` 已清零。`commonui` 的 `FormConstraintLayout` / `FormMedia` 改为继承 `CornerConstraintLayout`。

---

## 附录 A：4.5.1（从 4.4.x 单体拆到 core）

> 以下是 **4.4.x → 4.5.1** 的历史说明。若工程已经在 4.5.1 / AppCompat 主题线上，只需看上文 §1（主题）与 §2（MVVM），不必再做附录里的模块拆分。

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
| common 发布号 | 本轮 MVVM 剔除未升 common，Maven 宿主需显式钉 core-\*（见 §2.1） |
