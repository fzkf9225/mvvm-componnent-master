# common

兼容门面（facade）：业务侧推荐的 **单一入口**，通过 `api` 透出 `core-*`，本身几乎无业务实现。

当前版本：**4.5.1**  
Maven：`io.coderf.arklab.common:common:4.5.1`  
namespace：`io.coderf.arklab.common.facade`

---

## 职责

- 聚合依赖：`api` → `core-network` / `core-db` / `core-ui`（进而带上 `core-base` / `core-utils`）
- 新旧 API 桥接（如 `RequestUiBridge`）
- 让旧工程继续只依赖一个坐标即可用完整核心栈

---

## 何时依赖

宿主 / 业务模块希望「一个依赖拿齐核心能力」时使用：

```gradle
implementation 'io.coderf.arklab.common:common:4.5.1'
// 或工程内：
implementation project(':common')
```

也可不经 facade，直接依赖各个 `core-*`（见仓库根目录 [MODULES.md](../MODULES.md)）。

---

## 发布

```bash
./gradlew :common:publish
```

需环境变量 `ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`。
