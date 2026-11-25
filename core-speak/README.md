# core-speak

可选语音能力库：仿微信输入法「按住说话」面板、本地 AMR 录音、文件契约与 Room 元数据。  
**推荐入口**是 [`SpeakHelper.attach(View)`](src/main/java/io/coderf/arklab/speak/SpeakHelper.kt)——把能力挂到任意 View（底栏图标 / `ImageView` / 普通 `Button` 等）。`SpeakButton` 仅为带 XML attrs 的便捷封装。  
**本期不含 ASR / 网关**；松手「编辑」仅回传录音结果，转写由宿主自行接入。

| 项 | 值 |
|----|-----|
| 当前版本 | **1.0.0** |
| Maven | `io.coderf.arklab.speak:speak:1.0.0` |
| namespace | `io.coderf.arklab.speak` |
| Gradle 模块 | `:core-speak` |

---

## 职责

- **SpeakHelper**：权限 → 录音 → 上滑热区 → 松手回调，可 bind 到任意 View
- 底部 IME 风格面板（撤回 / 发送 / 可选编辑白弧）
- 麦克风权限前置说明（`ConfirmDialog`）→ 系统 `RECORD_AUDIO`
- `MediaRecorder` AMR_NB + 振幅波形
- `VoiceFileStore` / Room `VoiceMessageRepository`（可选）
- `SpeakerView` 列表气泡播放
- `SpeakButton`：可选 Material 按钮壳，内部委托 Helper

**不**做：语音转文字、消息上行、Gateway。

---

## 包结构

`io.coderf.arklab.speak.*`：

| 包 / 类 | 内容 | 说明 |
| --- | --- | --- |
| `SpeakHelper` | `attach` / `detach` + 配置链 | **推荐接入入口** |
| `callback` | `OnSpeakListener` / `OnSpeakHoldUiListener` | 松手结果 / 按住态 UI |
| `widget` | `SpeakButton` / Overlay / `WaveformView` / `SpeakerView` | 便捷控件与面板 |
| `style` | `SpeakUiStyle` | 颜色 / 字号 / 文案 / 编辑开关 / 白弧高度 |
| `permission` | `SpeakPermissionConfig` / Dialog | 权限说明 |
| `model` | `SpeakFinishAction` / `SpeakHitZone` / `VoiceRecordResult` | 结果模型 |
| `record` / `file` / `db` | 录音、文件、Room | 按需使用 |

---

## 依赖

- `api project(':core-log')`
- `api project(':core-db')`
- `implementation project(':core-utils')`
- AndroidX Room / AppCompat / Material / Activity 等（见 `build.gradle`）

Manifest：`RECORD_AUDIO`。权限申请要求宿主为 `ComponentActivity`。

---

## 何时依赖

```gradle
implementation 'io.coderf.arklab.speak:speak:1.0.0'
// 或
implementation project(':core-speak')
```

Demo：`SpeakDemoActivity`（`SpeakHelper.attach` 普通按钮）+ `SpeakSmartPagingFragment`。

---

## 用法说明

### 1. 推荐：挂到任意 View（底栏图标等）

```kotlin
private var speakHelper: SpeakHelper? = null

override fun onCreate(...) {
    val helper = SpeakHelper.create(this)
        .setUserId("uid")
        .setPermissionConfig(SpeakPermissionConfig.defaults(this))
        .setSpeakUiStyle(SpeakUiStyle.defaults(this) {
            editEnabled = true
        })
        .setOnHoldUiListener { holding ->
            // ImageView 可忽略；Button 可切文案
        }
        .setOnSpeakListener { action, result ->
            when (action) {
                SpeakFinishAction.Send -> { /* 入库 / 上传 */ }
                SpeakFinishAction.Edit -> { /* 自行 ASR */ }
                SpeakFinishAction.Cancel -> Unit
            }
        }
    speakHelper = helper
    helper.attach(binding.aiIcon) // 任意 View
}

override fun onDestroy() {
    speakHelper?.detach()
    super.onDestroy()
}
```

手势：未明显上滑松手 → `Edit`（若 `editEnabled`，否则按发送）；左上滑撤回、右上滑发送。

### 2. 可选：XML `SpeakButton` 便捷封装

```xml
<io.coderf.arklab.speak.widget.SpeakButton
    android:id="@+id/btnSpeak"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:speakAccentColor="#2ECF9B"
    app:speakEditEnabled="true"
    app:speakEditArcHeight="112dp" />
```

内部会 `attach(this)`，并自动切换「按住说话 / 松开发送」文案；高级场景仍可用 `btnSpeak.getSpeakHelper()`。

### 3. 自定义文件目录

```kotlin
helper.setVoiceFileStore(DefaultVoiceFileStore(context, folderName = "MyVoice"))
```

### 4. Room 元数据 + Paging（可选）

```kotlin
val repo = VoiceMessageRepository(
    VoiceDatabase.getInstance(app).voiceMessageDao(),
    DefaultVoiceFileStore(app)
)
```

---

## SpeakUiStyle 可配置项（摘要）

| 类别 | 字段 |
|------|------|
| 颜色 | `waveformColor` / `accentColor` / `cancelTextColor` / `sendTextColor` / `editTextColor` … |
| 字号 | `hintTextSizePx` / `editTextSizePx` / `cancelTextSizePx` / `sendTextSizePx` |
| 布局 | `editArcHeightPx`（默认约 112dp） |
| 能力 | `editEnabled`、`showEditArc` |
| 文案 | `hintRecording` / `actionEdit` / `actionSend` / `actionCancel` … |

---

## 混淆

`consumer-rules.pro` 随 AAR 发布：`SpeakHelper` / `callback` / `widget` / `style` / `permission` / `model` / `record` / `file` / `db` 保留。

---

## 版本变更

> 新版本请插在本节最上方。

### 1.0.0 — 2026-11-25

首发：独立可选语音模块。

**能力**

- **`SpeakHelper`**：按住说话逻辑与 View 解耦，`attach` / `detach` 挂到任意 View
- `SpeakButton` 薄封装（attrs + 文案），内部委托 Helper
- `SpeakUiStyle` / 权限说明 / AMR 录音 / 波形 / Room 元数据 / `SpeakerView`
- Demo：`SpeakHelper.attach` + `SpeakSmartPagingFragment`

**边界**

- 无 ASR / Gateway；`Edit` 仅回传本地录音结果

---

## 发布

```bash
./gradlew :core-speak:publish
```

需环境变量：`ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`。
