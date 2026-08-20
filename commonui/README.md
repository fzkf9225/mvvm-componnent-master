# commonui

可选 UI 能力库：通用表单与若干可复用界面组件（日历、文件展示等）。

当前版本：**3.5.1**  
Maven：`io.coderf.arklab.ui:ui:3.5.1`  
namespace：`io.coderf.arklab.ui`

---

## 职责

- 表单组件与通用 UI 封装
- 依赖核心栈（`core-base` / `core-network`），部分能力内部使用 `commonmedia`

---

## 依赖

- `api` → `:core-base`、`:core-network`
- `implementation` → `:commonmedia`

**不**再依赖 `common` facade。宿主若只引本库，会带上对应 core；媒体实现按需由 app 侧处理 Gateway。

---

## 何时依赖

需要表单 / 通用 UI、超出 `core-base` 内置 widget 时：

```gradle
implementation 'io.coderf.arklab.ui:ui:3.5.1'
// 或
implementation project(':commonui')
```

> 勿与 `io.coderf.arklab.core:ui`（`:core-ui`）混淆。

---

## 发布

```bash
./gradlew :commonui:publish
```
