package io.coderf.arklab.mqtt.session

import io.coderf.arklab.mqtt.utils.LogUtil
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.coderf.arklab.mqtt.MqttClient
import java.lang.ref.WeakReference
import java.util.WeakHashMap

/**
 * 基于 Lifecycle 的 MQTT 订阅会话管理。
 *
 * 参考业务侧「单页持有订阅」模式：
 * - [observe] 绑定页面生命周期；onResume 应用目标主题，onPause/onDestroy 释放
 * - 同一时刻仅一个 [LifecycleOwner] 持有 Broker 订阅（切页先退旧订新）
 * - 未连接时缓存 pending 主题，连上后再 flush
 *
 * 传输仍委托 [MqttClient]，本类只编排「谁该订哪些主题」。
 *
 * @param client 异步 MQTT 客户端
 *
 * @author fz
 * @version 1.3
 * @since 1.2
 * @created 2026/7/27 10:10
 */
class MqttSession(
    private val client: MqttClient,
) {

    private val lock = Any()
    private val sessions = WeakHashMap<LifecycleOwner, PageSession>()
    private var currentOwner: WeakReference<LifecycleOwner>? = null

    /**
     * 绑定页面并开始观察生命周期。
     *
     * @param owner 页面 LifecycleOwner
     * @param topicsProvider 返回当前页面期望订阅的主题；可在数据加载后通过 [updateTopics] 刷新
     * @param unsubscribeOnPause 是否在 onPause 时退订（默认 true，省流量）
     */
    @JvmOverloads
    fun observe(
        owner: LifecycleOwner,
        topicsProvider: () -> Collection<String>,
        unsubscribeOnPause: Boolean = true,
    ) {
        synchronized(lock) {
            val existing = sessions[owner]
            if (existing != null) {
                existing.topicsProvider = topicsProvider
                existing.unsubscribeOnPause = unsubscribeOnPause
                return
            }
            val session = PageSession(
                owner = WeakReference(owner),
                topicsProvider = topicsProvider,
                unsubscribeOnPause = unsubscribeOnPause,
            )
            sessions[owner] = session
            owner.lifecycle.addObserver(session.observer)
        }
    }

    /**
     * 更新某页面的目标主题并立即 diff 应用（仅当该页是当前持有者且 RESUMED）。
     */
    fun updateTopics(owner: LifecycleOwner, topics: Collection<String>) {
        synchronized(lock) {
            val session = sessions[owner] ?: return
            session.cachedTopics = topics.filter { it.isNotBlank() }.toSet()
            if (isCurrentOwner(owner) && session.resumed) {
                applyTopicsLocked(session.cachedTopics)
            }
        }
    }

    /** 主动解除绑定（通常无需调用，onDestroy 会自动清理） */
    fun release(owner: LifecycleOwner) {
        synchronized(lock) {
            val session = sessions.remove(owner) ?: return
            owner.lifecycle.removeObserver(session.observer)
            if (isCurrentOwner(owner)) {
                applyTopicsLocked(emptySet())
                currentOwner = null
            }
        }
    }

    /** 连接成功后调用，把当前持有页的 pending 主题刷到 Broker */
    fun flushPendingSubscriptions() {
        synchronized(lock) {
            val owner = currentOwner?.get() ?: return
            val session = sessions[owner] ?: return
            if (session.resumed) {
                val topics = session.resolveTopics()
                applyTopicsLocked(topics)
            }
        }
    }

    private fun isCurrentOwner(owner: LifecycleOwner): Boolean {
        return currentOwner?.get() === owner
    }

    private fun applyTopicsLocked(desired: Set<String>) {
        client.syncTopics(desired)
    }

    private fun onPageResumed(owner: LifecycleOwner) {
        synchronized(lock) {
            val session = sessions[owner] ?: return
            session.resumed = true
            val previous = currentOwner?.get()
            if (previous != null && previous !== owner) {
                // 切页：先清空旧订阅
                applyTopicsLocked(emptySet())
            }
            currentOwner = WeakReference(owner)
            val topics = session.resolveTopics()
            if (client.isConnected()) {
                applyTopicsLocked(topics)
            } else {
                LogUtil.logger(TAG, "尚未连接，主题已缓存，待 flushPendingSubscriptions")
            }
        }
    }

    private fun onPagePaused(owner: LifecycleOwner) {
        synchronized(lock) {
            val session = sessions[owner] ?: return
            session.resumed = false
            if (!isCurrentOwner(owner)) {
                return
            }
            if (session.unsubscribeOnPause) {
                applyTopicsLocked(emptySet())
            }
        }
    }

    private fun onPageDestroyed(owner: LifecycleOwner) {
        release(owner)
    }

    /**
     * 单个页面会话状态。
     *
     * @author fz
     * @version 1.3
     * @since 1.2
     * @created 2026/7/27 10:10
     */
    private inner class PageSession(
        val owner: WeakReference<LifecycleOwner>,
        var topicsProvider: () -> Collection<String>,
        var unsubscribeOnPause: Boolean,
    ) {
        var resumed: Boolean = false
        var cachedTopics: Set<String> = emptySet()

        fun resolveTopics(): Set<String> {
            val fromProvider = runCatching { topicsProvider() }
                .getOrElse {
                    LogUtil.logger(TAG, "topicsProvider 异常: ${it.message}")
                    emptyList()
                }
                .filter { it.isNotBlank() }
                .toSet()
            if (fromProvider.isNotEmpty()) {
                cachedTopics = fromProvider
            }
            return cachedTopics.ifEmpty { fromProvider }
        }

        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                onPageResumed(owner)
            }

            override fun onPause(owner: LifecycleOwner) {
                onPagePaused(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                onPageDestroyed(owner)
            }
        }
    }

    companion object {
        private const val TAG = "MqttSession"
    }
}
