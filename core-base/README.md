# core-base

框架核心 UI / MVVM 基座：Activity / Fragment / ViewModel、历史 widget、helper，以及 **common 体系的资源与 DataBinding**（R 包名保持 `io.coderf.arklab.common`）。

当前版本：**2.0.0**  
Maven：`io.coderf.arklab.core:base:2.0.0`  
namespace：`io.coderf.arklab.common`

> 注意：本模块 ≠ Demo case 的 Gradle `:base`。Maven 坐标是 `core:base`，对应工程模块是 **`:core-base`**。  
> 业务工程升级见 [MIGRATION-2.0.0.md](../MIGRATION-2.0.0.md)、[UPGRADE.md](../UPGRADE.md)；协程下载见 [UPGRADE.md §0.2](../UPGRADE.md#02-协程下载真内核base-1100)；Loading 可取消见 [QuickStart.md](../QuickStart.md)。

---

## 2.0.0

**破坏性**（须与 **network / db 2.0.0** 同升）。业务迁移见仓库根目录 [MIGRATION-2.0.0.md](../MIGRATION-2.0.0.md)。

| 项 | 说明 |
|----|------|
| ViewModel | 无泛型 `BaseViewModel`；删除 `createRepository` / `ensureRepository` / `launchUiRequest` |
| 请求入口 | `launchRequest`：推荐 Flow 重载（强制 Host）；多步用 Session + `collectSuccess` |
| 可见性 | `trackUiRequest` → `protected` |
| Loading 取消 | 仅系统返回 / 手势返回；点外侧不取消 |
| Options | `RequestOptions` UI 字段仅对 `launchRequest` / `collect*` 生效 |

---

## 版本

### 1.10.0

文件下载 / APK 更新：**协程真内核**（与网络栈双轨策略对齐，不是 Rx 外包一层 Flow）：

| 能力 | 说明 |
|------|------|
| `DownloadClient` | 新推荐入口：`download` / `downloadAwait` / `downloadBatch*`，调度 / 权限 / 写盘不经 RxJava |
| `DownloadEngine` + `DownloadWriter` + `DownloadCoroutineApi` | suspend 拉流 + 协程写盘、断点续传、通知栏进度 |
| `DownloadHttpClients` / `DownloadFileNames` | OkHttp·Retrofit 构建与文件名解析，**Rx / 协程双栈共用** |
| `DownloadOptions` + 可选 `RequestUi` | 按次注入（勿绑单例）；失败可自动 `showError`；默认无全屏 Loading（通知栏即可） |
| `UpdateManager`（Kotlin） | 静默 / 对话框更新委托 `DownloadClient`；`update()` 返回 `Job`（原 `Disposable`） |
| `DownloadManager`（Rx） | **冻结保留**，存量 `.subscribe` 可继续用；新代码请走 Client |

推荐写法（业务通常只处理成功）：

```kotlin
launchUiRequest {
    val file = DownloadClient.getInstance().downloadAwait(
        config,
        DownloadOptions.defaults(),
        networkRequestUiHost // 失败自动 Toast；取消不报错
    )
}
```

兼容说明见 [UPGRADE.md §0.2](../UPGRADE.md#02-协程下载真内核base-1100)。

### 1.9.0

Loading 可取消闭环 + 遮罩引用计数（配合 **core-network ≥1.8.0** / **core-db ≥1.7.0**）：

- `LoadingCancelPolicy`：`None` / `CancelRequest` / `CancelRequestAndExit`
- `NetworkRequestUiHost`：Loading 引用计数；`forceHideLoading()`；`RequestLoadingState` 携带 cancelPolicy
- `BaseViewModel`：`launchRequest`（Flow 重载 / Session）/ `cancelUiRequests`；`trackUiRequest` 为 protected
- `LoadingDelegate` / `UIController` / `LoadingProgressDialog`：可取消 + OnCancel → 页面 `onLoadingCancelled`
- `BaseActivity` / `BaseFragment`：取消后按策略留页或 finish / 回退
- `ApiRequestOptions`：`loadingCancelPolicy` + `cancellable()` / `cancellableExit()`
- **默认仍为不可取消**（`None`），兼容历史行为

### 1.7.2

附件列表小图优先缩略图（配合 **core-form ≥3.7.7**）：

- `AttachmentBean.getDisplayPath(preferThumbnail)` / `AttachmentUtil.getDisplayPath`
- `BaseMediaRecyclerViewAdapter.preferThumbnail`（默认 `true`）；列表 Add/Show Adapter 的 Glide 改走展示地址
- 大图 ViewPager 仍加载 `path`；底部相册条优先 `thumbnailPath`

详见 [UPGRADE.md §0.4.3](../UPGRADE.md#043-附件小图优先缩略图base-172--ui-377)。

### 1.7.1

附件实体简化（**破坏性**，需配合 **core-db ≥1.6.2**）：

- `AttachmentBean` 删除 `mainId` / `fieldName`（业务侧持有 `mobileId`，服务端用 `fileId`）
- `AttachmentUtil` 去掉 `mainId`/`field` 参数与 `coverAttachmentList`；新增 `parseIds` / `normalizeIds` / `getFileIds`

详见 [UPGRADE.md §0.4.2](../UPGRADE.md#042-附件模型简化base-171--db-162)、[core-db/README.md](../core-db/README.md)。

### 1.7.0

- 迁移部分工具类至 `core-utils`

### 1.6.0

- M3 night 状态色对齐；Dialog 按钮 style 与页面对齐
- `ToolbarConfig.setLightMode` 废弃 → `setStatusBarIconsLight`
- `DensityUtil` 弃用 `scaledDensity`；软键盘监听可销毁；删除死代码 `SoftHideKeyBoardUtil`

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
- 文件下载 / APK 更新：`DownloadClient`（协程，推荐）与 `DownloadManager`（Rx，存量）

---

## 依赖

- `api` → `:core-utils`、`:core-ui`、`:core-log`、`:core-autosize`

业务侧通常通过 `:common` facade 间接依赖，无需单独拆 R。
屏幕适配实现已下沉到 [`core-autosize`](../core-autosize/README.md)（包名 `io.coderf.arklab.core.autosize`）。

---

## 何时依赖

需要基类 / 通用 UI / DataBinding 资源时（推荐经 `common`）：

```gradle
implementation 'io.coderf.arklab.core:base:1.10.0'
// 或
implementation project(':core-base')
```

---

## 发布

```bash
./gradlew :core-base:publish
```
