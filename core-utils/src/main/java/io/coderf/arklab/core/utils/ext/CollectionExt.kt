package io.coderf.arklab.core.utils.ext

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 集合相关扩展：条件执行 + 字符串/数值列表的统计。
 *
 * 数值统计部分从 core-base Extensions 迁移。
 * 下标安全访问请用标准库 [List.getOrNull]，过滤映射请用 [Iterable.mapNotNull]。
 */

// ==================== 条件执行 ====================

/**
 * 若集合非空则执行 [block]，并返回其结果；否则返回 null。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
inline fun <T, R> Collection<T>.ifNotEmpty(block: (Collection<T>) -> R): R? {
    return if (isNotEmpty()) block(this) else null
}

// ==================== 字符串列表数值统计 ====================

/**
 * 计算字符串数值列表的平均值；空列表返回 0。
 */
fun List<String>.average(scale: Int = 2): BigDecimal {
    if (this.isEmpty()) return BigDecimal.ZERO
    val sum = this.fold(BigDecimal.ZERO) { acc, s -> acc + s.toBigDecimalOrZero() }
    return sum.divide(BigDecimal(this.size), scale, RoundingMode.HALF_UP)
}

/**
 * 计算字符串数值列表的和。
 */
fun List<String>.sum(scale: Int = 2): BigDecimal {
    return this.fold(BigDecimal.ZERO) { acc, s -> acc + s.toBigDecimalOrZero() }
        .setScale(scale, RoundingMode.HALF_UP)
}

/**
 * 字符串数值列表最大值；空或全部非法时返回 null。
 */
fun List<String>.max(): BigDecimal? {
    if (this.isEmpty()) return null
    return this.mapNotNull { it.toBigDecimalOrNull() }.maxOrNull()
}

/**
 * 字符串数值列表最小值；空或全部非法时返回 null。
 */
fun List<String>.min(): BigDecimal? {
    if (this.isEmpty()) return null
    return this.mapNotNull { it.toBigDecimalOrNull() }.minOrNull()
}

/**
 * Double 列表求和（BigDecimal 精度）。
 */
fun List<Double>.sumBigDecimal(scale: Int = 2): BigDecimal {
    return this.fold(BigDecimal.ZERO) { acc, d -> acc + BigDecimal(d.toString()) }
        .setScale(scale, RoundingMode.HALF_UP)
}
