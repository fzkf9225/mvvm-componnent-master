# commonmedia

可选媒体能力库：拍照、相册、压缩等（`MediaHelper` / `MediaBuilder` …）。

当前版本：**3.5.1**
Maven：`io.coderf.arklab.media:media:3.5.1`
namespace：`io.coderf.arklab.media`

---

## 职责

- 选图 / 拍摄 / 压缩等媒体流程
- 提供 Hilt 作用域下的 `MediaHelper`（`MediaModule`）
- **不**依赖 case 层 `userapi` / `:base`；Gateway 适配写在宿主 app

---

## 依赖

- 工程内仅 `api project(':core-log')`

---

## 何时依赖

**仅 app（组装层）** 依赖本模块并做 Gateway 绑定；业务模块应注入 `MediaGateway`（在 case `:base`），禁止直接依赖 media：

```gradle
// app
implementation 'io.coderf.arklab.media:media:3.5.1'
```

Demo 参考：`app/.../media/MediaHelperGateway`、`MediaGatewayModule`。

---

## MediaBuilder 可选配置

未设置时：图片压缩按**原图像素自动等比缩放**（不套 720×1280 框），再按 `imageQualityCompress` 做体积压缩。

```java
new MediaBuilder(context)
    .bindLifeCycle(this)
    .setImageQualityCompress(200)           // 小于该大小(kb)的图片跳过压缩，默认 200
    // .setImageCompressMaxSize(720, 1280)  // 可选：指定最大宽高框；不设则按原图等比缩放
    .setVideoSkipCompressUnderKb(0)         // 小于该大小(kb)的视频跳过压缩，默认 0=不跳过
    .setFileProviderAuthority(null)         // 空则 packageName.FileProvider
    .builder();
```

裁剪独立入口同样支持 `ImageCropBuilder.setFileProviderAuthority(...)`。

---

## 发布

```bash
./gradlew :commonmedia:publish
```
