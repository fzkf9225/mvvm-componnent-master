package io.coderf.arklab.core.utils.ext

/**
 * 字符串通用扩展（无业务 R 依赖）。
 *
 * 补充 core-base 迁移内容之外的常用能力，便于业务与其它 core 模块复用。
 */

/**
 * 是否为非空且非纯空白（与 [String.isNullOrBlank] 相反的「有效内容」判断）。
 */
fun String?.isNotNullOrBlank(): Boolean = !isNullOrBlank()

/**
 * 空白则返回 [default]，否则返回自身（已 trim 可选）。
 *
 * @param trim 是否先 trim，默认 true
 */
fun String?.orDefault(default: String, trim: Boolean = true): String {
    val value = if (trim) this?.trim() else this
    return if (value.isNullOrBlank()) default else value
}

/**
 * 仅保留数字字符（可用于手机号、验证码清洗）。
 */
fun String.digitsOnly(): String = filter { it.isDigit() }

/**
 * 是否全部为数字（空串返回 false）。
 */
fun String.isDigitsOnly(): Boolean = isNotEmpty() && all { it.isDigit() }

/**
 * 安全转 Int；失败返回 [default]。
 */
fun String?.toIntOrDefault(default: Int = 0): Int {
    return this?.toIntOrNull() ?: default
}

/**
 * 安全转 Long；失败返回 [default]。
 */
fun String?.toLongOrDefault(default: Long = 0L): Long {
    return this?.toLongOrNull() ?: default
}

/**
 * 安全转 Double；失败返回 [default]。
 */
fun String?.toDoubleOrDefault(default: Double = 0.0): Double {
    return this?.toDoubleOrNull() ?: default
}

/**
 * 中间打码：保留前后各 [prefix]/[suffix] 位，中间用 [mask] 填充。
 *
 * 例：`"13812345678".mask(3, 4)` → `"138****5678"`
 * 长度不足时原样返回。
 */
fun String.mask(prefix: Int = 3, suffix: Int = 4, mask: Char = '*'): String {
    if (length <= prefix + suffix) return this
    val middle = mask.toString().repeat(length - prefix - suffix)
    return take(prefix) + middle + takeLast(suffix)
}

/**
 * 按最大长度截断，超出部分追加 [ellipsis]（默认 `"..."`）。
 */
fun String.ellipsis(maxLength: Int, ellipsis: String = "..."): String {
    if (maxLength <= 0) return ""
    if (length <= maxLength) return this
    if (maxLength <= ellipsis.length) return take(maxLength)
    return take(maxLength - ellipsis.length) + ellipsis
}
