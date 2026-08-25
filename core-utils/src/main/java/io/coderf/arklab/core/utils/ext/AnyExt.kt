package io.coderf.arklab.core.utils.ext

/**
 * 任意类型上的轻量条件 / 作用域扩展。
 */

/**
 * 当 [condition] 为 true 时执行 [block]，并返回接收者本身（便于链式调用）。
 *
 * ```
 * textView.whatIf(showHint) { hint = "请输入" }
 * ```
 */
inline fun <T> T.whatIf(condition: Boolean, block: T.() -> Unit): T {
    if (condition) block()
    return this
}

/**
 * 当接收者非 null 且 [condition] 为 true 时执行 [block]。
 */
inline fun <T : Any> T?.whatIfNotNull(
    condition: Boolean = true,
    block: (T) -> Unit
): T? {
    if (this != null && condition) block(this)
    return this
}

/**
 * 将可空值转换为默认值（语义同 `?:`，适合作为具名参数传递时更清晰）。
 */
fun <T> T?.orElse(default: T): T = this ?: default
