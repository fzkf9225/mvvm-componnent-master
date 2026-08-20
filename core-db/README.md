# core-db

本地持久化：Room Database / DAO / Entity，以及 `RoomRepository*` 等封装。

当前版本：**1.0.1**  
Maven：`io.coderf.arklab.core:db:1.0.1`  
namespace：`io.coderf.arklab.core.db`

---

## 职责

- Room 基础设施与通用 Attachment 等表能力
- Repository 层本地数据访问封装
- 配合 `:room-processor`（KSP）处理观测实体等注解

---

## 依赖

- `api` → `:core-base`
- `ksp` → `:room-processor`（本模块构建时）

业务侧一般经 `:common` 获得；直接依赖时注意 Room / KSP 版本对齐。

---

## 何时依赖

需要本地 Room 持久化时：

```gradle
implementation 'io.coderf.arklab.core:db:1.0.1'
// 或
implementation project(':core-db')
```

---

## 发布

```bash
./gradlew :core-db:publish
```
