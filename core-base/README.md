# core-base

框架核心 UI / MVVM 基座：Activity / Fragment / ViewModel、历史 widget、helper，以及 **common 体系的资源与 DataBinding**（R 包名保持 `io.coderf.arklab.common`）。

当前版本：**1.1.0**  
Maven：`io.coderf.arklab.core:base:1.1.0`  
namespace：`io.coderf.arklab.common`

> 注意：本模块 ≠ Demo case 的 Gradle `:base`。Maven 坐标是 `core:base`，对应工程模块是 **`:core-base`**。

**1.1.0（相对 AppCompat 1.0.9）**：`AppBaseTheme` 为 Material3 DayNight；Toolbar 为 `AppBarLayout` + `MaterialToolbar`；圆角控件走 `ShapeAppearance`。对外 API 不变。详见仓库 [UPGRADE.md](../UPGRADE.md)。

---

## 职责

- `BaseActivity` / `BaseFragment` / `BaseViewModel` 等基类
- Toolbar（`ActionToolbar` / `TitleBar`）、Loading、通用工具与大量历史 UI 组件
- 统一资源、M3 色板（含 `values-night`）与 DataBinding（业务 `R` / Binding 仍指向本模块包名）

---

## 依赖

- `api` → `:core-utils`、`:core-ui`、`:core-log`

业务侧通常通过 `:common` facade 间接依赖，无需单独拆 R。

---

## 何时依赖

需要基类 / 通用 UI / DataBinding 资源时（推荐经 `common`）：

```gradle
implementation 'io.coderf.arklab.core:base:1.1.0'
// 或
implementation project(':core-base')
```

---

## 发布

```bash
./gradlew :core-base:publish
```
