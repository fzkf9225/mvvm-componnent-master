# commonmedia

可选媒体能力库：拍照、相册、压缩等（`MediaHelper` / `MediaBuilder` …）。

当前版本：**3.3.2**
Maven：`io.coderf.arklab.media:media:3.3.2`
namespace：`io.coderf.arklab.media`

---

## 职责

- 选图 / 拍摄 / 压缩等媒体流程
- 提供 Hilt 作用域下的 `MediaHelper`（`MediaModule`）
- **不**依赖 case 层 `userapi` / `:base`；Gateway 适配写在宿主 app

---

## 依赖

- 无工程内 `project` 依赖（纯能力库）

---

## 何时依赖

**仅 app（组装层）** 依赖本模块并做 Gateway 绑定；业务模块应注入 `MediaGateway`（在 case `:base`），禁止直接依赖 media：

```gradle
// app
implementation 'io.coderf.arklab.media:media:3.3.2'
```

Demo 参考：`app/.../media/MediaHelperGateway`、`MediaGatewayModule`。

---

## 发布

```bash
./gradlew :commonmedia:publish
```
