# core-ws

可复用的 Android WebSocket 组件库（OkHttp），面向多通道、可配置、Java / Kotlin 双友好。

当前版本：**1.0.0**  
Maven 坐标：`io.coderf.arklab.ws:ws:1.0.0`

> **边界**：本库只提供连接 / 逻辑频道编排 / Presence / Lifecycle / 重连 UX 等基础能力。  
> 业务信封、bizCode、设备 SN、订退 JSON 格式等请在宿主项目二次扩展（通过 [WsChannelBinder] 发帧），不要写进框架。

---

## 30 秒接入（推荐）

```java
import io.coderf.arklab.ws.Ws;
import io.coderf.arklab.ws.WsClient;
import io.coderf.arklab.ws.AbstractWsListener;
import io.coderf.arklab.ws.session.WsSession;

WsClient client = Ws.createClient("BizWs");
client.setChannelBinder((desired, added, removed) -> {
    // 宿主按业务协议编码订退帧后：
    // client.send(encodeSubscribe(desired));
});

WsSession session = Ws.createSession(client);
session.observe(this, () -> Arrays.asList("channel_a", "channel_b"));

client.connect(
    Ws.simpleConfig("wss://host/ws", token),
    new AbstractWsListener() {
        @Override public void onMessage(String text) { /* UI 线程 */ }
    },
    ok -> { if (ok) session.flushPendingSubscriptions(); }
);
```

匿名直连：

```java
client.connect(Ws.anonymousConfig("ws://host/ws"), listener, null);
```

Query 鉴权（兼容旧 `?Blade-Auth=`）：

```java
client.connect(Ws.queryAuthConfig(url, token, "Blade-Auth"), listener, null);
```

调试日志：

```java
Ws.enableDebug(application, true);
```

---

## 类名怎么记？

| 推荐类名 | 做什么 | 不要用混 |
|---------|--------|----------|
| **`Ws`** | 静态快捷入口 | — |
| **`WsClient`** | 日常异步建连 / 发送 / 频道同步 | ≠ OkHttp `WebSocket` |
| **`WsConfig`** | 连接参数 Builder | — |
| **`WsListener`** | 连接 + 消息回调 | — |
| **`WsSession`** | Lifecycle 逻辑频道编排 + Overlay | 不是网络 Session |
| **`WsChannelBinder`** | 宿主编码订退帧的扩展点 | — |
| **`core.WsConnection`** | 同步底层（进阶） | 一般业务别直接用 |
| **`presence.PresenceClient`** | 在线心跳专用通道 | 与业务推送通道分开实例 |
| **`widget.WsReconnectDialog`** | 有限重连弹窗 | — |
| **`utils.WsLog`** | 本模块日志（委托 core-log） | — |

---

## 包结构

```
io.coderf.arklab.ws          ★ 日常入口（Ws / WsClient / WsConfig / WsListener）
├── session/                 Lifecycle 频道编排 + Overlay
├── handler/                 按 key 路由（可选；key 由宿主从 payload 自行解析）
├── presence/                在线心跳（PresenceClient / PresenceManager）
├── widget/                  重连弹窗 WsReconnectDialog
├── core/                    同步底层 WsConnection
├── utils/                   WsLog
└── internal/                ChannelDiff（勿依赖）
```

---

## 重连策略

| `maxReconnectAttempts` | 行为 |
|------------------------|------|
| `null`（默认） | 无限重连，指数退避 1s→120s；**不**触发 `onReconnecting` |
| `> 0` | 固定间隔重连；每次触发 `onReconnecting`；耗尽触发 `onReconnectExhausted` |

有限重连可配合 `WsReconnectDialog`：

```java
client.connect(Ws.reconnectConfig(url, token, 20, 5), new AbstractWsListener() {
    @Override public void onReconnecting(int attempt, int max, int delaySec) {
        dialog.updateReconnectState(attempt, max, delaySec);
    }
}, null);
```

---

## 与旧 wscomponent

旧模块仅为测试封装，已删除。请直接依赖 `core-ws`，不要把业务 `SubscribeEnum` / `HeaderBean` 写回框架层。
