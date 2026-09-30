# core-autosize

今日头条密度适配（JessYan AndroidAutoSize）独立模块：通过 `InitProvider` 自动初始化，改写 `DisplayMetrics` 实现全局 dp/sp 等比缩放。

当前版本：**1.0.1**  
Maven：`io.coderf.arklab.core:autosize:1.0.1`  
namespace / Java 包：`io.coderf.arklab.core.autosize`  
子包：`strategy` / `lifecycle` / `external` / `unit` / `util`；公开 API：`CustomAdapt` / `CancelAdapt` / `OnAdaptListener` 在根包

---

## 职责

- `AutoSize` / `AutoSizeCompat` / `AutoSizeConfig`：密度换算与启停
- `InitProvider`：ContentProvider 自动 init（authority = `${applicationId}.autosize-init-provider`）
- `CustomAdapt` / `CancelAdapt`：单页自定义或取消适配
- Dialog 等场景可配合 `AutoSizeCompat` 在 inflate 前同步宿主密度

---

## 1.0.1 兼容调优（非换方案）

针对 targetSdk 35 / Android 14+ / Edge-to-Edge 的点状修复：

| 项 | 说明 |
|----|------|
| `ScreenUtils` | API 30+ 用 `WindowMetrics` 取当前窗口；低版本回退 `Display` |
| 分屏 / 旋转 | `WrapperAutoAdaptStrategy` 适配前自动 `refreshScreenSize` |
| 字体缩放 | API 34+ 用 `Configuration.fontScale`；`setExcludeFontScale(true)` 同步写 fontScale |
| Dialog | 临时改密度后用 `Configuration.setTo` 还原，避免 `updateConfiguration` |

---

## 依赖

- 无其它工程 `project` 依赖
- `implementation` → `androidx.core:core-ktx`、`androidx.appcompat`

本仓库内由 `:core-base` `api` 引用，经 `common` facade 传递。

---

## 何时依赖

一般经 `core-base` / `common` 间接使用；仅需屏幕适配、避免拉基座时可显式：

```gradle
implementation 'io.coderf.arklab.core:autosize:1.0.1'
// 或
implementation project(':core-autosize')
```

宿主 Manifest（可选）：

```xml
<meta-data
    android:name="design_width_in_dp"
    android:value="360" />
<meta-data
    android:name="design_height_in_dp"
    android:value="640" />
```

框架侧开关见 `Config.setAutoSizeEnabled`（须在 `Config.init` 前设置才对首个 Activity 生效）。

不跟随系统字体（Android 14+ 已兼容）：

```java
AutoSizeConfig.getInstance().setExcludeFontScale(true);
```

---

## 发布

```bash
./gradlew :core-autosize:publish
```
