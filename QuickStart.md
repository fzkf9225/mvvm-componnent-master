# 五分钟快速入门

> 对应框架 **common 4.6.0** + **core-base/network/db/ui `1.2.0`** + **core-utils `1.1.1`** + **commonui `3.7.0`**，主题为 **Material3 DayNight**。  
> 模块说明见 [MODULES.md](MODULES.md)。升级（含剔除 `BaseView`）见 [UPGRADE.md](UPGRADE.md)。控件包装类删除见 [core-base/README.md](core-base/README.md)。

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
roomProcessor = "1.1.0"
commonui = "3.7.0"
commongps = "3.2.1"
commonmedia = "3.5.0"
commonVersion = "4.6.0"
coreVersion = "1.2.0"
coreBase = "1.2.0"
coreUtils = "1.1.1"
[libraries]
# 基础：common 为 facade；本轮 common 未升版号，请显式钉 core-*（见 UPGRADE §2.1）
base-common = { module = "io.coderf.arklab.common:common", version.ref = "commonVersion" }
base-core-base = { module = "io.coderf.arklab.core:base", version.ref = "coreBase" }
base-core-network = { module = "io.coderf.arklab.core:network", version.ref = "coreVersion" }
base-core-db = { module = "io.coderf.arklab.core:db", version.ref = "coreVersion" }
base-core-ui = { module = "io.coderf.arklab.core:ui", version.ref = "coreVersion" }
base-core-utils = { module = "io.coderf.arklab.core:utils", version.ref = "coreUtils" }
base-media = { module = "io.coderf.arklab.media:media", version.ref = "commonmedia" }
base-commonui = { module = "io.coderf.arklab.ui:ui", version.ref = "commonui" }
base-googlegps = { module = "io.coderf.arklab.googlegps:googlegps", version.ref = "commongps" }
base-annotation = { module = "io.coderf.arklab.annotation:annotation", version.ref = "annotation" }
# Room KSP：配合 @RoomObservedEntity 生成 XxxDaoRawQueryBridge（需已发布 room-processor）
room-processor = { module = "io.coderf.arklab.room:room-processor", version.ref = "roomProcessor" }
```

使用 `@RoomObservedEntity` 时，在 **app / 含 @Dao 的模块** 的 `build.gradle` 增加：

```groovy
ksp libs.room.processor   // 或 ksp "io.coderf.arklab.room:room-processor:1.1.0"
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
implementation libs.base.commonui
implementation libs.base.googlegps
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
新建一个类`EventRepositoryImpl`，继承`RepositoryImpl`，具体实现参考项目实例代码，普通请求集成`RepositoryImpl`,paging分页请求集成`PagingRepositoryImpl`，协程请求继承`FlowRepositoryImpl`和`PagingFlowRepositoryImpl`
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
新建一个`ViewModel`，继承`BaseViewModel`，具体实现参考项目实例代码，请注意一定要类上添加`@HiltViewModel`，构造方法上添加`@Inject`注解，一定要加
```kotlin
//注入接口
@Inject
lateinit var businessApiService: BusinessApiService

```
然后实现无参 `createRepository`（**不要**传入 Activity/Fragment）：
```kotlin
// 可通过 RepositoryFactory 或直接 new；页面由 BaseActivity/Fragment 自动 ensureRepository + bind
    override fun createRepository(): PatrolRepositoryImpl {
        return RepositoryFactory.create(
            PatrolRepositoryImpl::class.java,
            patrolApiService
        )
    }
```
#### 添加请求
在具体的`Activity`或者`Fragment`中请求
```kotlin
mViewModel.iRepository?.getEventPageList(riverSectionCode)
```
监听回调
```kotlin
        mViewModel.iRepository.patrolRecordLiveData.observe(this) {
            //这里操作数据
            
        }
```
业务导航（如登录后跳转）用 ViewModel 的 `LiveData` / `SharedFlow`，由页面 observe 后自行跳转，**不要**再写页面契约接口回调 ViewModel。

#### 特别注意
所有的`Activity`或者`Fragment`都要继承对应的Base类，且一定要添加`@AndroidEntryPoint`注解

## 请求过程 UI（NetworkRequestUiHost / RequestUiCallback）

网络 / 本地数据请求过程中的**加载框、Toast、`onErrorCode` 业务码回调**，统一走 **`RequestUiCallback`**（位于 `core-base`）。`BaseViewModel` 默认持有 **`NetworkRequestUiHost`**，在 `ensureRepository()` 时注入到 `BaseRepository#setRequestUi` / 新栈 `RequestUiHost`。Repository 内**只调用 `getRequestUi()`**，禁止持有或回调页面。

页面侧：`BaseActivity` / `BaseFragment` 实现 **`RequestUiCallback`**，框架在创建 ViewModel 后自动 `ensureRepository()` + `NetworkRequestUiBinder.bind(...)`，把 Host 的 LiveData 落到对话框与 Toast。

**新网络 API**：优先 `DefaultNetworkRepository` + `RequestUi` / `RequestResult`（见 [MODULES.md](MODULES.md)）。旧 Repository 已 `@Deprecated`，可继续编译但勿新增。

### 默认用法（推荐）

继承 `BaseActivity` / `BaseFragment`，实现无参 `createRepository()` 即可。默认 `provideRequestUiCallback()` 返回 `NetworkRequestUiHost`，业务侧一般**不需要**手写 `setRequestUi` 或 `bind`。

需要自定义请求 UI 时：重写 `provideRequestUiCallback()`，或重写 `bindNetworkRequestUi()` 为空后自行 observe `getNetworkRequestUiHost()`。

### 未经过 BaseViewModel 时自行持有 Repository

若通过 `RepositoryFactory` 或 `new` 得到仓库、**没有经过** `BaseViewModel.ensureRepository`，则不会自动注入 UI。发起请求前手动：

```kotlin
repository.setRequestUi(viewModel.getNetworkRequestUiHost())
// 新栈：
(repository as RequestUiHost).setRequestUi(RequestUiAdapters.toRequestUi(callback))
```

未注入时请求仍会执行，但**不会出现加载框 / 错误侧 UI**（适合纯后台或单测）。

### 迁移 checklist

1. Repository / ViewModel：**禁止** 持有页面引用，统一 `getRequestUi()?.…` 与 LiveData/Flow 下发业务状态。
2. 默认页面：继承 Base，实现无参 `createRepository()`，无需改 `provideRequestUiCallback`。
3. 自定义 UI：重写 `provideRequestUiCallback` 和/或 `bindNetworkRequestUi`。
4. 仅工厂创建的仓库：记得 `setRequestUi`，否则无请求侧 UI。
5. 业务导航用 `LiveData` / `SharedFlow`（参考登录页 `PostLoginRoute`），不要再写 MVP 式 View 接口。

## 各模块之间解耦方案说明
一般`app`模块只包含基础启动页和`MainActivity`，其他都都要新建模块
1. 新建base模块，这里一般放一些公共的服务，比如：行政区划获取，字典获取等，他不依赖任何其他模块，只有别的模块依赖他
2. 新建具体的业务模块，比如：`business`模块，这里写具体的业务代码
3. 新建`businessapi`模块，这里提供对外接口服务，在`business`模块实现接口，并提供`module`,在别的模块需要使用`business`模块功能的时候，就通过`Hilt`注入接口调用服务

### Gateway（媒体 / MQTT，4.5.0 起）

业务模块**禁止**直接依赖 `commonmedia` / `mqttcomponent`，只依赖 api 中的接口：

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