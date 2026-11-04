# commonui

可选 UI 能力库：通用表单与若干可复用界面组件（日历、文件展示等）。

当前版本：**3.7.7**  
Maven：`io.coderf.arklab.ui:ui:3.7.7`  
namespace：`io.coderf.arklab.ui`

需要与 **core-base ≥1.7.2** 一起使用（`api` 传递；当前配套 **1.7.2**）。升级说明见 [UPGRADE.md](../UPGRADE.md)。

---

## 版本

### 3.7.7

- `FormMedia` 新增 `app:preferThumbnail`（默认 `true`）与 `setPreferThumbnail(boolean)`：列表/宫格小图优先 `AttachmentBean.thumbnailPath`，无则回退 `path`
- 经已有 `applyPreviewPhotoConfigure` 下发到媒体 Adapter；大图预览、上传仍用 `path`
- 需配合 **core-base ≥1.7.2**

详见 [UPGRADE.md §0.4.3](../UPGRADE.md#043-附件小图优先缩略图base-172--ui-377)。

### 3.7.6

- **破坏性**：移除 `app:formInputType` / `app:formImeOptions`，请改用 `android:inputType` / `android:imeOptions`（由 `FormEditText` / `FormEditArea` 读取并应用到内部 EditText）
- 删除 `attr.xml` 中对系统 inputType/imeOptions flag 的冗余拷贝

### 3.7.5

- `FormHost.validateAll` 覆盖 `FormConstraintLayout` 与 `FormMedia`（`FormValidatable`）
- 媒体 `required` + `minCount`（默认 1）参与整页校验；`setError` 仅底线变色
- 日期/时间选择确认写回 `dataSource`；子类读属性统一走 `obtainFormUi`（formStyle 生效）
- `formEditable`：显式 false 等同 `setEnabled(false)`；未设置不改变现状
- FormRichText 走基类 dataSource 生命周期；Binding AttrChanged / TextWatcher 防重复注册

### 3.7.4

- `FormHost.validateAll` / `validate()` 薄 API
- Form dataSource 回调防泄漏
- `FormMedia.itemBg` 走主题色

### Form 样式扩展（相对 3.7.0）

- 主题 attr `formStyle` + 默认 style `Widget.App.Form`；默认尺寸在 `dimens.xml`，宿主可全局覆盖
- 新增 `requiredText` / `requiredTextColor` / `bottomBorderHeight`
- 运行时：`setFormStyleOverlay(styleRes)`、`applyFormConfig(FormUiConfig)`

宿主 Theme 示例：

```xml
<item name="formStyle">@style/Widget.App.Form</item>
```

### 样式优先级（只记这一条）

```
控件 XML app:*  >  Theme formStyle  >  Widget.App.Form  >  dimens / ThemeAttrs
```

运行时另有 `setFormStyleOverlay` / `applyFormConfig(FormUiConfig)`，**仅用于换肤或动态表单**；日常请：

1. 在宿主 Theme 挂 `formStyle`（继承 `Widget.App.Form` 改默认）
2. 单控件例外才写 XML attrs
3. 不要四通道齐上

### 校验

- 单行：`form.validate()`（`required` 且空 → `setError`）；媒体看条数是否 ≥ `minCount`（默认 1）
- 整页：`FormHost.validateAll(root)`（遍历 `FormValidatable`：输入行 + 媒体行，失败聚焦并滚动）
- **不**默认绑定 `annotation` 的 `EntityValidator`

```java
if (!FormHost.validateAll(binding.formContainer)) {
    return;
}
// 提交
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
implementation 'io.coderf.arklab.ui:ui:3.7.7'
// 或
implementation project(':commonui')
```

> 勿与 `io.coderf.arklab.core:ui`（`:core-ui`）混淆。

---

## 发布

```bash
./gradlew :commonui:publish
```
