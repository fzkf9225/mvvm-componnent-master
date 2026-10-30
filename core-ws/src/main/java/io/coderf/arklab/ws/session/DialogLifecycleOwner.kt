package io.coderf.arklab.ws.session

import android.app.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * 将经典 [Dialog] 的 show / dismiss 桥接为 [LifecycleOwner]，
 * 供 [WsSession.observe] 做自动频道编排。
 *
 * ## 优先选用（无需本类）
 * - DialogFragment：直接 `observe(fragment)` 或 `observe(viewLifecycleOwner)`
 * - ComponentDialog：本身已是 LifecycleOwner，直接 `observe(dialog)`
 *
 * ## 行为
 * - `show` → CREATED → STARTED → RESUMED
 * - `dismiss` → PAUSED → STOPPED，保留 CREATED
 * - [destroy] → DESTROYED
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
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
