# core-db

本地持久化：Room Database / DAO / Entity，以及 Rx / 协程两套 Repository。

当前版本：**1.6.0**  
Maven：`io.coderf.arklab.core:db:1.6.0`  
namespace：`io.coderf.arklab.core.db`  
配套 KSP：`io.coderf.arklab.room:room-processor:1.2.0`（见 [room-processor/README.md](../room-processor/README.md)）

---

## 1.6.0 变更摘要

相对 **1.5.0**（协程栈落地），本版完善 `RoomRequestOptions` 与协程错误 UI，对齐网络侧 `RequestOptions` 习惯：

| 项 | 说明 |
|----|------|
| Toast / 错误交付 | 新增 `deliverErrorToUi`、`showErrorToast`、`errorToastMessage`；工厂 `noToast()` |
| `silent()` | 对齐网络：无 Loading，**且**协程栈不把错误交给 `RequestUi`（仍可 collect `RequestResult.Error`） |
| 工厂补齐 | `defaults()`、`noTimeout()`（`timeoutSeconds=0`） |
| Bug 修复 | `query(showLoading=false)` 不再误用 `silent()`，**仍保留** `throwOnEmptyList=true` |
| 协程接线 | `DefaultRoomRepository` 按 Options 控制 `showError` / Toast 文案 |
| 日志 | `RoomLog` 打印 `Deliver Error To Ui` / `Show Error Toast` |

**兼容说明：**

- Loading / `timeoutSeconds` / `throwOnEmptyList` 语义不变；超时单位仍为**秒**。
- Rx 栈（`RoomRepositorySupport`）**仍不读** Toast / `deliverErrorToUi`，错误由订阅方处理。
- `RoomDataRepository` 等默认 `silent()` 的调用：升级后协程侧失败**不再自动 Toast**；需要提示时请用 `withLoading(...)` / `defaults()`，或显式 `setDeliverErrorToUi(true)`。
- 无需升级 room-processor（本版未改 Bridge 生成）。

### 1.5.0 回顾（协程栈落地）

| 项 | 说明 |
|----|------|
| 协程栈 | `DefaultRoomRepository` / `BaseRoomRepository` / `RoomDataRepository` |
| Dao | `BaseRoomDao` 增加 Room 原生 `*Suspend` |
| 分页 | 协程 `RoomPagingSource`；Rx 保留 `RxRoomPagingSource` |
| 清理 | 删除过渡 blocking 桥接与过时 DELETE API |

---

## 职责

- Room 基础设施与通用 Attachment 等表能力
- Repository：Rx 栈（`common.repository`）与协程栈（`core.db`）**并行独立**
- Debug 日志：`RoomLog`
- 配合 `:room-processor`（KSP）生成 RawQuery Bridge（含 suspend `do*`）

---

## 两套并行栈（对齐网络层）

与 `core-network` 一样：**Rx 旧栈保留，协程新栈独立实现，互不 blocking 桥接**。

| | RxJava3 | 协程（推荐新代码） |
|--|---------|-------------------|
| 包 | `io.coderf.arklab.common.repository` | `io.coderf.arklab.core.db` |
| 仓库 | `RoomRepositoryImpl` | `RoomDataRepository` / `DefaultRoomRepository` |
| Dao 返回 | `Completable` / `Single` / `Flowable` | Room 原生 `*Suspend` |
| 结果形态 | Rx 类型 | `Flow<RequestResult<T>>` |
| 分页 | `RxRoomPagingSource` | `RoomPagingSource` |
| ViewModel | 继承即可（`IRepository`） | 继承 `BaseRoomRepository` / `RoomDataRepository` |
| Options 错误 UI | 订阅方自行 `showError` | `deliverErrorToUi` / Toast 字段生效 |

```kotlin
// 协程栈
class PersonRepository(dao: PersonDao) :
    RoomDataRepository<Person, PersonDao>(dao)

viewModelScope.launch {
    repository.insert(person, RoomRequestOptions.withLoading("保存中..."))
        .collect { it.onSuccess { }.onError { } }
}

// 或自定义 execute
class PersonRepository(private val dao: PersonDao) : BaseRoomRepository() {
    fun loadAll() = execute(RoomRequestOptions.withLoading("加载中...")) {
        dao.findAllSuspend()
    }
}

// 分页
Pager(pagingConfig) {
    RoomPagingSource(repository, roomPagingQuery)
}
```

`BaseRoomDao` 同时提供 Rx 与 `*Suspend` 方法；KSP Bridge（processor **1.2.0+**）已生成 suspend RawQuery 入口。

---

## 1.6.0 RoomRequestOptions 用法

```kotlin
// 静默：无 Loading；协程栈也不交错误 UI
execute(RoomRequestOptions.silent()) { dao.findAllSuspend() }

// 有 Loading，不弹 Toast
execute(
    RoomRequestOptions.Builder()
        .setShowDialog(true)
        .setDialogMessage("保存中...")
        .setShowErrorToast(false)
        .build()
) { dao.insertSuspend(entity) }

// 或工厂：默认无 Loading + 关 Toast（需要 Loading 请用 Builder）
execute(RoomRequestOptions.noToast()) { dao.countAllSuspend() }

// 大批量：不超时
execute(RoomRequestOptions.noTimeout()) { dao.insertAllSuspend(list) }

// 查询：无 Loading 时仍空列表 → NOT_FOUND（1.6.0 已修复）
repository.findAll(RoomRequestOptions.query(false, true))
```

| 字段 / 工厂 | 含义 |
|-------------|------|
| `showDialog` / `dialogMessage` / `enableDynamicEllipsis` | Loading |
| `timeoutSeconds` | ≤0 不超时；默认 15 |
| `throwOnEmptyList` | 空 `List` → `NOT_FOUND` |
| `deliverErrorToUi` | 协程栈是否 `RequestUi.showError` |
| `showErrorToast` / `errorToastMessage` | Toast 开关与覆盖文案 |
| `silent()` / `noToast()` / `noTimeout()` / `defaults()` / `withLoading(msg)` | 常用工厂 |
| `query` / `insert` / `update` / `delete` | 按操作类型的默认文案 |

**不要硬塞进 Options：** 事务、Dispatcher、鉴权重试等——本地库无鉴权；事务放在 Dao / `block` 内。

---

## 主键策略（推荐客户端赋值）

本模块 **不提供** `@Insert` 返回 rowId 的通用封装。推荐客户端 UUID（如 `AttachmentBean.mobileId`）插入前赋值。

---

## API 演进：及时清理过时方法

`@Deprecated` 只作短期迁移窗口，落地后删除。本模块已移除：DELETE 返回 `Flowable<List>` / DELETE LiveData、过渡用 `RoomRepositoryCoroutineExt`（blocking 桥接）等。

| 已删除 | 请改用 |
|--------|--------|
| `RoomRepositoryCoroutineExt` | `RoomDataRepository` / Dao `*Suspend` |
| `deleteAll()` / `deleteByParams()` → Flowable | `delete*Count` / `delete*Rows` |
| `delete*LiveData` | 同上 |

---

## 依赖

- `api` → `:core-base`（含共享 `RequestResult`）
- `ksp` → `:room-processor` **1.2.0**（或同源工程 `project(':room-processor')`）

```gradle
implementation 'io.coderf.arklab.core:db:1.6.0'
ksp 'io.coderf.arklab.room:room-processor:1.2.0'
// 或本仓库
implementation project(':core-db')
ksp project(':room-processor')
```

---

## Debug 日志

`Config.getInstance().enableDebug(true)`；构建 DB 时 `RoomLog.attachTo(builder)`。Tag：`RoomDb`。

---

## 发布

```bash
./gradlew :core-db:publish
# 若 Bridge 有变更，请同时发布配套 processor
./gradlew :room-processor:publish
```
