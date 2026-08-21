# core-network 升级迁移说明

> 面向从 **RxJava3 / 旧 Flow 中间层** 迁到 **新版 Kotlin Flow 网络栈** 的完整对照。  
> 模块版本：`core-network 1.0.x` · 包名：`io.coderf.arklab.core.network` / `io.coderf.arklab.common.*`

---

## 目录

1. [总览与选型](#1-总览与选型)
2. [类映射速查](#2-类映射速查)
3. [依赖与 Hilt 装配](#3-依赖与-hilt-装配)
4. [普通请求：RxJava3 vs 新版 Flow](#4-普通请求rxjava3-vs-新版-flow)
5. [请求选项对照](#5-请求选项对照)
6. [结果消费与 UI](#6-结果消费与-ui)
7. [鉴权刷新（Token / Retry）](#7-鉴权刷新token--retry)
8. [分页请求](#8-分页请求)
9. [API 接口写法（Observable → Suspend）](#9-api-接口写法observable--suspend)
10. [ViewModel 完整示例](#10-viewmodel-完整示例)
11. [特殊场景](#11-特殊场景)
12. [迁移清单与注意事项](#12-迁移清单与注意事项)
13. [废弃 API 说明](#13-废弃-api-说明)

---

## 1. 总览与选型

本模块同时保留三套能力，**可共存**，建议新代码只走最右侧：

| 栈 | 入口 | 状态 | 适用 |
|----|------|------|------|
| **RxJava3** | `RepositoryImpl` + `Observable` / `Flowable` | `@Deprecated`，仍可用 | 存量 Java / Rx 代码 |
| **旧 Flow 中间层** | `FlowRepositoryImpl` + `sendRequest { }` | `@Deprecated`，仍可用 | 早期协程迁移 |
| **新版 Flow（推荐）** | `DefaultNetworkRepository` / `BaseNetworkRepository` + `request { }` | **推荐** | 所有新功能 |

### 能力对照

| 能力 | RxJava3 (`RepositoryImpl`) | 旧 Flow (`FlowRepositoryImpl`) | 新版 Flow (`DefaultNetworkRepository`) |
|------|---------------------------|--------------------------------|----------------------------------------|
| 请求封装 | `sendRequest(Observable, …)` 大量重载 | `sendRequest` / `sendFlow` / `sendToLiveData` | **唯一入口** `request(options) { block }` |
| 返回类型 | `Disposable` / `Observable<T>` | `Flow<T>` / `Job` | `Flow<RequestResult<T>>` |
| Loading / 错误 UI | `ApiRequestOptions` + `ErrorConsumer` + `RequestUi` | 同左 | `RequestOptions` + `RequestUi` |
| 通用重试 | 依赖业务 / RetryService | 同左 | `RetryPolicy`（指数退避） |
| 鉴权刷新 | `RetryService`（局部或 `ApiRetrofit` 全局） | `FlowRetryService`（局部或 Builder 全局） | `TokenRefresher`（局部或 **当前 ApiService 的 Builder**） |
| 分页 | `PagingRepositoryImpl` + Rx `PagingSource` | `PagingFlowRepositoryImpl` | `NetworkPagingRepository` + `NetworkFlowPagingViewModel` |
| 与 BaseViewModel | `createRepository()` | 同左 | 同左（继承 `BaseNetworkRepository`） |

**设计目标（新栈）：**

- 一个 `request` 覆盖 Loading、错误交付、通用重试、鉴权刷新
- 结果统一为 `RequestResult`，ViewModel 只处理 Success / Error
- 不强制每个 ViewModel 注入 `TokenRefresher`（由当前 ApiService 的 Builder 兜底）
- 仍可单测：构造传入自定义 `TokenRefresher` / `RequestUi` 即可

---

## 2. 类映射速查

| 旧（Rx / 旧 Flow） | 新（推荐） | 说明 |
|--------------------|------------|------|
| `RepositoryImpl` | `BaseNetworkRepository` / `DefaultNetworkRepository` | 业务仓库基类 |
| `FlowRepositoryImpl` | 同上 | 旧协程中间层，勿新增 |
| `PagingRepositoryImpl` | `NetworkPagingRepository` | 分页仓库 |
| `PagingFlowRepositoryImpl` | `NetworkPagingRepository` | 同上 |
| `PagingViewModel` | `NetworkFlowPagingViewModel` | 分页 VM |
| `FlowPagingViewModel` | `NetworkFlowPagingViewModel` | 同上 |
| `ApiRequestOptions` | `RequestOptions` | 请求选项 |
| `ErrorConsumer` | `RequestResult.Error` + `RequestUi.showError` | 错误不再强制 Consumer |
| `RetryService` | `TokenRefresher` | 鉴权刷新契约 |
| `FlowRetryService` | `TokenRefresher` | 同一实现可同时实现两者 |
| Builder 上 `setRetryService` / `setFlowRetryService` | 同 Builder `setTokenRefresher` / `setFlowRetryService` | **按 ApiService 实例** |
| `MutableLiveData` 直写成功数据 | `RequestResult` + `onSuccess` | 成功/失败显式分支 |
| Rx `PagingSource` | `NetworkPagingSource` | 对接 `NetworkPagingRepository` |

相关包：

```text
io.coderf.arklab.core.network.*          // 新栈
io.coderf.arklab.core.request.*           // RequestOptions / RequestResult / TokenRefresher
io.coderf.arklab.common.repository.*      // 旧 RepositoryImpl / FlowRepositoryImpl（Deprecated）
io.coderf.arklab.common.api.ApiRetrofit   // 仍负责创建 Retrofit / 旧全局 Retry
```

---

## 3. 依赖与 Hilt 装配

### 3.1 Gradle

```gradle
// 通常经 common facade
implementation project(':common')
// 或显式
implementation project(':core-network')
implementation project(':core-base')
```

Maven：

```gradle
implementation 'io.coderf.arklab.core:network:1.0.1'
```

### 3.2 ApiService（新旧共用）

```java
@Provides
public ApiServiceHelper provideApiServiceHelper(
        Application application,
        AppPropertiesConfig config,
        ErrorService errorService,
        RetryService retryService,           // 旧 Rx 全局
        FlowRetryService flowRetryService    // 旧 Flow 全局
) {
    return new ApiRetrofit.Builder(application)
            .setSingleInstance(false)
            .setBaseUrl(config.getBaseUrl())
            .setRetryService(retryService)
            .setFlowRetryService(flowRetryService)
            .setErrorService(errorService)
            .builder()
            .getApiService(ApiServiceHelper.class);
}
```

### 3.3 按 ApiService 实例配置 TokenRefresher（推荐）

业务模块绑定 Singleton 实现（Single-Flight 仍要全局唯一实例），但**生效范围由各 ApiRetrofit.Builder 决定**：

```java
// user: RetryModule — 只提供实现；是否生效由各 ApiRetrofit.Builder 决定
@Binds @Singleton abstract FlowRetryService bindFlowRetryService(FlowRetryServiceImpl impl);
@Binds @Singleton abstract TokenRefresher bindTokenRefresher(FlowRetryServiceImpl impl);

// app: 需要鉴权的主站 API
@Provides
public ApiServiceHelper provideApiServiceHelper(..., FlowRetryService flowRetryService) {
    return new ApiRetrofit.Builder(application)
        .setBaseUrl(config.getBaseUrl())
        .setRetryService(retryService)
        .setFlowRetryService(flowRetryService)  // 实现类若同时是 TokenRefresher，会自动挂到本 Builder
        // .setTokenRefresher(tokenRefresher)   // 也可显式设置
        .builder()
        .getApiService(ApiServiceHelper.class);
}

// app: 不需要鉴权的文件/第三方 API — 不要 setFlowRetryService / setTokenRefresher
@Provides
public FileApiService provideFileApiService(...) {
    return new ApiRetrofit.Builder(application)
        .setBaseUrl(config.getFileBaseUrl())
        .setErrorService(errorService)
        .builder()
        .getApiService(FileApiService.class);
}
```

Repository 构造时传入 `boundApiService = api`，即可自动用**该 api 所属 Builder** 上的 TokenRefresher。

---

## 4. 普通请求：RxJava3 vs 新版 Flow

### 4.1 Repository 定义

**旧 · RxJava3**

```java
public class UserRepositoryImpl extends RepositoryImpl<UserApiService, BaseView> {

    public UserRepositoryImpl(UserApiService api) {
        this.apiService = api;
    }

    public void loadUser(MutableLiveData<UserInfo> liveData) {
        // 默认 ApiRequestOptions：有 Loading + ErrorConsumer
        sendRequest(apiService.getUserInfo(), liveData);
    }

    public void loadUserCustom(MutableLiveData<UserInfo> liveData) {
        ApiRequestOptions options = new ApiRequestOptions.Builder()
                .setShowDialog(true)
                .setDialogMessage("加载中...")
                .setShowToast(true)
                .build();
        sendRequest(apiService.getUserInfo(), options, liveData);
    }

    /** 仅拿 Observable，自行 subscribe */
    public Observable<UserInfo> loadUserObservable() {
        return sendRequest(apiService.getUserInfo(), ApiRequestOptions.getDefault());
    }
}
```

**旧 · FlowRepositoryImpl（中间层，不推荐新增）**

```kotlin
class UserRepositoryImpl(
    api: UserApiService
) : FlowRepositoryImpl<UserApiService, BaseView>(api) {

    fun loadUser(): Flow<UserInfo> = sendRequest(
        request = { apiService!!.getUserInfoSuspend() },
        apiRequestOptions = ApiRequestOptions.getDefault()
    )

    fun loadUserToLiveData(liveData: MutableLiveData<UserInfo>): Job =
        sendToLiveData(
            request = { apiService!!.getUserInfoSuspend() },
            liveData = liveData,
            showDialog = true
        )
}
```

**新 · BaseNetworkRepository（推荐）**

```kotlin
class UserRepositoryImpl(
    private val api: UserApiService
    // tokenRefresher 可省略 → 使用 api 所属 Builder 上的配置
) : BaseNetworkRepository<BaseView>(boundApiService = api) {

    fun loadUser(
        options: RequestOptions = RequestOptions.defaults()
    ): Flow<RequestResult<UserInfo>> {
        return request(options) {
            api.getUserInfoSuspend()
        }
    }

    fun loadUserSilent(): Flow<RequestResult<UserInfo>> {
        return request(RequestOptions.silent()) {
            api.getUserInfoSuspend()
        }
    }
}
```

不需要挂 `BaseViewModel` 时可用更轻的基类：

```kotlin
class PureRepo(
    private val api: UserApiService
) : DefaultNetworkRepository() {
    fun load() = request { api.getUserInfoSuspend() }
}
```

### 4.2 对照表：同一次「拉用户资料」

| 步骤 | RxJava3 | 新版 Flow |
|------|---------|-----------|
| API | `Observable<UserInfo> getUserInfo()` | `suspend fun getUserInfoSuspend(): UserInfo` |
| Repository | `sendRequest(api.getUserInfo(), liveData)` | `request { api.getUserInfoSuspend() }` |
| 返回 | 写入 `LiveData` 或 `Disposable` | `Flow<RequestResult<UserInfo>>` |
| Loading | `ApiRequestOptions.isShowDialog` | `RequestOptions.showLoading` |
| 错误 | `ErrorConsumer` → Toast / `onErrorCode` | `RequestUi.showError` + `RequestResult.Error` |
| 线程 | `subscribeOn(IO)` + `observeOn(Main)` | 内部 `flowOn(IO)`，UI 回调切 Main |
| 取消 | `Disposable` / `CompositeDisposable` | `viewModelScope` / Job cancel / Flow 取消 |

---

## 5. 请求选项对照

| 含义 | 旧 `ApiRequestOptions` | 新 `RequestOptions` |
|------|------------------------|---------------------|
| 显示 Loading | `setShowDialog(true)` | `showLoading(true)` |
| Loading 文案 | `setDialogMessage("...")` | `loadingMessage("...")` |
| 动态省略号 | `enableDynamicEllipsis(true)` | `enableDynamicEllipsis(true)` |
| 错误 Toast / 交付 UI | `setShowToast(true)` 等 | `deliverErrorToUi(true)` |
| 鉴权刷新 | 依赖是否挂了 RetryService | `enableAuthRetry(true/false)` |
| 超时 | 多在 OkHttp 层 | `timeoutMs(10_000)`（可选） |
| 通用重试次数/退避 | 较少统一建模 | `retryPolicy(RetryPolicy(...))` |

### 工厂方法（新）

```kotlin
RequestOptions.defaults()                          // 有 Loading、有错误 UI、开鉴权重试
RequestOptions.silent()                            // 无 Loading、不交付错误 UI
RequestOptions.noAuthRetry()                       // 关鉴权重试（登录/验证码/refresh 自身）
RequestOptions.builder()
    .showLoading(true)
    .loadingMessage("提交中...")
    .enableAuthRetry(false)
    .retryPolicy(RetryPolicy(maxRetries = 0))      // 关闭通用指数退避
    .timeoutMs(15_000)
    .deliverErrorToUi(true)
    .build()
```

### 旧 → 新示例

```java
// 旧
new ApiRequestOptions.Builder()
    .setShowDialog(true)
    .setDialogMessage("登录中，请稍后...")
    .setShowToast(true)
    .build();
```

```kotlin
// 新
RequestOptions.builder()
    .showLoading(true)
    .loadingMessage("登录中，请稍后...")
    .enableAuthRetry(false)   // 登录接口务必关闭
    .build()
```

---

## 6. 结果消费与 UI

### 6.1 ViewModel 收集（新）

```kotlin
viewModelScope.launch {
    iRepository.loadUser().collect { result ->
        result
            .onSuccess { user -> userInfoLiveData.value = user }
            .onError { /* 多数错误已由 RequestUi 展示；此处可做埋点或分支 */ }
    }
}
```

或 `when`：

```kotlin
when (val result = iRepository.loadUser().first()) {
    is RequestResult.Success -> userInfoLiveData.value = result.data
    is RequestResult.Error -> { /* AppError */ }
}
```

### 6.2 旧 Rx 写法对照

```java
// 旧：成功进 LiveData，失败进 ErrorConsumer
sendRequest(apiService.getUserInfo(), liveData);

// 旧：自定义错误
sendRequest(apiService.getUserInfo(), options, liveData, throwable -> {
    // 自定义
});
```

```kotlin
// 新：成功/失败都在 Flow 里；默认错误已 deliver 到 RequestUi
iRepository.loadUser().collect { result ->
    result.onSuccess { liveData.value = it }
}
```

### 6.3 RequestUi 从哪里来？

与旧栈一致：由 `BaseViewModel` 在绑定 View 时注入到 Repository 的 `RequestUiHost`。

- 新栈：`DefaultNetworkRepository` / `BaseNetworkRepository` 实现 `RequestUiHost`
- 无需在业务 `request` 里手动 `showLoading` / `hideLoading`

---

## 7. 鉴权刷新（Token / Retry）

### 7.1 解析顺序对比

**旧 Rx / 旧 Flow**

```text
Repository 构造传入的 RetryService / FlowRetryService
        ↓ 没有
ApiRetrofit.Builder 上 setRetryService / setFlowRetryService（全局）
```

**新版 Flow（按 ApiService 实例，对齐旧栈）**

```text
Repository 构造传入的 TokenRefresher（局部覆盖）
        ↓ 没有
boundApiService → ApiRetrofit.Builder.getTokenRefresher()
  （Module 里对该 Builder setTokenRefresher / setFlowRetryService）
        ↓ 没有
不鉴权重试（例如未配置的 FileApiService）
```

### 7.2 业务侧用法

```kotlin
// ✅ 推荐：不传局部 TokenRefresher，绑定当前 api 实例
class UserRepositoryImpl(api: UserApiService) :
    BaseNetworkRepository<BaseView>(boundApiService = api)

// ✅ 局部覆盖（单测 / 特殊策略）
class UserRepositoryImpl(
    api: UserApiService,
    tokenRefresher: TokenRefresher
) : BaseNetworkRepository<BaseView>(tokenRefresher = tokenRefresher)

// ✅ 登录 / 验证码：关鉴权重试，避免与 refresh 递归
fun login(bean: RequestLoginBean) = request(
    RequestOptions.builder()
        .loadingMessage("登录中，请稍后...")
        .enableAuthRetry(false)
        .build()
) {
    val token = api.getTokenSuspend(bean)
    // 存 token…
    api.getUserInfoSuspend()
}
```

**ViewModel 不必再写：**

```kotlin
// ❌ 旧新栈过渡期写法（已可删除）
class XxxViewModel @Inject constructor(
    ...,
    private val tokenRefresher: TokenRefresher
) {
    override fun createRepository() = XxxRepository(api, tokenRefresher)
}

// ✅ 现在
override fun createRepository() = XxxRepository(api)
```

### 7.3 实现要求（业务模块）

`TokenRefresher` 实现内必须 **Single-Flight**（可用 `CompletableDeferred`），避免并发 401 重复消费 `refresh_token`。  
参考：`user` 模块 `FlowRetryServiceImpl`（同时实现 `FlowRetryService` + `TokenRefresher`）。

---

## 8. 分页请求

### 8.1 仓库

**旧 · Rx**

```java
public class NewsPagingRepositoryImpl
        extends PagingRepositoryImpl<ApiServiceHelper, NotificationMessageBean, BaseView> {

    public NewsPagingRepositoryImpl(ApiServiceHelper api) {
        super(api);
    }

    @Override
    public Observable<List<NotificationMessageBean>> requestPaging(int currentPage, int pageSize) {
        return apiService.getNewList(currentPage, pageSize, filter)
                .map(page -> page.getList() != null ? page.getList() : Collections.emptyList());
    }
}
```

**旧 · Flow 中间层**

```kotlin
class NewsPagingRepositoryImpl(
    api: ApiServiceHelper
) : PagingFlowRepositoryImpl<ApiServiceHelper, NotificationMessageBean, BaseView>(api) {

    override suspend fun requestPaging(currentPage: Int, pageSize: Int): Flow<List<NotificationMessageBean>>? {
        // …
    }
}
```

**新 · NetworkPagingRepository**

```kotlin
class NewsPagingRepositoryImpl(
    private val api: ApiServiceHelper
) : NetworkPagingRepository<NotificationMessageBean, BaseView>() {

    // 默认 pagingRequestOptions：showLoading = false

    override suspend fun fetchPage(page: Int, pageSize: Int): List<NotificationMessageBean> {
        val pageBean = api.getNewListSuspend(page, pageSize, filter)
        return pageBean?.list ?: emptyList()
    }

    /** 非分页详情也可复用同一仓库 */
    fun getInfoById(id: String): Flow<RequestResult<NotificationMessageBean>> {
        return requestPage(
            RequestOptions.builder().showLoading(true).build()
        ) {
            api.getNewInfoByIdSuspend(id)
        }
    }
}
```

### 8.2 ViewModel

**旧**

```java
public class NewsPagingViewModel extends PagingViewModel<NewsPagingRepositoryImpl, ..., BaseView> {
    @Override
    protected NewsPagingRepositoryImpl createRepository() {
        return new NewsPagingRepositoryImpl(api);
    }
}
```

**新**

```kotlin
@HiltViewModel
class NewsPagingViewModel @Inject constructor(
    application: Application,
    private val api: ApiServiceHelper
) : NetworkFlowPagingViewModel<NewsPagingRepositoryImpl, NotificationMessageBean, BaseView>(
    application
) {
    override fun createRepository() = NewsPagingRepositoryImpl(api)

    // dataFlow: StateFlow<PagingData<T>>
    // items: LiveData<PagingData<T>>  // 兼容旧 Java Fragment
}
```

UI 收集：

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.dataFlow.collectLatest { pagingData ->
        adapter.submitData(pagingData)
    }
}
// 或
viewModel.items.observe(viewLifecycleOwner) { adapter.submitData(lifecycle, it) }
```

刷新：`viewModel.refreshData()`（基类已实现，会重建 `Pager`）。

---

## 9. API 接口写法（Observable → Suspend）

Retrofit 可同时声明 Rx 与 Suspend，迁移期并存：

```kotlin
interface UserApiService : BaseApiService {

    // 旧
    @GET("user/info")
    fun getUserInfo(): Observable<UserInfo>

    // 新（推荐）
    @GET("user/info")
    suspend fun getUserInfoSuspend(): UserInfo

    @POST("auth/token")
    suspend fun getTokenSuspend(@Body body: RequestLoginBean): TokenBean

    @POST("auth/refresh")
    suspend fun refreshTokenSuspend(@Query("refresh_token") token: String): TokenBean
}
```

说明：

- 响应体 `code/msg/data` 拆包仍由 `BaseConverterFactory` 完成；`suspend` 方法拿到的已是 **成功 data**（与旧 Observable 一致）
- 业务错误会以异常形式抛出，由 `request` 的 `catch` 转为 `RequestResult.Error`

---

## 10. ViewModel 完整示例

### 10.1 普通请求（新）

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val refreshUserProfile: RefreshUserProfileUseCase
) : BaseViewModel<UserRepositoryImpl, BaseView>(application) {

    @Inject lateinit var userApiService: UserApiService

    val userInfoLiveData = MutableLiveData<UserInfo>()

    override fun createRepository() = UserRepositoryImpl(userApiService)

    fun refreshUserInfo() {
        viewModelScope.launch {
            refreshUserProfile.execute(iRepository).collect { result ->
                result.onSuccess { userInfoLiveData.value = it }
            }
        }
    }
}
```

### 10.2 登录（关鉴权重试）

```kotlin
// Repository
fun login(bean: RequestLoginBean): Flow<RequestResult<UserInfo>> =
    request(
        RequestOptions.builder()
            .loadingMessage("登录中，请稍后...")
            .enableAuthRetry(false)
            .build()
    ) {
        val tokenBean = api.getTokenSuspend(bean)
        UserAccountHelper.setToken(tokenBean.access_token)
        UserAccountHelper.setRefreshToken(tokenBean.refresh_token)
        api.getUserInfoSuspend()
    }

// ViewModel
viewModelScope.launch {
    iRepository.login(state).collect { result ->
        result.onSuccess { liveData.value = it }
    }
}
```

### 10.3 与旧 Rx ViewModel 对照

```java
// 旧
public void load() {
    iRepository.sendRequest(api.getUserInfo(), userLiveData);
}
```

```kotlin
// 新
fun load() {
    viewModelScope.launch {
        iRepository.loadUser().collect { result ->
            result.onSuccess { userLiveData.value = it }
        }
    }
}
```

---

## 11. 特殊场景

### 11.1 静默请求（无 Loading、不弹错误）

```kotlin
request(RequestOptions.silent()) { api.pingSuspend() }
```

### 11.2 只关 Loading、仍交付错误

```kotlin
request(RequestOptions.builder().showLoading(false).build()) { api.xxx() }
```

### 11.3 关闭通用网络重试，仅保留鉴权重试

```kotlin
request(
    RequestOptions.builder()
        .retryPolicy(RetryPolicy.None)
        .enableAuthRetry(true)
        .build()
) { api.xxx() }
```

### 11.4 单次超时

```kotlin
request(RequestOptions.builder().timeoutMs(8_000).build()) { api.slowSuspend() }
```

### 11.5 在 UseCase 中编排

```kotlin
class RefreshUserProfileUseCase @Inject constructor() {
    fun execute(repo: UserProfileRepository): Flow<RequestResult<UserInfo>> =
        repo.refreshUserInfo()
}
```

Repository 只暴露 `Flow<RequestResult<T>>`，UseCase / ViewModel 负责收集与导航。

### 11.6 单元测试注入假 TokenRefresher

```kotlin
val repo = DefaultNetworkRepository(
    requestUi = NoOpRequestUi,
    tokenRefresher = FakeTokenRefresher()
)
// 或不传 tokenRefresher / boundApiService → 不做鉴权重试
val silent = DefaultNetworkRepository(requestUi = NoOpRequestUi)
```

---

## 12. 迁移清单与注意事项

### 建议步骤

1. **Hilt**：确认 `TokenRefresher` / `FlowRetryService` 已绑定，并在需要鉴权的 `ApiRetrofit.Builder` 上 `setFlowRetryService` 或 `setTokenRefresher`。
2. **API**：为接口补充 `suspend` 方法（可与 Rx 方法并存）。
3. **Repository**：新建或改继承 `BaseNetworkRepository`，用 `request { }` 替换 `sendRequest`。
4. **ViewModel**：`viewModelScope.launch { flow.collect { } }`；去掉多余的 `TokenRefresher` 构造注入。
5. **分页**：改 `NetworkPagingRepository` + `NetworkFlowPagingViewModel`。
6. **登录 / 验证码 / refresh**：`enableAuthRetry(false)`。
7. **回归**：401 自动刷新、并发多请求 Single-Flight、Loading 与错误 Toast、旋转屏后 UI 重绑。

### 注意

| 点 | 说明 |
|----|------|
| 旧类未删除 | `RepositoryImpl` / `FlowRepositoryImpl` 仍可编译，标记 `@Deprecated` |
| 按实例 Retry | 旧栈与新栈都读各自使用的 `ApiRetrofit.Builder`；未 set 的 ApiService 不鉴权重试 |
| 不要在 VM 重复注入 | 新栈默认全局即可 |
| `RequestResult` 必处理 | 即使 UI 已 Toast，业务若依赖成功数据需 `onSuccess` |
| 取消 | 依赖协程作用域，勿再手动管大量 `Disposable`（除非混用 Rx） |
| 混用 | 同一 App 可 Rx 与新 Flow 并存，按模块逐步迁 |

---

## 13. 废弃 API 说明

以下 **请勿在新代码中使用**，仅作兼容：

| 类型 | 替代 |
|------|------|
| `RepositoryImpl.sendRequest(...)` 全系列重载 | `DefaultNetworkRepository.request` |
| `FlowRepositoryImpl.sendRequest` / `sendFlow` / `sendToLiveData` | 同上 |
| `PagingRepositoryImpl` | `NetworkPagingRepository` |
| `PagingFlowRepositoryImpl` | `NetworkPagingRepository` |
| `PagingViewModel` / `FlowPagingViewModel` | `NetworkFlowPagingViewModel` |
| 期望新栈自动鉴权 | 对该 ApiService 的 Builder `setFlowRetryService`/`setTokenRefresher`，且 Repository `boundApiService = api` |

---

## 附录 A：最小可运行新栈骨架

```kotlin
// Repository
class DemoRepository(
    private val api: DemoApi
) : BaseNetworkRepository<BaseView>() {
    fun fetch(id: String) = request { api.getByIdSuspend(id) }
}

// ViewModel
@HiltViewModel
class DemoViewModel @Inject constructor(
    app: Application,
    private val api: DemoApi
) : BaseViewModel<DemoRepository, BaseView>(app) {
    val data = MutableLiveData<DemoBean>()
    override fun createRepository() = DemoRepository(api)
    fun load(id: String) = viewModelScope.launch {
        iRepository.fetch(id).collect { it.onSuccess { data.value = it } }
    }
}
```

## 附录 B：相关源码路径

```text
core-network/
  core/network/NetworkRepository.kt          # DefaultNetworkRepository
  core/network/BaseNetworkRepository.kt
  core/network/NetworkPagingRepository.kt
  core/network/NetworkFlowPagingViewModel.kt
  core/network/NetworkPagingSource.kt
  common/repository/RepositoryImpl.java      # 旧 Rx
  common/repository/FlowRepositoryImpl.kt    # 旧 Flow
  common/api/ApiRetrofit.java

core-base/
  core/request/RequestOptions.kt
  core/request/RequestResult.kt
  core/request/TokenRefresher.kt
  core/request/RequestUi.kt
```

---

*文档随 core-network 维护。存量 Rx 代码可继续运行；新功能请统一使用 `request { }` + `RequestResult`。*
