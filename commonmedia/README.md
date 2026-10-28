# commonmedia

可选媒体能力库：选图 / 拍照 / 录像 / 压缩 / 水印 / 裁剪等（`MediaHelper` / `MediaBuilder` …）。

| 项 | 值 |
|----|-----|
| 当前版本 | **3.7.0** |
| Maven | `io.coderf.arklab.media:media:3.7.0` |
| namespace | `io.coderf.arklab.media` |

---

## 职责

- 选图、拍摄、文件/音频选择、压缩、水印、独立裁剪页
- 提供 Hilt 作用域下的 `MediaHelper`（`MediaModule`）
- **不**依赖 case 层 `userapi` / `:base`；Gateway 适配写在宿主 app

---

## 依赖

- 工程内仅 `api project(':core-log')`
- 对外：`appcompat` / `material` / `activity` / `lifecycle` / Hilt / ExifInterface 等（见 `build.gradle`）

---

## 何时依赖

**仅 app（组装层）** 依赖本模块并做 Gateway 绑定；业务模块应注入 `MediaGateway`（在 case `:base`），禁止直接依赖 media：

```gradle
// app
implementation 'io.coderf.arklab.media:media:3.7.0'
```

Demo 参考：`app/.../media/MediaHelperGateway`、`MediaGatewayModule`、`MediaActivity`。

---

## 用法说明

### 1. Hilt 注入（推荐）

Activity / Fragment 内注入即可，默认：`MediaPickerTypeEnum.PICK`、开启拍照 EXIF 写入、各类型最大选 9。

```java
@Inject
@ActivityMediaHelper   // Fragment 用 @FragmentMediaHelper
MediaHelper mediaHelper;
```

注入后可再改配置（无需重新 `builder()`）：

```java
mediaHelper.getMediaBuilder()
    .setImageMaxSelectedCount(5)
    .setVideoMaxSelectedCount(2)
    .setChooseType(MediaPickerTypeEnum.PICK)
    .setWriteCaptureExifMetadata(true)
    .setImageQualityCompress(200)
    .setImageSelectedListener(() -> imageList.size());
```

> `@ActivityMediaHelper` 要求宿主为 `ComponentActivity`（含 `AppCompatActivity`）。

### 2. 手动创建

```java
MediaHelper mediaHelper = new MediaBuilder(this)
    .bindLifeCycle(this)                 // 必填：绑定 Lifecycle
    .setChooseType(MediaPickerTypeEnum.PICK)
    .setImageQualityCompress(200)
    // .setImageCompressMaxSize(720, 1280)  // 可选：最大宽高框；不设则原图等比缩放
    .setVideoSkipCompressUnderKb(0)      // 小于该 kb 跳过视频压缩；0=不跳过
    .setFileProviderAuthority(null)      // 空则 packageName.FileProvider
    .builder();
```

### 3. 打开选择 / 拍摄

| 场景 | API |
|------|-----|
| 底部 ActionSheet（相册+拍照） | `openImageDialog(view, OpenImageDialog.CAMERA_ALBUM)` |
| 仅相册 / 仅拍照 | `openImg()` / `camera()` |
| 视频选择 / 录像 | `openShoot()` / `shoot()` |
| 图+视频混合 | `openMedia()` / `openMediaDialog(...)` |
| 音频 / 文件 | `openAudio()` / `openFile()` 或 `openFileDialog(...)` |

权限说明弹窗 → 系统授权 → 拒绝后提示去设置，由库内处理；业务侧一般只需调上述入口。

### 4. 结果回调

**分路 LiveData（历史兼容）：**

```java
mediaHelper.getMutableLiveData().observe(this, bean -> { /* 选择 */ });
mediaHelper.getMutableLiveDataCompress().observe(this, bean -> { /* 压缩 */ });
mediaHelper.getMutableLiveDataWaterMark().observe(this, bean -> { /* 水印 */ });
```

**统一结果（推荐新代码）：**

```java
// Java
mediaHelper.getMediaResultLiveData().observe(this, result -> {
    if (result instanceof MediaResult.Pick) { ... }
    else if (result instanceof MediaResult.Compress) { ... }
    else if (result instanceof MediaResult.WaterMark) { ... }
});
```

```kotlin
// Kotlin Flow
lifecycleScope.launch {
    mediaHelper.mediaResultFlow.collect { result ->
        when (result) {
            is MediaResult.Pick -> { }
            is MediaResult.Compress -> { }
            is MediaResult.WaterMark -> { }
        }
    }
}
```

`MediaBean.mediaType`：`IMAGE` / `VIDEO` / `IMAGE_AND_VIDEO` / `AUDIO` / `FILE`。

### 5. 压缩 / 水印

```java
mediaHelper.startCompressImage(uriList);
mediaHelper.startCompressVideo(uriList);
mediaHelper.startCompressMedia(uriList);   // 按 MIME 自动分图片/视频
mediaHelper.startWaterMark(bitmap);        // 或 startWaterMark(bitmap, alpha)
```

### 6. 独立裁剪

```java
new ImageCropBuilder(this)
    .setSourceUri(uri)
    // .setFileProviderAuthority("xxx.FileProvider")
    .setOnImageCropListener(new OnImageCropListener() { ... })
    .start();
```

### 7. 组装层 Gateway（Demo）

业务模块只依赖 `:base` 的 `MediaGateway`；app 里用 `MediaHelper` 适配：

- `app/.../media/MediaHelperGateway`
- `app/.../di/MediaGatewayModule`

### 8. 宿主注意

- 声明与本库一致的 **FileProvider**（默认 authority：`包名.FileProvider`）
- 库 Manifest 已声明媒体读/相机/录音/可选定位等；勿再依赖库内无关权限（3.7.0 已瘦身）
- 录像公共目录默认落在 **Movies**（非 Pictures）；可用 `setDefaultVideoSubPath` 自定义相对路径

---

## MediaBuilder 常用配置

未设置压缩宽高时：图片按**原图像素自动等比缩放**，再按 `imageQualityCompress`（默认 200kb）做体积压缩。

| 方法 | 说明 |
|------|------|
| `bindLifeCycle` | 必填 |
| `setChooseType` | `DEFAULT` 传统选择器 / `PICK` Photo Picker |
| `setImageMaxSelectedCount` 等 | 各类型最大可选数 |
| `setImageQualityCompress` | 小于该 kb 跳过图片压缩 |
| `setImageCompressMaxSize` | 可选最大宽高框 |
| `setVideoQuality` / `setVideoCompressTimeoutMs` | 视频质量与超时 |
| `setWriteCaptureExifMetadata` | 拍照后写姿态/GPS 等到 EXIF |
| `setShowPermissionDialog` | 是否展示授权前说明弹窗 |
| `setPermissionPositiveTextColor` 等 | 仅 setter 调用后覆盖；默认跟 M3 主题色 |
| `setFileProviderAuthority` | 非默认 FileProvider 时必设 |
| `setSavePublic` / `setDefaultImageSubPath` / `setDefaultVideoSubPath` | 输出目录 |

---

## 版本变更

> 新版本请插在本节最上方；保持「版本号 + 日期 + 要点列表」格式，便于持续迭代。

### 3.7.0 — 2026-10-28

权限与回调加固、Manifest 瘦身、Dialog/M3 对齐与若干体验优化（在 3.6.0 能力之上）。

**缺陷修复**

- 权限回调：无任何授予时不再执行 continuation；有授予再续传，并防止拒绝后死循环弹窗
- `openAudio` / `openFile`：授权成功后自动继续打开选择器
- `openMedia`：图片或视频任一侧缺权限都会申请（修正原先「与」逻辑漏申请）
- `ImgCompressor`：失败只回调 `onCompressFail`，不再失败后再 `onCompressEnd(RESULT_OK)`
- 自定义权限「确定」监听：业务回调后仍会 `launch` 系统授权，避免只改文案/埋点时流程中断

**精简与主题**

- 库 Manifest 移除与媒体无关的敏感权限（开机、电话状态等）
- `PermissionReminderDialog` 复用 `TipDialog` + `MediaBaseDialog`（横屏/刘海补偿不变）
- 确认框 / 未自定义的按钮色对齐 M3 语义色；裁剪确定按钮默认跟宿主 `colorPrimary`
- 录像公共目录改为 `DIRECTORY_MOVIES`
- 裁剪页源图改为后台解码，降低主线程 ANR 风险
- `MediaModule` 抽取默认配置；非 `ComponentActivity` 给出明确异常

### 3.6.0

> （发布时未整理说明；若需补历史能力变更，可在此追加。）

---

## 发布

```bash
./gradlew :commonmedia:publish
```

需配置环境变量：`ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`。
