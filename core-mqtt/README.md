# core-mqtt

可复用的 Android MQTT 组件库（Eclipse Paho MQTT **v5**），面向多通道、可配置、Java / Kotlin 双友好。

当前版本：**1.5.1**
Maven 坐标：`io.coderf.arklab.mqtt:mqtt:1.5.1`

> **边界**：本库只提供连接 / 订退 / Presence / Lifecycle 编排等基础能力。  
> 业务信封、bizCode、设备 SN 匹配等请在宿主项目二次扩展，不要写进框架。

---

## 30 秒接入（推荐）

```java
import io.coderf.arklab.mqtt.Mqtt;
import io.coderf.arklab.mqtt.MqttClient;
import io.coderf.arklab.mqtt.AbstractMqttListener;
import io.coderf.arklab.mqtt.session.MqttSession;

// 1) 客户端
MqttClient client = Mqtt.createClient("BizMqtt");

// 2) 生命周期自动订退（Activity / Fragment / DialogFragment 均可）
MqttSession session = Mqtt.createSession(client);
session.observe(this, () -> Arrays.asList("push/#", "room/" + roomId + "/osd"));

// 3) 连接（连上后刷 pending 订阅）
client.connect(
    Mqtt.simpleConfig(broker, clientId, user, pass),
    new AbstractMqttListener() {
        @Override public void onMessage(String topic, String payload) { /* UI 线程 */ }
    },
    ok -> { if (ok) session.flushPendingSubscriptions(); }
);
```

调试日志：

```java
// 推荐：宿主用 ArkLog 统一 init + 落盘，模块只开开关
// ArkLog.init(app, LogConfig.builder().globalTag("ArkLab").fileLogLevel(FileLogLevel.DEBUG).build());
Mqtt.enableDebug(application, true);
```

---

## 类名怎么记？（避免和其它模块搞混）

| 推荐类名 | 做什么 | 不要用混 |
|---------|--------|----------|
| **`Mqtt`** | 静态快捷入口（建客户端 / 简配 / debug） | — |
| **`MqttClient`** | 日常异步建连 / 订退 / 发布 | ≠ Paho 的 `MqttClient`；≠ `PresenceClient` |
| **`MqttConfig`** | 连接参数 Builder | — |
| **`MqttListener`** | 连接 + 消息回调 | — |
| **`MqttSession`** | Lifecycle 自动订退 + Overlay | 不是网络 Session |
| **`MqttSubscribePolicy`** | 焦点栈如何贡献页面主题 | — |
| **`core.MqttConnection`** | 同步底层（进阶） | 一般业务别直接用 |
| **`presence.PresenceClient`** | 在线心跳专用通道 | 与业务推送通道分开实例 |
| **`presence.PresenceManager`** | 宿主实现的在线生命周期接口 | 旧名 `DevicePresenceManager` 已废弃 |
| **`utils.MqttLog`** | 本模块日志（委托 core-log） | 落盘请用 `ArkLog.startFileLog` |

---

## Dialog：替换 vs 与页面并存（Overlay）

`MqttSession` 绑定的是 **`LifecycleOwner`**，不限于 Activity / Fragment。

| 场景 | 写法 |
|------|------|
| Activity / Fragment | `session.observe(this, topics)` |
| **DialogFragment** | `session.observe(this, topics)` 或 `observe(getViewLifecycleOwner(), …)` |
| **ComponentDialog**（AndroidX） | 本身是 LifecycleOwner：`session.observe(dialog, topics)` |
| 经典 Dialog **替换**页面订阅 | `session.observeDialog(dialog, topics)` |
| 经典 Dialog **与页面并存** | `session.bindOverlayTopics(dialog, topics)` ← **推荐** |

### 1）替换（焦点栈，1.4 行为）

```java
Dialog dialog = new Dialog(context);
DialogLifecycleOwner bridge = session.observeDialog(dialog, () ->
        Collections.singletonList("dialog/" + id + "/cmd"));
dialog.show();
bridge.destroy(); // 弹窗彻底不用时
```

弹窗叠在页面上时，Broker 订阅切到弹窗主题；dismiss 后恢复下层仍 resumed 的页面主题。

### 2）并存（Overlay / Union，1.5）

最终 Broker 订阅 = **页面侧主题 ∪ 全部 Overlay**（与焦点栈正交）。

```java
// 页面照常 observe OSD / 直播等
session.observe(this, () -> Arrays.asList("room/1/osd", "room/1/video"));

// 弹窗只加临时主题，不抢占页面订阅
session.bindOverlayTopics(dialog, "room/1/flight_before_check");
// dismiss 时：
session.unbindOverlayTopics(dialog);
```

| API | 语义 |
|-----|------|
| `bindOverlayTopics(token, …)` | 绑定/覆盖该 token 的叠加主题 |
| `unbindOverlayTopics(token)` | 移除该 token |
| `clearOverlayTopics()` | 清空全部 Overlay |
| `getOverlayTopics()` / `getEffectiveTopics()` | 调试用快照 |

策略（构造 `MqttSession` / `Mqtt.createSession` 时传入）：

| `MqttSubscribePolicy` | 页面侧主题 |
|----------------------|------------|
| `FOCUS_REPLACE`（默认） | 仅焦点栈顶 Owner |
| `UNION_RESUMED` | 栈内所有仍 resumed Owner 取并集 |

两种策略下 **Overlay 都会并入**最终集合。

---

## 页面内动态改订阅

| API | 语义 |
|-----|------|
| `updateTopics(owner, topics)` | **全量替换**目标集合（原有行为保留） |
| `addTopics(owner, …)` | **追加**（已存在则忽略） |
| `removeTopics(owner, …)` | **移除**（不存在则忽略） |
| `getTopics(owner)` | 查看当前缓存的目标主题 |

```java
session.observe(this, () -> Arrays.asList("room/1/osd"));

// 追加
session.addTopics(this, "room/1/event", "room/1/cmd");
// 移除
session.removeTopics(this, "room/1/cmd");
// 仍可用全量替换
session.updateTopics(this, Arrays.asList("room/1/osd", "room/1/event"));
```

说明：调用 `addTopics` / `removeTopics` / `updateTopics` 后，pause/resume 会保持这批手动主题，不会被初始 `topicsProvider` 覆盖；再次 `observe` 同一 Owner 会重新以 provider 为准。

---

## 包结构

```
io.coderf.arklab.mqtt          ★ 日常入口（Mqtt / MqttClient / MqttConfig / MqttListener）
├── session/                   Lifecycle 订退 + Overlay（MqttSession、MqttSubscribePolicy、OverlayTopicBook）
├── handler/                   按 key 路由（可选；key 由宿主从 payload 自行解析）
├── presence/                  在线心跳（PresenceClient / PresenceManager）
├── widget/                    重连弹窗 MqttReconnectDialog
├── core/                      同步底层 MqttConnection（进阶）
├── utils/                     MqttLog（委托 core-log）
└── internal/                  TopicDiff（勿依赖）
```

---

## 可选能力

### Handler 路由（宿主解析 key 后分发）

```java
MqttHandlerRegistry registry = new MqttHandlerRegistry();
registry.register(new AbstractMqttMessageHandler() {
    @Override public Set<String> supportedKeys() {
        return Collections.singleton("device_osd"); // key 含义由宿主定义
    }
    @Override public void onMessage(String key, String topic, String payload) { }
});
// onMessage 里由宿主解析业务字段后再：registry.dispatch(bizKey, topic, payload);
```

### Presence 在线心跳

```java
PresenceClient presence = new PresenceClient();
presence.connect(info, new AbstractPresenceListener() {
    @Override public void onConnected(boolean reconnect) {
        scheduler.start(() -> presence.publishHeartbeat(
                info.heartbeatTopic,
                presence.buildHeartbeatPayload(userId)
        ));
    }
});
```

完整 App 生命周期请实现 `PresenceManager`（`init` / `start` / `stop` / `reconnectIfNeeded`）。

### 重连 UI

```java
// 在 onReconnecting 中：
dialog.updateReconnectState(attempt, maxAttempts, delaySec);
// 配置可用：Mqtt.reconnectConfig(broker, id, user, pass, 20, 5);
```

---

## 重连策略

| `maxReconnectAttempts` | 行为 |
|------------------------|------|
| `null`（默认） | Paho `automaticReconnect`，指数退避约 1s→120s，无限重试 |
| `>0` | 固定间隔 `reconnectIntervalSeconds`（默认 5s），超限触发 `onReconnectExhausted` |

主动 `disconnect()` 不会重连，也不会回调 `onDisconnected`。

---

## 1.5.0 变更摘要

- **`MqttSession` Overlay**：`bindOverlayTopics` / `unbindOverlayTopics` / `clearOverlayTopics` / `getEffectiveTopics`
- **`MqttSubscribePolicy`**：`FOCUS_REPLACE`（默认）/ `UNION_RESUMED`
- **`OverlayTopicBook`**：可单测的 Overlay 集合工具
- 明确框架边界：不含业务消息模型 / 解析

默认行为兼容 1.4：未使用 Overlay 时，焦点栈语义与原先一致。

## 1.4.0 变更摘要

- **`Mqtt` 快捷入口**：`createClient` / `simpleConfig` / `bindTopics` / `enableDebug`
- **`MqttSession`**：焦点栈 + `observeDialog`；`updateTopics` / `addTopics` / `removeTopics`
- **日志**：`MqttLog` 委托 `core-log`；落盘由 `ArkLog` 统一管理
- **命名澄清**：`PresenceManager`

### 相对旧版的破坏性提示

| 旧 | 新 |
|----|-----|
| `utils.LogUtil` / 自建 Logger | `utils.MqttLog` → `core-log` |
| `utils.MqttDebug` / `DebugUtil` | `Mqtt.enableDebug` / `MqttLog.setEnableDebug` |
| `utils.MqttFileLogLevel` / `MqttLogcatHelper` | `io.coderf.arklab.log.FileLogLevel` / `ArkLog.startFileLog` |
| `presence.DevicePresenceManager` | `presence.PresenceManager` |

---

## 依赖

```gradle
implementation 'io.coderf.arklab.mqtt:mqtt:1.5.1'
```

宿主 Manifest 需声明：

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 本地发布

```bash
./gradlew :mqttcomponent:publish
```

需配置环境变量 `ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`。

---

## 作者

- **author**: fz
- **module version**: 1.5.1
