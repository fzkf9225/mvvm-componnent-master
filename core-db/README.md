# core-db

本地持久化：Room Database / DAO / Entity，以及 `RoomRepository*` 等封装。

当前版本：**1.2.1**  
Maven：`io.coderf.arklab.core:db:1.2.1`  
namespace：`io.coderf.arklab.core.db`

---

## 职责

- Room 基础设施与通用 Attachment 等表能力
- Repository 层本地数据访问封装（含 `RoomRequestOptions`）
- Debug 日志：`RoomLog`（对齐网络 `ApiRetrofit.printLog`）
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
implementation 'io.coderf.arklab.core:db:1.2.1'
// 或
implementation project(':core-db')
```

---

## Debug 日志

与网络请求同一开关：`Config.getInstance().enableDebug(true)`。

| 能力 | 说明 |
|------|------|
| 仓库操作 | `RoomRepositoryImpl` 自动打印 Operation / Table / Options / Payload / Duration / Result |
| 动态 SQL | `BaseRoomDao` RawQuery 自动打印 SQL + Bind Args |
| 全部 SQL | 构建 DB 时套 `RoomLog.attachTo(builder)`，可覆盖 `@Insert` / `@Update` 等生成语句 |

```java
YourDatabase db = RoomLog.attachTo(
        Room.databaseBuilder(context, YourDatabase.class, "xxx")
                .allowMainThreadQueries()
).build();
```

Logcat 过滤 tag：`RoomDb`。开启 `setResponseBodyLogConverterJson(true)` 时，结果体会额外走 JSON 美化输出。

---

## 发布

```bash
./gradlew :core-db:publish
```
