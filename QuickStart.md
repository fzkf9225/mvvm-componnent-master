# 五分钟快速入门

> 对应框架 **common `4.7.0`** · **core-base `1.6.0`** · **core-network `1.7.0`** · **core-db `1.6.1`** · **core-ui `1.3.1`** · **core-utils `1.1.1`** · **core-form `3.7.4`** · **core-media `3.7.0`** · **core-ws `1.0.1`** · **core-gnss `4.0.0`**，主题为 **Material3 DayNight**。  
> 模块说明见 [MODULES.md](MODULES.md)。升级见 [UPGRADE.md](UPGRADE.md)。控件包装类删除见 [core-base/README.md](core-base/README.md)。

> **升到 core-* 2.0.0：** 请读 [MIGRATION-2.0.0.md](./MIGRATION-2.0.0.md)。

## 创建项目
直接打开`Android Studio`，选择`File->New->New Project`，选择最低`SDK 版本26` ，最高建议对齐框架 `targetSdk`（当前 Demo 为 35），然后等待同步完成
## 添加依赖
### 配置阿里云
在`settings.gradle`添加阿里云仓库
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
            url  = 'https://packages.aliyun.com/maven/repository/2405978-snapshot-rT6GM2/'
        }
```
`ALIYUN_USER_NAME`为阿里云账号，`ALIYUN_PASSWORD`为阿里云密码，后面会单独提供，不写在这里，需要将用户名密码添加到电脑的`环境变量`中
也可以添加阿里云国内镜像，这个是可选择的
```groovy
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://maven.aliyun.com/repository/central") }
        maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
```
### 添加基础库依赖
打开`libs.versions.toml`文件，添加基础库配置
```toml
annotation = "3.3.1"
roomProcessor = "1.2.0"
coreForm = "3.7.4"
coreGnss = "4.0.0"
coreMedia = "3.7.0"
commonVersion = "4.7.0"
coreNetwork = "1.7.0"
coreDb = "1.6.1"
coreBase = "1.6.0"
coreUi = "1.3.1"
coreUtils = "1.1.1"
coreAutosize = "1.0.0"
coreWs = "1.0.1"
coreMqtt = "1.6.2"
[libraries]
# 基础：common 为 facade；Maven 宿主请显式钉 core-*（见 UPGRADE 文首 / §0.4）
base-common = { module = "io.coderf.arklab.common:common", version.ref = "commonVersion" }
base-core-base = { module = "io.coderf.arklab.core:base", version.ref = "coreBase" }
base-core-network = { module = "io.coderf.arklab.core:network", version.ref = "coreNetwork" }
base-core-db = { module = "io.coderf.arklab.core:db", version.ref = "coreDb" }
base-core-ui = { module = "io.coderf.arklab.core:ui", version.ref = "coreUi" }
base-core-utils = { module = "io.coderf.arklab.core:utils", version.ref = "coreUtils" }
base-core-autosize = { module = "io.coderf.arklab.core:autosize", version.ref = "coreAutosize" }
base-core-ws = { module = "io.coderf.arklab.ws:ws", version.ref = "coreWs" }
base-core-mqtt = { module = "io.coderf.arklab.mqtt:mqtt", version.ref = "coreMqtt" }
base-media = { module = "io.coderf.arklab.media:media", version.ref = "coreMedia" }
base-core-form = { module = "io.coderf.arklab.ui:ui", version.ref = "coreForm" }
base-core-gnss = { module = "io.coderf.arklab.gnss:core-gnss", version.ref = "coreGnss" }
base-annotation = { module = "io.coderf.arklab.annotation:annotation", version.ref = "annotation" }
# Room KSP：配合 @RoomObservedEntity 生成 XxxDaoRawQueryBridge（配套 db ≥1.6.0）
room-processor = { module = "io.coderf.arklab.room:room-processor", version.ref = "roomProcessor" }
```

使用 `@RoomObservedEntity` 时，在 **app / 含 @Dao 的模块** 的 `build.gradle` 增加：

```groovy
ksp libs.room.processor   // 或 ksp "io.coderf.arklab.room:room-processor:1.2.0"
```

并确保已依赖 `common`（注解包名仍为 `io.coderf.arklab.common.annotation`，实现在 core 分层中）。

在需要的模块引入即可。**业务模块**示例（如 `user`）：

```groovy
implementation libs.base.common
// 需要选图 / MQTT 时依赖本工程 *api 中的 Gateway 接口，不要直接依赖 media / mqtt 实现库
```

**app 组装层**再按需：

```groovy
implementation libs.base.common
implementation libs.base.media      // 激活 MediaGateway Hilt 绑定
implementation libs.base.core.form
implementation libs.base.core-gnss
implementation libs.base.annotation
```

### 统一依赖库版本（可选）
可以对比下框架的`libs.versions.toml`文件和自己项目的`libs.versions.toml`文件，将`ksp版本`、`kotlin版本`、`gradle版本`、`gradle插件`和`一些常用库`版本进行统一

## `AndroidManifest.xml`配置
### 权限添加
权限添加可以参考case项目的示例，按需添加
### 配置图标和application
1. 新建一个`Application`类，继承`BaseApplication`类，并添加到`AndroidManifest.xml`中。默认主题使用`android:theme="@style/AppBaseTheme"`（父类为 `Theme.Material3.DayNight.NoActionBar`，不要改回 `Theme.AppCompat.*`）。暗色由 `values-night` 色板处理，系统强制暗色已关闭。
在`onCreate`中初始化框架的初始化方法
```kotlin
        // 以下均须在 init 之前；不写则用默认值
        // Config.getInstance().setDynamicColorEnabled(true)           // Material You，默认关
        // Config.getInstance().setAutoSizeEnabled(false)              // 屏幕适配，默认开
        // Config.getInstance().setCrashHandlerEnabled(false)          // 自带崩溃捕获，默认开
        // Config.getInstance().setCrashLogRetainDays(7)               // 崩溃日志保留天数，默认 5；<=0 不清理
        // Config.getInstance().setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        // Config.getInstance().setEdgeToEdgeEnabled(false)            // BaseActivity 全局默认，默认开
        // Config.getInstance().setDefaultPlaceholderRes(R.drawable.my_placeholder)
        // Config.getInstance().setDefaultErrorImageRes(R.drawable.my_error)
        Config.getInstance().init(this)
        if (BuildConfig.LOG_DEBUG) {
            Config.getInstance().enableDebug(true)
        }
```
2. 配置网络，打开`app`模块在`res`->`xml`下面新建`network_security_config`文件，并添加内容，涉及到的域名、ip地址和传输协议、约束条件等配置好
`applicaiton`节点配置示例如下
```xml
    <application
        android:name=".api.ApplicationHelper"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:networkSecurityConfig="@xml/network_security_config"
        android:requestLegacyExternalStorage="true"
        android:supportsRtl="true"
        android:theme="@style/AppBaseTheme"
        tools:targetApi="34">
</application>
```
### 配置`FileProvider`
在`application节点`下新建配置,`file_paths`在`res`->`xml`下面新建`file_paths`文件，并添加内容
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
### 配置今日头条UI适配方案
在`application节点`下添加如下配置，默认以`design_width_in_dp`宽度大小为适配基准，下面两个可以添加其中一个即可
```xml
        <meta-data
            android:name="design_width_in_dp"
            android:value="360" />
        <meta-data
            android:name="design_height_in_dp"
            android:value="640" />
```

## 添加配置文件
在`assets`目录下添加`prod.properties`和`dev.properties`文件，并添加内容，示例如下：
`prod.properties`示例：
```properties
BASE_URL=http://202.68.1.1:8001/
FILE_BASE_URL=https://202.68.1.1:19908/api/FileServer/view/
SM2_PUBLIC_KEY=04e3b100db8d2a8c77bd02fe23ecc557068f1263581c5ba77cbe33bd50d57fb51c29f83c2c2f8a866ba3d657bf8f4fb965f92d3e1f69e6116d543a680ecc27e4be
DATA_BASE=system_pro
PROTOCOL_VERSION=1.0
TOKEN_TYPE=bearer
TENANT_ID=000000
BUSINESS_DATA_BASE=business_pro
ATTACHMENT_DATA_BASE=attachment_pro
```
`dev.properties`示例：
```properties
BASE_URL=http://192.168.1.1:8001/
FILE_BASE_URL=https://192.168.1.1:19908/api/FileServer/view/
SM2_PUBLIC_KEY=04e3b100db8d2a8c77bd02fe23ecc557068f1263581c5ba77cbe33bd50d57fb51c29f83c2c2f8a866ba3d657bf8f4fb965f92d3e1f69e6116d543a680ecc27e4be
DATA_BASE=system_dev
PROTOCOL_VERSION=1.0
TOKEN_TYPE=bearer
TENANT_ID=000000
BUSINESS_DATA_BASE=business_dev
ATTACHMENT_DATA_BASE=attachment_dev
```
#### 打开`app`模块的`build.gradle`文件
`android`节点下添加如下代码：
```groovy
    buildTypes {
        release {
            resValue("string", "app_config_file", "prod.properties")//这里的app_config_file会自动生成对应R文件字符串资源通过R.string.app_config_file获取
            buildConfigField("boolean", "LOG_DEBUG", "false")  // 加上括号
            minifyEnabled = true// 混淆
            zipAlignEnabled = true// Zipalign优化
            shrinkResources = true // 移除无用的resource
            proguardFiles(getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro')
        }
        debug {
            resValue("string", "app_config_file", "dev.properties")//这里的app_config_file会自动生成对应R文件字符串资源通过R.string.app_config_file获取
            buildConfigField("boolean", "LOG_DEBUG", "true")  // 加上括号
            minifyEnabled = false// 混淆
            zipAlignEnabled = false// Zipalign优化
            shrinkResources = false // 移除无用的resource
            proguardFiles(getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro')
        }
    }
```

### 读取配置文件示例
可新建一个枚举类，映射配置的key
```kotlin

enum class PropertiesKeyEnum constructor(
    val key: String,
    val desc: String
) {
    BASE_URL("BASE_URL", "系统模块baseUrl"),
    FILE_BASE_URL("FILE_BASE_URL", "文件的baseUrl"),
    SM2_PUBLIC_KEY("SM2_PUBLIC_KEY", "sm2的publicKey"),
    DATA_BASE("DATA_BASE", "数据库名称"),
    BUSINESS_DATA_BASE("BUSINESS_DATA_BASE", "业务模块数据库名称"),
    ATTACHMENT_DATA_BASE("ATTACHMENT_DATA_BASE", "附件数据库名称"),
    PROTOCOL_VERSION("PROTOCOL_VERSION", "app接口服务版本"),
    TOKEN_TYPE("TOKEN_TYPE", "token类型"),
    TENANT_ID("TENANT_ID", "租户ID"),
    ;
}
```
读取配置：
```kotlin
        val baseUrl = PropertiesUtil.getInstance().loadConfig(
            application,
            ContextCompat.getString(application, R.string.app_config_file)
        ).baseUrl
```
这样会自动读取`prod.properties`和`dev.properties`不同的配置，如果你还需要别的配置文件，只需要在添加一个`properties`配置文件，然后在`build.gradle`中配置一下即可

### 基础依赖库配置
#### 以`app`模块为例，
打开项目的`build.gradle`文件，添加：
```groovy
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.navigation.safeargs) apply false
```
在`app`模块的`build.gradle`文件中添加：
```groovy
    alias(libs.plugins.android.application)//app模块是application，其他模块为 library
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.hilt.android)
    alias(libs.plugins.navigation.safeargs)
    id('kotlin-kapt')//可选，如果不使用databinding理论上可以不添加
```
配置sdk版本
```groovy
    compileSdk {
        version = release(libs.versions.compileSdkVersion.get().toInteger())
    }
    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInteger()
        targetSdk = libs.versions.targetSdkVersion.get().toInteger()
        ndk {
            abiFilters = ['arm64-v8a', 'x86_64']//仅适配64位系统
        }
    }
```
添加jdk和kotlin一些其他配置，在`android 节点下`
```groovy

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
    }

    compileOptions {
        encoding = "UTF-8"
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)  // 这是 kotlin DSL 的写法
    }
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    resourcePrefix "base_"

    buildFeatures {
        dataBinding = true
        buildConfig = true
        viewBinding = true
    }
```
### 对外服务接口实现
在`app`模块新建一个package`impl`，新建一个类`ErrorServiceImpl`，实现`ErrorService`，具体实现可以直接参考项目实例代码，里面是包含一些网络请求错误等判断条件和路由的回调和统一请求头配置
#### 配置`ErrorServiceImpl`
新建一个package`module`,再在下面新建一个类`ErrorServiceModule`，这样`ErrorServiceImpl`会自动生效
```kotlin
@Module //必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(SingletonComponent::class) //表示这个module中的配置是用来注入到Activity中的
abstract class ErrorServiceModule {
    @Binds
    abstract fun bindErrorService(errorServiceImpl: ErrorServiceImpl): ErrorService
}
```
### 网络请求框架的使用
#### 新建接口
新建一个接口`BusinessApiService`，继承`BaseService`，具体实现参考项目实例代码，
```kotlin
interface BusinessApiService : BaseApiService {
}
```
不同的`baseUrl`、不同的`module`可以分开建，这个无所谓，为每一个接口添加一个对应的`module`，新建对应的`module`类`AppModule`，下面的方法源码里都有注释，这里就不解释了
```kotlin
@Module //必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(SingletonComponent::class) //表示这个module中的配置是用来注入到Activity中的
class BusinessApiModule {
    @Provides
    fun provideBusinessApiService(
        application: Application,
        errorService: ErrorService,
        retryService: RetryService
    ): BusinessApiService {
        val baseUrl = PropertiesUtil.getInstance().loadConfig(
            application,
            ContextCompat.getString(application, R.string.app_config_file)
        ).baseUrl
        return ApiRetrofit.Builder(application)
            .setSingleInstance(false)
            .setBaseUrl(baseUrl)
            .setErrorService(errorService)
            .setSuccessCode(ResponseCode.SUCCESS)
            .setRetryService(retryService)
            .setTimeOut(15)
            .builder()
            .getApiService(BusinessApiService::class.java)
    }

}
```
#### 添加请求
随机在`BusinessApiService`添加一个请求，返回理论上应该是`BaseResponse<BasePage<EventReportingBean>>`，`BaseResponse`负责解析外部的请求`code`、`message`等，但是框架里已经自动解析了请求是否成功这里所以只需要写：`BasePage<EventReportingBean>`
```kotlin
    /**
     * 我的事件上报分页列表
     *
     * @param current 当前页
     * @param size 每页数量
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param origin 事件来源
     * @param queryType 查询类型to？do: 待我处理，done: 我已处理，reported: 我上报的
     * @return 分页结果
     */
    @GET("serm-server/event/myList-app")
    fun getEventUserPageList(
        @Query("current") current: Int,
        @Query("size") size: Int,
        @Query("startTime") startDate: String?,
        @Query("endTime") endDate: String?,
        @Query("origin") origin: String?,
        @Query("type") type: String?,
        @Query("queryType") queryType: String?,
        @Query("keyword") keyword: String?,
        @Query("status")status: Array<String>?
    ): Observable<BasePage<EventReportingBean>> //
```
#### 新建respository
新建一个类`EventRepositoryImpl`：Rx 普通请求继承 `RepositoryImpl`，Rx 分页继承 `PagingRepositoryImpl`；协程请继承 `BaseNetworkRepository` / `NetworkPagingRepository`（`core` 包）。过渡层 `FlowRepositoryImpl` 已删除。
```kotlin
class EventRepositoryImpl(
    eventApiService: EventApiService
) : RepositoryImpl<EventApiService>(eventApiService) {
    //这里支持flow 和 liveData和Rxjava的Consumer都可
    val eventLiveData by lazy {
        MutableLiveData<List<EventReportingBean>>()
    }


    fun getEventPageList(
        riverSectionCode: String?,
        startDate: String?= DateUtil.getToday()+" 00:00:00",
        endDate: String? = DateUtil.getToday()+" 23:59:59"
    ): Disposable {
        return sendRequest(
            apiService.getEventPageList(startDate, endDate, riverSectionCode),
            ApiRequestOptions.getDefault(),
            eventLiveData
        )
    }

}

```
#### 新建一个`ViewModel`
新建一个`ViewModel`，继承**无泛型**的 `BaseViewModel`，类上加 `@HiltViewModel`，构造注入 Repository：
```kotlin
@HiltViewModel
class PatrolViewModel @Inject constructor(
    application: Application,
    private val patrolRepository: PatrolRepositoryImpl
) : BaseViewModel(application) {

    private val _list = MutableLiveData<List<Patrol>>()
    val list: LiveData<List<Patrol>> = _list

    fun load(code: String) {
        launchRequest(RequestOptions.cancellable()) {
            patrolRepository.getEventPageList(code).collectSuccess { _list.value = it }
        }
    }
}
```
Repository 只返回 `Flow<RequestResult<T>>`；ViewModel 用 `launchRequest` + `collectSuccess`，Error（登录 / Toast）由 Host 自动处理。
**禁止**在 `launchRequest` 块内对网络 Flow 裸 `.collect` / `.first()`。

业务导航（如登录后跳转）用 ViewModel 的 `LiveData` / `SharedFlow`，由页面 observe 后自行跳转，**不要**再写页面契约接口回调 ViewModel。

#### 特别注意
所有的`Activity`或者`Fragment`都要继承对应的Base类，且一定要添加`@AndroidEntryPoint`注解

## 请求过程 UI（NetworkRequestUiHost / RequestUi）

网络 / 本地数据请求过程中的**加载框、错误展示、业务码处理**，统一走 **`RequestUi`**。`BaseViewModel` 持有 **`NetworkRequestUiHost`**；**唯一请求入口**为 `launchRequest`（登记 Job + Loading），结果用 `collectSuccess` / `collectRequest`。

页面侧：`BaseActivity` / `BaseFragment` 实现 **`RequestUi`**，创建 ViewModel 后自动 `NetworkRequestUiBinder.bind(...)`。

**已删除（勿再使用）**：`createRepository` / `ensureRepository` / `launchUiRequest` / `BaseViewModel<IR>` 泛型 / Repository 装配生命周期。

**推荐路径**：`BaseViewModel` + `launchRequest` + `collectSuccess` + Flow Repository（`BaseNetworkRepository` / `DefaultRoomRepository`）。

**【已过时】Rx**：仍保留 `RepositoryImpl`；存量用 `@Deprecated` 的 `BaseRxViewModel.subscribeRequest`（Demo：`LegacyRxSampleViewModel`）。新功能禁止走 Rx UI 封装。框架已不再 `ensureRepository` 注入 RequestUi。

### 默认用法（推荐）

```kotlin
// Repository：无 UI，只返回 Flow（showLoading/deliverErrorToUi 传给 request 无效）
class UserRepository @Inject constructor(private val api: UserApi) :
    BaseNetworkRepository(boundApiService = api) {
    fun loadProfile() = request { api.getProfile() }
}

// ViewModel：推荐 Flow 重载（内部强制 Host 错误处理，勿裸 collect）
fun load() {
    launchRequest(
        options = RequestOptions.cancellable(),
        flow = { userRepository.loadProfile() }
    ) { data ->
        _profile.value = data
    }
}

// 多步编排仍用 Session + collectSuccess
fun loadThenSave() {
    launchRequest(RequestOptions.cancellable()) {
        userRepository.loadProfile().collectSuccess { profile ->
            userRepository.save(profile).collectSuccess { }
        }
    }
}
```

页面继承 `BaseActivity` / `BaseFragment` 即可自动 bind Host；一般**不需要**手写 Dialog。

### Loading 可取消

默认：`RequestOptions.defaults()` 为 `LoadingCancelPolicy.None`（遮罩不可取消）。

| 策略 | 含义 | 工厂 |
|------|------|------|
| `None` | 不可取消（默认） | `defaults()` 等 |
| `CancelRequest` | 系统返回/手势返回取消，留在本页（点外侧不取消） | `RequestOptions.cancellable()` |
| `CancelRequestAndExit` | 系统返回/手势返回取消并 finish / 回退（点外侧不取消） | `cancellableExit()` |

```kotlin
fun loadDetail(id: String) {
    launchRequest(RequestOptions.cancellable()) {
        repository.loadDetail(id).collectSuccess { detailLiveData.value = it }
    }
}
```

要点：

1. **Options 管策略，ViewModel 管会话**：`cancelUiRequests()` 取消已登记的 `launchRequest` Job（Rx 存量另见 `BaseRxViewModel`）。
2. **引用计数**：并发带 Loading 请求共用遮罩，全部结束后才关。
3. **静默请求**：`launchRequest(RequestOptions.silent()) { repo.xxx().collectSuccess { } }`
4. **不要**在 Fragment/Activity 里直接 collect 网络 Repository Flow；页面只 observe ViewModel 状态。
5. **`RequestOptions.showLoading` / `deliverErrorToUi` 只对 `launchRequest` / `collectSuccess` 生效**，传给仓库 `request()` / Room `execute` 会被忽略。


### 分页与 launchRequest

- **列表分页**：不使用 `launchRequest`；错误经 `NetworkPagingSource` → `networkRequestUiHost`（默认 `NetworkFlowPagingViewModel.createPagingSource`）。
- **分页页内单次请求**（详情、删除）：使用 Flow 重载 `launchRequest(flow=...)` 或 Session + `collectSuccess`。
- **禁止**为每一页 `load` 调用 `launchRequest`（会误弹全屏 Loading）。

### 迁移 checklist

1. ViewModel：**无** Repository 泛型；构造注入仓库；`launchRequest` + `collectSuccess`。
2. 删除：`createRepository` / `ensureRepository` / `launchUiRequest` / `iRepository`。
3. Repository：返回 `Flow<RequestResult<T>>`，不持有页面、不在 Flow 里 showLoading。
4. 页面：只 bind Host（Base 已做）+ observe 业务状态。
5. Rx 存量：用 `BaseRxViewModel`（@Deprecated），参考 `LegacyRxSampleViewModel`。

## 各模块之间解耦方案说明
一般`app`模块只包含基础启动页和`MainActivity`，其他都都要新建模块
1. 新建base模块，这里一般放一些公共的服务，比如：行政区划获取，字典获取等，他不依赖任何其他模块，只有别的模块依赖他
2. 新建具体的业务模块，比如：`business`模块，这里写具体的业务代码
3. 新建`businessapi`模块，这里提供对外接口服务，在`business`模块实现接口，并提供`module`,在别的模块需要使用`business`模块功能的时候，就通过`Hilt`注入接口调用服务

### Gateway（媒体 / MQTT，4.5.0 起）

业务模块**禁止**直接依赖 `core-media`（原 `commonmedia`）/ `mqttcomponent`，只依赖 api 中的接口：

```kotlin
@Inject lateinit var mediaGateway: MediaGateway
mediaGateway.pickImages(1) { uris -> /* … */ }

@Inject lateinit var messageGateway: MessageGateway
messageGateway.connect()
```

- 接口：`:base` → `MediaGateway` / `MessageGateway`（case 契约）
- 实现绑定：在 **app** 依赖 media / mqtt，并由组装层 Hilt Module 适配（Demo：`MediaGatewayModule`、`GatewayModule`）
- 详细说明：[MODULES.md](MODULES.md) §4、[UPGRADE.md](UPGRADE.md) 附录 A.3

# 好了，你出师了！！！