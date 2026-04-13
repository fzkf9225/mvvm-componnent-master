# 五分钟快速入门
## 创建项目
直接打开`Android Studio`，选择`File->New->New Project`，选择最低`SDK 版本26` ，最高`SDK 版本34`，然后等待同步完成
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
`ALIYUN_USER_NAME`为阿里云账号，`ALIYUN_PASSWORD`为阿里云密码，后面会单独提供，不卸载这里，需要将用户名密码添加到电脑的`环境变量`中
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
annotation = "2.0.0"
commonui = "2.0.11"
commongps = "2.0.3"
commonmedia = "2.0.3"
commonVersion = "3.0.11"
[libraries]
# 基础
base-common = { module = "com.casic.otitan.common:common", version.ref = "commonVersion" }
base-media = { module = "com.casic.otitan.media:media", version.ref = "commonmedia" }
base-commonui = { module = "com.casic.otitan.commonui:commonui", version.ref = "commonui" }
base-googlegps = { module = "com.casic.otitan.googlegps:googlegps", version.ref = "commongps" }
base-annotation = { module = "com.casic.otitan.annotation:annotation", version.ref = "annotation" }
```
在需要的模块引入即可，比如，在`user模块`引入`common`,示例： 打开`user模块`的`build.gradle`
```groovy
implementation libs.base.common
```
### 统一依赖库版本（可选）
可以对比下框架的`libs.versions.toml`文件和自己项目的`libs.versions.toml`文件，将`ksp版本`、`kotlin版本`、`gradle版本`、`gradle插件`和`一些常用库`版本进行统一

## `AndroidManifest.xml`配置
### 权限添加
权限添加可以参考case项目的示例，按需添加
### 配置图标和application
1. 新建一个`Application`类，继承`BaseApplication`类，并添加到`AndroidManifest.xml`中，如果主题不需要修改的话默认就可以配置`android:theme="@style/AppBaseTheme"`
在`onCreate`中初始化框架的初始化方法
```kotlin
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
            //app模块不需要添加这个
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
    id('kotlin-kapt')//可选，如果不使用databinding理论上可以不添加
```
在`app`模块的`build.gradle`文件中添加：
```groovy
    alias(libs.plugins.android.application)//app模块是application，其他模块为 library
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.hilt.android)
    alias(libs.plugins.navigation.safeargs)
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
随机在`BusinessApiService`添加一个请求
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
    ): Observable<BasePage<EventReportingBean>>
```
#### 新建respository
新建一个类`EventRepositoryImpl`，继承`RepositoryImpl`，具体实现参考项目实例代码，普通请求集成`RepositoryImpl`,paging分页请求集成`PagingRepositoryImpl`，协程请求继承`FlowRepositoryImpl`和`PagingFlowRepositoryImpl`
```kotlin
class EventRepositoryImpl(
    baseView: BaseView?,
    eventApiService: EventApiService
) :
    RepositoryImpl<EventApiService, BaseView>(baseView, eventApiService) {
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
然后会提示里实现`createRepository`方法，
```kotlin
//这里可以通过封装的RepositoryFactory工厂类创建或者直接new RepositoryImpl都可以
    override fun createRepository(): PatrolRepositoryImpl {

        return RepositoryFactory.create(
            PatrolRepositoryImpl::class.java,
            baseView,
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
#### 特别注意
所有的`Activity`或者`Fragment`都要继承对应的Base类，且一定要添加`@AndroidEntryPoint`注解

## 各模块之间解耦方案说明
一般`app`模块只包含基础启动页和`MainActivity`，其他都都要新建模块
1. 新建base模块，这里一般放一些公共的服务，比如：行政区划获取，字典获取等，他不依赖任何其他模块，只有别的模块依赖他
2. 新建具体的业务模块，比如：`businsess`模块，这里写具体的业务代码
3. 新建`businessapi`模块，这里提供对外接口服务，在`businsess`模块实现接口，并提供`module`,在别的模块需要使用`businsess`模块功能的时候，就通过`Hilt`注入接口调用服务

# 好了，你出师了！！！