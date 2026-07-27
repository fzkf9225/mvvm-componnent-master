# mqttcomponent

可复用的 Android MQTT 组件库（Eclipse Paho MQTT **v5**），面向多通道、可配置、Java/Kotlin 双友好封装。

当前版本：**1.2.0**  
Maven 坐标：`io.coderf.arklab.mqtt:mqtt:1.2.0`

---

## 架构分层

```
┌─────────────────────────────────────────────────────────────┐
│  widget/     MqttReconnectDialog（重连等待 UI）               │
├─────────────────────────────────────────────────────────────┤
│  presence/   PresenceMqttClient / HeartbeatScheduler         │
│              DevicePresenceManager（宿主实现生命周期）         │
├─────────────────────────────────────────────────────────────┤
│  mqtt/ 编排   MqttAsyncClient（Worker 异步）                  │
│              MqttLifecycleSession（Lifecycle 单页订阅）       │
│              MqttHandlerRegistry + MqttMessageHandler        │
│              MqttTopicDiff / syncTopics（主题差量订退）        │
├─────────────────────────────────────────────────────────────┤
│  mqtt/ 传输   MqttConnection（Paho 封装）                     │
│              MqttConnectionConfig / Listener / Lwt / Logger  │
└─────────────────────────────────────────────────────────────┘
```

| 层级 | 职责 | 不负责 |
|------|------|--------|
| 传输 `MqttConnection` | 连接、发布、动态订退、双模式重连、LWT | 业务信封解析、页面生命周期 |
| 编排 `MqttAsyncClient` 等 | 线程切换、主题 diff、Handler 路由、会话持有 | 具体 bizCode / 设备拓扑 |
| Presence | 在线心跳 + 遗嘱辅助 | HTTP 拉配置、Token 缓存 |
| UI | 重连弹窗 | 业务导航 |

业务域（如无人机 OSD、巡护轨迹）应放在宿主 App / Feature 模块，通过 Handler / 解析器插件接入。

---

## 能力一览

| 能力 | API | 说明 |
|------|-----|------|
| 多实例连接 | `new MqttConnection(tag)` | 每个实例独立 clientId，互不影响 |
| 同步连接 | `MqttConnection.connect` | **阻塞**，勿在主线程调用 |
| 异步连接 | `MqttAsyncClient.connect` | Worker 执行，主线程回调 |
| 动态订阅/退订 | `subscribe` / `unsubscribe` | 未连接时先登记，连上后恢复 |
| 差量同步主题 | `syncTopics` / `MqttTopicDiff` | 页面切设备时避免全量重订 |
| 双模式重连 | `maxReconnectAttempts` | null=Paho 指数退避；有值=固定间隔+上限 |
| LWT | `MqttLwtConfig` | 异常离线由 Broker 代发 |
| 可选鉴权 | `requireAuth(false)` | 支持匿名 Broker |
| 回调线程 | `dispatchConnectOnMainThread` / `dispatchMessageOnMainThread` | 连接与消息分开控制 |
| 原始消息 | `onMessageRaw(MqttRawMessage)` | 二进制 + QoS + retained |
| Handler 路由 | `MqttHandlerRegistry` | 按 bizCode 等路由键安全分发 |
| Lifecycle 会话 | `MqttLifecycleSession` | 单页持有订阅，onResume/onPause |
| Presence | `PresenceMqttClient` | 心跳/离线/完整事件转发 |
| 重连 UI | `MqttReconnectDialog` | 配合自定义重连进度 |

---

## 快速开始

### 1. 依赖

```gradle
implementation 'io.coderf.arklab.mqtt:mqtt:1.2.0'
```

宿主 Manifest 需声明：

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 2. 业务推送通道（推荐异步）

```java
MqttAsyncClient client = new MqttAsyncClient("BizMqtt");

MqttConnectionConfig config = MqttConnectionConfig.builder()
        .brokerAddress("tcp://broker:1883")
        .clientId("app_" + deviceId)
        .username(user)
        .password(token)
        .subscribeTopics("push/#")
        .maxReconnectAttempts(20)          // 自定义重连，配合弹窗
        .reconnectIntervalSeconds(5)
        .build();

client.connect(config, new AbstractMqttConnectionListener() {
    @Override
    public void onConnected(boolean reconnect) {
        // 已在主线程
    }

    @Override
    public void onMessage(String topic, String payload) {
        // 解析 bizCode 后交给 registry
    }

    @Override
    public void onReconnecting(int attempt, int max, int delaySec) {
        dialog.updateReconnectState(attempt, max, delaySec);
    }
});

// 设备列表变化：差量订退
client.syncTopics(Arrays.asList("dev/A/osd", "dev/B/osd"));
```

### 3. Handler 路由（业务插件）

```java
MqttHandlerRegistry registry = new MqttHandlerRegistry();
registry.register(new AbstractMqttMessageHandler() {
    @Override
    public Set<String> supportedKeys() {
        return Collections.singleton("device_osd");
    }

    @Override
    public void onMessage(String key, String topic, String payload) {
        // 更新 LiveData / UI
    }
});

// 在 onMessage 中：
String bizCode = YourParser.extractBizCode(payload);
registry.dispatch(bizCode, topic, payload);
```

### 4. Lifecycle 单页订阅

```kotlin
val session = MqttLifecycleSession(client)
session.observe(this) { buildTopicsFromUiState() }

// 数据加载完成后刷新目标主题
session.updateTopics(this, newTopics)

// 连接成功后补订
client.connect(config, listener) { success ->
    if (success) session.flushPendingSubscriptions()
}
```

### 5. Presence 在线心跳

```java
PresenceMqttClient presence = new PresenceMqttClient();
presence.connect(info, new AbstractPresenceConnectionListener() {
    @Override
    public void onConnected(boolean reconnect) {
        scheduler.start(() -> presence.publishHeartbeat(
                info.heartbeatTopic,
                presence.buildHeartbeatPayload(userId)
        ));
    }

    @Override
    public void onDisconnected() {
        scheduler.stop();
    }
});
```

完整 App 生命周期请实现 `DevicePresenceManager`（`init` / `start` / `stop` / `reconnectIfNeeded`）。

---

## 重连策略说明

| `maxReconnectAttempts` | 行为 |
|------------------------|------|
| `null`（默认） | Paho `automaticReconnect`，指数退避约 1s→120s，无限重试 |
| `>0` | 关闭 Paho 自动重连，固定间隔 `reconnectIntervalSeconds`（默认 5s），超限触发 `onReconnectExhausted` |

仅对「曾经连上过后意外断连」生效；主动 `disconnect()` 不会重连，也不会回调 `onDisconnected`。

---

## 与业务工程的推荐映射

参考 `dlap-laea-serm-app` 的用法：

| 业务侧 | 本组件对应 |
|--------|------------|
| `base.mqtt.MqttConnection` | `mqtt.MqttConnection` |
| `DroneMqttClient`（Worker） | `mqtt.MqttAsyncClient` |
| 页面主题 diff 订阅 | `syncTopics` / `MqttLifecycleSession` |
| `DroneMqttBizHandler` | `MqttMessageHandler` + `MqttHandlerRegistry` |
| `PresenceMqttClient` | `presence.PresenceMqttClient` |

**请勿**把设备拓扑、OSD 合并、租户 topic 模板等业务逻辑下沉到本库。

---

## 包结构

```
io.coderf.arklab.mqttcomponent
├── mqtt/          传输 + 编排原语
├── presence/      在线心跳场景
└── widget/        重连 Dialog
```

---

## 版本与兼容

- **minSdk**：与工程 Version Catalog 一致（当前 26）
- **Java / Kotlin**：17
- **1.2.0** 相对 1.1.0：新增动态订退、异步门面、Handler、Lifecycle 会话、消息主线程分发、可选鉴权、Presence 完整回调；旧的 `connect` / `publish` / Presence 简化回调保持兼容

---

## 本地发布

```bash
./gradlew :mqttcomponent:publish
```

需配置环境变量 `ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`。

---

## 作者

- **author**: fz
- **module version**: 1.2.0
