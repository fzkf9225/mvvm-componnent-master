package io.coderf.arklab.mqtt.session

import android.app.Dialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.coderf.arklab.mqtt.MqttClient
import io.coderf.arklab.mqtt.utils.MqttLog
import java.util.ArrayDeque
import java.util.WeakHashMap

/**
 * 基于 Lifecycle 的 MQTT 订阅会话管理。
 *
 * - [observe] 绑定任意 [LifecycleOwner]（Activity / Fragment / DialogFragment / ComponentDialog）
 * - [observeDialog] 绑定经典 [Dialog]（内部用 [DialogLifecycleOwner] 桥接；会进入焦点栈）
 * - [bindOverlayTopics] / [unbindOverlayTopics]：与焦点无关的临时叠加主题（页面 ∪ Overlay）
 * - 焦点策略见 [MqttSubscribePolicy]
 * - 主题变更：[updateTopics] 全量替换；[addTopics] / [removeTopics] 增量追加 / 移除
 * - 未连接时缓存 pending 主题，连上后再 [flushPendingSubscriptions]
 *
 * 传输仍委托 [MqttClient]，本类只编排「谁该订哪些主题」。
 * **不包含**业务消息解析、信封、bizCode 等，由宿主二次扩展。
 *
 * @param client 异步 MQTT 客户端
 * @param subscribePolicy 焦点栈如何贡献页面侧主题；Overlay 始终并入最终集合
 *
 * @author fz
 * @version 1.5
 * @since 1.2
 */
class MqttSession @JvmOverloads constructor(
    private val client: MqttClient,
    private val subscribePolicy: MqttSubscribePolicy = MqttSubscribePolicy.FOCUS_REPLACE,
) {

    private val lock = Any()
    private val sessions = WeakHashMap<LifecycleOwner, OwnerSession>()
    /** 焦点栈：末尾为最近 resumed 的 Owner */
    private val focusStack = ArrayDeque<LifecycleOwner>()
    private val overlayBook = OverlayTopicBook()
    /**
     * pause 且 [OwnerSession.unsubscribeOnPause]=false、焦点栈空时，
     * 仍保留该 Owner 主题（兼容 1.4）。
     */
    private var pausedKeepOwner: LifecycleOwner? = null

    /** 当前订阅策略 */
    fun getSubscribePolicy(): MqttSubscribePolicy = subscribePolicy

    /**
     * 绑定 LifecycleOwner 并观察生命周期。
     *
     * @param owner Activity / Fragment / DialogFragment / ComponentDialog 等
     * @param topicsProvider 返回当前期望订阅的主题；可在数据加载后通过 [updateTopics] / [addTopics] / [removeTopics] 刷新
     * @param unsubscribeOnPause 是否在 onPause 时退订（默认 true，省流量）
     */
    @JvmOverloads
    fun observe(
        owner: LifecycleOwner,
        topicsProvider: MqttTopicsProvider,
        unsubscribeOnPause: Boolean = true,
    ) {
        synchronized(lock) {
            val existing = sessions[owner]
            if (existing != null) {
                existing.topicsProvider = topicsProvider
                existing.unsubscribeOnPause = unsubscribeOnPause
                // 重新绑定 provider 时回到「以 provider 为准」
                existing.manualTopics = false
                return
            }
            val session = OwnerSession(
                topicsProvider = topicsProvider,
                unsubscribeOnPause = unsubscribeOnPause,
            )
            sessions[owner] = session
            owner.lifecycle.addObserver(session.observer)
        }
    }

    /**
     * 绑定经典 [Dialog]：show 时进入焦点栈，dismiss 时退出并恢复下层。
     *
     * 在 [MqttSubscribePolicy.FOCUS_REPLACE] 下会「替换」下层页面主题。
     * 若弹窗主题需与页面 **并存**，请改用 [bindOverlayTopics]，不要调用本方法。
     *
     * DialogFragment / ComponentDialog 请直接用 [observe]。
     *
     * @return 桥接用的 [DialogLifecycleOwner]；弹窗不再使用时调用 [DialogLifecycleOwner.destroy]
     */
    @JvmOverloads
    fun observeDialog(
        dialog: Dialog,
        topicsProvider: MqttTopicsProvider,
        unsubscribeOnPause: Boolean = true,
    ): DialogLifecycleOwner {
        val bridge = DialogLifecycleOwner(dialog)
        observe(bridge, topicsProvider, unsubscribeOnPause)
        return bridge
    }

    /**
     * 绑定与 Lifecycle 焦点无关的叠加主题。
     *
     * 最终 Broker 订阅 = [resolveFocusTopics] ∪ 全部 Overlay。
     * 同一 [token] 再次调用会覆盖该 token 的主题集合；传空集合等价于 [unbindOverlayTopics]。
     *
     * 典型：起飞自检 Dialog 只加临时主题，同时保留详情页 OSD / 直播订阅。
     */
    fun bindOverlayTopics(token: Any, topics: Collection<String>) {
        synchronized(lock) {
            overlayBook.bind(token, topics)
            MqttLog.logger(
                TAG,
                "bindOverlay token=${tokenLabel(token)} count=${overlayBook.get(token).size} " +
                    "overlayTotal=${overlayBook.all().size}",
            )
            applyEffectiveLocked()
        }
    }

    /** 叠加主题（Java / Kotlin vararg） */
    fun bindOverlayTopics(token: Any, vararg topics: String) {
        bindOverlayTopics(token, topics.asList())
    }

    /**
     * 移除 [token] 对应的叠加主题，并重新对齐 Broker。
     *
     * @return 是否确实移除过该 token
     */
    fun unbindOverlayTopics(token: Any): Boolean {
        synchronized(lock) {
            val removed = overlayBook.unbind(token)
            if (removed) {
                MqttLog.logger(TAG, "unbindOverlay token=${tokenLabel(token)}")
                applyEffectiveLocked()
            }
            return removed
        }
    }

    /** 清空全部 Overlay 并重新对齐 */
    fun clearOverlayTopics() {
        synchronized(lock) {
            if (overlayBook.isEmpty()) {
                return
            }
            overlayBook.clear()
            MqttLog.logger(TAG, "clearOverlay")
            applyEffectiveLocked()
        }
    }

    /** 全部 Overlay 主题并集快照 */
    fun getOverlayTopics(): Set<String> = overlayBook.all()

    /** 某 token 的 Overlay 快照 */
    fun getOverlayTopics(token: Any): Set<String> = overlayBook.get(token)

    /**
     * 当前应对齐到 Broker 的有效主题（页面侧 ∪ Overlay）。
     * 未连接时仍返回目标集合，便于调试。
     */
    fun getEffectiveTopics(): Set<String> {
        synchronized(lock) {
            return OverlayTopicBook.union(resolveFocusTopicsLocked(), overlayBook.all())
        }
    }

    /**
     * 全量替换某 Owner 的目标主题，并立即 diff 应用（仅当其为主题贡献方且 RESUMED）。
     *
     * 之后 pause/resume 仍以本次集合为准，直到再次 [observe] 或调用本方法 / 增量 API。
     */
    fun updateTopics(owner: LifecycleOwner, topics: Collection<String>) {
        synchronized(lock) {
            val session = sessions[owner] ?: return
            session.cachedTopics = OverlayTopicBook.normalizeTopics(topics)
            session.manualTopics = true
            applyIfOwnerContributesLocked(owner)
        }
    }

    /**
     * 在当前目标主题上追加订阅（已存在的忽略）。
     *
     * 若尚未手动改过主题，会先用 [MqttTopicsProvider] 的结果作为基底再合并。
     */
    fun addTopics(owner: LifecycleOwner, topics: Collection<String>) {
        synchronized(lock) {
            val session = sessions[owner] ?: return
            val toAdd = OverlayTopicBook.normalizeTopics(topics)
            if (toAdd.isEmpty()) {
                return
            }
            session.ensureManualBaseLocked()
            session.cachedTopics = session.cachedTopics + toAdd
            applyIfOwnerContributesLocked(owner)
        }
    }

    /** 追加订阅（Java / Kotlin vararg） */
    fun addTopics(owner: LifecycleOwner, vararg topics: String) {
        addTopics(owner, topics.asList())
    }

    /**
     * 从当前目标主题中移除订阅（不存在的忽略）。
     *
     * 若尚未手动改过主题，会先用 [MqttTopicsProvider] 的结果作为基底再减去。
     */
    fun removeTopics(owner: LifecycleOwner, topics: Collection<String>) {
        synchronized(lock) {
            val session = sessions[owner] ?: return
            val toRemove = OverlayTopicBook.normalizeTopics(topics)
            if (toRemove.isEmpty()) {
                return
            }
            session.ensureManualBaseLocked()
            session.cachedTopics = session.cachedTopics - toRemove
            applyIfOwnerContributesLocked(owner)
        }
    }

    /** 移除订阅（Java / Kotlin vararg） */
    fun removeTopics(owner: LifecycleOwner, vararg topics: String) {
        removeTopics(owner, topics.asList())
    }

    /** 当前 Owner 缓存的目标主题快照（未 observe 时返回空集） */
    fun getTopics(owner: LifecycleOwner): Set<String> {
        synchronized(lock) {
            val session = sessions[owner] ?: return emptySet()
            return if (session.manualTopics) {
                session.cachedTopics
            } else {
                session.resolveTopics()
            }
        }
    }

    /** 主动解除绑定（通常无需调用，onDestroy 会自动清理） */
    fun release(owner: LifecycleOwner) {
        synchronized(lock) {
            val session = sessions.remove(owner) ?: return
            owner.lifecycle.removeObserver(session.observer)
            removeFromFocusStackLocked(owner)
            if (pausedKeepOwner === owner) {
                pausedKeepOwner = null
            }
            applyEffectiveLocked()
        }
    }

    /** 连接成功后调用，把当前有效主题刷到 Broker */
    fun flushPendingSubscriptions() {
        synchronized(lock) {
            applyEffectiveLocked()
        }
    }

    private fun applyIfOwnerContributesLocked(owner: LifecycleOwner) {
        if (!ownerContributesLocked(owner)) {
            return
        }
        applyEffectiveLocked()
    }

    private fun ownerContributesLocked(owner: LifecycleOwner): Boolean {
        val session = sessions[owner] ?: return false
        if (!session.resumed) {
            return false
        }
        return when (subscribePolicy) {
            MqttSubscribePolicy.FOCUS_REPLACE -> focusStack.peekLast() === owner
            MqttSubscribePolicy.UNION_RESUMED -> focusStack.contains(owner)
        }
    }

    private fun applyEffectiveLocked() {
        val desired = OverlayTopicBook.union(resolveFocusTopicsLocked(), overlayBook.all())
        if (client.isConnected()) {
            client.syncTopics(desired)
        } else {
            MqttLog.logger(
                TAG,
                "尚未连接，有效主题已缓存 count=${desired.size}，待 flushPendingSubscriptions",
            )
        }
    }

    /**
     * 按 [subscribePolicy] 解析页面侧主题（不含 Overlay）。
     * 栈空且 [pausedKeepOwner] 有效时，保留其主题（兼容 1.4 unsubscribeOnPause=false）。
     */
    private fun resolveFocusTopicsLocked(): Set<String> {
        pruneFocusStackLocked()
        if (focusStack.isNotEmpty()) {
            return when (subscribePolicy) {
                MqttSubscribePolicy.FOCUS_REPLACE -> {
                    val top = focusStack.peekLast() ?: return emptySet()
                    sessions[top]?.resolveTopics().orEmpty()
                }
                MqttSubscribePolicy.UNION_RESUMED -> {
                    val merged = linkedSetOf<String>()
                    for (owner in focusStack) {
                        val session = sessions[owner] ?: continue
                        if (session.resumed) {
                            merged.addAll(session.resolveTopics())
                        }
                    }
                    merged
                }
            }
        }
        val keep = pausedKeepOwner ?: return emptySet()
        val session = sessions[keep]
        if (session != null && !session.resumed && !session.unsubscribeOnPause) {
            return session.resolveTopics()
        }
        pausedKeepOwner = null
        return emptySet()
    }

    private fun onOwnerResumed(owner: LifecycleOwner) {
        synchronized(lock) {
            val session = sessions[owner] ?: return
            session.resumed = true
            pausedKeepOwner = null
            removeFromFocusStackLocked(owner)
            focusStack.addLast(owner)
            applyEffectiveLocked()
        }
    }

    private fun onOwnerPaused(owner: LifecycleOwner) {
        synchronized(lock) {
            val session = sessions[owner] ?: return
            session.resumed = false
            removeFromFocusStackLocked(owner)
            if (!session.unsubscribeOnPause && focusStack.isEmpty()) {
                pausedKeepOwner = owner
            } else if (pausedKeepOwner === owner) {
                pausedKeepOwner = null
            }
            applyEffectiveLocked()
        }
    }

    private fun onOwnerDestroyed(owner: LifecycleOwner) {
        release(owner)
    }

    private fun pruneFocusStackLocked() {
        while (focusStack.isNotEmpty()) {
            val top = focusStack.peekLast() ?: break
            val session = sessions[top]
            if (session == null || !session.resumed) {
                focusStack.removeLast()
                continue
            }
            break
        }
        // 清理栈中已失效中间项（UNION_RESUMED 需要）
        val it = focusStack.iterator()
        while (it.hasNext()) {
            val owner = it.next()
            val session = sessions[owner]
            if (session == null || !session.resumed) {
                it.remove()
            }
        }
    }

    private fun removeFromFocusStackLocked(owner: LifecycleOwner) {
        val it = focusStack.iterator()
        while (it.hasNext()) {
            if (it.next() === owner) {
                it.remove()
            }
        }
    }

    private fun tokenLabel(token: Any): String {
        return token.javaClass.simpleName + "@" + Integer.toHexString(System.identityHashCode(token))
    }

    private inner class OwnerSession(
        var topicsProvider: MqttTopicsProvider,
        var unsubscribeOnPause: Boolean,
    ) {
        var resumed: Boolean = false
        var cachedTopics: Set<String> = emptySet()
        /**
         * true：已通过 [updateTopics] / [addTopics] / [removeTopics] 指定目标集合，
         * resume 时不再被 provider 覆盖。
         */
        var manualTopics: Boolean = false

        fun ensureManualBaseLocked() {
            if (!manualTopics) {
                cachedTopics = readProviderTopics()
                manualTopics = true
            }
        }

        fun resolveTopics(): Set<String> {
            if (manualTopics) {
                return cachedTopics
            }
            val fromProvider = readProviderTopics()
            if (fromProvider.isNotEmpty()) {
                cachedTopics = fromProvider
            }
            return cachedTopics.ifEmpty { fromProvider }
        }

        fun readProviderTopics(): Set<String> {
            return runCatching { topicsProvider.topics() }
                .getOrElse {
                    MqttLog.logger(TAG, "topicsProvider 异常: ${it.message}")
                    emptyList()
                }
                .let { OverlayTopicBook.normalizeTopics(it) }
        }

        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                onOwnerResumed(owner)
            }

            override fun onPause(owner: LifecycleOwner) {
                onOwnerPaused(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                onOwnerDestroyed(owner)
            }
        }
    }

    companion object {
        private const val TAG = "MqttSession"
    }
}

/**
 * Java / Kotlin 共用的主题提供者（SAM）。
 *
 * ```java
 * session.observe(this, () -> Arrays.asList("room/1/osd"));
 * session.bindOverlayTopics(dialog, "dialog/" + id + "/cmd");
 * ```
 *
 * @author fz
 * @version 1.5
 * @since 1.4
 */
fun interface MqttTopicsProvider {
    fun topics(): Collection<String>
}
