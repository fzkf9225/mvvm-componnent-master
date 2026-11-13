# MVVM 请求路径 2.0.0 — 升级迁移说明（业务工程必读）

> **适用**：业务工程从 `core-base 1.x` / `core-network 1.x` / `core-db 1.x` 升到 **`2.0.0`**。  
> **性质**：破坏性升级（主版本跳号）。请 **三库同升**，勿只升其一。  
> **非目标**：不删 Rx `RepositoryImpl`；不改 DataBinding / Compose；不要求一次迁完所有页面。  
> **配套文档**：[QuickStart.md](./QuickStart.md) · [MODULES.md](./MODULES.md) · [UPGRADE.md](./UPGRADE.md) · [core-network/MIGRATION.md](./core-network/MIGRATION.md)

---

## 0. Maven 版本（发布后请钉死）

```gradle
// 必须三库同升到 2.0.0（经 common facade 传递时也建议显式声明）
implementation 'io.coderf.arklab.core:base:2.0.0'
implementation 'io.coderf.arklab.core:network:2.0.0'
implementation 'io.coderf.arklab.core:db:2.0.0'

// facade（版本以仓库实际发布为准；未抬 common 时务必显式钉上面三库）
implementation 'io.coderf.arklab.common:common:4.8.0'
```

工程内源码依赖：`implementation project(':common')` 即可随源码。

---

## 1. 一句话结论

| 以前（1.x） | 现在（2.0.0） |
|-------------|---------------|
| `BaseViewModel<IR>` + `createRepository` / `ensureRepository` / `launchUiRequest` | **无泛型** `BaseViewModel` + **唯一入口** `launchRequest` |
| Repository 持有 `RequestUi`，在 Flow 里 showLoading / showError | Repository **只返回** `Flow<RequestResult<T>>`，**无 UI** |
| ViewModel 里 `flow.collect { }` 或仓库回调 UI | ViewModel：`launchRequest(flow=…)` 或 Session + `collectSuccess` |
| 分页错误靠仓库 RequestUi | 分页：**不走** `launchRequest`；`NetworkPagingSource.onAppError` → Host |
| `trackUiRequest` 公开可滥用 | `trackUiRequest` → **protected** |

页面仍继承 `BaseActivity` / `BaseFragment`，自动 `NetworkRequestUiBinder.bind(Host)`，**一般不用改页面绑定**。

---

## 2. 破坏性变更清单（编译期必改）

### 2.1 已删除 / 不可用

| API / 概念 | 替代 |
|------------|------|
| `BaseViewModel<IR extends IRepository>` 泛型 | `BaseViewModel(application)`，构造注入仓库 |
| `createRepository()` / `ensureRepository()` | 构造 `@Inject` 仓库；框架不再给仓库注入 RequestUi |
| `launchUiRequest { }` | `launchRequest { }` 或 Flow 重载 |
| 仓库内 `getRequestUi()?.showLoading`（协程新栈） | ViewModel `launchRequest` 的 `RequestOptions` |
| `DefaultNetworkRepository` 独立类名（若仍引用） | `BaseNetworkRepository` |
| `FlowRepositoryImpl` 等旧 Flow 过渡层 | 早已删除；用 `BaseNetworkRepository.request` |

### 2.2 行为变更（能编译但易踩坑）

| 行为 | 说明 |
|------|------|
| 仓库 `request(RequestOptions(showLoading=…))` | **UI 字段无效**；只对 ViewModel `launchRequest` / `collect*` 生效 |
| Room 协程 `execute` / `RoomRequestOptions.showDialog` | **无效**；UI 同样只在 ViewModel |
| Session 块内裸 `.collect` / `.first()` | Loading 可能有，**Error 不进 Host**（无 Toast / 登录） |
| Loading 可取消 | 仅 **系统返回 / 手势返回**；**点外侧不取消** |
| `trackUiRequest` | 业务模块不可再外部调用；请用 `launchRequest` |

### 2.3 仍保留（可渐进）

- Rx：`RepositoryImpl` / `PagingRepositoryImpl` / `RoomRepositoryImpl`（`@Deprecated`）
- Rx UI：`BaseRxViewModel.subscribeRequest`（Demo：`LegacyRxSampleViewModel`）
- 旧 Rx 分页：`PagingViewModel`（**Legacy**，错误需自管 Host 或迁 `NetworkFlowPagingViewModel`）

---

## 3. 推荐写法对照

### 3.1 ViewModel（推荐：Flow 重载）

```kotlin
@HiltViewModel
class PatrolViewModel @Inject constructor(
    application: Application,
    private val repository: PatrolRepository
) : BaseViewModel(application) {

    private val _detail = MutableLiveData<Patrol>()
    val detail: LiveData<Patrol> = _detail

    fun load(id: String) {
        launchRequest(
            options = RequestOptions.cancellable(),
            flow = { repository.loadDetail(id) }
        ) { data ->
            _detail.value = data
        }
    }

    // 可选：整页 UiState
    // launchRequest(options, state = _uiState, flow = { repository.loadDetail(id) })
}
```

### 3.2 多步编排（Session + collectSuccess）

```kotlin
fun loadThenSave() {
    launchRequest(RequestOptions.cancellable()) {
        repository.load().collectSuccess { data ->
            repository.save(data).collectSuccess { }
        }
    }
}
```

### 3.3 Repository（无 UI）

```kotlin
class PatrolRepository @Inject constructor(
    private val api: PatrolApi
) : BaseNetworkRepository(boundApiService = api) {

    fun loadDetail(id: String): Flow<RequestResult<Patrol>> =
        request { api.getDetail(id) }   // 勿再传 showLoading / deliverErrorToUi
}
```

### 3.4 Room 协程

```kotlin
class PersonRepo(dao: PersonDao) : RoomDataRepository<Person, PersonDao>(dao)

// ViewModel
launchRequest(RequestOptions.cancellable(), flow = { personRepo.findAll() }) { list ->
    _list.value = list
}
```

### 3.5 分页（重要）

| 场景 | 用法 |
|------|------|
| 列表翻页 / 加载更多 | `NetworkFlowPagingViewModel`；**禁止**每页 `launchRequest` |
| 页内详情 / 删除 | `launchRequest(flow=…)` |
| override `createPagingSource` | **必须**传 `onAppError`，推荐 `defaultPagingAppErrorHandler()` |

默认已接 Host：

```kotlin
// NetworkFlowPagingViewModel 默认
NetworkPagingSource(..., defaultPagingAppErrorHandler())
```

---

## 4. 业务工程迁移步骤（建议顺序）

1. **改依赖**：`base` / `network` / `db` 全部 `2.0.0`，Sync。
2. **编译扫红**：搜索并删除  
   `createRepository` · `ensureRepository` · `launchUiRequest` · `BaseViewModel<` · `iRepository`。
3. **ViewModel**：去掉 Repository 泛型；构造注入仓库；请求改为 `launchRequest`。
4. **Repository**：协程栈改为只 `request { }` / Room `execute`，去掉 UI Options。
5. **分页**：确认继承 `NetworkFlowPagingViewModel` 且未丢掉 `onAppError`；详情用 Flow 重载。
6. **Rx 存量**：暂可 `BaseRxViewModel.subscribeRequest`；**新功能禁止**扩 Rx UI。
7. **回归**：  
   - 失败 Toast / 401·403 登录跳转  
   - Loading 返回取消请求  
   - 分页加载更多失败有 LoadState + Host  
   - 点 Loading 外侧**不会**取消  

---

## 5. 全仓检索关键词

```text
createRepository
ensureRepository
launchUiRequest
BaseViewModel<
iRepository
getRequestUi(
setRequestUi(
.collect {          // 在 launchRequest 块内对 RequestResult 慎用
trackUiRequest(
FlowRepositoryImpl
DefaultNetworkRepository
```

---

## 6. 模块变更摘要

### core-base `2.0.0`

- 无泛型 `BaseViewModel`；`launchRequest` Session 版 + **Flow 重载**（强制 Host）
- `RequestSession.collectSuccess` / `collectRequest` / `collectToState`
- `trackUiRequest` → `protected`
- Loading 可取消：仅系统返回 / 手势返回（点外侧永不取消）
- 删除：`createRepository` / `ensureRepository` / `launchUiRequest` / 泛型装配

### core-network `2.0.0`

- `BaseNetworkRepository`：仓库无 UI；`RequestOptions` UI 字段对本类无效
- `NetworkFlowPagingViewModel` + 非空 `NetworkPagingSource.onAppError`
- `defaultPagingAppErrorHandler()`；override 须接 Host
- Rx `RepositoryImpl` 保留，标注迁 Flow；不再靠 `ensureRepository` 注 UI

### core-db `2.0.0`

- `DefaultRoomRepository` / `BaseRoomRepository` / `RoomDataRepository`：**无 RequestUi**
- `RoomRequestOptions` 的 showDialog / deliverErrorToUi 对协程栈无效
- Rx `RoomRepositoryImpl` 仍可经 RequestUi（需业务自行注入，框架不自动 ensure）

---

## 7. 验收清单

- [ ] 三库均为 `2.0.0`，工程可编译
- [ ] 无 `createRepository` / `ensureRepository` / `launchUiRequest`
- [ ] 单次请求：失败进 Host（Toast / 业务码）；可取消 Loading 用返回键取消 Job
- [ ] 点 Loading 外侧不取消
- [ ] 分页 401 仍进 Host；加载更多无全屏 `launchRequest` Loading
- [ ] 新代码未再扩展仓库内 showLoading

---

## 8. FAQ

**Q：旧项目大量 Rx，必须一次改完吗？**  
A：不必。Rx 仓库可暂留；新页面用 Flow + `launchRequest`。Rx UI 用 `BaseRxViewModel`，勿扩新。

**Q：为什么仓库传 `showLoading=true` 没效果？**  
A：2.0 起 UI 只在 ViewModel。请把 Options 写在 `launchRequest(options, …)`。

**Q：可以继续在 Fragment 里 collect 仓库 Flow 吗？**  
A：不推荐。页面只观察 ViewModel 状态；请求经 `launchRequest` 才能统一 Loading / 取消 / Host。

**Q：`state` 参数是什么？**  
A：可选 `MutableStateFlow<UiState<T>>`，自动写 Loading/Success/Error；不传则只用 `onSuccess` 写 LiveData。

**Q：common 还是 4.8.0？**  
A：facade 版本以发布为准。即使用旧 common 坐标，也请**显式**依赖 `base/network/db:2.0.0`，避免传递到 1.x。
