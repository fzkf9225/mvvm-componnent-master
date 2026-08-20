package io.coderf.arklab.mqtt.session

import android.app.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * 将经典 [Dialog] 的 show / dismiss 桥接为 [LifecycleOwner]，
 * 供 [MqttSession.observe] 做自动订退。
 *
 * ## 优先选用（无需本类）
 * - [androidx.fragment.app.DialogFragment]：直接 `observe(fragment)` 或 `observe(viewLifecycleOwner)`
 * - [androidx.activity.ComponentDialog]：本身已是 LifecycleOwner，直接 `observe(dialog)`
 *
 * ## 行为
 * - `show` → CREATED → STARTED → RESUMED（触发订阅）
 * - `dismiss` → PAUSED → STOPPED，保留 CREATED（可再次 show）
 * - [destroy] → DESTROYED（从 [MqttSession] 清理）
 *
 * **注意**：本类会设置 Dialog 的 OnShow / OnDismiss；若业务也需要监听，
 * 请先调用 [MqttSession.observeDialog]，再自行包装，或改用 DialogFragment / ComponentDialog。
 *
 * @author fz
 * @version 1.4
 * @since 1.4
 */
class DialogLifecycleOwner(
    dialog: Dialog,
) : LifecycleOwner {

    private val registry = LifecycleRegistry(this)

    init {
        registry.currentState = Lifecycle.State.INITIALIZED
        dialog.setOnShowListener { moveToResumed() }
        dialog.setOnDismissListener { moveToCreated() }
    }

    override val lifecycle: Lifecycle
        get() = registry

    /** 弹窗彻底不用时调用，触发 ON_DESTROY 并从 [MqttSession] 清理 */
    fun destroy() {
        if (registry.currentState == Lifecycle.State.INITIALIZED
            || registry.currentState == Lifecycle.State.DESTROYED
        ) {
            return
        }
        if (registry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
        if (registry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
        if (registry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }

    private fun moveToResumed() {
        if (registry.currentState == Lifecycle.State.DESTROYED) {
            return
        }
        if (registry.currentState == Lifecycle.State.INITIALIZED) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }
        if (registry.currentState == Lifecycle.State.CREATED) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
        if (registry.currentState == Lifecycle.State.STARTED) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    private fun moveToCreated() {
        if (!registry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            return
        }
        if (registry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
        if (registry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
    }
}
