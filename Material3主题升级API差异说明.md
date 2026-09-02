# AppCompat → Material3 主题升级：API 差异与开发注意点

对比范围：

- 升级前：`d:\AndroidDemoWorkSpace\mvvm-component-master-appcompat`（`Theme.AppCompat.Light.NoActionBar`）
- 升级后：当前工程 `mvvm-component-master`（`Theme.Material3.DayNight.NoActionBar`）

Material 库版本两边都是 `com.google.android.material:material:1.14.0`，`appcompat:1.7.1`。这次升级主要是**主题父类、颜色 token、控件实现与调用方式**，不是依赖版本 bump。Activity 基类仍是 `AppCompatActivity`，没有切到 `ComponentActivity`。

后续新页面、新自定义 View、改 Dialog / Toolbar，请按本文「升级后」写法，不要再套 AppCompat 时代的 `android:background`、`setSupportActionBar`、`R.color.white/black`。

---

## 1. 先记住的三条原则

1. **颜色走语义 token，不走白/黑硬编码。** 表面用 `?attr/colorSurface` 或 `R.color.cardSurface`，文字/图标用 `?attr/colorOnSurface` 或 `R.color.cardOnSurface`。暗色由 `values-night` 自动切换。
2. **Material 控件用 Material 属性。** `MaterialButton` 不要再用 `android:background="@drawable/xxx"` 填色；改用 `app:backgroundTint`、`app:cornerRadius`、`app:strokeColor`。代码里优先 `setBackgroundTintList()`，不要 `setBackground()` / `setBackgroundColor()`。
3. **Toolbar 不再挂 ActionBar。** 不要 `setSupportActionBar`、`onCreateOptionsMenu`、`getSupportActionBar()`。直接操作 `MaterialToolbar`：`setTitle` / `setNavigationIcon` / `inflateMenu`。

---

## 2. 主题与颜色体系

### 2.1 根主题

| | 升级前 | 升级后 |
|---|---|---|
| 文件 | `core-base/src/main/res/values/styles.xml` | 同路径 |
| 父主题 | `Theme.AppCompat.Light.NoActionBar` | `Theme.Material3.DayNight.NoActionBar` |
| 核心 attr | `colorPrimary` / `colorPrimaryDark` / `colorAccent` | 完整 M3 角色色 + 默认控件 style |
| 暗色 | 固定 Light + `forceDarkAllowed=true` | DayNight + `values-night` + `forceDarkAllowed=false` |

升级后 `AppBaseTheme` 还会在主题层指定默认控件：

```xml
<item name="toolbarStyle">@style/Widget.App.Toolbar</item>
<item name="materialButtonStyle">@style/Widget.Material3.Button</item>
<item name="textInputStyle">@style/Widget.Material3.TextInputLayout.OutlinedBox</item>
<item name="bottomNavigationStyle">@style/Widget.Material3.BottomNavigationView</item>
<item name="tabStyle">@style/customTabLayout</item>
<item name="borderlessButtonStyle">@style/Widget.Material3.Button.TextButton</item>
```

布局里写 `<MaterialButton>` / `<MaterialToolbar>` 时，即使不写 `style`，也会吃到这些默认值。需要扁平、无 inset 的按钮时，必须显式覆盖（见第 6 节）。

### 2.2 颜色 attr 对照（开发时最容易写错）

| 升级前习惯 | 升级后应使用 | 说明 |
|---|---|---|
| `colorAccent` | `colorSecondary` 或控件自己的 tint | 主题里已删除 `colorAccent` |
| `colorPrimaryDark` | `android:statusBarColor` + `colorSurface` | 不再作为主题 attr；资源名 `colorPrimaryDark` 仍在 `colors.xml`，不要当主题色用 |
| `colorControlNormal`（图标着色） | `navigationIconTint` / `?attr/colorOnSurface` | Toolbar 导航图标专用 `navigationIconTint` |
| `@color/white` 做卡片/Dialog 底 | `?attr/colorSurface` 或 `R.color.cardSurface` | 暗色下 white 会刺眼 |
| `@color/black` / `@color/autoColor` 做正文 | `?attr/colorOnSurface` 或 `R.color.cardOnSurface` | `autoColor` 已改成 `onSurfacePrimary` 的别名 |
| `@color/white` 做主色按钮字 | `@color/onPrimary` 或 `?attr/colorOnPrimary` | 暗色下主色变浅，字必须用 onPrimary |
| 分割线 `@color/h_line_color` | `?attr/colorOutlineVariant` | 媒体模块 ActionSheet 已这样改 |

`colorPrimary` 语义也变了：升级前它会连带影响 Toolbar / 状态栏；升级后它主要驱动**填充按钮、选中态、FAB**，Toolbar 默认走 `colorSurface` + `colorOnSurface`。品牌色 Toolbar（搜索页）用 `Widget.App.Toolbar.OnPrimary`。

### 2.3 代码里取色

两边都没有大规模使用 `MaterialColors.getColor()`，仍以 `ContextCompat.getColor` 为主。新代码建议：

```java
// 表面 / 卡片 / Dialog 底
ContextCompat.getColor(context, R.color.cardSurface);

// 正文 / 图标
ContextCompat.getColor(context, R.color.cardOnSurface);

// XML
android:textColor="?attr/colorOnSurface"
android:background="?attr/colorSurface"
app:backgroundTint="?attr/colorPrimary"
```

需要跟随当前 Dialog 主题时，解析 attr，不要写死资源：

```java
TypedValue tv = new TypedValue();
if (context.getTheme().resolveAttribute(
        com.google.android.material.R.attr.colorSurface, tv, true)) {
    return ContextCompat.getColor(context, tv.resourceId);
}
```

`googlegps` 的 `GPSConfirmDialog` 已按此方式取 `colorSurface`。

### 2.4 暗色模式

| | 升级前 | 升级后 |
|---|---|---|
| 主题 | Light 固定 | `DayNight` |
| `values-night/colors.xml` | 无（core-base） | 有完整暗色 palette |
| `forceDarkAllowed` | `true`（系统强制压暗，颜色不可控） | `false`（只走我们自己的 night 资源） |
| 状态栏图标 | `ToolbarConfig.isLightMode` 手动指定 | `ThemeUtils.setupStatusBarAuto()` 按背景亮度计算 |

新模块如果有独立颜色，必须同时提供 `values` 与 `values-night`。不要再依赖系统 Force Dark。

---

## 3. XML 控件标签对照

布局里几乎是一对一替换。新 XML 不要再写 `androidx.appcompat.widget.AppCompat*`。

| 升级前 | 升级后 | 典型文件 |
|---|---|---|
| `AppCompatTextView` | `MaterialTextView` | `dialog_confirm.xml`、`activity_login.xml` |
| `AppCompatEditText` | `TextInputEditText` | `dialog_input.xml`、`feedback.xml` |
| `AppCompatButton` | `MaterialButton` | `menu_dialog.xml`、`search_view.xml` |
| `AppCompatImageView` | `ShapeableImageView` | `adapter_media_add_item.xml`、`me_fragment.xml` |
| `AppCompatImageButton` | `MaterialButton`（IconButton）或 Toolbar 导航图标 | 旧 `view_title_bar.xml` 已删除 |
| `SwitchCompat` | 表单用自定义 `ShapeableImageView`；Demo 可用 `MaterialSwitch` | `FormSwitch` |
| `AppCompatSeekBar` | 系统 `SeekBar` | `video_layout_ark.xml` |
| `androidx.appcompat.widget.Toolbar` | `MaterialToolbar` / 工程内 `ActionToolbar` | `activity_coordinator.xml` |

`TextInputLayout` 目前多数表单**没有包一层**，只单独用 `TextInputEditText` 当 EditText。主题默认 `textInputStyle` 是 OutlinedBox，以后若包 `TextInputLayout`，会自动出描边框。

---

## 4. 同类、同方法：升级前后用法差异（重点）

这一节是后续开发最容易踩坑的地方：**类名还在，方法还在，内部实现和正确调用方式已经变了。**

### 4.1 `BaseActivity`：Toolbar 不再走 ActionBar

| | 升级前 | 升级后 |
|---|---|---|
| `getToolbar()` | 返回 `Toolbar` | 返回 `MaterialToolbar` |
| 初始化 | `setSupportActionBar` + `setDisplayHomeAsUpEnabled` | 只 `setToolbarConfig` + `setNavigationOnClickListener` |
| 默认背景 | `R.color.white` | `R.color.cardSurface` |
| 菜单 | 子类 `onCreateOptionsMenu` / `onOptionsItemSelected` | `inflateToolbarMenu(menuRes, listener)` |

升级前：

```java
setSupportActionBar(toolbarBind.mainBar);
getSupportActionBar().setDisplayHomeAsUpEnabled(true);
getSupportActionBar().setDisplayShowTitleEnabled(false);
```

升级后（不要再写上面三行）：

```java
toolbarBind.setToolbarConfig(createdToolbarConfig());
toolbarBind.mainBar.setNavigationOnClickListener(
        v -> getOnBackPressedDispatcher().onBackPressed());

// 菜单
inflateToolbarMenu(R.menu.menu_browser, item -> {
    if (item.getItemId() == R.id.toolbar_web_menu) { ... }
    return true;
});
```

`WebViewActivity` 已从 `onCreateOptionsMenu` 改成 `inflateToolbarMenu`。新 Activity 如果还 override `onCreateOptionsMenu`，菜单不会出现。

`createdToolbarConfig()` 默认色：

```java
// 前
.setBgColor(R.color.white)

// 后
.setBgColor(R.color.cardSurface)
```

重写 `createdToolbarConfig()` 时不要把背景设回 `R.color.white`。

### 4.2 `ToolbarDelegate` / `ToolbarHost`

签名从 `Toolbar` 换成 `MaterialToolbar`，并且**不再调用** `setSupportActionBar`。

升级前：

```kotlin
activity.setSupportActionBar(toolbar)
activity.supportActionBar?.apply {
    title = setup.title
    setDisplayHomeAsUpEnabled(setup.showUp)
}
```

升级后：

```kotlin
toolbar.title = setup.title
toolbar.isTitleCentered = setup.titleCentered
toolbar.setNavigationIcon(resolveUpIndicator())
toolbar.setNavigationOnClickListener { ... }
```

新增参数 `titleCentered`（默认 `true`）。调用方必须传 `MaterialToolbar`，不能再传 `androidx.appcompat.widget.Toolbar`。

同类签名一并改了：`ToolbarConfig` BindingAdapter、`EdgeToEdgeHelper.applyToolbarInsets`、`MenuUtil`。

### 4.3 `ToolbarConfig.applyStatusBar()`

| | 升级前 | 升级后 |
|---|---|---|
| 默认文字色 | `R.color.black` | `R.color.cardOnSurface` |
| 默认背景色 | `R.color.white` | `R.color.cardSurface` |
| 状态栏图标 | 跟 `isLightMode` 走 | `ThemeUtils.setupStatusBarAuto(activity, color)` 按亮度 |

品牌色 Toolbar 只要 `setBgColor(R.color.themeColor)`，状态栏图标会自动变成浅色；不必再手动 `setLightMode`。

### 4.4 `TitleBar`：从 ConstraintLayout 变成 MaterialToolbar

这是结构变化最大的自定义 View。对外链式 API 名字大多还在，但内部完全不同。

| | 升级前 | 升级后 |
|---|---|---|
| 基类 | `ConstraintLayout` | `MaterialToolbar` |
| 布局 | inflate `view_title_bar.xml` | 无 XML，代码里加右侧 `MaterialButton` |
| `view_title_bar.xml` | 存在 | **已删除**，不要再引用 |
| 返回键 | `AppCompatImageButton` + `setImageTintList` | `setNavigationIcon` / `setNavigationIconTint` |
| 标题 | 自己的 `AppCompatTextView` | Toolbar 自带 title，`setTitleCentered(true)` |
| `getTitleView()` | 返回 `AppCompatTextView`（稳定存在） | 返回内部 `TextView`，可能为 null，需 `post` 后再找 |
| `getRightView()` | `AppCompatTextView` | `MaterialButton`（当作 `TextView` 用） |
| `getBackButton()` | 稳定的 `AppCompatImageButton` | 遍历 child 找 `ImageButton`，可能为 null |
| 默认标题色 | `R.color.black` | `R.color.cardOnSurface` |
| 返回 | `Activity.onBackPressed()` | `OnBackPressedDispatcher` |

还能继续用的 API：

```java
titleBar.setTitle("标题")
        .setTitleColor(color)
        .setBackIcon(R.drawable.icon_fh)
        .setBackIconTint(color)
        .setShowBackButton(true)
        .setRightText("保存")
        .setOnBackClickListener(...)
        .bind("标题", listener);
```

不要再做的事：

```java
// 不要再当 ConstraintLayout 往里 addView 当普通容器
// 不要再 findViewById(R.id.title_bar_back / title_bar_title)
// 不要再强转 getTitleView() 为 AppCompatTextView
titleBar.getTitleView().setTypeface(...); // 可能 NPE，需判空
```

隐藏返回按钮：升级前是 `INVISIBLE`（占位），升级后是 `setNavigationIcon(null)`（不占位）。布局上会差出导航图标宽度。

### 4.5 `ActionToolbar`

```java
// 前
public class ActionToolbar extends Toolbar

// 后
public class ActionToolbar extends MaterialToolbar
```

布局里可以继续写 `<io.coderf.arklab.common.widget.customview.ActionToolbar>`。新能力：`app:titleCentered`、`app:navigationIconTint`。请加 `style="@style/Widget.App.Toolbar"`。

### 4.6 `CornerButton`：方法还在，禁止再 `setBackground`

```java
// 前
public class CornerButton extends AppCompatButton
    // 内部 GradientDrawable + setBackground()

// 后
public class CornerButton extends MaterialButton
    // ShapeAppearanceModel + setBackgroundTintList + setStrokeColor
```

对外方法名保持：`setBackColor`、`setRadius`、`setStroke`、`setBgColorAndRadius`、`setGradientDrawable` 等。

**关键行为变化：**

1. `setGradientDrawable(GradientDrawable)` 不再把 drawable 设为 background，只读取颜色/圆角再映射到 Shape。传入的 GradientDrawable 不会显示渐变。
2. 启用自定义圆角时会 `setInsetTop/Bottom(0)`、`setElevation(0)`，否则会露出 MaterialButton 默认上下 inset（看起来比旧按钮矮一截、两边有空隙）。
3. XML 里不要写 `android:background`；用 `app:bgColor`、`app:radius`、`app:strokeColor`（仍走原来的 `CornerTextView` attrs）。

错误 vs 正确：

```java
// 错误：会被 MaterialShapeDrawable 盖掉或行为异常
cornerButton.setBackground(drawable);
cornerButton.setBackgroundColor(Color.WHITE);
cornerButton.setBackgroundResource(R.drawable.round_theme_color);

// 正确
cornerButton.setBackColor(color);
cornerButton.setBgColorAndRadius(color, radiusPx);
cornerButton.setStroke(strokeColor, strokeWidthPx);
```

无自定义 attrs 的 `CornerButton` 会保留 Material3 Filled Button 的 inset / elevation。Dialog 底部按钮如果要铺满，必须设 `bgColor`/`radius`，或 XML：

```xml
android:insetTop="0dp"
android:insetBottom="0dp"
app:elevation="0dp"
```

### 4.7 `CornerImageView` / `RoundImageView`

| | 升级前 | 升级后 |
|---|---|---|
| 基类 | `AppCompatImageView` | `ShapeableImageView` |
| 裁剪 | 自己 `Canvas`/`Path`/`BitmapShader` | `ShapeAppearanceModel` |
| 描边 | 自绘 Paint | `setStrokeColor` / `setStrokeWidth` |

XML 的 `app:radius` 等自定义 attr 仍可用。也可以直接用 Material 属性：

```xml
<io.coderf.arklab.common.widget.customview.CornerImageView
    app:shapeAppearance="@style/..."
    app:strokeColor="?attr/colorOutline"
    app:strokeWidth="1dp" />
```

不要再 override `onDraw` 做 clip，也不要再对这两个类 `setScaleType` 期望旧 BitmapShader 行为。`RoundImageView` 现在是 50% 圆角的 `ShapeableImageView`。

唯一仍继承 `AppCompatImageView` 的是 `CirclePaddingImageView`（圆形底 + 内边距图标，不是图片圆角裁剪）。新需求不要再扩展 AppCompat 控件。

### 4.8 `CornerEditText` / `ClearableEditText` / `CustomSearchEditText`

基类：`AppCompatEditText` → `TextInputEditText`。

自定义圆角背景前必须清掉默认 underline/box：

```java
editText.setBackground(null);
editText.setBackground(customDrawable); // 或 CornerShapeHelper.createBackground(...)
```

`CornerEditText.setGradientDrawable()` 同样只映射颜色/圆角，不再 `setBackground(gradientDrawable)`。

`CounterEditText.getEditText()` 返回类型从 `AppCompatEditText` 改为 `TextInputEditText`，外部强转要改。

### 4.9 TextView 家族

`CornerTextView`、`CircleTextView`、`SquareLabelView`、`GradationRectTextView`、`ScalingTextView` 基类都改为 `MaterialTextView`。自绘/渐变逻辑基本不变，但会继承 M3 的 lineHeight / textAppearance。

返回类型 breaking：

| 方法 | 升级前 | 升级后 |
|---|---|---|
| `ValueLabelView.getValueTextView()` | `AppCompatTextView` | `MaterialTextView` |
| `ValueDisplayView.getValueTextView()` | `AppCompatTextView` | `MaterialTextView` |
| `IconDotTextView` / `IconLabelValueView` 内部 Image | `AppCompatImageView` | `ShapeableImageView` |

### 4.10 `SpeakButton`

`AppCompatButton` → `MaterialButton`。长按录音逻辑不变，但会吃到主题 `minHeight=48dp` 和 inset。若视觉变「更高/更扁」，需要像 `CornerButton` 一样清 inset。

### 4.11 表单控件

`FormConstraintLayout` 内部创建的子 View 类型全部换了，`instanceof` / 强转必须更新。

| 控件 | 升级前 | 升级后 | 注意 |
|---|---|---|---|
| `FormEditText` / `FormEditArea` | `new AppCompatEditText()` + 直接 `setBackground(drawable)` | `new TextInputEditText()`，**先 `setBackground(null)`** | 不先清空会叠一层 TextInput 默认背景 |
| `FormSwitch` | 内部 `SwitchCompat`；`getSwitchCompat()`；`setThumbTintList` / `setTrackTintList` | 内部 `ShapeableImageView`；**`getSwitchIcon()`**；点击切换 + 自定义 pill drawable | **breaking**，不要再调 `getSwitchCompat()` |
| `FormCheckbox` / `FormRadio` | `AppCompatImageView` + `AppCompatTextView` | `ShapeableImageView` + `MaterialTextView` | 视觉 API 差不多 |
| `FormStepper` | `AppCompatEditText` | `TextInputEditText`，同样 `setBackground(null)` | |
| `FormToggleIconAnimator.applyIcon` | 参数 `AppCompatImageView` | 参数 `ShapeableImageView` | 外部调用要改签名 |
| `FormDateRange` 默认色 | confirm=`black`，selected=`white` | confirm=`cardOnSurface`，selected=`onPrimary` | 自定义颜色请继续用语义色 |

`FormSwitch` 没有 `CompoundButton.OnCheckedChangeListener`，用点击 + `ObservableField`。不要把它改回 `MaterialSwitch`：官方 Switch 内部 padding 会撑乱表单行高。

### 4.12 Dialog：架构没换，默认色和按钮控件换了

工程**没有**使用 `AlertDialog.Builder` / `MaterialAlertDialogBuilder`，仍是 `BaseDialog extends Dialog`。变的是主题父类、默认表面色、布局里的控件。

| | 升级前 | 升级后 |
|---|---|---|
| Dialog 主题父类 | `@android:style/Theme.Dialog` | `App.Material3.Dialog` → `Theme.Material3.DayNight.Dialog` |
| ActionSheet | 同上 | `ThemeOverlay.Material3.MaterialAlertDialog`（commonui / googlegps / commonmedia） |
| `BaseDialog` 默认背景 | `Color.WHITE` | `R.color.cardSurface` |
| `ChoiceSelectDialog` | 无专门表面处理 | `applySurfaceBackground()`，16dp 圆角 + `cardSurface` |
| 布局文本 | `AppCompatTextView` | `MaterialTextView` |
| 取消按钮 | `AppCompatButton` + `android:background="@drawable/rounded_white"` | `MaterialButton` + `Widget.App.Button.TextButton` + `app:backgroundTint="?attr/colorSurface"` |

`MenuDialog.setupCancelButton()` 里如果调用方传了 `cancelButtonBackgroundColor`，仍会 `setBackgroundColor()`。对 `MaterialButton` 这不可靠，自定义取消按钮背景请改：

```java
buttonCancel.setBackgroundTintList(ColorStateList.valueOf(color));
```

布局侧正确示例（`menu_dialog.xml`）：

```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/button_cancel"
    style="@style/Widget.App.Button.TextButton"
    app:backgroundTint="?attr/colorSurface"
    app:cornerRadius="8dp"
    android:insetTop="0dp"
    android:insetBottom="0dp"
    android:textColor="?attr/colorOnSurface" />
```

新 Dialog 不要把父主题设回 `Theme.Dialog` / `Theme.AppCompat.*.Dialog`。模块内独立 Dialog（MQTT、GPS、媒体）已经分别改成 `Theme.Material3.DayNight.Dialog` 或 `ThemeOverlay.Material3.*`。

### 4.13 菜单与 Overflow

升级前 `WebViewActivity`：

```java
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_browser, menu);
    return true;
}
```

升级后：

```java
inflateToolbarMenu(R.menu.menu_browser, item -> { ... });
```

Overflow 样式：

```xml
<!-- 前 -->
<style name="OverflowMenuStyle" parent="Widget.AppCompat.PopupMenu.Overflow">
    <item name="android:background">@android:color/white</item>
</style>

<!-- 后 -->
<style name="OverflowMenuStyle" parent="Widget.Material3.PopupMenu.Overflow">
    <item name="android:background">?attr/colorSurface</item>
    <item name="android:textColor">?attr/colorOnSurface</item>
</style>
```

Coordinator 折叠 Toolbar：

```xml
<!-- 前 -->
android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar"
<androidx.appcompat.widget.Toolbar style="@style/Widget.AppCompat.Toolbar" />

<!-- 后 -->
android:theme="@style/ThemeOverlay.Material3.Dark.ActionBar"
<com.google.android.material.appbar.MaterialToolbar style="@style/Widget.Material3.Toolbar" />
```

### 4.14 全屏 / 视频播放隐藏标题栏

升级前通过 `getSupportActionBar().show()/hide()`。升级后 `VideoPlayerController` 判断 `toolbar instanceof MaterialToolbar` 再 `setVisibility`。嵌入播放不要再找 ActionBar。

---

## 5. MaterialButton 布局写法（新页面必看）

搜索按钮是典型对比。

升级前 `search_view.xml`：

```xml
<androidx.appcompat.widget.AppCompatButton
    android:background="@drawable/round_theme_color"
    android:textColor="@color/white" />
```

升级后：

```xml
<com.google.android.material.button.MaterialButton
    app:backgroundTint="@color/themeColor"
    app:cornerRadius="8dp"
    android:textColor="@color/onPrimary"
    android:minHeight="0dp"
    android:minWidth="0dp" />
```

常用对照：

| 目的 | 不要写 | 要写 |
|---|---|---|
| 填充色 | `android:background="@drawable/xxx"` | `app:backgroundTint="?attr/colorPrimary"` |
| 圆角 | 自定义 shape drawable | `app:cornerRadius="8dp"` |
| 描边按钮 | 带 stroke 的 shape | `style="@style/Widget.App.Button.OutlinedButton"` + `app:strokeColor` |
| 文字按钮 | 透明 background | `style="@style/Widget.App.Button.TextButton"` |
| 图标按钮 | `ImageButton` + ripple | `style="@style/Widget.App.Button.IconButton"` |
| 铺满高度 | 只设 `layout_height` | 同时 `android:insetTop="0dp"` `android:insetBottom="0dp"` |
| 字色（主色底） | `@color/white` | `@color/onPrimary` 或 `?attr/colorOnPrimary` |
| 水波纹 | 自定义 ripple drawable | 默认即可，或 `app:rippleColor="?attr/colorControlHighlight"` |

`android:background` 和 `app:backgroundTint` 一起写，tint 经常不生效或被 ShapeDrawable 吃掉。

工程里兼容旧 style 名：`Widget.App.Button` / `.TextButton` / `.OutlinedButton` / `.IconButton`，父类都是 `Widget.Material3.*`，并带 `minHeight=48dp`。表单按钮用 `Widget.App.Button.FormButton`（8dp 圆角）。

---

## 6. 其它升级后开发约定

### 6.1 状态栏 / 导航栏

`AppBaseTheme` 已设置：

```xml
<item name="android:statusBarColor">@android:color/transparent</item>
<item name="android:navigationBarColor">@color/cardSurface</item>
<item name="android:windowLightStatusBar">@bool/window_light_status_bar</item>
```

`values/bools.xml` 亮色为 `true`，`values-night/bools.xml` 为 `false`。页面级改状态栏继续用 `ToolbarConfig.applyStatusBar()`，不要自己 `window.setStatusBarColor(Color.WHITE)`。

### 6.2 TabLayout / BottomNavigationView

`customTabLayout` 父类：`Widget.Design.TabLayout` → `Widget.Material3.TabLayout`。背景从 `@color/white` 改为 `?attr/colorSurface`。`StyledTabLayout` / `IndicatorTabLayout` / `BottomNavBar` 类继承没变，只吃新主题。选中字色仍是品牌 `themeColor`。

### 6.3 EmptyLayout / 进度

加载动画从第三方 `genius.ui.widget.Loading` 换成 `CircularProgressIndicator`。空态背景从 `R.color.white` 换成 `R.color.cardSurface`。

### 6.4 Manifest 局部主题

`commonmedia` 中相关 Activity：`Theme.AppCompat.NoActionBar` → `Theme.Material3.DayNight.NoActionBar`。第三方库或独立 Activity 不要再贴 AppCompat 主题，否则控件和颜色会对不上。

### 6.5 未改、可继续用的 API

- 基类仍是 `AppCompatActivity` / `Fragment`
- `ContextCompat.getColor` / `getDrawable`、`DrawableCompat.setTint`
- `WindowCompat` / `WindowInsetsControllerCompat` / `EdgeToEdgeHelper`
- `Snackbar.make`（样式由主题提供）
- `BottomSheetDialog` 仍继承 Material 的 `BottomSheetDialog`
- 自定义 attr（`attrs.xml`）基本未改，`CornerTextView_bgColor` 等 XML 属性名可继续用

---

## 7. 自定义 View 基类速查

| 类 | 升级前 extends | 升级后 extends | 调用方要注意 |
|---|---|---|---|
| `TitleBar` | `ConstraintLayout` | `MaterialToolbar` | 结构变了；getter 类型/空安全变了 |
| `ActionToolbar` | `Toolbar` | `MaterialToolbar` | 菜单走 inflateMenu |
| `CornerButton` | `AppCompatButton` | `MaterialButton` | 禁止 setBackground；用 tint/shape |
| `SpeakButton` | `AppCompatButton` | `MaterialButton` | 注意 inset/minHeight |
| `CornerImageView` | `AppCompatImageView` | `ShapeableImageView` | 裁剪走 ShapeAppearance |
| `RoundImageView` | `AppCompatImageView` | `ShapeableImageView` | 不再自绘 BitmapShader |
| `PhotoView` | `AppCompatImageView` | `ShapeableImageView` | 缩放手势仍在 |
| `CirclePaddingImageView` | `AppCompatImageView` | **仍是 AppCompatImageView** | 有意保留 |
| `CornerTextView` 等 | `AppCompatTextView` | `MaterialTextView` | getter 返回类型 |
| `CornerEditText` 等 | `AppCompatEditText` | `TextInputEditText` | 先 setBackground(null) |
| `FormSwitch` | 内含 `SwitchCompat` | 内含 `ShapeableImageView` | `getSwitchCompat` → `getSwitchIcon` |

---

## 8. 新功能开发检查清单

写布局

- [ ] 不用 `AppCompatTextView` / `AppCompatButton` / `AppCompatEditText` / `AppCompatImageView`
- [ ] 按钮用 `MaterialButton` + `backgroundTint` / `cornerRadius`，需要贴边时加 `insetTop/Bottom=0dp`
- [ ] 颜色用 `?attr/colorSurface`、`?attr/colorOnSurface`、`?attr/colorPrimary`、`?attr/colorOnPrimary`
- [ ] 不用 `@color/white`、`@color/black` 当表面/正文（图标资源本身除外）

写 Activity / Toolbar

- [ ] 不调用 `setSupportActionBar`、不 override `onCreateOptionsMenu`
- [ ] 菜单用 `inflateToolbarMenu` 或 `toolbar.inflateMenu` + `setOnMenuItemClickListener`
- [ ] `getToolbar()` 按 `MaterialToolbar` 使用
- [ ] `createdToolbarConfig()` 背景用 `cardSurface`，不要 `white`

写自定义 View

- [ ] 文本/按钮/图片分别继承 `MaterialTextView` / `MaterialButton` / `ShapeableImageView`
- [ ] 圆角用 `ShapeAppearanceModel` 或 `CornerShapeHelper`，不要新写一套 `GradientDrawable` + `setBackground`
- [ ] 若必须对 `TextInputEditText` 设自定义背景，先 `setBackground(null)`

写 Dialog

- [ ] 主题用 `App.Material3.Dialog` 或 `ThemeOverlay.Material3.*`
- [ ] 表面色 `R.color.cardSurface`，不要 `Color.WHITE`
- [ ] 对 `MaterialButton` 改底色用 `setBackgroundTintList`

写颜色 / 暗色

- [ ] 新 color 在 `values` 和 `values-night` 成对出现，或引用已有语义色
- [ ] 模块级 Dialog 若覆盖 `colorSurface`，同时覆盖 `colorOnSurface`（参考 googlegps）

---

## 9. 文件索引

| 主题 | 升级后主要位置 |
|---|---|
| 全局主题 / 控件默认 style | `core-base/src/main/res/values/styles.xml` |
| 亮色 / 暗色 palette | `core-base/src/main/res/values/colors.xml`、`values-night/colors.xml` |
| Toolbar 逻辑 | `BaseActivity.java`、`ToolbarConfig.java`、`core-ui/.../ToolbarDelegate.kt` |
| 标题栏 | `TitleBar.java`、`ActionToolbar.java` |
| 圆角按钮 / 图 | `CornerButton.java`、`CornerImageView.java`、`CornerShapeHelper.java` |
| 表单 | `commonui/.../form/FormEditText.java`、`FormSwitch.java` |
| Dialog 基类 | `BaseDialog.java`、`ChoiceSelectDialog.java`、`MenuDialog.java` |
| 菜单示例 | `WebViewActivity.java` |

同路径对照升级前工程即可看到完整 diff。
