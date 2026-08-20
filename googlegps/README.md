# googlegps

可选定位能力库：GNSS / NMEA、Socket 定位客户端及 GPX/CSV 等记录辅助。

当前版本：**3.1.6**  
Maven：`io.coderf.arklab.googlegps:googlegps:3.1.6`  
namespace：`io.coderf.arklab.googlegps`

---

## 职责

- GPS / 定位服务与监听回调
- 网络定位 / 日志记录相关封装（按需使用）

---

## 依赖

- 无工程内核心 `project` 依赖（独立能力库）

---

## 何时依赖

App 需要定位能力时按需引入：

```gradle
implementation 'io.coderf.arklab.googlegps:googlegps:3.1.6'
// 或
implementation project(':googlegps')
```

业务侧可按项目在 case `:base` 再包一层 Gateway（与 media/mqtt 同样模式），本库本身不强制 Gateway。

---

## 发布

```bash
./gradlew :googlegps:publish
```
