# core-db

本地持久化：Room Database / DAO / Entity，以及 Rx / 协程两套 Repository。

当前版本：**1.5.0**  
Maven：`io.coderf.arklab.core:db:1.5.0`  
namespace：`io.coderf.arklab.core.db`  
配套 KSP：`io.coderf.arklab.room:room-processor:1.2.0`（见 [room-processor/README.md](../room-processor/README.md)）

---

## 1.5.0 变更摘要

相对此前 1.4.x / 过渡实现，本版重点：

| 项 | 说明 |
|----|------|
| 协程栈 | 新增 `io.coderf.arklab.core.db`：`DefaultRoomRepository` / `BaseRoomRepository` / `RoomDataRepository`，对齐网络层 `DefaultNetworkRepository` |
| Dao | `BaseRoomDao` 增加 Room 原生 `*Suspend` CRUD / 查询 / 条件删除 |
| 分页 | 新增协程 `RoomPagingSource`；Rx 侧保留 `RxRoomPagingSource` |
| 结果类型 | 协程栈统一 `Flow<RequestResult<T>>`（`RequestResult` 已下沉 `core-base`） |
| 清理 | 删除过渡用 `RoomRepositoryCoroutineExt`（blocking 桥接）；删除 DELETE→Flowable / DELETE LiveData 等过时 API |
| 职责 | `RxRoomPagingSource` / `RoomPagingQuery` 归属本模块（不再挂在 `core-network`） |

**升级注意：** 使用 `@RoomObservedEntity` 的工程请同步升到 **room-processor 1.2.0**（Bridge 需生成 suspend `do*`），否则协程 RawQuery 无法编译。

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
implementation 'io.coderf.arklab.core:db:1.5.0'
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
