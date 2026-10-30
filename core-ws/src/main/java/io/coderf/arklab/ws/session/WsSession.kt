package io.coderf.arklab.ws.session

import android.app.Dialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.coderf.arklab.ws.WsClient
import io.coderf.arklab.ws.utils.WsLog
import java.util.ArrayDeque
import java.util.WeakHashMap

/**
 * 基于 Lifecycle 的 WebSocket 逻辑频道订阅会话管理。
 *
 * - [observe] 绑定任意 [LifecycleOwner]（Activity / Fragment / DialogFragment / ComponentDialog）
 * - [observeDialog] 绑定经典 [Dialog]（内部用 [DialogLifecycleOwner] 桥接；会进入焦点栈）
 * - [bindOverlayChannels] / [unbindOverlayChannels]：与焦点无关的临时叠加频道（页面 ∪ Overlay）
 * - 焦点策略见 [WsSubscribePolicy]
 * - 频道变更：[updateChannels] 全量替换；[addChannels] / [removeChannels] 增量追加 / 移除
 * - 未连接时缓存 pending 频道，连上后再 [flushPendingSubscriptions]
 *
 * 传输仍委托 [WsClient]，本类只编排「谁该订哪些逻辑频道」。
 * **不包含**业务消息解析、信封、bizCode 等，由宿主二次扩展。
 *
 * @param client 异步 WebSocket 客户端
 * @param subscribePolicy 焦点栈如何贡献页面侧频道；Overlay 始终并入最终集合
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
class WsSession @JvmOverloads constructor(
    private val client: WsClient,
    private val subscribePolicy: WsSubscribePolicy = WsSubscribePolicy.FOCUS_REPLACE,
) {

    /** 同步锁，保护会话表与焦点栈 */
    private val lock = Any()

    /** Owner → 会话状态表（弱引用，随 Owner 回收） */
    private val sessions = WeakHashMap<LifecycleOwner, OwnerSession>()
    /** 焦点栈：末尾为最近 resumed 的 Owner */
    private val focusStack = ArrayDeque<LifecycleOwner>()
    /** 叠加频道簿 */
    private val overlayBook = OverlayChannelBook()
    /**
     * pause 且 [OwnerSession.unsubscribeOnPause]=false、焦点栈空时，
     * 仍保留该 Owner 频道（兼容 1.4）。
     */
    private var pausedKeepOwner: LifecycleOwner? = null

    /** 当前订阅策略 */
    fun getSubscribePolicy(): WsSubscribePolicy = subscribePolicy

    /**
     * 绑定 LifecycleOwner 并观察生命周期。
     *
     * @param owner Activity / Fragment / DialogFragment / ComponentDialog 等
     * @param channelsProvider 返回当前期望订阅的逻辑频道；可在数据加载后通过 [updateChannels] / [addChannels] / [removeChannels] 刷新
     * @param unsubscribeOnPause 是否在 onPause 时退订（默认 true，省流量）
     */
    @JvmOverloads
    fun observe(
        owner: LifecycleOwner,
        channelsProvider: WsChannelsProvider,
        unsubscribeOnPause: Boolean = true,
    ) {
        synchronized(lock) {
            val existing = sessions[owner]
            if (existing != null) {
                existing.channelsProvider = channelsProvider
                existing.unsubscribeOnPause = unsubscribeOnPause
                // 重新绑定 provider 时回到「以 provider 为准」
                existing.manualChannels = false
                return
            }
            val session = OwnerSession(
                channelsProvider = channelsProvider,
                unsubscribeOnPause = unsubscribeOnPause,
            )
            sessions[owner] = session
            owner.lifecycle.addObserver(session.observer)
        }
    }

    /**
     * 绑定经典 [Dialog]：show 时进入焦点栈，dismiss 时退出并恢复下层。
     *
     * 在 [WsSubscribePolicy.FOCUS_REPLACE] 下会「替换」下层页面频道。
     * 若弹窗频道需与页面 **并存**，请改用 [bindOverlayChannels]，不要调用本方法。
     *
     * DialogFragment / ComponentDialog 请直接用 [observe]。
     *
     * @return 桥接用的 [DialogLifecycleOwner]；弹窗不再使用时调用 [DialogLifecycleOwner.destroy]
     */
    @JvmOverloads
    fun observeDialog(
        dialog: Dialog,
        channelsProvider: WsChannelsProvider,
        unsubscribeOnPause: Boolean = true,
    ): DialogLifecycleOwner {
        val bridge = DialogLifecycleOwner(dialog)
        observe(bridge, channelsProvider, unsubscribeOnPause)
        return bridge
    }

    /**
     * 绑定与 Lifecycle 焦点无关的叠加频道。
     *
     * 最终对端订阅 = [resolveFocusChannels] ∪ 全部 Overlay。
     * 同一 [token] 再次调用会覆盖该 token 的频道集合；传空集合等价于 [unbindOverlayChannels]。
     *
     * 典型：起飞自检 Dialog 只加临时频道，同时保留详情页 OSD / 直播订阅。
     */
    fun bindOverlayChannels(token: Any, channels: Collection<String>) {
        synchronized(lock) {
            overlayBook.bind(token, channels)
            WsLog.logger(
                TAG,
                "bindOverlay token=${tokenLabel(token)} count=${overlayBook.get(token).size} " +
                    "overlayTotal=${overlayBook.all().size}",
            )
            applyEffectiveLocked()
        }
    }

    /** 叠加频道（Java / Kotlin vararg） */
    fun bindOverlayChannels(token: Any, vararg channels: String) {
        bindOverlayChannels(token, channels.asList())
    }

    /**
     * 移除 [token] 对应的叠加频道，并重新对齐对端。
     *
     * @return 是否确实移除过该 token
     */
    fun unbindOverlayChannels(token: Any): Boolean {
        synchronized(lock) {
            val removed = overlayBook.unbind(token)
            if (removed) {
                WsLog.logger(TAG, "unbindOverlay token=${tokenLabel(token)}")
                applyEffectiveLocked()
            }
            return removed
        }
    }

    /** 清空全部 Overlay 并重新对齐 */
    fun clearOverlayChannels() {
        synchronized(lock) {
            if (overlayBook.isEmpty()) {
                return
            }
            overlayBook.clear()
            WsLog.logger(TAG, "clearOverlay")
            applyEffectiveLocked()
        }
    }

    /** 全部 Overlay 频道并集快照 */
    fun getOverlayChannels(): Set<String> = overlayBook.all()

    /** 某 token 的 Overlay 快照 */
    fun getOverlayChannels(token: Any): Set<String> = overlayBook.get(token)

    /**
     * 当前应对齐到对端的有效频道（页面侧 ∪ Overlay）。
     * 未连接时仍返回目标集合，便于调试。
     */
    fun getEffectiveChannels(): Set<String> {
        synchronized(lock) {
            return OverlayChannelBook.union(resolveFocusChannelsLocked(), overlayBook.all())
        }
    }

    /**
     * 全量替换某 Owner 的目标频道，并立即 diff 应用（仅当其为频道贡献方且 RESUMED）。
     *
     * 之后 pause/resume 仍以本次集合为准，直到再次 [observe] 或调用本方法 / 增量 API。
     */
    fun updateChannels(owner: LifecycleOwner, channels: Collection<String>) {
        synchronized(lock) {
            val session = sessions[owner] ?: return
            session.cachedChannels = OverlayChannelBook.normalizeChannels(channels)
            session.manualChannels = true
            applyIfOwnerContributesLocked(owner)
        }
    }

    /**
     * 在当前目标频道上追加订阅（已存在的忽略）。
     *
     * 若尚未手动改过频道，会先用 [WsChannelsProvider] 的结果作为基底再合并。
     */
    fun addChannels(owner: LifecycleOwner, channels: Collection<String>) {
        synchronized(lock) {
            val session = sessions[owner] ?: return
            val toAdd = OverlayChannelBook.normalizeChannels(channels)
            if (toAdd.isEmpty()) {
                return
            }
            session.ensureManualBaseLocked()
            session.cachedChannels += toAdd
            applyIfOwnerContributesLocked(owner)
        }
    }

    /** 追加订阅（Java / Kotlin vararg） */
    fun addChannels(owner: LifecycleOwner, vararg channels: String) {
        addChannels(owner, channels.asList())
    }

    /**
     * 从当前目标频道中移除订阅（不存在的忽略）。
     *
     * 若尚未手动改过频道，会先用 [WsChannelsProvider] 的结果作为基底再减去。
     */
    fun removeChannels(owner: LifecycleOwner, channels: Collection<String>) {
        synchronized(lock) {
            val session = sessions[owner] ?: return
            val toRemove = OverlayChannelBook.normalizeChannels(channels)
            if (toRemove.isEmpty()) {
                return
            }
            session.ensureManualBaseLocked()
            session.cachedChannels -= toRemove
            applyIfOwnerContributesLocked(owner)
        }
    }

    /** 移除订阅（Java / Kotlin vararg） */
    fun removeChannels(owner: LifecycleOwner, vararg channels: String) {
        removeChannels(owner, channels.asList())
    }

    /** 当前 Owner 缓存的目标频道快照（未 observe 时返回空集） */
    fun getChannels(owner: LifecycleOwner): Set<String> {
        synchronized(lock) {
            val session = sessions[owner] ?: return emptySet()
            return if (session.manualChannels) {
                session.cachedChannels
            } else {
                session.resolveChannels()
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

    /** 连接成功后调用，把当前有效频道刷到对端 */
    fun flushPendingSubscriptions() {
        synchronized(lock) {
            applyEffectiveLocked()
        }
    }

    /** 仅当 owner 当前贡献频道时刷新有效订阅 */
    private fun applyIfOwnerContributesLocked(owner: LifecycleOwner) {
        if (!ownerContributesLocked(owner)) {
            return
        }
        applyEffectiveLocked()
    }

    /** 判断 owner 是否为频道贡献方（resumed 且满足焦点策略） */
    private fun ownerContributesLocked(owner: LifecycleOwner): Boolean {
        val session = sessions[owner] ?: return false
        if (!session.resumed) {
            return false
        }
        return when (subscribePolicy) {
            WsSubscribePolicy.FOCUS_REPLACE -> focusStack.peekLast() === owner
            WsSubscribePolicy.UNION_RESUMED -> focusStack.contains(owner)
        }
    }

    /** 将页面侧 ∪ Overlay 对齐到对端；未连接时仅缓存 */
    private fun applyEffectiveLocked() {
        val desired = OverlayChannelBook.union(resolveFocusChannelsLocked(), overlayBook.all())
        if (client.isConnected()) {
            client.syncChannels(desired)
        } else {
            WsLog.logger(
                TAG,
                "尚未连接，有效频道已缓存 count=${desired.size}，待 flushPendingSubscriptions",
            )
        }
    }

    /**
     * 按 [subscribePolicy] 解析页面侧频道（不含 Overlay）。
     * 栈空且 [pausedKeepOwner] 有效时，保留其频道（兼容 1.4 unsubscribeOnPause=false）。
     */
    private fun resolveFocusChannelsLocked(): Set<String> {
        pruneFocusStackLocked()
        if (focusStack.isNotEmpty()) {
            return when (subscribePolicy) {
                WsSubscribePolicy.FOCUS_REPLACE -> {
                    val top = focusStack.peekLast() ?: return emptySet()
                    sessions[top]?.resolveChannels().orEmpty()
                }
                WsSubscribePolicy.UNION_RESUMED -> {
                    val merged = linkedSetOf<String>()
                    for (owner in focusStack) {
                        val session = sessions[owner] ?: continue
                        if (session.resumed) {
                            merged.addAll(session.resolveChannels())
                        }
                    }
                    merged
                }
            }
        }
        val keep = pausedKeepOwner ?: return emptySet()
        val session = sessions[keep]
        if (session != null && !session.resumed && !session.unsubscribeOnPause) {
            return session.resolveChannels()
        }
        pausedKeepOwner = null
        return emptySet()
    }

    /** Owner resume：入焦点栈并刷新订阅 */
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

    /** Owner pause：出焦点栈，必要时记录保留 Owner */
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

    /** Owner 销毁：释放会话 */
    private fun onOwnerDestroyed(owner: LifecycleOwner) {
        release(owner)
    }

    /** 清理焦点栈中已失效（未注册或未 resumed）的 Owner */
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

    /** 从焦点栈移除指定 Owner */
    private fun removeFromFocusStackLocked(owner: LifecycleOwner) {
        val it = focusStack.iterator()
        while (it.hasNext()) {
            if (it.next() === owner) {
                it.remove()
            }
        }
    }

    /** token 日志标签：类名@哈希 */
    private fun tokenLabel(token: Any): String {
        return token.javaClass.simpleName + "@" + Integer.toHexString(System.identityHashCode(token))
    }

    private inner class OwnerSession(
        var channelsProvider: WsChannelsProvider,
        var unsubscribeOnPause: Boolean,
    ) {
        /** 是否处于 resumed 状态 */
        var resumed: Boolean = false

        /** 最近一次解析出的目标频道缓存 */
        var cachedChannels: Set<String> = emptySet()
        /**
         * true：已通过 [updateChannels] / [addChannels] / [removeChannels] 指定目标集合，
         * resume 时不再被 provider 覆盖。
         */
        var manualChannels: Boolean = false

        /** 首次手动修改前，以 provider 结果作为基底 */
        fun ensureManualBaseLocked() {
            if (!manualChannels) {
                cachedChannels = readProviderChannels()
                manualChannels = true
            }
        }

        /** 解析当前目标频道：手动集优先，否则读 provider（并缓存） */
        fun resolveChannels(): Set<String> {
            if (manualChannels) {
                return cachedChannels
            }
            val fromProvider = readProviderChannels()
            if (fromProvider.isNotEmpty()) {
                cachedChannels = fromProvider
            }
            return cachedChannels.ifEmpty { fromProvider }
        }

        /** 安全读取 provider 频道（异常返回空） */
        fun readProviderChannels(): Set<String> {
            return runCatching { channelsProvider.channels() }
                .getOrElse {
                    WsLog.logger(TAG, "channelsProvider 异常: ${it.message}")
                    emptyList()
                }
                .let { OverlayChannelBook.normalizeChannels(it) }
        }

        /** 生命周期观察者，桥接到会话编排 */
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
        private const val TAG = "WsSession"
    }
}

/**
 * Java / Kotlin 共用的逻辑频道提供者（SAM）。
 *
 * ```java
 * session.observe(this, () -> Arrays.asList("room/1/osd"));
 * session.bindOverlayChannels(dialog, "dialog/" + id + "/cmd");
 * ```
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
fun interface WsChannelsProvider {
    fun channels(): Collection<String>
}
