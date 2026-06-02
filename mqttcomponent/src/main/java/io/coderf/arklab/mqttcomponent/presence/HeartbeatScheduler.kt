package io.coderf.arklab.mqttcomponent.presence

import android.os.Handler
import android.os.Looper

/**
 * 应用层 MQTT 心跳调度器。
 *
 * 与服务端约定的「业务心跳」配合使用：在前台每隔固定间隔向心跳主题发布一条消息，
 * 用于刷新服务端在线状态（例如 30s 间隔 + 90s TTL）。
 *
 * ## 与协议层 keepAlive 的区别
 * | 层级   | 机制                        | 本类是否负责 |
 * |--------|-----------------------------|--------------|
 * | 应用层 | 定时 PUBLISH 到 heartbeat 主题 | **是**（仅 App 前台） |
 * | 协议层 | MQTT PINGREQ / PINGRESP     | **否**（由 Paho 按 keepAlive 自动发送） |
 *
 * ## 前后台策略（由宿主 App 的 [DevicePresenceManager] 实现驱动）
 * - **进入前台**：调用 [start]，并建议立即 [publishOnce] 补发一次
 * - **进入后台**：调用 [stop]，停止应用层心跳以省电；连接保持，依赖协议层 keepAlive + Broker LWT
 *
 * ## Java 接入示例
 * ```
 * HeartbeatScheduler scheduler = new HeartbeatScheduler();
 * scheduler.start(() -> client.publishHeartbeat(topic, payload));
 * scheduler.publishOnce();
 * scheduler.stop();
 * ```
 */
class HeartbeatScheduler @JvmOverloads constructor(
    private val intervalMs: Long = DEFAULT_INTERVAL_MS,
) {

    private val handler = Handler(Looper.getMainLooper())
    private var tickRunnable: Runnable? = null
    private var publishAction: (() -> Unit)? = null

    /**
     * 启动周期性心跳。
     *
     * 会先取消已有调度再重新开始；首次执行在下一个消息循环立即触发，
     * 之后每 [intervalMs] 执行一次。
     *
     * @param publish 单次心跳执行体，内部应自行切到 IO 线程（若 Paho 调用阻塞）
     */
    fun start(publish: () -> Unit) {
        publishAction = publish
        stopTicks()
        val runnable = object : Runnable {
            override fun run() {
                publishAction?.invoke()
                handler.postDelayed(this, intervalMs)
            }
        }
        tickRunnable = runnable
        handler.post(runnable)
    }

    /**
     * Java 友好重载。
     */
    fun start(action: HeartbeatAction) {
        start { action.run() }
    }

    /** 停止周期性心跳，不清空 publishAction（便于 [publishOnce] 仍可使用） */
    fun stop() {
        stopTicks()
    }

    /**
     * 立即执行一次心跳（不重置周期计时）。
     *
     * 用于 App 从后台回到前台时「补发心跳」，缩短服务端感知在线的延迟。
     */
    fun publishOnce() {
        publishAction?.invoke()
    }

    private fun stopTicks() {
        tickRunnable?.let { handler.removeCallbacks(it) }
        tickRunnable = null
    }

    companion object {
        /** 默认应用层心跳间隔：30 秒 */
        const val DEFAULT_INTERVAL_MS = 30_000L
    }
}

/** Java 心跳动作接口 */
fun interface HeartbeatAction {
    fun run()
}
