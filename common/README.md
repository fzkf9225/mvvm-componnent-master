# common

兼容门面（facade）：业务侧推荐的 **单一入口**，通过 `api` 透出 `core-*`，本身几乎无业务实现。

当前版本：**4.8.0**  
Maven：`io.coderf.arklab.common:common:4.8.0`  
namespace：`io.coderf.arklab.common.facade`

---

## 4.8.0

透传 Loading 可取消能力：**core-base ≥1.9.0** · **core-network ≥1.8.0** · **core-db ≥1.7.0**。  
协程下载真内核请显式钉 **core-base ≥1.10.0**（见 [UPGRADE.md §0.2](../UPGRADE.md#02-协程下载真内核base-1100)）。  
用法见 [QuickStart.md](../QuickStart.md)、[UPGRADE.md §0.3](../UPGRADE.md#03-loading-可取消base-190--network-180--db-170)。

---

## 职责

- 聚合依赖：`api` → `core-network` / `core-db` / `core-ui`（进而带上 `core-base` / `core-utils` / `core-log` / `core-autosize`）
- 作为业务侧单一依赖入口（不再提供 RequestUi 桥接；统一使用 `RequestUi`）
- 让旧工程继续只依赖一个坐标即可用完整核心栈

---

## 何时依赖

宿主 / 业务模块希望「一个依赖拿齐核心能力」时使用：

```gradle
implementation 'io.coderf.arklab.common:common:4.8.0'
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
