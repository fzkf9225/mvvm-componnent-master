# mvvm-componnent-master
# 文档索引

| 文档 | 说明 |
|------|------|
| [MODULES.md](MODULES.md) | 各模块职责、依赖关系、Maven 坐标、Gateway / 新网络 API 用法 |
| [UPGRADE.md](UPGRADE.md) | **§2：core-* 1.2.0 剔除 BaseView / MVP**；§1：4.6.0 Material3；附录：4.4.x → 4.5.1 拆分 |
| [QuickStart.md](QuickStart.md) | 五分钟接入（依赖、Manifest、主题、配置、页面脚手架） |
| [core-base/README.md](core-base/README.md) | 基座模块；控件与主题补丁 |
| [commonui/README.md](commonui/README.md) | 表单 / 日历等 |

当前主干版本：**common `4.6.0`（facade）+ core-base/network/db/ui `1.2.0` + core-utils `1.1.1` + commonui `3.7.0` + commonmedia `3.5.0`**，主题为 **Material3 DayNight**（Views + MDC 1.14）。业务侧仍可依赖 `common`，但 Maven 请按 [UPGRADE.md §2.1](UPGRADE.md#21-版本号) **显式钉住**本次升版的 core / ui / media。包名多为 `io.coderf.arklab.common.*`。相对 AppCompat 工程与本次 MVVM 契约变更见 [UPGRADE.md](UPGRADE.md)。

**1.2.0**：删除 `BaseView` 与 Repository/ViewModel 上的页面泛型；请求 UI 走 `NetworkRequestUiHost` + **`RequestUi`**（已删除旧版 `RequestUiCallback`）；业务导航用 LiveData/Flow。

**1.1.x / 3.6.x 控件补丁**（历史）：官方已有圆角、描边的包装类已去掉，改用 `MaterialButton` / `ShapeableImageView` / `TextInputLayout`。详情见 [core-base/README.md](core-base/README.md)。

# 框架简介
框架全面采用`MVVM`架构结合JetPack全家桶进行封装，框架中主要封装了常用功能，比如网络请求、数据库、数据存储、工具类、UI组件、业务逻辑封装等等,其中90%为`Java`,10%为`Kotlin`，最低兼容到`Android 8` ，`targetSdk` 对齐 Demo（当前 35）。
从架构上根本的进行拆解，充分发挥解耦的思路，从原始的`Activity`干所有事件，拆分成各个分开的模块，发挥`ViewModel`特性专门使用`Repository`进行数据处理，接口通过`Hilt`依赖注入的方式进行一键注入省去大量的`New`操作。

**4.5.0 起**：原单体 `common` 拆为 `core-utils` / `core-base` / `core-network` / `core-db` / `core-ui`，`common` 变为 **facade**（`api` 聚合 core）。MQTT / 媒体等实现库经 **Gateway**（case 模块 `:base`）注入，业务模块不要直接依赖 `mqttcomponent` / `commonmedia`。

**4.6.0**：`AppBaseTheme` 从 AppCompat Light 升级为 `Theme.Material3.DayNight.NoActionBar`；暗色走 `values-night` tonal 色板（关闭系统 `forceDarkAllowed`）。控件仍是 Views，不是 Compose。对外 API 与旧工程对齐，详见 [UPGRADE.md](UPGRADE.md)。

**core-base 1.1.1 / commonui 3.6.1**：在 4.6.0 主题之上做控件落地——能用官方 Material3 控件的不再包一层；需要 XML `radius` / `bgColor` / `stroke*` 的三个容器/文字控件改为 `MaterialShapeDrawable`。主题默认按钮 `inset`/`minHeight` 清零，`layout_height` 即视觉高度。

主要封装功能：
1. 基础`BaseActivity`、`BaseFragment`主要的页面UI的基础类（位于 `core-base`）
2. 自动集成`今日头条UI适配方案`
3. 网络请求封装：`OkHttp`+`RxJava`+`Retrofit`，以及 `Flow`；**推荐**新 API `DefaultNetworkRepository`（旧 `RepositoryImpl` / `FlowRepositoryImpl` 已 `@Deprecated`）
4. 本地数据库封装，使用`Room`+`RxJava` / `Flow`（`core-db`）
5. 轻量化存储框架，使用`MMKV`，替换自带的`SharedPreferences`
6. 集成`Paging3`分页，更加智能化的分页列表，同时也提供原始的分页功能
7. 拆分`Media`模块`commonmedia`，专门处理媒体数据；业务侧注入 `MediaGateway`
8. 拆分常用表单组件，单独module`commonui`，比如表单校验，表单验证，表单提交等等
9. 拆分单独的`GPS`服务处理模块`googlegps`，提供GPS服务，从检测GPS是否打开到获取权限到获取经纬度一键集成
10. 使用`properties`配置文件，配置文件会自动拆分环境，防止频繁切换环境导致的一系列问题
11. 单独拆分`annotation`模块，一键注解校验字段表单参数是否满足约束条件
12. 架构上通过模块拆分 + Gateway 解耦，业务只依赖 `*api`，实现由 app 组装层绑定
13. Material3 主题与 Top App Bar / 按钮 / 圆角色（`AppBaseTheme`，动态取色默认关）

# 工程介绍
    - **core-utils**：无 R 依赖的轻量工具（预留分层）
    - **core-base**：Base* / widget / helper / res（namespace 仍为 `io.coderf.arklab.common`）；主题与 Material 控件入口在此
    - **core-network**：Retrofit / Repository / `DefaultNetworkRepository`
    - **core-db**：Room DAO / Database / RoomRepository
    - **core-ui**：Activity 委托（如 `InitDataPolicy`）
    - **common**：兼容门面（facade），业务推荐只依赖它
    - annotation：注解框架，主要用于数据校验的注解
    - app：框架 case 示例（含 Gateway Hilt 绑定）
    - commonmedia：摄像头、相册、媒体相关封装（不含 Gateway）
    - commonui：UI组件封装，主要为表单组件
    - googlegps：GPS工具类
    - mqttcomponent：MQTT 封装（不含 Gateway）
    - base：Demo/case 基建（Gateway、AppPropertiesConfig、BaseAppActivity 等；**非框架**，勿与 core-base 混淆）
    - userapi：用户模块对外接口（UserService / UserInfo / Router）
    - user：示例业务 module（依赖 common + base + userapi）
    - wscomponent：WebSocket最基础的封装示例
    - room-processor：`@RoomObservedEntity` KSP 处理器

全面使用``ksp``

# 五分钟快读开始
[五分钟快读开始](QuickStart.md)（文内已补充 **请求 UI（`NetworkRequestUiHost` / `RequestUi`）** 说明与示例。）  
从旧版升级请先读 [UPGRADE.md](UPGRADE.md)。

## MVVM架构示例代码，重构版本

### 业务侧依赖入口：继续用 `common`（4.6.0 facade）

#### 在线引用（Maven）

仓库与凭证同前（`ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`）。推荐坐标：

```gradle
implementation 'io.coderf.arklab.common:common:4.6.0'
// 一般可由 common POM 传递；若解析不全可显式补：
// implementation 'io.coderf.arklab.core:base:1.1.1'
// implementation 'io.coderf.arklab.core:network:1.1.0'
// implementation 'io.coderf.arklab.core:db:1.1.0'
// implementation 'io.coderf.arklab.core:ui:1.1.0'
// implementation 'io.coderf.arklab.core:utils:1.1.0'
```

本仓库发布：`./gradlew :common:publish` / `:core-*:publish`（详见 [MODULES.md](MODULES.md)）。

工程内依赖：

```gradle
implementation project(':common')
implementation project(':base')      // Gateway / 配置 / BaseApp*
implementation project(':userapi')   // 需要用户契约时
```

### mqttcomponent / commonmedia / websocket

实现库仍可单独依赖，但 **业务 module 不要直接依赖**。应注入：

- `MessageGateway`（MQTT）
- `MediaGateway`（媒体）

在 **app** 中 Hilt 绑定实现（参考 `GatewayModule`、`MediaGatewayModule`）。接口在 **`:base`**。

### userapi

为 **user 模块** 的公共 api：接口、实体、路由。不要放 Gateway / 全局配置。

### base（case）

项目基础模块样板：Gateway、`AppPropertiesConfig`、`PropertiesKeyEnum`、`FileTypeEnum`、`BasePage`、`BaseAppActivity` 等。各宿主可按 serm-app 的 `:base` 思路扩展字典、文件服务等，**不要把业务字典硬塞进框架**。

### user

组件化示例：依赖 `common` + `base` + `userapi`；其他业务 module 不要依赖 `user` 实现模块。
## 封装思路博文，注意观看顺序哈

一：https://blog.csdn.net/fzkf9225/article/details/105197996

二：https://blog.csdn.net/fzkf9225/article/details/105200803

三：https://blog.csdn.net/fzkf9225/article/details/132182449

## 查看网络请求等相关日志

### 初始化和打开日志

在app中的Application中调用初始化方法

```
        // 以下均须在 init 之前；不写则用默认值
        // Config.getInstance().setDynamicColorEnabled(true);          // Material You，默认关
        // Config.getInstance().setAutoSizeEnabled(false);             // 屏幕适配，默认开
        // Config.getInstance().setCrashHandlerEnabled(false);         // 自带崩溃捕获，默认开
        // Config.getInstance().setCrashLogRetainDays(7);              // 崩溃日志保留天数，默认 5；<=0 不清理
        // Config.getInstance().setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        // Config.getInstance().setEdgeToEdgeEnabled(false);           // BaseActivity 全局默认，默认开
        // Config.getInstance().setDefaultPlaceholderRes(R.drawable.my_placeholder);
        // Config.getInstance().setDefaultErrorImageRes(R.drawable.my_error);
        Config.getInstance().init(this);
        if (BuildConfig.LOG_DEBUG) {
            Config.getInstance().enableDebug(true);
        }
```
### 常用Api

#### 系统配置
1. 继承`BaseApplication`，然后初始化`Config.getInstance().init(this)`；屏幕适配、崩溃捕获、夜间模式、Edge-to-Edge、占位图等全局开关须在 `init` 前设置。如果需要获取全局的`Application`对象可以调用`Config`的方法
2. `ErrorService`：提供全局的api，处理网络请求回调的一些错误和跳转登录拦截等问题，还有统一网络请求头的配置等

#### UI相关
1. 普通`Activity`继承`BaseActivity`即可
2. 普通`Fragment`继承`BaseFragment`即可
3. 普通`Adapter`继承`BaseRecyclerViewAdapter`即可
4. 如果需要`pinging`分页的`Adapter`的话，继承`BasePagingAdapter`
5. 原始的列表自带的下拉刷新，`paging`分页可以继承`BasePagingFragment`
6. 不带`pinging`分页的列表`Fragment`继承`BaseRecyclerViewFragment`
7. 最常用的分页列表`Fragment`，支持多样式刷新，加载更多，`paging3`分页继承`BaseSmartPagingFragment`
8. 请求侧 UI（加载框 / 错误 / 业务码）由 `BaseActivity` / `BaseFragment` 实现 **`RequestUi`**，经 `NetworkRequestUiBinder` 订阅 ViewModel 内 `NetworkRequestUiHost`；错误统一 `showError(AppError)`；业务导航用 LiveData/Flow（如登录 `PostLoginRoute`），ViewModel/Repository 不持有页面
9. `adapter`如果需要实现自定义的事件控件等，可以自定义`BaseViewHolder`，然后在adapter中重写createViewHold方法
10. 普通`ViewModel`可以直接继承`BaseViewModel`
11. 分页列表的`ViewModel`继承`PagingViewModel`
12. 支持tab标签侧滑功能的`Fragment`继承`BaseViewPagerFragment`，如果不需要`Fragment`的话直接`Activity`继承`BaseViewPagerActivity`
13. 自定义相机可以直接调用`CameraActivity`，视频播放可以直接调用`VideoPlayerActivity`，二维码扫描可以直接调用`CaptureActivity`，WebView可以直接调用`WebViewActivity`

#### 数据存储和网络请求相关
1. 网络请求：新代码推荐继承 / 使用 `DefaultNetworkRepository`（`core-network`，返回 `Flow<RequestResult<T>>`）。旧 `RepositoryImpl` / `FlowRepositoryImpl` 仍可用但已 `@Deprecated`，勿再新增调用。Demo 见 `SampleCoreNetworkRepository`
2. 网络请求封装`ApiRetrofit`，通过`Builder`模式创建，具体`Api`参考`ApiRetrofit.Builder`
3. 如果本地存储数据的话可以使用`MMKVHelper`代替`SharedPreferences`，数据库的话，可以继承`BaseRoomDao`和`RoomRepositoryImpl`，提供常见的一些Api方法，附件表已经提供了基础的模版`AttachmentDao`和`AttachmentDatabase`和`AttachmentRepositoryImpl`。DAO 上添加 `@RoomObservedEntity(实体.class)` 后 KSP 会生成 `XxxDaoRawQueryBridge`，将 `extends BaseRoomDao` 改为 `extends XxxDaoRawQueryBridge` 即可，无需手写 6 个 RawQuery。协程/Flow 见 `RoomRepositoryCoroutineExt`、`RoomRepositoryFlowExt`。**外部工程**需依赖 `common`（注解仍在 `io.coderf.arklab.common.annotation`，实现于 `core-base`/`core-db`）与 KSP 处理器 `io.coderf.arklab.room:room-processor`
4. 所有的`Retrofit`的网络请求代理接口都需要集成`BaseApiService`
5. 网络请求分页`PagingSource`继承`PagingSource`，协程`flow`方式继承`FlowPagingSource`，数据库存储分页继承`RxRoomPagingSource`
6. `RetryService`：封装了网络请求拦截重试机制，可以在`Repository`中设置或者`ApiRetrofit`中配置，`ApiRetrofit`配置为默认的，`Repository`中为当前类覆盖默认的配置
7. `FlowRetryService`：协程方式的网络请求重试，可以在`Repository`中设置或者`ApiRetrofit`中配置，`ApiRetrofit`配置为默认的，`Repository`中为当前类覆盖默认的配置
8. `RoomRepositoryImpl`：数据库的一些常用方法
9. `PagingRepositoryImpl`：`paging3`分页的`Repository`层封装
10. `PagingFlowRepositoryImpl`：关于协程的`paging3``Repository`层封装
11. `FlowRepositoryImpl`：协程方式`Repository`封装（已 Deprecated，优先新 API）
#### 常用工具类
1. `AppManager`：获取`Activity`栈信息和获取App版本号、版本名称、App是否在前台运行等API
2. `AppSettingHelper`：可以设置基础的app是否首次运行等方法
3. `ConstantsHelper`：基础的一些常用静态常量
4. `MobileHelper`：获取手机的一些信息，比如获取手机的IMEI、MAC地址、手机型号、手机系统版本、手机分辨率、手机唯一标识符等
5. `RegexUtils`： 正则表达式
6. `AuthManager`：登录相关的一些调用，`Fragment`、`Activity`持有的对象，可以直接获取使用
7. `UIController`：控制`Fragment`、`Activity`一些`dialog`、`toast`、`loading`等
8. `PropertiesModule`：加载Properties对象可以获取配置文件里的参数
9. `CacheUtil`：缓存工具
10. `CollectionUtil`：集合工具类
11. `DownloadManger`：下载管理工具
12. `PermissionManager`：系统权限管理工具类
13. `NetworkStateUtil`：网络状态工具类
14. `StringUtil`: 字符串工具类
15. `RxMenuView`：`Toolbar`菜单控件点击拦截时间，可拦截快速点击事件
16. `RxView`：`View`点击拦截时间，可拦截快速点击事件
17. `QRCodeUtil`：二维码工具类
18. `MathUtil`：数学计算工具类，加减乘除和一些格式化，保留小数位数等
19. `NumberUtil`：数字大小写转化，数字格式化等
20. `KeyBoardUtil`：软键盘控制、唤醒、关闭和监听等
21. `GeoMapUtil`：坐标转化工具
22. `FastBlur`：高斯模糊工具类
23. `ThreadExecutor`：线程池工具类
24. `ThreadExecutorBounded`：线程池工具类，边界更大默认为128

#### 自定义View相关

官方已有圆角 / 描边的场景，直接用 Material3 控件（**1.1.1 已删除对应包装类**）：

| 场景 | 使用 |
|------|------|
| 圆角 / 描边按钮 | `MaterialButton`（`app:cornerRadius` / `app:backgroundTint` / `app:stroke*`） |
| 圆角 / 圆形图片 | `ShapeableImageView`（`app:shapeAppearanceOverlay`，如 `CircleShapeAppearance` / `RoundedShapeAppearanceL`；描边 `app:strokeColor` / `app:strokeWidth`）。**填色用 `android:background`，不要用 `app:backgroundTint`** |
| 密铺输入框 | `TextInputLayout` + `TextInputEditText`（可用 `Widget.App.TextInputLayout.Dense`） |
| 代码里给任意 View 铺圆角底 | `CornerShapeHelper.apply` / `applyFromAttributes`（读 `ShapeView`：`radius` / `bgColor` / `stroke*`） |

官方 **没有** XML 圆角、填充、描边属性，因此仍保留封装（背景为 `MaterialShapeDrawable`）：

1. `AutoNextLineLinearlayout`：自动换行的`ViewGroup`
2. `CircleTextView`：圆形 `MaterialTextView`，XML `bgColor` / `stroke*`，文字按圆心居中
3. `CirclePaddingImageView`：圆形**背景** + `android:padding` 内缩图标（父类 `AppCompatImageView`，不是裁圆头像；头像用 `ShapeableImageView` + `CircleShapeAppearance`）
4. `CustomScrollView`：支持下拉回弹效果的 `ScrollView`
5. `Code`：生成图形验证码的工具类，可绘制随机字符和干扰线
6. `DividerView`：可绘制横线或竖线的虚线控件（Kotlin 实现）
7. `CircleProgressBar`：圆形进度条，支持文字显示、动画进度更新
8. `GradationRectTextView`：支持渐变背景、左右文字样式不同的 `TextView`
9. `GridMenuView`：分页网格菜单，继承 `CornerConstraintLayout`（`Kotlin`）
10. `ClearableEditText` / `PasswordEditText`：圆角输入 + 清除 / 明文切换（compound drawable；官方 `TextInputLayout` 无法同时清 + 眼）
11. `CornerLabelView`：三角形角标控件，可显示在四个角落，支持文字和背景色
12. `CornerTextView`：圆角 `MaterialTextView`，XML `radius` / `bgColor` / `stroke*`
13. `HorizontalProgressBar`：水平横向进度条，支持圆角背景和文字显示
14. `ConfigurableWebView`：可配置的 `WebView`，支持加载本地 assets 或网络 URL
15. `AutoTextView`：带 3D 翻转动画的文字切换控件（基于 `TextSwitcher`）
16. `CustomSearchEditText`：带搜索图标和清除功能的 `EditText`，支持圆角背景
17. `CornerConstraintLayout`：圆角 `ConstraintLayout`，XML `radius` / `bgColor` / `stroke*`
18. `BannerView`：轮播图控件，支持圆点指示器、自动轮播、圆角裁剪
19. `StarBar`：自定义星星评分控件，支持实心/空心五角星、整数/小数评分、触摸滑动评分等功能
20. `IconDotTextView`：图标+文字+数字角标的组合控件，支持多种布局方向（图标上下位置）和角标自定义
21. `SquareLabelView`：左侧带方块的`TextView`，用于模块名称等场景，支持方块形状（矩形、椭圆、圆角矩形）和位置自定义
22. `IconLabelValueView`：图标-标签-值-图标的布局视图，常用于设置项或详情展示，支持左右图标开关、底部边框、值对齐方式等配置
23. `VerificationCodeInputView`：验证码输入控件，支持多格独立输入框、光标闪烁、输入类型限制（数字、字母等）、边框样式自定义
24. `ScalingTextView`：可展开/收起的文本控件，默认显示指定行数，支持“查看全文/收起全文”点击切换
25. `LoadingProgressDialog`：加载进度对话框，支持动态省略号动画效果、自定义提示文字、可设置是否可取消
26. `DatePickDialog`：年月日选择对话框，支持多种日期模式（年月日、年月、年份、时间等）、自定义日期范围和标签
27. `BottomSheetDialog`：底部选择框（Material `BottomSheetDialog`），支持列表选项显示、自定义分割线样式和取消按钮
28. `MenuDialog`：底部菜单对话框，功能类似·BottomSheetDialog·但使用传统Dialog实现，支持自定义位置和样式
29. `ImageSaveDialog`：图片保存选择对话框，提供“保存到本地”等选项，用于图片保存场景
30. `EmptyLayout`：空白占位布局控件，可显示加载中、加载失败、无数据等状态，支持自定义图标和点击重试
31. `InputDialog`：单行输入对话框，支持文本输入、最大字数限制、输入类型设置和提示文字
32. `EditAreaDialog`：多行文本输入对话框，功能类似`InputDialog`但支持多行文本编辑
33. `UpdateMessageDialog`：应用更新提示对话框，显示版本信息和更新内容，支持链接识别和流量提醒
34. `ConfirmDialog`：确认对话框，提供确定和取消按钮，支持富文本内容显示和按钮可见性控制
35. `ProgressBarDialog`：进度条对话框，支持圆形和水平两种进度条样式，可自定义进度条外观和按钮
36. `MessageDialog`：信息提示对话框，单按钮设计，用于简单信息提示场景
37. `ProtectionGuidelinesDialog`：启动页的用户权限隐私指引，请求权限提示弹框，支持富文本内容、自定义按钮文字和样式，用于权限申请场景
38. `CascadeSinglePopupWindow`：单选项级联弹窗
39. `PopupView`：普通下拉选择弹窗（单选）
40. `TextPopupView`：自定义文本+图标组合控件
41. `MultiPopupView`：多选（非级联）弹窗
42. `TreePopupView`：二级树形选择弹窗
43. `CascadeMultiPopupWindow`：多选项级联弹窗
44. `TxSlideRecyclerView`：支持侧滑的`RecyclerView`
45. `SpeakButton`：仿微信的长按说话View
46. `VoiceView`：语音播放条控件

#### Form表单相关控件
主要用于常见的一些表单输入和回显情况
1. `FormDate`：日期选择控件
2. `FormDateRange`：日期范围选择控件
3. `FormDateTime`：日期+时间选择控件
4. `FormEditArea`：文本域输入控件
5. `FormEditText`：单行文本输入控件
6. `FormFile`：文件选择控件，包含全部格式的文件选择
7. `FormFilePreview`：文件预览控件
8. `FormImage`：图片选择控件
9. `FormImageAndVideo`：图片+视频选择控件
10. `FormImageAndVideoPreview`：图片+视频预览控件
11. `FormImagePreview`：图片预览控件
12. `FormRichText`：富文本控件
13. `FormSelection`：下拉选择控件
14. `FormTextView`：文本预览控件，支持单行多行显示，主要用于详情页的表单回显
15. `FormTime`：时间选择控件
16. `FormVideo`：视频选择控件
17. `FormVideoPreview`：视频预览控件
18. `DateRangePickDialog`：日期范围选择`dialog`
19. `TickViewMessageDialog`：对勾动画弹窗
20. `CalendarView`：自定义日历

#### GPS相关api
1. `GpsService`: GPS后台服务
2. `GpsLifecycleObserver`：GPS生命周期观察者，可以直接添加这个观察者，包括GPS是否开启、权限判断等等
### 过滤网络请求日志

调用ApiRetrofit下的TAG
TAG值为类名：`ApiRetrofit`

## 接入指引：

### 导入公共依赖包

由于集成的aar包有隔离效果因此需要在继承base类时导入他们的包，不然就会找不到引用的第三方库，但是如果你没有以aar引用而是直接module引用的话，可以直接将
`implementation` 改成`api`即可
可参考app模块的demo依赖库

```
    implementation fileTree(include: ['*.jar', '*.aar'], dir: 'libs')
    implementation libs.activity
    implementation libs.androidx.core.ktx
    implementation platform(libs.kotlin.bom)
    implementation libs.androidx.appcompat
    implementation libs.material
    testImplementation libs.junit
    androidTestImplementation libs.androidx.junit
    androidTestImplementation libs.androidx.espresso.core
    implementation libs.constraintlayout

    implementation project(':common')
    implementation project(':userapi')
    implementation project(':commonui')
    implementation project(':googlegps')
    // 组装层才依赖实现库；业务 module 通过 Gateway 注入
    implementation project(':commonmedia')
    implementation project(':mqttcomponent')
    implementation project(':user')
    implementation project(':wscomponent')

    implementation project(':annotation')
    annotationProcessor project(':annotation')

    implementation libs.github.glide

    implementation libs.logger

    implementation libs.google.hilt.android
    ksp libs.google.hilt.android.compiler

    implementation libs.androidx.core.splashscreen
    implementation libs.androidx.multidex
    implementation libs.androidx.navigation.fragment
    implementation libs.androidx.navigation.ui
    implementation libs.androidx.navigation.ui.ktx
    implementation libs.mmkv
    implementation libs.reactivex.rxandroid
    implementation libs.rxjava3.rxjava
    implementation libs.gson
    implementation libs.scwang90.refresh.layout.kernel
    implementation libs.refresh.header.classics
    implementation libs.refresh.footer.classics
    implementation libs.okhttp
    implementation libs.retrofit

    implementation libs.zxing.core
    implementation libs.zxing.android.embedded
    //paging3
    implementation libs.androidx.paging.runtime
    // 用于测试
    testImplementation libs.androidx.paging.common// [可选] RxJava 支持
    implementation libs.paging.rxjava3
    implementation libs.swiperefreshlayout

    implementation libs.androidx.recyclerview
    implementation libs.graphics
    implementation libs.net.res
    implementation libs.net.ui

    implementation libs.androidx.room.ktx
    implementation libs.androidx.room.runtime
    ksp libs.room.compiler
    // optional - RxJava3 support for Room
    implementation libs.room.rxjava3
    // optional - Paging 3 Integration
    implementation libs.room.paging
```

### 混淆配置

```
# ==================== MVVM Common / Core Library ====================
# common facade + core-*（包名仍多为 io.coderf.arklab.common.*）
-keep class io.coderf.arklab.common.** { *; }
-dontwarn io.coderf.arklab.common.**
-keep class io.coderf.arklab.core.** { *; }
-dontwarn io.coderf.arklab.core.**
-keep class io.coderf.arklab.base.gateway.** { *; }

# 保留数据绑定相关类
-keep class io.coderf.arklab.common.bean.** { *; }
-keepclassmembers class io.coderf.arklab.common.bean.** {
    void set*(***);
    *** get*();
}

# 保留 BaseResponse 和 ViewModel
-keep class io.coderf.arklab.common.base.BaseResponse { *; }
-keep class io.coderf.arklab.common.viewmodel.** { *; }

# 保留注解处理器生成的类
-keep class * extends io.coderf.arklab.common.base.** { *; }

# 保留资源绑定相关类（如果有自动生成的绑定类）
-keep class *Binding { *; }
-keep class *BindingImpl { *; }
```
```
# ==================== Media Common Library ====================
# 保留 Media 框架核心类
-keep class io.coderf.arklab.media.** { *; }
-dontwarn io.coderf.arklab.media.**
-keep class io.coderf.arklab.media.gateway.** { *; }

# 保留数据模型类
-keep class io.coderf.arklab.media.bean.** { *; }
-keepclassmembers class io.coderf.arklab.media.bean.** {
    void set*(***);
    *** get*();
}

# 保留核心工具类和构建器
-keep class io.coderf.arklab.media.MediaBuilder { *; }
-keep class io.coderf.arklab.media.MediaHelper { *; }
-keep class io.coderf.arklab.media.utils.** { *; }

# 保留回调接口
-keep class io.coderf.arklab.media.callback.** { *; }
-keep interface io.coderf.arklab.media.callback.** { *; }

# 保留多媒体处理方法
-keepclassmembers class io.coderf.arklab.media.** {
    public * compress*(...);
    public * process*(...);
    public * encode*(...);
    public * decode*(...);
}
```

```
# ==================== CommonUI Library ====================
# 保留 CommonUI 框架核心类
-keep class io.coderf.arklab.commonui.** { *; }
-dontwarn io.coderf.arklab.commonui.**

# 确保数据模型不被混淆
-keep class io.coderf.arklab.commonui.bean.** { *; }
-keepclassmembers class io.coderf.arklab.commonui.bean.** {
    void set*(***);
    *** get*();
}

# 保留自定义 Widget 组件
-keep class io.coderf.arklab.commonui.widget.** { *; }
-keepclassmembers class io.coderf.arklab.commonui.widget.** {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
}

# 保留 Fragment
-keep class io.coderf.arklab.commonui.fragment.** { *; }

# 保留适配器
-keep class io.coderf.arklab.commonui.adapter.** { *; }

# 保留表单相关功能
-keep class io.coderf.arklab.commonui.form.** { *; }

# 保留接口和回调
-keep interface io.coderf.arklab.commonui.inter.** { *; }
-keep class io.coderf.arklab.commonui.impl.** { *; }

# 保留数据绑定相关类
-keep class *Binding { *; }
-keep class *BindingImpl { *; }
```

```
# ==================== Google GPS Library ====================
# 保留 GPS 框架核心类
-keep class io.coderf.arklab.googlegps.** { *; }
-dontwarn io.coderf.arklab.googlegps.**

# 保留定位服务
-keep class io.coderf.arklab.googlegps.service.** { *; }
-keepclassmembers class io.coderf.arklab.googlegps.service.** {
    public * onStartCommand(...);
    public * onBind(...);
}

# 保留 Socket 通信
-keep class io.coderf.arklab.googlegps.socket.** { *; }
-keepclassmembers class io.coderf.arklab.googlegps.socket.** {
    public * connect*(...);
    public * send*(...);
}

# 保留工具类
-keep class io.coderf.arklab.googlegps.utils.** { *; }

# 保留监听器接口
-keep interface io.coderf.arklab.googlegps.listener.** { *; }

# 保留位置数据处理方法
-keepclassmembers class * {
    public * getLocation*(...);
    public * onLocation*(...);
    public * format*Gps*(...);
}
```
### hilt框架依赖

在项目根目录下build.gradle添加

```
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.hilt.android)
}
```

在各个模块添加插件引用，那个模块用到在哪个模块添加，每个用到的额都要添加

```
plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.hilt.android)
}
```

在各个模块添加插件引用，那个模块用到在哪个模块添加，每个用到的额都要添加

```
    implementation libs.google.hilt.android
    ksp libs.google.hilt.android.compiler
```

### 新建assets目录

下面新建dev.properties和release.properties，里面有一些基础配置，在common模块里已经配置了，参数key具体参考demo中的的properties配置文件，也可以自定义一些配置

调用默认配置，注意参数key不能错误，参考demo中的

```
    PropertiesUtil.getInstance().loadConfig(application).getBaseUrl()
```

调用自定义文件中的自定义配置

```
    PropertiesUtil.getInstance().loadConfig(application).getPropertyValue(key,defaultValue)
```

上面默认加载的是'prod.properties'配置文件中的配置如果需要加载别的直接再builde.gradle中添加如下resValue配置即可

```
    buildTypes {
        release {
            resValue "string", "app_config_file", "prod.properties"//指定文件名，app_config_file不能修改，后面的prod.properties"可以修改
            minifyEnabled true// 混淆
            shrinkResources true // 移除无用的resource
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
        debug {
            resValue "string", "app_config_file", "dev.properties"//指定文件名，app_config_file不能修改，后面的dev.properties"可以修改
            minifyEnabled false// 混淆
            shrinkResources false // 移除无用的resource
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
```

### 新建Application继承BaseApplication

一定要配置，不然common中很多代码会报错，在application中初始化一些基础配置例如：MMKV、网络框架、WebSocket、mqtt等
如果需要打印日志的话请调用Config接口
调用初始化方法

```
    // 其他全局开关见上文「系统配置」，须在 init 前设置
    Config.getInstance().init(this);
```

### CommonUI中附件组件配置统一的上传服务
```
        MediaUploadConfig.getInstance()
                .setFileApiService(fileApiService)
                .setUploadUrl("minioc/upload");
```

### 新建ErrorServiceImpl实现ErrorService接口

里面有很多封装好的api需要实现，当然你也可以不用，不建议直接修改common组件，因为组件化讲的就是解耦，你什么都在common离实现那耦合性必然就会很高，当然也要新建ErrorServiceModule，这是hilt用法，具体你们自己百度了

### 强调一下

在接入hilt时每个module都要导入这两个包，不能少，少了的话你可以编译但是无法运行，而且你还找不到错在哪
每个用到的模块都要加这两个，不能使用api

``` 
    implementation libs.google.hilt.android
    ksp libs.google.hilt.android.compiler
```

### 项目中自动为您集成了今日头条的适配方案，因此需要你在配置文件中新增属性

    <meta-data
        android:name="design_width_in_dp"
        android:value="360" />
    <meta-data
        android:name="design_height_in_dp"
        android:value="640" />

### 关于网络访问重试

在各实现`RetryService`接口，该接口会自动实现最好在用户模块实现，因为该接口主要用于实现过期免登录，因此在登录模块实现最为合理，示例代码里有示例，此功能关于hilt的使用，因此需要了解hilt框架用法,需要再ViewModel上添加注解@HiltViewModel,
当然你也可以冲过重写方法指定错误重试方法，可以不重写，也可以不加注解，系统会默认一个方法，但是请不要重写为null

### 关于同意尺寸的配置

在dimens资源文件下配置了统一的尺寸大小，方便维护

### 关于一些封装方法的调用

#### 图片视频选择库

暂时图片视频选择没有增加数量限制，大家可以自行实现，后期我会更新
选择图片：

```
    new OpenImageDialog(requireActivity())
        .setMediaType(OpenImageDialog.CAMERA_ALBUM)
        .setOnOpenImageClickListener(mediaHelper)
        .builder()
        .show();
```

选择视频

```
    new OpenShootDialog(requireActivity())
        .setMediaType(OpenShootDialog.CAMERA_ALBUM)
        .setOnOpenVideoClickListener(mediaHelper)
        .builder()
        .show();
```

初始化及获取其回调信息

```
//传统初始化方式
    mediaHelper = new MediaBuilder(this, this)
        .setImageMaxSelectedCount(1)
        .builder();
    mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            
    });
//hilt初始化方式，activity中
    @Inject
    @MediaModule.ActivityMediaHelper
    MediaHelper mediaHelper;
    
//hilt初始化方式，fragment中
    @Inject
    @MediaModule.FragmentMediaHelper
    MediaHelper mediaHelper;
```

#### 欢迎页隐私权限dialog
```
        new ProtectionGuidelinesDialog(this)
                .setCanOutSide(false)
                .setPositiveBackgroundColor(ContextCompat.getColor(this, io.coderf.arklab.common.R.color.theme_green))
                .setSpannableContent(getSpannableContent())
                .setOnNegativeClickListener(dialog -> {

                    showToast("拒绝可能会导致部分功能使用异常");
                    keepOnScreenCondition.compareAndSet(false, true);
                    startCountDown();
                })
                .setOnPositiveClickListener(dialog -> {
                    UserAccountHelper.setAgreement(true);
                    if (!permissionManager.lacksPermissions(permissions())) {
                        keepOnScreenCondition.compareAndSet(false, true);
                        startCountDown();
                    } else {
                        permissionManager.request(permissions());
                    }
                })
                .builder()
                .show();
```
### 自定义注解使用

```java
import android.net.Uri;

import androidx.databinding.Bindable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.TypeConverters;

import java.util.List;

import io.coderf.arklab.annotation.annotation.VerifyEntity;
import io.coderf.arklab.annotation.annotation.VerifyField;
import io.coderf.arklab.annotation.annotation.VerifyParams;
import io.coderf.arklab.annotation.annotation.VerifySort;
import io.coderf.arklab.annotation.enums.VerifyType;
import io.coderf.arklab.annotation.inter.VerifyGroup;
import io.coderf.arklab.common.bean.BaseDaoBean;
import io.coderf.arklab.common.converter.RoomListStringConverter;

/**
 * Created by fz on 2023/9/5 18:32
 * describe :
 */
@Entity
@VerifyEntity(sort = true)
public class Person extends BaseDaoBean {
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "姓名为空！"),
            @VerifyParams(type = VerifyType.LENGTH_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, minLength = 2, maxLength = 10, errorMsg = "姓名输入错误！"),
            @VerifyParams(type = VerifyType.EQUALS, group = {VerifyGroup.Default.class}, errorMsg = "您只能填张三！", equalStr = "张三")
    })
    @VerifySort(1)
    @ColumnInfo
    private String name;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "请选择性别！"),
    })
    @VerifySort(2)
    @ColumnInfo
    private String sex;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "请选择生日！"),
    })
    @VerifySort(3)
    @ColumnInfo
    private String birthday;

    @Ignore
    private String educationalExperienceDate;

    @Ignore
    private String schoolStartTime;

    @Ignore
    private String classStartTime;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "请填写手机号码！"),
            @VerifyParams(type = VerifyType.MOBILE_PHONE, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "手机号码格式输入不正确！")
    })
    @VerifySort(4)
    @ColumnInfo
    private String mobile;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "请填写固话号码！"),
            @VerifyParams(type = VerifyType.TEL_PHONE, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "固话号码格式输入不正确！")
    })
    @VerifySort(5)
    @ColumnInfo
    private String tel;

    @VerifySort(6)
    @VerifyParams(type = VerifyType.NUMBER_RANGE, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, minNumber = 0, maxNumber = 120, errorMsg = "您是神仙吗？")
    @ColumnInfo
    private String age;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "体重为空"),
            @VerifyParams(type = VerifyType.NUMBER_00, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "体重输入格式不正确"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, maxNumber = 200, errorMsg = "你该减肥了！！！"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, minNumber = 40, errorMsg = "你已经瘦成竹竿了！！！")
    })
    @VerifySort(7)
    @ColumnInfo
    private String weight;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "身高为空"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, maxNumber = 300, errorMsg = "姚明都没你高！！！"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, minNumber = 40, errorMsg = "建议您补补钙，多晒晒太阳！！！")

    })
    @VerifySort(8)
    @ColumnInfo
    private String height;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Create.class}, errorMsg = "邮箱地址为空！"),
            @VerifyParams(type = VerifyType.EMAIL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "邮箱地址错误！")
    })
    @VerifySort(9)
    @ColumnInfo
    private String email;

    @VerifySort(10)
    @VerifyParams(type = VerifyType.NOT_EMPTY, errorMsg = "您填填写您的爱好！")
    @ColumnInfo
    @TypeConverters({RoomListStringConverter.class})
    private List<String> hobby;

    //    @VerifyFieldSort(11)
//    @VerifyParams(type = VerifyType.NOTNULL, notNull = true, errorMsg = "您选择您的本人照片！")
    @Ignore
    private List<Uri> imageList;

    //    @VerifyFieldSort(12)
//    @Valid(notNull = true, errorMsg = "请选择您的家庭信息！")
    @Ignore
    public Family family;

    //    @VerifyFieldSort(13)
//    @Valid(notNull = true, errorMsg = "请选择您的家庭集合信息！")
    @ColumnInfo
    @Ignore
    public List<Family> familyList;


    public Person() {
    }

    @Ignore
    public Person(String name, String birthday, String mobile, String tel, String age, String weight, String height, String email, List<String> hobby) {
        this.name = name;
        this.birthday = birthday;
        this.mobile = mobile;
        this.tel = tel;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.email = email;
        this.hobby = hobby;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.name);
    }

    @Bindable
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.birthday);
    }

    @Bindable
    public String getEducationalExperienceDate() {
        return educationalExperienceDate;
    }

    public void setEducationalExperienceDate(String educationalExperienceDate) {
        this.educationalExperienceDate = educationalExperienceDate;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.educationalExperienceDate);
    }

    @Bindable
    public String getSchoolStartTime() {
        return schoolStartTime;
    }

    public void setSchoolStartTime(String schoolStartTime) {
        this.schoolStartTime = schoolStartTime;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.schoolStartTime);
    }

    @Bindable
    public String getClassStartTime() {
        return classStartTime;
    }

    public void setClassStartTime(String classStartTime) {
        this.classStartTime = classStartTime;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.classStartTime);
    }

    @Bindable
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.age);
    }

    @Bindable
    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.weight);
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.email);
    }

    public List<String> getHobby() {
        return hobby;
    }

    public void setHobby(List<String> hobby) {
        this.hobby = hobby;
    }

    @Bindable
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.mobile);
    }

    @Bindable
    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.tel);
    }

    @Bindable
    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.height);
    }

    @Bindable
    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.sex);
    }


    public List<Uri> getImageList() {
        return imageList;
    }

    public void setImageList(List<Uri> imageList) {
        this.imageList = imageList;
    }

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family family) {
        this.family = family;
    }

    public List<Family> getFamilyList() {
        return familyList;
    }

    public void setFamilyList(List<Family> familyList) {
        this.familyList = familyList;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", mobile='" + mobile + '\'' +
                ", tel='" + tel + '\'' +
                ", age='" + age + '\'' +
                ", weight='" + weight + '\'' +
                ", height='" + height + '\'' +
                ", email='" + email + '\'' +
                ", hobby=" + hobby +
                ", imageList=" + imageList +
                ", family=" + family +
                ", familyList=" + familyList +
                '}';
    }
}
```
开始校验
```
            VerifyResult verifyResult = EntityValidator.validate(binding.getData());
            showToast((verifyResult.isOk() ? "验证成功" : "验证失败：") + StringUtil.filterNull(verifyResult.getErrorMsg()));
            if (!verifyResult.isOk()) {
                return;
            }
```

#### 自定义相机
参考示例代码`CustomCameraActivity`

#### Dialog常见封装示例
参考示例代码`DialogActivity`

#### 自定义组件封装示例
参考示例代码`WightActivity`

#### 二维码能力示例
参考示例代码`ScanQrCodeActivity`

#### GPS能力示例
参考示例代码`GoogleGPSActivity`

#### 自定义表单组件
参考示例代码`VerifyActivity`

#### 断点续传下载

普通文件下载示例：

```
               Disposable disposable = DownloadManger.getInstance().download(this, binding.editUrl.getText().toString().trim())
                    .subscribe(file -> {
                        LogUtil.show(ApiRetrofit.TAG, "下载成功：" + file.getAbsolutePath());
                        showToast("下载成功！");
                    }, throwable -> {
                        if (throwable instanceof BaseException baseException) {
                            showToast(baseException.getErrorMsg());
                            return;
                        }
                        showToast(throwable.getMessage());
                    });
```

版本更新示例：

```
    UpdateManger.getInstance().checkUpdateInfo((Activity) view.getContext(),
                        "http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk",
                        "1、修复已知bug",
                        "1.0.1");
```

#### 视频播放

项目中集成了github上的gsy视频播放库，目前是我觉得开源库中兼容性最好的了吧直接调用VideoPlayerActivity这个Activity就行了，基础播放功能，如果你的场景更负责那可能需要自己单独集成了

#### 大图预览框架

```
    new PreviewPhotoDialog(this)
        .createImageInfo(imageUrl)
        .currentPosition(0)
        .show()
```

#### 关于一些其他的公共库

base封装、一些dialog框、自定义view封装等等可以自行在源码中查看了解，不一一介绍了

