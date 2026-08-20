package io.coderf.arklab.mqtt

import androidx.lifecycle.LifecycleOwner
import io.coderf.arklab.mqtt.session.MqttSession
import io.coderf.arklab.mqtt.session.MqttSubscribePolicy
import io.coderf.arklab.mqtt.session.MqttTopicsProvider
import io.coderf.arklab.mqtt.utils.MqttDebug
import android.app.Application
import io.coderf.arklab.mqtt.utils.MqttFileLogLevel

/**
 * 降低接入门槛的静态入口：三步完成「建客户端 → 绑生命周期订阅 → 连接」。
 *
 * ## 最快路径（Java）
 * ```java
 * MqttClient client = Mqtt.createClient("BizMqtt");
 * MqttSession session = Mqtt.createSession(client);
 * session.observe(activity, () -> Arrays.asList("push/#"));
 * // 弹窗主题与页面并存（不抢占焦点）：
 * // session.bindOverlayTopics(dialog, "tmp/cmd");
 * client.connect(Mqtt.simpleConfig(broker, clientId, user, pass, "push/#"), listener, ok -> {
 *     if (ok) session.flushPendingSubscriptions();
 * });
 * ```
 *
 * ## 类名对照（避免与其它模块混淆）
 * | 推荐 | 含义 |
 * |------|------|
 * | [MqttClient] | 业务异步客户端（日常入口） |
 * | [MqttConfig] | 连接参数 |
 * | [MqttSession] | Lifecycle 自动订退 + Overlay |
 * | [io.coderf.arklab.mqtt.core.MqttConnection] | 同步底层（一般别直接用） |
 * | [io.coderf.arklab.mqtt.presence.PresenceClient] | 在线心跳专用通道 |
 *
 * 业务消息解析（信封 / bizCode 等）请在宿主模块自行扩展，本入口不提供。
 *
 * @author fz
 * @version 1.5
 * @since 1.4
 */
object Mqtt {

    /** 创建异步客户端 */
    @JvmStatic
    @JvmOverloads
    fun createClient(tag: String = "Mqtt"): MqttClient = MqttClient(tag)

    /**
     * 创建 Lifecycle 订阅会话。
     *
     * @param subscribePolicy 焦点栈策略；Overlay 始终与页面主题取并集，见 [MqttSubscribePolicy]
     */
    @JvmStatic
    @JvmOverloads
    fun createSession(
        client: MqttClient,
        subscribePolicy: MqttSubscribePolicy = MqttSubscribePolicy.FOCUS_REPLACE,
    ): MqttSession = MqttSession(client, subscribePolicy)

    /**
     * 一站式：创建 session 并绑定 Owner（仍需自行 [MqttClient.connect] + [MqttSession.flushPendingSubscriptions]）。
     */
    @JvmStatic
    @JvmOverloads
    fun bindTopics(
        client: MqttClient,
        owner: LifecycleOwner,
        topicsProvider: MqttTopicsProvider,
        unsubscribeOnPause: Boolean = true,
        subscribePolicy: MqttSubscribePolicy = MqttSubscribePolicy.FOCUS_REPLACE,
    ): MqttSession {
        val session = MqttSession(client, subscribePolicy)
        session.observe(owner, topicsProvider, unsubscribeOnPause)
        return session
    }

    /**
     * 最简连接配置：Broker + 鉴权 + 可选初始订阅主题。
     * 默认：keepAlive 60s、自动重连（Paho 无限）、主线程回调由 [MqttClient.connect] 强制打开。
     */
    @JvmStatic
    @JvmOverloads
    fun simpleConfig(
        brokerAddress: String,
        clientId: String,
        username: String,
        password: String,
        vararg subscribeTopics: String,
    ): MqttConfig {
        val builder = MqttConfig.builder()
            .brokerAddress(brokerAddress)
            .clientId(clientId)
            .username(username)
            .password(password)
        if (subscribeTopics.isNotEmpty()) {
            builder.subscribeTopics(*subscribeTopics)
        }
        return builder.build()
    }

    /**
     * 带有限次固定间隔重连的配置（适合展示重连 UI）。
     */
    @JvmStatic
    @JvmOverloads
    fun reconnectConfig(
        brokerAddress: String,
        clientId: String,
        username: String,
        password: String,
        maxReconnectAttempts: Int = 20,
        reconnectIntervalSeconds: Int = 5,
        vararg subscribeTopics: String,
    ): MqttConfig {
        val builder = MqttConfig.builder()
            .brokerAddress(brokerAddress)
            .clientId(clientId)
            .username(username)
            .password(password)
            .maxReconnectAttempts(maxReconnectAttempts)
            .reconnectIntervalSeconds(reconnectIntervalSeconds)
        if (subscribeTopics.isNotEmpty()) {
            builder.subscribeTopics(*subscribeTopics)
        }
        return builder.build()
    }

    /** 打开模块 debug 日志 */
    @JvmStatic
    @JvmOverloads
    fun enableDebug(
        application: Application,
        enable: Boolean = true,
        fileLogLevel: MqttFileLogLevel = MqttFileLogLevel.NONE,
    ) {
        MqttDebug.enableDebug(application, enable, fileLogLevel)
    }
}
