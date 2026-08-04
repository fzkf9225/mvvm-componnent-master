# mqttcomponent

可复用的 Android MQTT 组件库（Eclipse Paho MQTT **v5**），面向多通道、可配置、Java/Kotlin 双友好封装。

当前版本：**1.3.0**  
Maven 坐标：`io.coderf.arklab.mqtt:mqtt:1.3.0`

---

## 从哪开始？（入口）

日常业务 **只需要关心根包** `io.coderf.arklab.mqtt`：

| 你要做的事 | 导入 |
|-----------|------|
| 建连 / 订退 / 发布（推荐） | **`MqttClient`** |
| 组装连接参数 | **`MqttConfig`** |
| 收连接 / 消息回调 | **`MqttListener`** / `AbstractMqttListener` |
| 遗嘱 | `MqttLwt` |
| 原始下行 | `MqttRawMessage` |

```java
import io.coderf.arklab.mqtt.MqttClient;
import io.coderf.arklab.mqtt.MqttConfig;
import io.coderf.arklab.mqtt.AbstractMqttListener;

MqttClient client = new MqttClient("BizMqtt");
MqttConfig config = MqttConfig.builder()
        .brokerAddress("tcp://broker:1883")
        .clientId("app_" + deviceId)
        .username(user)
        .password(token)
        .subscribeTopics("push/#")
        .maxReconnectAttempts(20)
        .reconnectIntervalSeconds(5)
        .build();

client.connect(config, new AbstractMqttListener() {
    @Override public void onConnected(boolean reconnect) { /* 主线程 */ }
    @Override public void onMessage(String topic, String payload) { /* 主线程 */ }
});
```

---

## 包结构（按需再深入）

```
io.coderf.arklab.mqtt          ★ 日常入口（MqttClient / MqttConfig / MqttListener …）
├── core/                      同步阻塞底层 MqttConnection（一般别直接用）
├── handler/                   业务 Handler 路由（可选）
├── session/                   Lifecycle 单页订阅 MqttSession（可选）
├── presence/                  在线心跳 PresenceClient（可选场景）
├── widget/                    重连弹窗 MqttReconnectDialog（可选 UI）
└── internal/                  内部工具 TopicDiff（勿依赖）
```

```
┌─────────────────────────────────────────────────────────────┐
│  widget/     MqttReconnectDialog                            │
├─────────────────────────────────────────────────────────────┤
│  presence/   PresenceClient / HeartbeatScheduler            │
│              DevicePresenceManager（宿主实现）               │
├─────────────────────────────────────────────────────────────┤
│  handler/    MqttHandlerRegistry + MqttMessageHandler       │
│  session/    MqttSession（Lifecycle 单页订阅）               │
├─────────────────────────────────────────────────────────────┤
│  ★ 入口      MqttClient（异步 Worker + 主线程回调）           │
│              MqttConfig / MqttListener / MqttLwt            │
├─────────────────────────────────────────────────────────────┤
│  core/       MqttConnection（Paho 同步封装，进阶用）         │
└─────────────────────────────────────────────────────────────┘
```

| 层级 | 职责 | 不负责 |
|------|------|--------|
| **入口 `MqttClient`** | 线程切换、异步 connect/publish、主线程回调 | 业务信封解析 |
| `core.MqttConnection` | 同步连接、订退、双模式重连、LWT | UI / Lifecycle |
| `handler` / `session` | 路由分发、页面订阅会话 | 具体 bizCode |
| `presence` | 心跳 / 遗嘱辅助 | HTTP 拉配置 |
| `widget` | 重连弹窗 | 业务导航 |

---

## 可选能力

### Handler 路由

```java
import io.coderf.arklab.mqtt.handler.MqttHandlerRegistry;
import io.coderf.arklab.mqtt.handler.AbstractMqttMessageHandler;

MqttHandlerRegistry registry = new MqttHandlerRegistry();
registry.register(new AbstractMqttMessageHandler() {
    @Override public Set<String> supportedKeys() {
        return Collections.singleton("device_osd");
    }
    @Override public void onMessage(String key, String topic, String payload) { }
});
// onMessage 里：registry.dispatch(bizCode, topic, payload);
```

### Lifecycle 单页订阅

```kotlin
import io.coderf.arklab.mqtt.session.MqttSession

val session = MqttSession(client)
session.observe(this) { buildTopicsFromUiState() }
session.updateTopics(this, newTopics)
client.connect(config, listener) { success ->
    if (success) session.flushPendingSubscriptions()
}
```

### Presence 在线心跳

```java
import io.coderf.arklab.mqtt.presence.PresenceClient;
import io.coderf.arklab.mqtt.presence.PresenceConfig;
import io.coderf.arklab.mqtt.presence.HeartbeatScheduler;
import io.coderf.arklab.mqtt.presence.AbstractPresenceListener;

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

完整 App 生命周期请实现 `DevicePresenceManager`（`init` / `start` / `stop` / `reconnectIfNeeded`）。

### 重连 UI

```java
import io.coderf.arklab.mqtt.widget.MqttReconnectDialog;
// 在 onReconnecting 中：dialog.updateReconnectState(attempt, max, delaySec);
```

---

## 重连策略

| `maxReconnectAttempts` | 行为 |
|------------------------|------|
| `null`（默认） | Paho `automaticReconnect`，指数退避约 1s→120s，无限重试 |
| `>0` | 固定间隔 `reconnectIntervalSeconds`（默认 5s），超限触发 `onReconnectExhausted` |

主动 `disconnect()` 不会重连，也不会回调 `onDisconnected`。

---

## 1.3.0 相对 1.2.0 的破坏性变更

为让「入口」一目了然，做了包与类名整理：

| 1.2.0 | 1.3.0 |
|-------|-------|
| `mqtt.MqttAsyncClient` | **`mqtt.MqttClient`**（推荐入口） |
| `mqtt.MqttConnectionConfig` | `mqtt.MqttConfig` |
| `mqtt.MqttConnectionListener` | `mqtt.MqttListener` |
| `mqtt.AbstractMqttConnectionListener` | `mqtt.AbstractMqttListener` |
| `mqtt.MqttLwtConfig` | `mqtt.MqttLwt` |
| `mqtt.MqttLogger` / `mqtt.MqttLog` | 已移除，统一使用 `utils.LogUtil.logger` |
| `mqtt.MqttLifecycleSession` | `session.MqttSession` |
| `mqtt.MqttHandlerRegistry` 等 | `handler.*` |
| `mqtt.MqttConnection` | `core.MqttConnection` |
| `mqtt.MqttTopicDiff` | `internal.TopicDiff` |
| `presence.PresenceMqttClient` | `presence.PresenceClient` |
| `presence.PresenceConnectionInfo` | `presence.PresenceConfig` |

---

## 依赖

```gradle
implementation 'io.coderf.arklab.mqtt:mqtt:1.3.0'
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
- **module version**: 1.3.0
