# 自定义 View 拆除：业务工程对照替换手册

> 面向即将发布的新版依赖。这是一次 **破坏性 API**：已删除的类没有兼容层、没有 Deprecated 过渡期。  
> 主题升级（AppCompat → Material3）见 [UPGRADE.md](UPGRADE.md) §1；本文只覆盖 **圆角 / 按钮 / 图片 / 输入框** 这一批控件拆除。  
> 更早的主题色、Toolbar、Dialog 写法仍以 [Material3主题升级API差异说明.md](Material3主题升级API差异说明.md) 为准，但其中 **Corner\* 仍存在** 的段落已过时，以本文为准。

**建议发布坐标（请在发布前同步改 `build.gradle`）：**

| 模块 | 4.6.0（主题升级，仍含 Corner\*） | 本次（拆除 Corner\*） |
|------|----------------------------------|------------------------|
| common | 4.6.0 | **4.7.0** |
| core-base | 1.1.0 | **1.2.0** |
| commonui | 3.6.0 | **3.7.0** |

其余 `core-*` 若未改行为可继续跟 1.1.0；若与 `core-base` 同发，建议一并升到 1.2.0。若 4.6.0 **尚未对外发布**，也可以把本次与主题升级捆成一次 4.6.0，但对外文档必须以本文为准，不能再写「CornerButton XML 属性名不变」。

---

## 0. 先做全库检索

升级依赖、Clean 之后，在业务工程里搜这些 **一定会编译失败** 的符号，按下表逐条改：

```
CornerButton
CornerImageView
RoundImageView
CornerTextView
CircleTextView
CornerConstraintLayout
CornerEditText
CounterEditText
R.styleable.CornerTextView
Custom_Round_Image_View
setBackColor(
setBgColorAndRadius(
setStrokeBgColorAndRadius(
setButtonStyle(
setGradientDrawable(
setCornerRadii(
```

`ClearableEditText` / `PasswordEditText` / `CustomSearchEditText` / `TitleBar` / Banner / Form\* **不要删**，它们还在。

---

## 1. 原则（三条就够）

1. **能用官方控件就用官方控件。** 新页面不要再包一层「圆角 XXXView」。
2. **`MaterialButton` 禁止 `setBackground()` / `setBackgroundColor()` / `android:background`。** 填色用 `app:backgroundTint` / `setBackgroundTintList()`，圆角用 `app:cornerRadius` / `setCornerRadius()`。
3. **需要在代码里给任意 View 铺圆角底时，调用 `CornerShapeHelper`，不要自己 new `GradientDrawable`。**

---

## 2. XML 标签替换总表

包名一律写全限定名。旧类全部在 `io.coderf.arklab.common.widget.customview`。

| 旧标签 | 新标签 | 典型场景 |
|--------|--------|----------|
| `...CornerButton` | `com.google.android.material.button.MaterialButton` | 圆角填充/描边按钮 |
| `...CornerImageView` | `com.google.android.material.imageview.ShapeableImageView` | 圆角图片、封面 |
| `...RoundImageView` | `ShapeableImageView` + `app:shapeAppearanceOverlay="@style/CircleShapeAppearance"` | 圆形头像 |
| `...CornerTextView` | 有底色/描边：包一层 `MaterialCardView`；纯文字：`MaterialTextView` | 圆角文字块、标签底 |
| `...CircleTextView` | `MaterialTextView` + `CornerShapeHelper.createOvalBackground(...)` 作 background；或 `MaterialCardView` 椭圆 | 圆形底文字 |
| `...CornerConstraintLayout` | 容器有阴影/描边：`com.google.android.material.card.MaterialCardView`；只要圆角底：`ConstraintLayout` + `app:bgColor`/`app:radius`（仅当该布局会走到 `CornerShapeHelper.applyFromAttributes`，见 §6） | 卡片、圆角区块 |
| `...CornerEditText` | 简单圆角输入：`TextInputEditText` + `android:background="@drawable/bg_input_outlined"`；要浮动 hint / 计数 / 官方 endIcon：包 `TextInputLayout` | 普通输入 |
| `...CounterEditText` | `TextInputLayout`（`app:counterEnabled="true"` + `app:counterMaxLength`）包 `TextInputEditText` | 带字数统计的多行输入 |

**不要改标签的（API 仍在）：**

| 类 | 说明 |
|----|------|
| `ClearableEditText` | 右侧清除图标（compound drawable） |
| `PasswordEditText` | 眼睛 + 可选清除；官方 TIL **做不到** 同时两种 endIcon |
| `CustomSearchEditText` | 搜索图标 + 清除 |
| `CirclePaddingImageView` | 圆形 **背景** 内缩图标，不是裁圆头像 |
| `CornerLabelView` | 三角形角标 |
| `TitleBar` / `ActionToolbar` / Banner / Tabs / `SpeakButton` | 业务形态控件 |
| `CircleProgressBar` / `HorizontalProgressBar` | 进度条带百分比文字 |
| Form\*（`FormEditText` 等） | 类型名不变，父类已改为 `ConstraintLayout` |

---

## 3. XML 属性对照

### 3.1 按钮：`CornerButton` → `MaterialButton`

| 旧（`app:`） | 新（`app:` / `android:`） | 备注 |
|--------------|---------------------------|------|
| `bgColor` | `backgroundTint` | 不要 `android:background` |
| `radius` | `cornerRadius` | px/dp 均可，官方 attr |
| `strokeColor` | `strokeColor` | 名称相同，但是 **MaterialButton 自己的 attr**，不再走 `ShapeView` |
| `strokeWidth` | `strokeWidth` | 同上 |
| `leftTopRadius` 等四角 | 官方 `MaterialButton` **不支持四角独立**。需要四角不同：改用 `MaterialCardView` 包文字，或代码 `CornerShapeHelper.shapeModel(...)` 后 `setShapeAppearanceModel` | |
| （无） | `icon` / `iconGravity` / `iconPadding` | 图标按钮 |
| （无） | `style="@style/Widget.App.Button.Flush"` | Dialog 底栏、要铺满 `layout_height` 且去掉 padding/elevation 时 |

主题已把默认 `materialButtonStyle` 指到 `Widget.App.Button`（`insetTop/Bottom=0`、`minHeight=0`）。因此 **业务 XML 一般不必再写 `android:insetTop/Bottom="0dp"`**。只有还要去掉左右 minWidth、内边距、阴影时才加 `.Flush`。

```xml
<com.google.android.material.button.MaterialButton
    android:layout_width="match_parent"
    android:layout_height="@dimen/height_xxl"
    android:text="确定"
    android:textColor="@color/onPrimary"
    android:textAllCaps="false"
    app:backgroundTint="@color/themeColor"
    app:cornerRadius="@dimen/radius_xxl"
    app:strokeColor="@color/theme_red"
    app:strokeWidth="2dp" />
```

铺满高度（对话框底栏等）再加：

```xml
style="@style/Widget.App.Button.Flush"
```

描边按钮可用 `style="@style/Widget.Material3.Button.OutlinedButton"`（主题默认已是 `Widget.App.Button.OutlinedButton`，inset 已清零）。

### 3.2 图片：`CornerImageView` / `RoundImageView` → `ShapeableImageView`

| 旧 | 新 | 备注 |
|----|----|------|
| `app:radius` | `app:shapeAppearanceOverlay="@style/RoundedShapeAppearance*"` | 或代码 `CornerShapeHelper.apply(imageView, radiusPx)` |
| 四角独立半径 | overlay 做不到四角不同；代码 `setShapeAppearanceModel(CornerShapeHelper.shapeModel(lt, rt, rb, lb))` | |
| `app:strokeColor` / `app:strokeWidth` | **同名** Material attr：`app:strokeColor`、`app:strokeWidth` | 画在 shape 边缘 |
| `RoundImageView` 圆形 | `app:shapeAppearanceOverlay="@style/CircleShapeAppearance"` | `cornerSize=50%` |
| `setScaleType` 期望旧 BitmapShader | 必须 `android:scaleType="centerCrop"`（或 `centerInside`） | 否则圆角裁切异常 |
| DataBinding `headerUrl` / `imageUrl` | 控件类型必须是 `ShapeableImageView` | 见 §8 |

框架已公开的 overlay（`core-base` / `layout_style.xml`）：

| style | 圆角 |
|-------|------|
| `CircleShapeAppearance` | 50% 圆 |
| `RoundedShapeAppearanceXs` | 2dp |
| `RoundedShapeAppearanceS` | `@dimen/radius_s`（4dp） |
| `RoundedShapeAppearanceM` | 8dp |
| `RoundedShapeAppearanceL` | 12dp |
| `RoundedShapeAppearanceXxl` | 24dp |

```xml
<!-- 圆形头像 -->
<com.google.android.material.imageview.ShapeableImageView
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:scaleType="centerCrop"
    app:shapeAppearanceOverlay="@style/CircleShapeAppearance"
    app:strokeWidth="1dp"
    app:strokeColor="?attr/colorOutline"
    app:headerUrl="@{user.avatar}" />

<!-- 圆角封面 -->
<com.google.android.material.imageview.ShapeableImageView
    android:layout_width="match_parent"
    android:layout_height="120dp"
    android:scaleType="centerCrop"
    app:shapeAppearanceOverlay="@style/RoundedShapeAppearanceL"
    app:imageUrl="@{item.cover}" />
```

### 3.3 容器 / 文字块：`CornerConstraintLayout` / `CornerTextView` → `MaterialCardView`

| 旧 | 新 |
|----|----|
| `app:bgColor` | `app:cardBackgroundColor` |
| `app:radius` | `app:cardCornerRadius` |
| `app:strokeColor` | `app:strokeColor` |
| `app:strokeWidth` | `app:strokeWidth` |
| （旧无阴影） | 默认 Card 有 elevation；要贴平用 `style="@style/Widget.App.Card.Flush"`（`cardElevation=0`、`strokeWidth=0`，描边请再单独写 `app:strokeWidth`） |

```xml
<com.google.android.material.card.MaterialCardView
    style="@style/Widget.App.Card.Flush"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardBackgroundColor="@color/cardSurface"
    app:cardCornerRadius="@dimen/radius_m"
    app:strokeColor="?attr/colorOutlineVariant"
    app:strokeWidth="1dp">

    <com.google.android.material.textview.MaterialTextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="标签"
        android:textColor="?attr/colorOnSurface" />
</com.google.android.material.card.MaterialCardView>
```

**例外：** `FormConstraintLayout` / `FormMedia` / `GridMenuView` 内部会 `CornerShapeHelper.applyFromAttributes`，XML 里 **继续写** `app:bgColor`、`app:radius`、`app:stroke*`、`app:leftTopRadius` 等即可，不必改成 Card。

### 3.4 输入框

#### A. 仍用自定义：`ClearableEditText` / `PasswordEditText` / `CustomSearchEditText`

属性 **不变**：

| attr | 含义 |
|------|------|
| `app:enableBgStyle` | 是否画圆角底（内部走 Shape，不是 `android:background`） |
| `app:bgColor` / `app:radius` / `app:strokeColor` / `app:strokeWidth` | 圆角底 |
| `app:clearIcon` | 清除图标，默认 `@mipmap/icon_clear` |
| `PasswordEditText`：`app:enableClear` | **默认请开** `true`，官方 TIL 无法同时眼睛+清除 |
| `PasswordEditText`：`app:enablePasswordToggle` | 眼睛开关 |
| `PasswordEditText`：`app:passwordVisibleIcon` / `passwordInvisibleIcon` | 默认 `ic_password_visible` / `ic_password_invisible` |
| `CustomSearchEditText`：`app:searchIcon`、`app:drawablePosition` 等 | `R.styleable.CustomEditText` |

```xml
<io.coderf.arklab.common.widget.customview.ClearableEditText
    android:layout_width="match_parent"
    android:layout_height="@dimen/height_xl"
    android:hint="可清除"
    app:enableBgStyle="true"
    app:bgColor="@color/cardSurface"
    app:radius="@dimen/radius_m"
    app:strokeColor="@color/themeColor"
    app:strokeWidth="1dp" />

<io.coderf.arklab.common.widget.customview.PasswordEditText
    android:layout_width="match_parent"
    android:layout_height="@dimen/height_xl"
    android:hint="密码"
    app:enableBgStyle="true"
    app:enableClear="true"
    app:enablePasswordToggle="true"
    app:bgColor="@color/cardSurface"
    app:radius="@dimen/radius_m"
    app:strokeColor="@color/themeColor"
    app:strokeWidth="1dp" />
```

#### B. 普通圆角输入：原 `CornerEditText`

```xml
<com.google.android.material.textfield.TextInputEditText
    android:layout_width="match_parent"
    android:layout_height="@dimen/height_xl"
    android:background="@drawable/bg_input_outlined"
    android:gravity="center_vertical"
    android:hint="请输入"
    android:paddingStart="@dimen/padding_l"
    android:paddingEnd="@dimen/padding_l" />
```

`bg_input_outlined` 在 `core-base`：表面色 + 12dp 圆角 + 1dp 描边。

#### C. 需要 hint 上浮 / 字数统计 / 官方清除：用 `TextInputLayout`

主题默认 `textInputStyle` 是 OutlinedBox。**高度 40dp 一类的紧凑框** 必须用 Dense，否则 M3 默认 16dp 上下 padding 会把字裁掉。

```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.App.TextInputLayout.Dense"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@null"
    app:placeholderText="请输入"
    app:endIconMode="clear_text">

    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="text" />
</com.google.android.material.textfield.TextInputLayout>
```

| 注意 | 原因 |
|------|------|
| Dense 的 `hintEnabled=false` | 浮动 hint 会占掉高度；提示改用 `placeholderText` 或 EditText `android:hint` |
| **不要** 密码框同时 `endIconMode=password_toggle` 又想要清除 | Material 只有一个 endIcon；用 `PasswordEditText` |
| 字数统计 | `app:counterEnabled="true"` + `app:counterMaxLength="200"`，替代已删除的 `CounterEditText` |
| 自定义 `ThemeOverlay.xxx.yyy` 必须 `parent=""` | 否则 AAPT 会去找不存在的 `ThemeOverlay.xxx` |

---

## 4. Java / Kotlin 方法对照

### 4.1 `CornerButton`（类已删除）

| 旧方法 | 替换 |
|--------|------|
| `setBackColor(int)` | `button.setBackgroundTintList(ColorStateList.valueOf(color))` 或 `CornerShapeHelper.applyTint(button, color)` |
| `setBackColor(ColorStateList)` | `setBackgroundTintList(list)`。**若变量已经是 ColorStateList，不要再 `valueOf` 包一层** |
| `setRadius(float)` | `button.setCornerRadius(Math.round(radiusPx))` 或 `CornerShapeHelper.apply(button, radius)` |
| `setStroke(color, width)` / `setStrokeColor` / `setStrokeWidth` | `setStrokeColor(ColorStateList.valueOf(c))` + `setStrokeWidth(px)` |
| `setBgColorAndRadius(color, radius)` | `CornerShapeHelper.apply(button, radius, color)` |
| `setStrokeBgColorAndRadius(stroke, width, bg, radius)` | `CornerShapeHelper.apply(button, radius, bg, width, stroke)` |
| `setBgColorAndCornerRadii` / `setCornerRadii` | MaterialButton 四角不能独立；改 Card 或 `setShapeAppearanceModel(CornerShapeHelper.shapeModel(...))` |
| `setButtonStyle(...)` | 拆成 tint + radius + stroke，或一次 `CornerShapeHelper.apply(button, radius, bg, strokeW, strokeC)` |
| `setGradientDrawable(GradientDrawable)` | **无等价**。渐变请用 `MaterialShapeDrawable` + shader，或 XML `<shape>`；禁止 `setBackground(gradient)` |
| `getLeftTopRadius()` 等 | 无。需要时自己持有 radius 变量 |

`CornerShapeHelper.apply(MaterialButton, ...)` 会同时把 inset / minHeight 清零。

### 4.2 `CornerImageView` / `RoundImageView`

| 旧方法 | 替换 |
|--------|------|
| `setRadius(int)` | `CornerShapeHelper.apply(imageView, radius)` 或 XML overlay |
| `setLeftTopRadius` 等 / `setCornerRadii` | `imageView.setShapeAppearanceModel(CornerShapeHelper.shapeModel(lt, rt, rb, lb))` |
| `setBgColor` | `imageView.setBackground(...)` 用 `CornerShapeHelper.createBackground(...)`，或 `CornerShapeHelper.apply(imageView, radius, bgColor)` |
| `setStroke(width, color)` | `imageView.setStrokeWidth(width)` + `imageView.setStrokeColor(ColorStateList.valueOf(color))` |
| `setStrokeBgColorAndRadius` | shape + background 分两步：`apply(imageView, radius, bg)` 再设 stroke |
| `getPaint()` / `setPaint` / `refreshCornerClip` | **删除，无替换**（不再自绘 clip） |
| `RoundImageView.setBorderColor/Width` | `setStrokeColor` / `setStrokeWidth` |
| `RoundImageView.setScaleType` | 继续用 `ImageView.setScaleType`，推荐 `CENTER_CROP` |

圆形：

```java
imageView.setShapeAppearanceModel(CornerShapeHelper.ovalModel());
// 或 XML CircleShapeAppearance
```

### 4.3 `CornerTextView` / `CircleTextView` / `CornerConstraintLayout` / `CornerEditText`

这些类上的 `setBackColor` / `setBgColor` / `setRadius` / `setStroke*` / `setBgColorAndRadius` / `setGradientDrawable` **全部删除**。

| 场景 | 替换 |
|------|------|
| 给任意 View 铺圆角底 | `CornerShapeHelper.apply(view, radius, bgColor)` |
| 带描边 | `CornerShapeHelper.apply(view, radius, bgColor, strokeWidth, strokeColor)` |
| 四角不同 | `CornerShapeHelper.apply(view, lt, rt, rb, lb, true, bg, true, strokeW, strokeC)` |
| 椭圆底 | `view.setBackground(CornerShapeHelper.createOvalBackground(bg, strokeW, strokeC))` |
| 卡片 | `MaterialCardView.setCardBackgroundColor` / `setRadius` / `setStrokeWidth` + `setStrokeColor` |
| 输入框动态改底 | 优先换 drawable；或 `CornerShapeHelper.apply(editText, ...)`（先保证没有 TIL box 背景冲突） |

### 4.4 仍保留方法的控件

`ClearableEditText` / `CustomSearchEditText` 仍有：

- `setBackColor(int)`
- `setBgColor(int)`
- `setRadius(float)`
- `setBgColorAndRadius(int, float)`
- `setStroke(int width, int color)`
- `setEnableBgStyle(boolean)`
- `setGradientDrawable`：只抽取颜色/圆角再映射，**不会**把渐变 drawable 设为 background

`ClearableEditText.setOnClearListener(OnClearListener)` 仍在。

`PasswordEditText`：`setEnableClear` / `setEnablePasswordToggle` / `setPasswordVisibleIcon` / `setPasswordInvisibleIcon` / `setBgColor` / `setRadius` / `isPasswordVisible()`。

`AutoTextView` / `MarqueeTextView` 的 `setBgColorAndRadius` **仍在**（控件本身没拆）。

### 4.5 Dialog / 按钮着色（业务里最常见的编译点）

| 旧写法 | 新写法 |
|--------|--------|
| `((CornerButton) btn).setBackColor(c)` | `((MaterialButton) btn).setBackgroundTintList(ColorStateList.valueOf(c))` |
| `dialogConfirm.setBackColor(positiveTextColor)` 且 `positiveTextColor` 已是 `ColorStateList` | `setBackgroundTintList(positiveTextColor)`，**不要** `ColorStateList.valueOf(positiveTextColor)` |
| 对 MaterialButton `setBackgroundResource(R.drawable.round_xxx)` | 改 tint + cornerRadius，或 `CornerShapeHelper` |

`UpdateMessageDialog` 链式 API（`setBgColor` / `setRadius` / `setStroke*`）仍在，内部改为 `CornerShapeHelper.apply(binding.updateBtn, ...)`。

---

## 5. `CornerShapeHelper` 公开 API

类：`io.coderf.arklab.common.widget.customview.CornerShapeHelper`（`core-base`）。

| 方法 | 用途 |
|------|------|
| `shapeModel(radius)` / `shapeModel(lt, rt, rb, lb)` | 构造 `ShapeAppearanceModel` |
| `ovalModel()` | 50% 相对圆角（圆/椭圆） |
| `createBackground(...)` | 生成 `MaterialShapeDrawable`（填充+可选描边） |
| `createOvalBackground(bg, strokeW, strokeC)` | 椭圆底 |
| `apply(View, radius, bgColor)` | 给任意 View 设 background |
| `apply(View, radius, bgColor, strokeW, strokeC)` | 带描边 |
| `apply(View, lt, rt, rb, lb, hasBg, bg, hasStroke, strokeW, strokeC)` | 四角+开关 |
| `apply(ShapeableImageView, radius)` | 只改裁剪圆角 |
| `apply(ShapeableImageView, radius, bgColor)` | 裁剪 + 底色 |
| `apply(MaterialButton, radius)` | 圆角并清 inset/minHeight |
| `apply(MaterialButton, radius, bgColor[, strokeW, strokeC])` | 圆角 + tint + 描边 |
| `applyTint(MaterialButton, bgColor)` | 只改填充 |
| `applyFromAttributes(View, AttributeSet)` | 读 **`R.styleable.ShapeView`**：`bgColor` / `radius` / `strokeColor` / `strokeWidth` / `leftTopRadius`… |
| `copyFromGradient(GradientDrawable, outRadii, outFill, outHasFill)` | 旧 `setGradientDrawable` 的内部迁移用 |

`R.styleable.CornerTextView` **已改名为** `R.styleable.ShapeView`，属性名未改。业务若 `obtainStyledAttributes(attrs, R.styleable.CornerTextView)` 会编不过，改成 `ShapeView`。

---

## 6. 主题默认 Style（升库后自动生效）

`AppBaseTheme` 现在指定：

| 主题 attr | style | 效果 |
|-----------|-------|------|
| `materialButtonStyle` | `Widget.App.Button` | inset=0，minHeight=0，`textAllCaps=false` → **`layout_height` 即可视高度** |
| `materialButtonOutlinedStyle` | `Widget.App.Button.OutlinedButton` | 同上 |
| `borderlessButtonStyle` | `Widget.App.Button.TextButton` | 同上 |
| `materialIconButtonStyle` | `Widget.App.Button.IconButton` | inset=0 |
| `materialCardViewStyle` | `Widget.Material3.CardView.Filled` | 填充卡片 |
| `textInputStyle` | `Widget.Material3.TextInputLayout.OutlinedBox` | 包 TIL 才生效 |

额外可选 style（布局里显式引用）：

| style | 何时用 |
|-------|--------|
| `Widget.App.Button.Flush` | 再清 minWidth、paddingTop/Bottom、elevation |
| `Widget.App.Button.Flush.TextButton` / `.Outlined` | 文本/描边按钮的 Flush 版 |
| `Widget.App.Card.Flush` | 无阴影卡片 |
| `Widget.App.TextInputLayout.Dense` | 紧凑 Outlined 输入，避免文字被裁 |

**不要** 再给每个按钮写一遍 `insetTop/Bottom=0`，主题已经处理。若宿主覆盖了 `AppBaseTheme` 且没继承这些 item，按钮会重新出现「高度比 layout_height 矮一截」。

---

## 7. 继承关系变化（库内部，业务若 `instanceof` / 强转要改）

| 类 | 旧父类 | 新父类 |
|----|--------|--------|
| `FormConstraintLayout` | `CornerConstraintLayout` | `androidx.constraintlayout.widget.ConstraintLayout` |
| `FormMedia` | `CornerConstraintLayout` | `ConstraintLayout` |
| `GridMenuView` | `CornerConstraintLayout` | `ConstraintLayout` |
| 已删除的 Corner\* | — | — |

Form / GridMenu XML 的 `app:bgColor` / `app:radius` **继续有效**（构造里 `applyFromAttributes`）。

`SpeakButton` 仍是 `MaterialButton`，现吃主题 inset=0，一般不必再手动清 inset。

---

## 8. DataBinding / Adapter 类型

`ImageViewAttrAdapter`（`core-base`）参数类型改为 `ShapeableImageView`：

| BindingAdapter | 必须用的 View |
|----------------|---------------|
| `headerUrl`（+ 可选 `placeholder` / `error`） | `ShapeableImageView`，并加圆形 overlay |
| `imageUrl` / `imageBitmap` / `imageUri` | `ShapeableImageView` |

布局里若仍是 `ImageView` / 已删除的 `RoundImageView`，DataBinding 会报找不到 adapter。

其它回调类型同样改为 `ShapeableImageView`：

- `PopupWindowSelectedAdapter.OnItemSelectedClearListener`
- `PopupWindowCheckBoxAdapter.OnItemSelectedChangedListener`
- `BaseMediaRecyclerViewAdapter.getClearLayoutParams(ShapeableImageView)`
- `PictureAdapter` 内部创建的也是 `ShapeableImageView`

业务实现这些 listener 时，把参数类型从 `CornerImageView` / `RoundImageView` / `ImageView` 改过来。

---

## 9. 已删除类与 styleable（编译错误速查）

**类（均无 stub）：**

- `io.coderf.arklab.common.widget.customview.CornerButton`
- `...CornerImageView`
- `...RoundImageView`
- `...CornerTextView`
- `...CircleTextView`
- `...CornerConstraintLayout`
- `...CornerEditText`
- `...CounterEditText`（Kotlin）

**styleable：**

- `R.styleable.CornerTextView` → `R.styleable.ShapeView`
- `R.styleable.Custom_Round_Image_View` 删除（改用 Material `stroke*` + overlay）
- `R.styleable.CounterEditText` / `CircleTextView` 删除
- `R.styleable.ClearableEditText` / `PasswordEditText` **保留**

---

## 10. 整改检查清单（业务工程）

布局

- [ ] 全局替换 XML 全限定类名（§2）
- [ ] `CornerButton` 的 `bgColor/radius` 改成 `backgroundTint/cornerRadius`
- [ ] 图片补 `android:scaleType="centerCrop"` + `shapeAppearanceOverlay`
- [ ] 头像不要再用 `CirclePaddingImageView`
- [ ] 卡片用 `cardBackgroundColor` / `cardCornerRadius`，贴平加 `Widget.App.Card.Flush`
- [ ] 紧凑 `TextInputLayout` 用 `Widget.App.TextInputLayout.Dense` + `placeholderText`
- [ ] 密码+清除继续用 `PasswordEditText`，`enableClear=true`

代码

- [ ] 删除所有 `import ...Corner*`
- [ ] `setBackColor` → `setBackgroundTintList` 或 `CornerShapeHelper`
- [ ] 已是 `ColorStateList` 的不要再 `valueOf`
- [ ] `R.styleable.CornerTextView` → `ShapeView`
- [ ] Binding / listener 参数改为 `ShapeableImageView`
- [ ] `instanceof CornerConstraintLayout` 改为 `ConstraintLayout` 或拿掉
- [ ] 自定义 `ThemeOverlay.A.B` 写 `parent=""`

主题

- [ ] 宿主继续 `android:theme="@style/AppBaseTheme"`
- [ ] 若自定义了 Theme 父类，补上 `materialButtonStyle=@style/Widget.App.Button` 等（§6）
- [ ] 品牌色底上的字用 `?attr/colorOnPrimary`，不要写死白

验证

- [ ] 登录 / 设置 / 反馈按钮：视觉高度 = `layout_height`
- [ ] 头像圆形、列表圆角图不变形
- [ ] 清除框、密码框图标大小与旧版一致，密码能同时清和切换可见
- [ ] 表单 `Form*` 圆角底仍在
- [ ] 亮色 / 暗色各走一遍

```bash
# 依赖升到新坐标后
./gradlew :app:assembleDebug
```

---

## 11. 与 4.6.0 文档的关系

| 文档 | 角色 |
|------|------|
| [UPGRADE.md](UPGRADE.md) §1 | AppCompat → Material3 主题、色板、Toolbar |
| **本文** | Corner\* 拆除后的控件/属性/方法对照 |
| [Material3主题升级API差异说明.md](Material3主题升级API差异说明.md) | 颜色 token、Dialog、Toolbar 细节；**忽略其中 CornerButton 方法还在的章节** |
| [README.md](README.md) | 仍保留的自定义 View 列表 |

升级路径建议：先按 §1 把主题升到 Material3，再按本文一次改完布局和 Java，最后升依赖、Clean、全量编译。
