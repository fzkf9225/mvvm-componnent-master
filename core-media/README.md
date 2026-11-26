# core-media

可选媒体能力库：选图 / 拍照 / 录像 / 压缩 / 水印 / 裁剪等（`MediaHelper` / `MediaBuilder` …）。

| 项 | 值 |
|----|-----|
| 当前版本 | **4.0.0** |
| Maven | `io.coderf.arklab.media:media:4.0.0` |
| namespace | `io.coderf.arklab.media` |
| Gradle 模块 | `:core-media`（原 `:commonmedia`） |

> **模块重命名**：工程模块由 `commonmedia` 改为 `core-media`；**包名与 Maven 坐标不变**（`io.coderf.arklab.media:media`）。依赖请写 `project(':core-media')` 或上述 Maven 坐标。

---

## 职责

- 选图、拍摄、文件/音频选择、压缩、水印、独立裁剪页
- 提供 Hilt 作用域下的 `MediaHelper`（`MediaModule`）
- **不**依赖 case 层 `userapi` / `:base`；Gateway 适配写在宿主 app

---

## 依赖

- 工程内：`api project(':core-log')`、`implementation project(':core-utils')`
- Media3 Transformer / Effect / Common **1.11.1**（视频压缩）
- 对外：`appcompat` / `material` / `activity` / `lifecycle` / Hilt / ExifInterface 等（见 `build.gradle`）

---

## 何时依赖

**仅 app（组装层）** 依赖本模块并做 Gateway 绑定；业务模块应注入 `MediaGateway`（在 case `:base`），禁止直接依赖 media：

```gradle
// app
implementation 'io.coderf.arklab.media:media:4.0.0'
```

Demo 参考：`app/.../media/MediaHelperGateway`、`MediaGatewayModule`、`MediaCompressActivity`。

---

## 用法说明

### 1. Hilt 注入（推荐）

```java
@Inject
@ActivityMediaHelper   // Fragment 用 @FragmentMediaHelper
MediaHelper mediaHelper;
```

```java
mediaHelper.getMediaBuilder()
    .setImageMaxSelectedCount(5)
    .setVideoMaxSelectedCount(2)
    .setChooseType(MediaPickerTypeEnum.PICK)
    .setWriteCaptureExifMetadata(true)
    .setImageTargetSizeKb(200)
    .setImageSkipUnderKb(200)
    .setImageMaxLongSide(1920)
    .setVideoPreset(VideoExportPreset.MEDIUM)
    .setImageSelectedListener(() -> imageList.size());
```

### 2. 手动创建

```java
MediaHelper mediaHelper = new MediaBuilder(this)
    .bindLifeCycle(this)
    .setChooseType(MediaPickerTypeEnum.PICK)
    .setImageCompressOptions(new ImageCompressOptions(1920, 200, 200, ImageOutputFormat.WEBP, 80))
    .setVideoCompressOptions(new VideoCompressOptions(VideoExportPreset.MEDIUM, null, null, true, 0, 120_000L))
    .setFileProviderAuthority(null)
    .builder();
```

### 3. 压缩入口

```java
mediaHelper.startCompressImage(uriList);
mediaHelper.startCompressImage(uriList, imageOptions); // 覆盖 Builder
mediaHelper.startCompressVideo(uriList);
mediaHelper.startCompressVideo(uriList, videoOptions);
mediaHelper.startCompressMedia(uriList); // 按 MIME 分流，串行
```

默认：**图片 WebP**、目标约 200KB；**视频 MEDIUM**（长边约 1280、约 2.5Mbps），优先 H.265，失败自动回退 H.264。

### 4. Gateway（业务侧）

业务只调 `:base` 的 `MediaGateway` + `GatewayImageCompressOptions` / `GatewayVideoCompressOptions`。  
app 的 `MediaHelperGateway` 负责映射到 media 内部 Options。

```kotlin
mediaGateway.compressImages(uris) { out -> /* 上传 */ }
mediaGateway.compressImages(uris, GatewayImageCompressOptions.JpegUpload) { out -> }
mediaGateway.compressVideos(uris, GatewayVideoCompressOptions.LowFast) { out -> }
```

---

## MediaBuilder 压缩相关配置（4.0）

| 方法 | 说明 |
|------|------|
| `setImageCompressOptions` | 完整图片 Options |
| `setImageTargetSizeKb` | 目标体积 kb（默认 200） |
| `setImageSkipUnderKb` | 小于该 kb 跳过（默认 200） |
| `setImageMaxLongSide` | 长边上限 px（默认 1920；0=不限） |
| `setVideoCompressOptions` | 完整视频 Options |
| `setVideoPreset` | `LOW` / `MEDIUM` / `HIGH` |
| `setVideoSkipCompressUnderKb` | 视频跳过阈值；0=不跳过 |
| `setVideoCompressTimeoutMs` | 超时毫秒；0=不超时（Builder 默认 120s） |

**已删除（破坏性）：** `setImageQualityCompress`、`setImageCompressMaxSize`、`setVideoQuality` / `VideoQualityEnum`、自研 `ImgCompressor` / `VideoController` / mp4parser。

体积策略：图片超标时 **二分 quality**，必要时再降长边；视频码率为 preset 表定量级，**禁止**再使用 `w×h×系数`。

---

## 版本变更

### 4.0.0 — 2026-11-26

**破坏性变更：压缩管线重写**

- 视频：Media3 Transformer 1.11.1；优先 HEVC，失败回退 H.264；`VideoExportPreset` 替代 `VideoQualityEnum`
- 图片：ImageDecoder（API 28+）/ BitmapFactory 兜底；默认 WebP；二分 quality
- 调度：`CompressScheduler` 串行；删除旧 `*CompressHandler` 与 mp4parser / isoparser
- Gateway：`compressImages/Videos` 支持 base 侧精简 Options（方案 A）
- Maven 版本 **4.0.0**

### 3.7.0 — 2026-10-28

权限与回调加固、Manifest 瘦身、Dialog/M3 对齐与若干体验优化。

---

## 发布

```bash
./gradlew :core-media:publish
```
