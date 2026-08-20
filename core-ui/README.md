# core-ui

轻量 Activity 委托层：Toolbar / 初始化策略等（`ToolbarDelegate`、`InitDataPolicy` …）。大量历史控件仍在 `core-base`。

当前版本：**1.0.1**  
Maven：`io.coderf.arklab.core:ui:1.0.1`  
namespace：`io.coderf.arklab.core.ui`

---

## 职责

- Activity 生命周期与 UI 策略委托，降低基类膨胀
- 与 `core-base` 配合，而不是替代表单 / 业务 UI 库（那是 `commonui`）

---

## 依赖

- 无其它工程 `project` 依赖（被 `core-base` / `common` 引用）

---

## 何时依赖

一般随 `common` / `core-base` 带入；仅在做极细拆分时显式依赖：

```gradle
implementation 'io.coderf.arklab.core:ui:1.0.1'
// 或
implementation project(':core-ui')
```

> 勿与 `io.coderf.arklab.ui:ui`（`:commonui`）混淆。

---

## 发布

```bash
./gradlew :core-ui:publish
```
