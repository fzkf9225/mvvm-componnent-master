# 升级与迁移说明

> 当前主干：**common 4.6.0 + core-base 1.1.1 + 其余 core-\* 1.1.0 + commonui 3.6.1**（Views + Material Components 1.14）。  
> 从 **AppCompat 主题线**（common 4.5.1 / core-base 1.0.9）升级：看下面 §1。  
> 控件包装类删除与官方替代见 **§1.6**（core-base 1.1.1）。  
> 仍停留在 **common 4.4.x 单体**：先完成 [附录 A](#附录-a4451从-444x-单体拆到-core)，再看 §1。  
> 模块职责与日常用法见 [MODULES.md](./MODULES.md)。

---

## 1. 4.6.0：AppCompat 主题 → Material3

这是一次 **主题与控件风格** 的大版本，不是再拆模块。官方路线是 **Views + Material Components for Android**，不是 Jetpack Compose。业务 API、页面结构、自定义 Dialog 形态与 AppCompat 工程对齐，观感保持不变。

### 1.1 版本号（相对 AppCompat 工程）

中间位 +1、补丁归零。文档里的坐标以 **build.gradle 发布号** 为准（4.5.x 文档曾落后于实际 `core-base 1.0.9` 等）。

| 模块 | AppCompat 线 | 现（Material3） |
|------|----------------|-----------------|
| common | 4.5.1 | **4.6.0** |
| core-base | 1.0.9 | **1.1.0**（控件补丁 **1.1.1**，见 §1.6） |
| core-network | 1.0.3 | **1.1.0** |
| core-db / core-ui / core-utils | 1.0.2 | **1.1.0** |
| core-log | 1.0.0 | **1.1.0** |
| room-processor | 1.0.1 | **1.1.0** |
| core-mqtt | 1.5.1 | **1.6.0** |
| commonui | 3.5.1 | **3.6.0**（随控件补丁 **3.6.1**） |
| commonmedia | 3.3.2 | **3.4.0** |
| googlegps | 3.1.7 | **3.2.0** |
| annotation | 3.2.0 | **3.3.0** |

Maven 示例：

```gradle
implementation 'io.coderf.arklab.common:common:4.6.0'
// 一般由 common POM 传递；解析不全时可显式：
// implementation 'io.coderf.arklab.core:base:1.1.1'
// implementation 'io.coderf.arklab.core:network:1.1.0'
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

### 1.6 补丁：core-base 1.1.1 / commonui 3.6.1

这是 4.6.0 主题之后的 **控件落地**，不是再改主题父类。详细对照见 [core-base/README.md](core-base/README.md)、[commonui/README.md](commonui/README.md)。

| 已删除 | 替代 |
|--------|------|
| `CornerButton` | `MaterialButton`（`cornerRadius` / `backgroundTint` / `stroke*`） |
| `CornerImageView` / `RoundImageView` | `ShapeableImageView` + `shapeAppearanceOverlay`；填色用 **`android:background`**，不要 `backgroundTint` |
| `CornerEditText` / `CounterEditText` | `TextInputLayout` + `TextInputEditText` |

**仍保留**：`CornerTextView`、`CircleTextView`、`CornerConstraintLayout`（官方没有 XML 圆角属性，背景改为 `MaterialShapeDrawable`）；`ClearableEditText` / `PasswordEditText`（官方 TIL 无法同时清 + 眼）。

主题默认按钮 `inset` / `minHeight` 已清零。`commonui` 的 `FormConstraintLayout` / `FormMedia` 改为继承 `CornerConstraintLayout`。

Maven：`io.coderf.arklab.core:base:1.1.1`、`io.coderf.arklab.ui:ui:3.6.1`。

---

## 附录 A：4.5.1（从 4.4.x 单体拆到 core）

> 以下是 **4.4.x → 4.5.1** 的历史说明。若工程已经在 4.5.1 / AppCompat 主题线上，只需看上文 §1，不必再做附录里的模块拆分。

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

**当时 Maven 坐标（已被 4.6.0 取代，勿再新接入）：**

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

### A.5 当时已知未完成（4.6.0 仍部分存在）

| 项 | 说明 |
|----|------|
| `core-base` 二次削片 | widget / 重 utils 仍集中在此 |
| 旧网络 API | 仅 Deprecated，未删除 |
| 库模块自身 R8 | library 仍 `minifyEnabled false`，靠宿主 + consumer-rules |
| user Gson Bean keep | 开启 minify 时登录 JSON 需回归（见 §1.4） |
