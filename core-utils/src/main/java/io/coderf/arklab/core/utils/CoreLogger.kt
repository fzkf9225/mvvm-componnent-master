package io.coderf.arklab.core.utils

/**
 * core 模块轻量日志门面，避免 core-* 反向依赖 common 的 LogUtil。
 * 业务侧仍可继续使用 common 的 LogUtil。
 */
object CoreLogger {
    @JvmStatic
    var enabled: Boolean = true

    @JvmStatic
    fun d(tag: String, message: String) {
        if (enabled) {
            android.util.Log.d(tag, message)
        }
    }

    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (enabled) {
            if (throwable != null) {
                android.util.Log.e(tag, message, throwable)
            } else {
                android.util.Log.e(tag, message)
            }
        }
    }

    @JvmStatic
    fun w(tag: String, message: String) {
        if (enabled) {
            android.util.Log.w(tag, message)
        }
    }
}
