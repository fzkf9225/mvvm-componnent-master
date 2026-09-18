# core-base

框架核心 UI / MVVM 基座：Activity / Fragment / ViewModel、历史 widget、helper，以及 **common 体系的资源与 DataBinding**（R 包名保持 `io.coderf.arklab.common`）。

当前版本：**1.2.0**  
Maven：`io.coderf.arklab.core:base:1.2.0`  
namespace：`io.coderf.arklab.common`

> 注意：本模块 ≠ Demo case 的 Gradle `:base`。Maven 坐标是 `core:base`，对应工程模块是 **`:core-base`**。  
> 业务工程升级（剔除 `BaseView`）见 [UPGRADE.md §2](../UPGRADE.md#2-120彻底剔除-baseview--mvp-页面绑定)。

---

## 版本
### 1.2.6
`Config`新增配置
开启（须在 init 前）：
```kotlin
Config.getInstance()
    .setStableImageCacheKeyEnabled(true)
    .addStableImageCacheIgnoredQueryParams("X-Amz-Signature")
    .init(this)
```
通过`Glide`特性注册`prepend 包装器`，解决`MinIO`等一些文件服务中服务地址每次都会变化导致`Glide`缓存测试无法选中的问题，但是其实只是签名等一些算法变化其实是同一张图片，通过配置可以重构缓存策略达到签名等变化可以识别缓存的策略算法

### 1.2.0（相对 1.1.3）

彻底剔除 MVP 页面绑定：删除 `BaseView`；`BaseViewModel` / `IRepository` / `BaseRepository` 不再带页面泛型；请求 UI 经 `NetworkRequestUiHost` + `NetworkRequestUiBinder` 落到页面的 **`RequestUi`**。

已删除旧版 `RequestUiCallback` 及 `RequestUiAdapters`；全链路只使用 `io.coderf.arklab.core.request.RequestUi`（`showLoading` / `hideLoading` / `refreshLoading` / `showError(AppError)` / `onBusinessCode`）。

### 1.1.1（相对 1.1.0）

Material3 控件落地补丁：官方已经能表达的圆角 / 描边不再包一层；官方没有 XML 圆角属性的三个控件改为 `MaterialShapeDrawable`。

**删除（请改用官方控件）**

| 已删除 | 替代 |
|--------|------|
| `CornerButton` | `MaterialButton`：`app:cornerRadius` / `app:backgroundTint` / `app:strokeColor` / `app:strokeWidth` |
| `CornerImageView` | `ShapeableImageView` + `app:shapeAppearanceOverlay`（如 `RoundedShapeAppearanceL`） |
| `RoundImageView` | `ShapeableImageView` + `@style/CircleShapeAppearance` |
| `CornerEditText` / `CounterEditText` | `TextInputLayout` + `TextInputEditText`（密铺可用 `Widget.App.TextInputLayout.Dense`） |

`ShapeableImageView` **填色用 `android:background` / `setBackground()`**。`app:backgroundTint` 只给已有 background 着色，没有 background 时无效（`MaterialButton` 自带 shape 背景，tint 仍然有效）。

**保留并改为 Material3 背景**

- `CornerTextView`、`CircleTextView`、`CornerConstraintLayout`：XML `app:radius` / `app:bgColor` / `app:stroke*`，背景为 `MaterialShapeDrawable`
- `CircleTextView` 文字按圆心 `FontMetrics` 居中，不再 `onDraw` 画圆
- `ClearableEditText` / `PasswordEditText`：官方 `endIconMode` 无法同时清 + 眼、图标尺寸也不同，继续用 compound drawable
- `GridMenuView` 继承 `CornerConstraintLayout`

**其它**

- `CornerShapeHelper`：给 `View` / `ShapeableImageView` / `MaterialButton` 铺圆角；XML 可走 `ShapeView` attrs
- 主题默认 `materialButtonStyle` → `Widget.App.Button`：`insetTop/Bottom=0`、`minHeight=0`，`layout_height` 即视觉高度；Dialog 铺满可用 `Widget.App.Button.Flush`
- `Widget.App.Card.Flush`、`Widget.App.TextInputLayout.Dense`

### 1.1.0（相对 AppCompat 1.0.9）

`AppBaseTheme` 为 Material3 DayNight；Toolbar 为 `AppBarLayout` + `MaterialToolbar`。详见仓库 [UPGRADE.md](../UPGRADE.md)。

---

## 职责

- `BaseActivity` / `BaseFragment` / `BaseViewModel` 等基类
- Toolbar（`ActionToolbar` / `TitleBar`）、Loading、通用工具与大量历史 UI 组件
- 统一资源、M3 色板（含 `values-night`）与 DataBinding（业务 `R` / Binding 仍指向本模块包名）

---

## 依赖

- `api` → `:core-utils`、`:core-ui`、`:core-log`

业务侧通常通过 `:common` facade 间接依赖，无需单独拆 R。

---

## 何时依赖

需要基类 / 通用 UI / DataBinding 资源时（推荐经 `common`）：

```gradle
implementation 'io.coderf.arklab.core:base:1.2.0'
// 或
implementation project(':core-base')
```

---

## 发布

```bash
./gradlew :core-base:publish
```
