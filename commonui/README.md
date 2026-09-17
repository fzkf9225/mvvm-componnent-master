# commonui

可选 UI 能力库：通用表单与若干可复用界面组件（日历、文件展示等）。

当前版本：**3.7.0**  
Maven：`io.coderf.arklab.ui:ui:3.7.0`  
namespace：`io.coderf.arklab.ui`

需要与 **core-base 1.2.0** 一起使用（`api` 传递）。升级说明见 [UPGRADE.md §2](../UPGRADE.md#2-120彻底剔除-baseview--mvp-页面绑定)。

---

## 版本

### Form 样式扩展（相对 3.7.0）

- 主题 attr `formStyle` + 默认 style `Widget.App.Form`；默认尺寸在 `dimens.xml`，宿主可全局覆盖
- 新增 `requiredText` / `requiredTextColor` / `bottomBorderHeight`
- 运行时：`setFormStyleOverlay(styleRes)`、`applyFormConfig(FormUiConfig)`

宿主 Theme 示例：

```xml
<item name="formStyle">@style/Widget.App.Form</item>
```

### 3.7.2（相对 3.7.0）

新增 `formStyle` 方式可全局配置默认样式能力，新增 `applyFormConfig` 配置方法和动态配置类文件 `FormUiConfig`,见 [FormUiConfig.java](../commonui/src/main/java/io/coderf/arklab/ui/api/FormUiConfig.java)

### 3.7.0（相对 3.6.2）

跟随 core-base 1.2.0：`FormMedia` 等去掉 `setBaseView`，改为 `setRequestUi(RequestUi)`（传 ViewModel 的 `NetworkRequestUiHost` 或其它 `RequestUi` 实现）。

### 3.6.1（相对 3.6.0）

跟随 core-base 1.1.1 的控件落地，表单容器不再自己铺圆角背景。

- `FormConstraintLayout` / `FormMedia` 改为继承 `CornerConstraintLayout`，XML `app:radius` / `app:bgColor` / `app:stroke*` 由父类处理
- 文件选择适配器等圆角改为 `CornerShapeHelper` + `ShapeableImageView`
- 登录等页的 `ShapeableImageView` 填色改为 `android:background`（`backgroundTint` 无效）

业务 XML 里若仍写已删除的 `CornerButton` / `CornerImageView` / `CornerEditText`，请改成官方控件，见 [core-base/README.md](../core-base/README.md)。

### 3.6.0（相对 3.5.1）

Material3 DayNight；日历选中日默认字色为 `onPrimary`。详见仓库 [UPGRADE.md](../UPGRADE.md)。

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
implementation 'io.coderf.arklab.ui:ui:3.7.0'
// 或
implementation project(':commonui')
```

> 勿与 `io.coderf.arklab.core:ui`（`:core-ui`）混淆。

---

## 发布

```bash
./gradlew :commonui:publish
```
