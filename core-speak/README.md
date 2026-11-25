# core-speak

可选语音能力库：仿微信输入法「按住说话」面板（`SpeakButton` / Overlay / 波形）、本地 AMR 录音、文件契约与 Room 元数据。**本期不含 ASR / 网关**；松手「编辑」仅回传录音结果，转写由宿主自行接入。

| 项 | 值 |
|----|-----|
| 当前版本 | **1.0.0** |
| Maven | `io.coderf.arklab.speak:speak:1.0.0` |
| namespace | `io.coderf.arklab.speak` |
| Gradle 模块 | `:core-speak` |

---

## 职责

- 按住说话 UI：`SpeakButton` + 底部 IME 风格面板（撤回 / 发送 / 可选编辑白弧）
- 麦克风权限前置说明（`ConfirmDialog` 风格）→ 系统 `RECORD_AUDIO`
- `MediaRecorder` AMR_NB 录音与振幅采样（驱动波形）
- 文件落盘契约 `VoiceFileStore`（默认 `files/Voice/{userId|All}/`）
- Room 元数据：`VoiceMessage` / `VoiceMessageRepository`（可与宿主 Paging 栈组合）
- 列表气泡播放：`SpeakerView`

**不**做：语音转文字、消息上行、Gateway 抽象（与 media/mqtt 不同，本期业务可直连本模块）。

---

## 包结构

`io.coderf.arklab.speak.*`：

| 包 | 内容 | 说明 |
| --- | --- | --- |
| `widget` | `SpeakButton` / `SpeakRecordOverlay` / `WaveformView` / `SpeakerView` | 宿主主要接入入口；XML 可引用 |
| `style` | `SpeakUiStyle` | 颜色 / 字号 / 文案 / 编辑开关 / 白弧高度等 |
| `permission` | `SpeakPermissionConfig` / `SpeakPermissionDialog` | 权限说明弹窗配置与展示 |
| `model` | `SpeakFinishAction` / `SpeakHitZone` / `VoiceRecordResult` | 手势结果与录音结果 |
| `record` | `VoiceRecorder` | MediaRecorder 封装 |
| `file` | `VoiceFileStore` / `DefaultVoiceFileStore` | 文件创建 / 删除 / 读取 |
| `db` | `VoiceDatabase` / `VoiceMessage` / Dao / Repository | 可选本地元数据；与业务库隔离 |

---

## 依赖

- `api project(':core-log')`
- `api project(':core-db')`（Room 协程栈 / `BaseDaoBean`；经 db 传递 base）
- `implementation project(':core-utils')`
- AndroidX Room / AppCompat / Material / Activity 等（见 `build.gradle`）

Manifest 声明：`RECORD_AUDIO`。

---

## 何时依赖

App 或需要语音录入的业务模块按需引入（**不经** `common` facade 传递）：

```gradle
implementation 'io.coderf.arklab.speak:speak:1.0.0'
// 或
implementation project(':core-speak')
```

Demo：`SpeakDemoActivity` + `SpeakSmartPagingFragment`（Room + SmartRefresh + Paging3）。

---

## 用法说明

### 1. XML 放置按住说话按钮

```xml
<io.coderf.arklab.speak.widget.SpeakButton
    android:id="@+id/btnSpeak"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="按住说话"
    app:speakAccentColor="#2ECF9B"
    app:speakWaveformColor="#2ECF9B"
    app:speakEditEnabled="true"
    app:speakEditArcHeight="112dp" />
```

常用 attrs：`speakCancelTextColor` / `speakSendTextColor` / `speakEditTextColor` / `speakShowEditArc` / `speakActionTextSize` 等（细分请用 `SpeakUiStyle`）。

### 2. 代码配置与回调

```kotlin
btnSpeak.setUserId("demo")
btnSpeak.setPermissionConfig(
    SpeakPermissionConfig.defaults(this) {
        showRationaleDialog = true
    }
)
btnSpeak.setSpeakUiStyle(
    SpeakUiStyle.defaults(this) {
        editEnabled = true
        editArcHeightPx = DensityUtil.dp2px(this@SpeakDemoActivity, 112f).toFloat()
        cancelTextColor = SpeakUiStyle.DEFAULT_CANCEL // 默认红
        // sendTextColor / editTextSizePx / actionCancel / hintRecording …
    }
)
btnSpeak.setOnSpeakListener { action, result ->
    when (action) {
        SpeakFinishAction.Send -> { /* result 非空：入库或上传 */ }
        SpeakFinishAction.Edit -> { /* 本期无 ASR，自行转写或打开编辑页 */ }
        SpeakFinishAction.Cancel -> { /* 文件已删 */ }
    }
}
```

手势约定：未明显上滑松手 → `Edit`（若 `editEnabled`，否则按发送逻辑）；左上滑撤回、右上滑发送。

### 3. 自定义文件目录

```kotlin
btnSpeak.setVoiceFileStore(DefaultVoiceFileStore(context, folderName = "MyVoice"))
// 或实现 VoiceFileStore
```

### 4. Room 元数据 + Paging（可选）

```kotlin
val repo = VoiceMessageRepository(
    VoiceDatabase.getInstance(app).voiceMessageDao(),
    DefaultVoiceFileStore(app)
)
// insertRecord / deleteMessage；或配合 RoomPagingSource + PagingViewModel
```

列表播放可用 `SpeakerView`（`TYPE_MINE` / `TYPE_OTHER` + `start(path)`）。

---

## SpeakUiStyle 可配置项（摘要）

| 类别 | 字段 |
|------|------|
| 颜色 | `waveformColor` / `accentColor` / `hintTextColor` / `cancelTextColor` / `sendTextColor` / `editTextColor` / `pillBackground` / `editArcBackground` / `glowColor` … |
| 字号 | `hintTextSizePx` / `editTextSizePx` / `cancelTextSizePx` / `sendTextSizePx` |
| 布局 | `editArcHeightPx`（白弧高度，默认约 112dp） |
| 能力 | `editEnabled`（编辑文案 + Edit 回调）、`showEditArc`（是否绘制白弧，可独立） |
| 文案 | `hintRecording` / `hintEdit` / `hintSend` / `hintCancel` / `actionEdit` / `actionSend` / `actionCancel` / `tooShort` … |

---

## 混淆

`consumer-rules.pro` 随 AAR 发布：对外 `widget` / `style` / `permission` / `model` / `record` / `file` / `db` 保留；宿主开启 R8 一般无需再配。细则见模块内 `consumer-rules.pro` 注释。

---

## 版本变更

> 新版本请插在本节最上方；保持「版本号 + 日期 + 要点列表」格式，便于持续迭代。

### 1.0.0 — 2026-11-25

首发：从 `core-base` 迁出语音能力，独立可选模块。

**能力**

- `SpeakButton` 仿微信 IME 按住说话：撤回 / 发送 / 可选编辑白弧；豆形按钮文字居中；撤回默认红色
- `SpeakUiStyle` + XML attrs：弧高、编辑开关、撤回/发送/编辑色与字号等可配
- `SpeakPermissionDialog` 对齐 `ConfirmDialog`（与 base / gnss 权限引导同风格）
- `VoiceRecorder`（AMR_NB）+ `WaveformView` 振幅动画
- `VoiceFileStore` / `DefaultVoiceFileStore`；`VoiceDatabase` + `VoiceMessageRepository`（Room 协程栈）
- `SpeakerView` 气泡播放
- Demo：`SpeakDemoActivity`（NavHost）+ `SpeakSmartPagingFragment`（`BaseSmartPagingFragment` + Room Paging）
- `consumer-rules.pro` / `proguard-rules.pro` 随模块发布

**边界**

- 无 ASR / Gateway；`SpeakFinishAction.Edit` 仅回传本地录音结果
- Room 库与业务库隔离（`io_coderf_arklab_speak_voice`）

---

## 发布

```bash
./gradlew :core-speak:publish
```

需配置环境变量：`ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`。
