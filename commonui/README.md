# commonui

可选 UI 能力库：通用表单与若干可复用界面组件（日历、文件展示等）。

当前版本：**3.6.0**  
Maven：`io.coderf.arklab.ui:ui:3.6.0`  
namespace：`io.coderf.arklab.ui`

---

## 职责

- 表单组件与通用 UI 封装（日历选中日默认字色为 `onPrimary`，与品牌色底配对）
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
implementation 'io.coderf.arklab.ui:ui:3.6.0'
// 或
implementation project(':commonui')
```

> 勿与 `io.coderf.arklab.core:ui`（`:core-ui`）混淆。

`FormConstraintLayout` / `FormMedia` 现继承 `ConstraintLayout`（不再继承已删除的 `CornerConstraintLayout`）。XML 的 `app:bgColor` / `app:radius` 仍有效。业务工程其它 Corner\* 替换见 [WIDGET_MIGRATION.md](../WIDGET_MIGRATION.md)（建议本库 **3.7.0** 与 common 4.7.0 同发）。

---

## 发布

```bash
./gradlew :commonui:publish
```
