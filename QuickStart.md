# 五分钟快速入门

> 对应各模块 `build.gradle` 发布号：**common `4.8.0`** · **core-base `2.0.3`** · **core-network `2.0.2`** · **core-db `2.0.0`** · **core-ui `1.3.1`** · **core-utils `1.2.2`** · **core-log `1.2.0`** · **core-form `3.8.1`** · **core-media `3.7.0`** · **core-ws `1.0.1`** · **core-gnss `4.0.0`**。主题为 **Material3 DayNight**（Views + Material Components，不是 Compose）。
> 新代码只走协程：网络用 `BaseNetworkRepository`，本地库用 `RoomDataRepository`。RxJava3 的 `RepositoryImpl` / `RoomRepositoryImpl` 只留给存量，不要再写新接口。
> 模块说明见 [MODULES.md](MODULES.md)。升级见 [UPGRADE.md](UPGRADE.md)。2.0 破坏性变更见 [MIGRATION-2.0.0.md](./MIGRATION-2.0.0.md)。

## 1. 创建项目

用 Android Studio 新建工程，`minSdk 26`，`targetSdk` 对齐框架 Demo（当前 35），同步完成后再接下面的依赖。

## 2. 添加依赖

### 2.1 阿里云仓库

在 `settings.gradle` 加入发布仓库。账号密码放环境变量，不要写进仓库。

```groovy
        maven {
            credentials {
                username = System.getenv("ALIYUN_USER_NAME")
                password = System.getenv("ALIYUN_PASSWORD")
            }
            url = 'https://packages.aliyun.com/maven/repository/2405978-release-ObRSGq/'
        }
        maven {
            credentials {
                username = System.getenv("ALIYUN_USER_NAME")
                password = System.getenv("ALIYUN_PASSWORD")
            }
            url = 'https://packages.aliyun.com/maven/repository/2405978-snapshot-rT6GM2/'
        }
```

可选的国内镜像：

```groovy
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://maven.aliyun.com/repository/central") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
```

### 2.2 版本目录

`libs.versions.toml` 按当前发布号填写。`common` 是门面，Maven 宿主仍要显式钉住 `core-base` / `core-network` / `core-db`，三者保持 2.0 线，不要只升 `common`。

```toml
annotation = "3.3.1"
roomProcessor = "1.2.0"
coreForm = "3.8.1"
coreGnss = "4.0.0"
coreMedia = "3.7.0"
commonVersion = "4.8.0"
coreNetwork = "2.0.2"
coreDb = "2.0.0"
coreBase = "2.0.3"
coreUi = "1.3.1"
coreUtils = "1.2.2"
coreLog = "1.2.0"
coreAutosize = "1.0.0"
coreWs = "1.0.1"
coreMqtt = "1.6.2"
[libraries]
base-common = { module = "io.coderf.arklab.common:common", version.ref = "commonVersion" }
base-core-base = { module = "io.coderf.arklab.core:base", version.ref = "coreBase" }
base-core-network = { module = "io.coderf.arklab.core:network", version.ref = "coreNetwork" }
base-core-db = { module = "io.coderf.arklab.core:db", version.ref = "coreDb" }
base-core-ui = { module = "io.coderf.arklab.core:ui", version.ref = "coreUi" }
base-core-utils = { module = "io.coderf.arklab.core:utils", version.ref = "coreUtils" }
base-core-log = { module = "io.coderf.arklab.core:log", version.ref = "coreLog" }
base-core-autosize = { module = "io.coderf.arklab.core:autosize", version.ref = "coreAutosize" }
base-core-ws = { module = "io.coderf.arklab.ws:ws", version.ref = "coreWs" }
base-core-mqtt = { module = "io.coderf.arklab.mqtt:mqtt", version.ref = "coreMqtt" }
base-media = { module = "io.coderf.arklab.media:media", version.ref = "coreMedia" }
base-core-form = { module = "io.coderf.arklab.ui:ui", version.ref = "coreForm" }
base-core-gnss = { module = "io.coderf.arklab.gnss:core-gnss", version.ref = "coreGnss" }
base-annotation = { module = "io.coderf.arklab.annotation:annotation", version.ref = "annotation" }
room-processor = { module = "io.coderf.arklab.room:room-processor", version.ref = "roomProcessor" }
```

业务模块：

```groovy
implementation libs.base.common
implementation libs.base.core.network
implementation libs.base.core.db
```

使用 `@RoomObservedEntity` 的模块再加：

```groovy
ksp libs.room.processor
```

`app` 组装层按需打开能力，业务模块不要直接依赖 media / mqtt 实现库：

```groovy
implementation libs.base.common
implementation libs.base.media
implementation libs.base.core.form
implementation libs.base.core.gnss
implementation libs.base.annotation
```

Kotlin、KSP、Gradle 插件版本与框架 `libs.versions.toml` 对齐。

## 3. Manifest 与初始化

新建 `Application`，继承 `BaseApplication`，Manifest 的 `android:theme` 使用 `@style/AppBaseTheme`。父主题已经是 `Theme.Material3.DayNight.NoActionBar`，不要改回 `Theme.AppCompat.*`，也不要再复制一份主题。品牌色覆盖见下一节。

`onCreate` 里先改开关，再 `init`。不调用就用默认值：屏幕适配开、崩溃捕获开且保留 5 天、夜间跟随系统、Edge-to-Edge 开、Material You 动态取色关。

```kotlin
        // 以下均须在 init 之前
        // Config.getInstance().setDynamicColorEnabled(true)           // Material You，默认关
        // Config.getInstance().setAutoSizeEnabled(false)              // 屏幕适配，默认开
        // Config.getInstance().setCrashHandlerEnabled(false)          // 自带崩溃捕获，默认开
        // Config.getInstance().setCrashLogRetainDays(7)               // 崩溃日志保留天数，默认 5
        // Config.getInstance().setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        // Config.getInstance().setEdgeToEdgeEnabled(false)            // BaseActivity 全屏，默认开
        // Config.getInstance().setDefaultPlaceholderRes(R.drawable.my_placeholder)
        // Config.getInstance().setDefaultErrorImageRes(R.drawable.my_error)
        Config.getInstance().init(this)
        if (BuildConfig.LOG_DEBUG) {
            Config.getInstance().enableDebug(true)
        }
```

`application` 节点示例：

```xml
    <application
        android:name=".api.ApplicationHelper"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:networkSecurityConfig="@xml/network_security_config"
        android:supportsRtl="true"
        android:theme="@style/AppBaseTheme"
        tools:targetApi="35">
    </application>
```

明文 HTTP 或自定义证书时，在 `res/xml/network_security_config.xml` 写域名和协议。拍照、选图需要 `FileProvider`：

```xml
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.FileProvider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
```

屏幕适配默认以宽度为基准，两个 meta 写一个即可：

```xml
        <meta-data
            android:name="design_width_in_dp"
            android:value="360" />
```

权限按业务补，可参考 case 工程的 Manifest。

## 4. Material3 主题适配

框架主题、暗色色板和默认控件样式都在 `core-base`。宿主只覆盖品牌色，并按 Material 控件的属性写布局。细项对照见 [Material3主题升级API差异说明.md](Material3主题升级API差异说明.md) 和 [UPGRADE.md](UPGRADE.md) §1。

### 4.1 覆盖品牌色

在宿主 `app` 或业务 `base` 模块同时提供 `values` 与 `values-night`。颜色名必须和框架同名（不要加模块前缀），资源合并后才会盖掉默认蓝 `#1C50B5`。只写亮色时，夜间仍会命中框架的 `values-night`。

示例见 `:base` 的 `base_host_colors.xml`。最少覆盖这几项：

| 名称 | 用途 |
|------|------|
| `themeColor` | 品牌主色，映射到 `colorPrimary` |
| `onPrimary` | 主色上的字和图标。暗色主色变浅时必须改成深色，不要写死白色 |
| `primaryContainer` / `onPrimaryContainer` | 浅底容器和上面的字 |
| `primaryInverse` | 反向主色 |
| `default_background` | 页面底 |

表面、描边、错误色不写就继续用框架色板。需要改时同样同名覆盖：`cardSurface`、`cardOnSurface`、`outline`、`outlineVariant`、`theme_red`、`onError`。

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="themeColor">#FF0F766E</color>
    <color name="onPrimary">#FFFFFFFF</color>
    <color name="primaryContainer">#FFCCFBF1</color>
    <color name="onPrimaryContainer">#FF042F2E</color>
    <color name="primaryInverse">#FF5EEAD4</color>
    <color name="default_background">#FFF5F5F5</color>
</resources>
```

`values-night` 再写一套同名颜色。暗色主色用浅色，`onPrimary` 用深色，保证对比度。

### 4.2 颜色怎么引用

颜色走语义 token，不要再写 `@color/white`、`@color/black` 当页面底或正文。

| 场景 | 写法 |
|------|------|
| 页面 / 卡片底 | `?attr/colorSurface` 或 `@color/cardSurface` |
| 正文 / 图标 | `?attr/colorOnSurface` 或 `@color/cardOnSurface` |
| 主色按钮上的字 | `@color/onPrimary` 或 `?attr/colorOnPrimary` |
| 分割线 | `?attr/colorOutlineVariant` |
| 填充按钮、选中态 | `?attr/colorPrimary` |

`colorPrimary` 现在主要驱动填充按钮、选中态和 FAB。普通 Toolbar 默认是 `colorSurface` + `colorOnSurface`。品牌色顶栏用 `Widget.App.Toolbar.OnPrimary`。

### 4.3 控件

布局不要再写 `androidx.appcompat.widget.AppCompat*`。

| 需要 | 使用 |
|------|------|
| 按钮 | `MaterialButton`。填色用 `app:backgroundTint`，圆角用 `app:cornerRadius`，描边用 `app:strokeColor` / `app:strokeWidth`。不要 `android:background` |
| 图片 / 头像 | `ShapeableImageView`。圆形头像加 `@style/CircleShapeAppearance`。填色用 `android:background`，不要 `backgroundTint` |
| 输入框 | `TextInputLayout` + `TextInputEditText`。主题默认是 OutlinedBox |
| 顶栏 | `MaterialToolbar` 或框架的 `ActionToolbar` / `TitleBar` |

主题里的 `Widget.App.Button` 已清零 `inset` 和 `minHeight`，`layout_height` 就是视觉高度。

官方没有 XML 圆角属性的文字和容器，继续用框架控件：`CornerTextView`、`CircleTextView`、`CornerConstraintLayout`。`CornerButton`、`CornerImageView`、`RoundImageView`、`CornerEditText` 已删除。

Toolbar 不再挂 ActionBar。不要调用 `setSupportActionBar`、`getSupportActionBar()`、`onCreateOptionsMenu`。页面继承 `BaseActivity` 后设置 `ToolbarConfig`，菜单用 `inflateToolbarMenu`。重写 `createdToolbarConfig()` 时背景用 `R.color.cardSurface`，不要改回 `R.color.white`。

### 4.4 夜间与动态取色

暗色只走 `values-night`。框架已关闭系统 `forceDarkAllowed`，不要再依赖 Force Dark。跟随系统或手动切换用 `Config.setNightMode`，并且必须在 `init()` 之前。

Material You 动态取色默认关闭，品牌色不跟壁纸走。要打开时：

```kotlin
Config.getInstance().setDynamicColorEnabled(true)
Config.getInstance().init(this)
```

相机、扫码、视频上的白字是叠在内容上的，不要改成语义色。

## 5. 环境配置

在 `assets` 放 `prod.properties` 和 `dev.properties`，用 `buildTypes` 选择当前文件。

```groovy
    buildTypes {
        release {
            resValue("string", "app_config_file", "prod.properties")
            buildConfigField("boolean", "LOG_DEBUG", "false")
            minifyEnabled = true
            shrinkResources = true
            proguardFiles(getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro')
        }
        debug {
            resValue("string", "app_config_file", "dev.properties")
            buildConfigField("boolean", "LOG_DEBUG", "true")
            minifyEnabled = false
        }
    }
```

```properties
BASE_URL=http://192.168.1.1:8001/
FILE_BASE_URL=https://192.168.1.1:19908/api/FileServer/view/
DATA_BASE=system_dev
TOKEN_TYPE=bearer
TENANT_ID=000000
```

用枚举映射 key，再用 `PropertiesUtil` 读取。`app_config_file` 会生成 `R.string.app_config_file`，debug / release 自动切文件。

```kotlin
enum class PropertiesKeyEnum(val key: String, val desc: String) {
    BASE_URL("BASE_URL", "系统模块 baseUrl"),
    FILE_BASE_URL("FILE_BASE_URL", "文件 baseUrl"),
    DATA_BASE("DATA_BASE", "数据库名称"),
    TOKEN_TYPE("TOKEN_TYPE", "token 类型"),
    TENANT_ID("TENANT_ID", "租户 ID"),
}

val baseUrl = PropertiesUtil.getInstance().loadConfig(
    application,
    ContextCompat.getString(application, R.string.app_config_file)
).baseUrl
```

## 6. 编译与插件

根工程：

```groovy
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
```

`app` 模块：

```groovy
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.hilt.android)
```

```groovy
    compileSdk {
        version = release(libs.versions.compileSdkVersion.get().toInteger())
    }
    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInteger()
        targetSdk = libs.versions.targetSdkVersion.get().toInteger()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        dataBinding = true
        buildConfig = true
        viewBinding = true
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
    }
```

## 7. 协程网络请求

接口返回 `suspend` 函数，Repository 只返回 `Flow<RequestResult<T>>`，ViewModel 用 `launchRequest` 接 UI。不要再写 `Observable`、`RepositoryImpl`、`sendRequest`、`Disposable`。

转换器会拆掉外层 `BaseResponse`，接口方法直接声明业务类型，例如 `PageBean<Event>` 或 `Event`，不要再包一层 `BaseResponse`。

### 7.1 错误处理与登录刷新

`ErrorService` 负责统一错误、登录拦截和请求头，用 Hilt 绑到 `SingletonComponent`。登录过期刷新实现 `TokenRefresher`（`shouldRefresh` / `refresh` 做成 Single-Flight），在创建 Api 时 `setTokenRefresher`。它按本次 `ApiRetrofit` 实例生效，不是整个进程自动生效。

`RetryService` 只服务旧的 Rx 栈。新接口不要再为它写调用链。

### 7.2 接口

```kotlin
interface BusinessApiService : BaseApiService {

    @GET("serm-server/event/myList-app")
    suspend fun getEventUserPageList(
        @Query("current") current: Int,
        @Query("size") size: Int,
        @Query("queryType") queryType: String?
    ): PageBean<EventReportingBean>

    @GET("serm-server/event/{id}")
    suspend fun getEventSuspend(@Path("id") id: String): EventReportingBean
}
```

不同 `baseUrl` 拆成不同 ApiService，各自一个 `@Provides`。

```kotlin
@Module
@InstallIn(SingletonComponent::class)
class BusinessApiModule {
    @Provides
    fun provideBusinessApiService(
        application: Application,
        errorService: ErrorService,
        tokenRefresher: TokenRefresher
    ): BusinessApiService {
        val baseUrl = PropertiesUtil.getInstance().loadConfig(
            application,
            ContextCompat.getString(application, R.string.app_config_file)
        ).baseUrl
        return ApiRetrofit.Builder(application)
            .setSingleInstance(false)
            .setBaseUrl(baseUrl)
            .setErrorService(errorService)
            .setTokenRefresher(tokenRefresher)
            .setSuccessCode(ResponseCode.SUCCESS)
            .setTimeOut(15)
            .builder()
            .getApiService(BusinessApiService::class.java)
    }
}
```

### 7.3 Repository

仓库不持有页面，也不弹 Loading。`RequestOptions` 传到 `request()` 时，只有超时、重试、鉴权重试生效；`showLoading` / `deliverErrorToUi` 会被忽略。

```kotlin
class EventRepository @Inject constructor(
    private val api: BusinessApiService
) : BaseNetworkRepository(boundApiService = api) {

    fun loadDetail(id: String): Flow<RequestResult<EventReportingBean>> =
        request { api.getEventSuspend(id) }
}
```

### 7.4 ViewModel 与页面

ViewModel 继承无泛型的 `BaseViewModel`，构造注入仓库。单次请求用 Flow 重载；多步用 Session 版，块里必须 `collectSuccess` / `collectRequest`，禁止对网络 Flow 裸 `collect`。

```kotlin
@HiltViewModel
class EventViewModel @Inject constructor(
    application: Application,
    private val repository: EventRepository
) : BaseViewModel(application) {

    private val _detail = MutableLiveData<EventReportingBean>()
    val detail: LiveData<EventReportingBean> = _detail

    fun load(id: String) {
        launchRequest(
            options = RequestOptions.cancellable(),
            flow = { repository.loadDetail(id) }
        ) { data ->
            _detail.value = data
        }
    }
}
```

登录成功后的跳转用 ViewModel 的 `LiveData` / `SharedFlow`，页面 observe 后再导航。不要再写页面契约接口回调 ViewModel。

`Activity` / `Fragment` 继承 `BaseActivity` / `BaseFragment`，并加上 `@AndroidEntryPoint`。基类会绑定 `NetworkRequestUiHost`，一般不用自己弹 Dialog。

## 8. 协程数据库

新表走协程栈：Dao 调用 Room 原生 `*Suspend`，仓库继承 `RoomDataRepository` 或 `BaseRoomRepository`，结果同样是 `Flow<RequestResult<T>>`。UI 仍由 `launchRequest` 处理。

不要新写 `RoomRepositoryImpl`、`Completable` / `Single`，也不要在协程里包一层 Rx。

```kotlin
class PersonRepository @Inject constructor(
    dao: PersonDao
) : RoomDataRepository<Person, PersonDao>(dao)

fun save(person: Person) {
    launchRequest(RequestOptions.cancellable()) {
        personRepository.insert(person).collectSuccess { }
    }
}
```

通用 CRUD 直接用 `findAll` / `findInfoById` / `insert`。自定义查询走 `execute`，参数用 `RoomRequestOptions`，块里只调 suspend Dao：

```kotlin
fun loadAll() = execute(RoomRequestOptions.silent()) {
    dao.findAllSuspend()
}
```

`RoomDataRepository` 的增删改默认 `RoomRequestOptions.silent()`：仓库侧不弹窗。需要遮罩时在 ViewModel 的 `launchRequest` 上传 `RequestOptions`，不要把 Loading 写进仓库。

带复杂条件查询时，在实体上加 `@RoomObservedEntity`，由 KSP 生成 `XxxDaoRawQueryBridge`。附件表以业务持有的 `mobileId` 为准，服务端 id 写 `fileId`。

本地分页用 `RoomPagingSource`。Rx 的 `RxRoomPagingSource` 不要用在新列表上。

## 9. 请求过程 UI

加载框、错误 Toast、登录过期，统一走 `RequestUi`。`BaseActivity` / `BaseFragment` 已实现，并在创建 ViewModel 后 `NetworkRequestUiBinder.bind`。

已删除，不要再写：`createRepository`、`ensureRepository`、`launchUiRequest`、`BaseViewModel<IR>`、`FlowRepositoryImpl`。

| 策略 | 行为 | 写法 |
|------|------|------|
| `None` | 遮罩不可取消（默认） | `RequestOptions.defaults()` |
| `CancelRequest` | 系统返回取消请求，留在本页 | `RequestOptions.cancellable()` |
| `CancelRequestAndExit` | 系统返回取消并离开页面 | `RequestOptions.cancellableExit()` |

点遮罩外侧不会取消。并发多个带 Loading 的请求共用一个遮罩，全部结束后才关闭。

```kotlin
fun loadDetail(id: String) {
    launchRequest(RequestOptions.cancellable()) {
        repository.loadDetail(id).collectSuccess { detailLiveData.value = it }
    }
}

fun refreshSilent() {
    launchRequest(RequestOptions.silent()) {
        repository.loadDetail(id).collectSuccess { detailLiveData.value = it }
    }
}
```

约定：

1. 页面只 observe ViewModel，不要在 Activity / Fragment 里 collect 仓库 Flow。
2. `showLoading` / `deliverErrorToUi` 只对 `launchRequest` 和 `collectSuccess` 生效。
3. 静默请求用 `RequestOptions.silent()`。
4. 业务码（登录过期、无权限）走 `showError(AppError)`，不要再覆写已删除的 `onErrorCode`。

## 10. 分页

列表翻页不要调用 `launchRequest`，否则每一页都会弹全屏 Loading。

网络列表继承 `NetworkFlowPagingViewModel`，仓库继承 `NetworkPagingRepository`，实现 `fetchPage` 并调用 suspend 接口。筛选条件放在 `PagingQuery` 里，由 PagingSource 快照传给 `fetchPage`。页内的详情、删除仍用 `launchRequest`。

分页失败经 `onAppError` 交给 `NetworkRequestUiHost`，不需要再包一层 Rx `PagingRepositoryImpl`。

## 11. 模块怎么拆

`app` 只放启动页、`MainActivity` 和 Hilt 组装。公共契约放 case 模块 `:base`（这不是 Maven 的 `core-base`）。业务放独立 module，对外能力放到 `xxxapi`，由 Hilt 注入接口。

媒体和 MQTT 只在 `app` 依赖实现库，业务模块注入 Gateway：

```kotlin
@Inject lateinit var mediaGateway: MediaGateway
mediaGateway.pickImages(1) { uris -> }

@Inject lateinit var messageGateway: MessageGateway
messageGateway.connect()
```

绑定示例见 `MediaGatewayModule`、`GatewayModule`。说明见 [MODULES.md](MODULES.md) §4。

存量 Rx 页面如果暂时不能改，留在原来的 `RepositoryImpl` / `RoomRepositoryImpl` 上，不要和协程仓库互相桥接。新页面按第 7、8 节写。
