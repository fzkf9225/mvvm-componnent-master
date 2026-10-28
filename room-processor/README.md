# room-processor

KSP 处理器：为带 `@RoomObservedEntity` 的 Room Dao 生成 `XxxDaoRawQueryBridge`，自动绑定正确的 `observedEntities`。

当前版本：**1.2.0**  
Maven：`io.coderf.arklab.room:room-processor:1.2.0`  
配套：`io.coderf.arklab.core:db:1.5.0`（见 [core-db/README.md](../core-db/README.md)）

---

## 1.2.0 变更摘要

相对 **1.1.0**：

| 项 | 说明 |
|----|------|
| suspend RawQuery | Bridge 新增 `doFindSuspend` / `doQueryListSuspend` / `doCountSuspend` / `doExecuteSuspend` |
| 用途 | 支撑 `BaseRoomDao` 协程查询 / 条件删除（core-db **1.5.0**） |
| 兼容 | 原有 Rx / sync / LiveData 的 8 个 `do*` 仍生成，存量 Dao 可平滑升级 |

升级到 core-db 1.5.0 且使用协程 `*Suspend` 动态查询时，**必须**使用本版本 processor，否则 KSP 生成的 Bridge 缺少 suspend 抽象方法，Room / 编译会失败。

---

## 职责

扫描 `io.coderf.arklab.common.annotation.RoomObservedEntity`，为每个 Dao 生成：

```text
abstract class XxxDaoRawQueryBridge : BaseRoomDao<Entity>() {
    // LiveData / Single / Flowable / sync
    // + suspend（1.2.0）
}
```

手写 Dao 改为：

```kotlin
@Dao
@RoomObservedEntity(Person::class)
abstract class PersonDao : PersonDaoRawQueryBridge() {
    override fun getTableName() = "Person"
}
```

无需再手写 6～12 个 `@RawQuery(observedEntities = …)`。

---

## 接入

**本仓库（多模块）：**

```gradle
plugins {
    alias(libs.plugins.ksp)
}
dependencies {
    implementation project(':core-db')   // 或 common facade
    ksp project(':room-processor')
}
```

**外部 Maven：**

```gradle
implementation 'io.coderf.arklab.core:db:1.5.0'
ksp 'io.coderf.arklab.room:room-processor:1.2.0'
```

注解仍在包名 `io.coderf.arklab.common.annotation`（随 `core-base` / `common` 传递）。

---

## 版本对应关系

| core-db | room-processor | 说明 |
|---------|----------------|------|
| 1.5.0 | **1.2.0** | 协程 RawQuery Bridge |
| ≤1.4.x | 1.1.0 | 仅 Rx / sync / LiveData Bridge |

---

## 发布

```bash
./gradlew :room-processor:jar :room-processor:publish
```

环境变量：`ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`。  
产物：`groupId=io.coderf.arklab.room`，`artifactId=room-processor`。
