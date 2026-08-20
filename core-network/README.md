# core-network

网络层：Retrofit / OkHttp、Repository 基类，以及推荐使用的请求 API（`RequestOptions` / `RequestResult` / `RequestUi` 等）。

当前版本：**1.0.1**  
Maven：`io.coderf.arklab.core:network:1.0.1`  
namespace：`io.coderf.arklab.core.network`

---

## 职责

- `ApiRetrofit`、拦截器、错误与重试扩展点
- `DefaultNetworkRepository` 与统一请求封装
- 新写法优先 `request(RequestOptions) { … }`，避免再扩 `sendRequest` 重载

---

## 依赖

- `api` → `:core-base`、`:core-db`

通常经 `:common` 透出；也可在宿主中显式 `api` 本模块。

---

## 何时依赖

需要 HTTP / Repository 网络栈时：

```gradle
implementation 'io.coderf.arklab.core:network:1.0.1'
// 或
implementation project(':core-network')
```

用法见仓库 [QuickStart.md](../QuickStart.md)、[UPGRADE.md](../UPGRADE.md)。

---

## 发布

```bash
./gradlew :core-network:publish
```
