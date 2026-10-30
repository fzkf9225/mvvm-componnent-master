# core-network

网络层：Retrofit / OkHttp、Repository 基类，以及推荐使用的请求 API（`RequestOptions` / `RequestResult` / `RequestUi` 等）。

当前版本：**1.6.0**  
Maven：`io.coderf.arklab.core:network:1.6.0`  
namespace：`io.coderf.arklab.core.network`

---

## 1.6.0 变更摘要

相对 1.5.x / 此前协程栈初版，本版以**实用补齐与行为对齐旧栈**为主，不改变成功路径默认能力：

| 项 | 说明 |
|----|------|
| `RequestOptions` Toast 对等 | 新增 `showErrorToast` / `errorToastMessage`；工厂 `noToast()`，对齐旧 `ApiRequestOptions.isShowToast` / `toastMsg` |
| 通用重试收紧 | `RetryPolicy` **仅对瞬时网络失败**退避；业务 `BaseException` 不再误重试（鉴权刷新仍走 `TokenRefresher` + `enableAuthRetry`） |
| 异常转换对齐 | 新栈 `.catch` 接入 `DefaultExceptionConverter`，保留 HTTP errorBody 业务文案解析 |
| `ApiRetrofit` | `useDefaultSign` 同步带 `o-timestamp`；新增 `addNetworkInterceptor`、`setLogBodyMaxBytes` |
| 转换工厂 | `data == null` 空壳按 `actualType` 构造；成功有 `data` 时去掉多余 toJson/fromJson |
| 旧 Options | 修复 `ApiRequestOptions.Builder` 多请求串改同一实例的 bug（Rx 栈仍在用） |

**兼容说明：**

- 默认值未改：`showLoading=true`、`deliverErrorToUi=true`、`enableAuthRetry=true`、`RetryPolicy.Default` 数值不变。
- **行为收紧：** 业务码失败不再被通用重试「连打」——这是修隐患，写入类请求更安全。
- Rx / 旧 Flow 栈继续可用；完整迁移见 [MIGRATION.md](./MIGRATION.md)。

---

## 职责

- `ApiRetrofit`、拦截器、错误与重试扩展点
- `DefaultNetworkRepository` 与统一请求封装
- 新写法优先 `request(RequestOptions) { … }`，避免再扩 `sendRequest` 重载

---

## 依赖

- `api` → `:core-base`

**不依赖** `:core-db`（网络与 Room 分层独立；需要本地库时经 `:common` 或直接依赖 `core-db`）。

通常经 `:common` 透出；也可在宿主中显式 `api` 本模块。

---

## 何时依赖

需要 HTTP / Repository 网络栈时：

```gradle
implementation 'io.coderf.arklab.core:network:1.6.0'
// 或
implementation project(':core-network')
```

用法见仓库 [QuickStart.md](../QuickStart.md)、[UPGRADE.md](../UPGRADE.md)。

**从 RxJava3 / 旧 Flow 迁到新版 `request { }`：** 见本模块 [MIGRATION.md](./MIGRATION.md)（含完整对照与示例）。

---

## 1.6.0 主要用法

### RequestOptions（协程栈）

```kotlin
// 默认：Loading + 错误交 UI + 鉴权重试 + 瞬时失败退避
request { api.loadProfile() }

// 静默：无 Loading、不交错误 UI（仍可 collect RequestResult.Error）
request(RequestOptions.silent()) { api.heartbeat() }

// 有 Loading，不弹 Toast（对齐旧 setShowToast(false)）
request(RequestOptions.noToast()) { api.submit() }

// 登录 / 验证码：关闭鉴权刷新，并关掉通用退避
request(
    RequestOptions.builder()
        .enableAuthRetry(false)
        .retryPolicy(RetryPolicy.None)
        .build()
) { api.login(body) }

// 自定义 Toast 文案 / 协程层超时（不改写 OkHttp 超时）
request(
    RequestOptions.builder()
        .errorToastMessage("提交失败，请稍后重试")
        .timeoutMs(15_000)
        .build()
) { api.submit() }
```

| 字段 / 工厂 | 含义 |
|-------------|------|
| `deliverErrorToUi` | 是否 `RequestUi.showError` |
| `showErrorToast` / `errorToastMessage` | Toast 开关与覆盖文案（仅 `deliverErrorToUi=true` 时生效） |
| `enableAuthRetry` | 是否走 `TokenRefresher` |
| `retryPolicy` | 瞬时网络失败的指数退避；`null` → `RetryPolicy.Default`；业务错误不走 |
| `timeoutMs` | 协程单次 attempt 超时；**不**改 OkHttp connect/read/write |
| `silent()` / `noToast()` / `noAuthRetry()` | 常用工厂 |

### ApiRetrofit.Builder 增量

```java
new ApiRetrofit.Builder(context)
    .setBaseUrl(url)
    .setSingleInstance(false)
    .setTokenRefresher(tokenRefresher)   // 按实例挂鉴权，非进程全局
    .addNetworkInterceptor(logging)     // 网络层拦截器（1.6.0+）
    .setLogBodyMaxBytes(512 * 1024)     // debug peek 上限，默认 1MB
    .setUseDefaultSign(true)            // 会同时带 o-timestamp + o-sign
    .builder()
    .getApiService(ApiService.class);
```

### 重试语义（与旧栈对齐）

1. **鉴权：** `enableAuthRetry` + Builder 上的 `TokenRefresher` → 刷新后重试原请求  
2. **通用：** 仅 `IOException` / 超时 / 连接类等瞬时失败按 `RetryPolicy` 退避  
3. **业务 `BaseException`：** 直接失败，不进入通用退避  

---

## 发布

```bash
./gradlew :core-network:publish
```
