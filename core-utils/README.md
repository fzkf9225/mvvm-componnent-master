# core-utils

轻量工具层：尽量不绑定 Android `R` / DataBinding，供 `core-base` 等上层复用。

当前版本：**1.2.3**  
Maven：`io.coderf.arklab.core:utils:1.2.3`  
namespace：`io.coderf.arklab.core.utils`

---

## 职责

- 无资源耦合的通用工具（字符串/集合/日期/密度/文件/编解码/网络状态等）
- 框架级 Runnable 执行器门面 `CoreExecutors`（与协程 `Dispatchers.IO` 双栈并存）
- 作为核心栈最底层之一，被 `core-base` `api` 引用

---

## 依赖

- `androidx.core:core-ktx`
- `androidx.lifecycle:lifecycle-runtime-ktx`（`SoftKeyBoardListener`）
- `org.bouncycastle:bcprov-jdk18on`（`SM3Utils`，`api` 透出）
- 无其它工程 `project` 依赖

---

## 包结构（节选）

| 包 | 内容 |
|----|------|
| `io.coderf.arklab.core.utils` | DateUtil / DensityUtil / FileUtil / StringUtil / NumberUtil / RegexUtils / ScreenUtil / SoftKeyBoardListener / WebViewUtil / **CoreExecutors** … |
| `io.coderf.arklab.core.utils.encode` | MD5Util / Base64Util / RSAUtils / RSAUtilsHelper / SM3Utils |
| `io.coderf.arklab.core.utils.network` | NetworkStateUtil |
| `io.coderf.arklab.core.utils.bean` | Gps（配合 GeoMapUtil） |
| `io.coderf.arklab.core.utils.ext` | Kotlin 扩展（DensityExt / DateTimeExt 等） |

上传 / 下载 / 主题 `R` / Glide / WebChromeClient 业务 / 权限 UI 等仍在 `core-base`。

---

## 执行器选型（`CoreExecutors`）

| 场景 | API |
|------|-----|
| 短时可并发 IO（上传等） | `CoreExecutors.io()` |
| 可排队后台任务（画廊下载等） | `CoreExecutors.bounded()` |
| 必须顺序（写日志 / 写文件） | `CoreExecutors.serial()` |
| CPU 密集（压缩投递） | `CoreExecutors.cpu()` |
| 回主线程 | `CoreExecutors.main()` |
| 可挂起网络 / Room | 协程 `Dispatchers.IO`（不要叠本门面） |
| 控件专属 Looper / Camera / WS·MQTT worker | 局部单线程，**禁止**占全局池 |

Java：`CoreExecutors.io().execute(runnable);`

---

## 何时依赖

一般经 `core-base` / `common` 间接使用；需要纯工具、避免 UI/R 耦合时可显式：

```gradle
implementation 'io.coderf.arklab.core:utils:1.2.3'
// 或
implementation project(':core-utils')
```

---

## 发布

```bash
./gradlew :core-utils:publish
```
