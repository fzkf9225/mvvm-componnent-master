package io.coderf.arklab.core.utils.ext

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * 数值 / [BigDecimal] / 安全四则运算扩展。
 *
 * 金额、税率等场景优先走 BigDecimal，避免 Double 精度问题。
 * 从 core-base Extensions 迁移。
 */

// ==================== String → BigDecimal ====================

/**
 * 安全转为 [BigDecimal]；null / 空白视为 0。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
fun String?.toBigDecimalOrZero(): BigDecimal {
    return if (this.isNullOrBlank()) BigDecimal.ZERO else BigDecimal(this)
}

/**
 * 安全转为 [BigDecimal]；解析失败或空白时返回 [defaultValue]。
 */
fun String?.toBigDecimalOrDefault(defaultValue: BigDecimal = BigDecimal.ZERO): BigDecimal {
    return try {
        if (this.isNullOrBlank()) defaultValue else BigDecimal(this)
    } catch (_: Exception) {
        defaultValue
    }
}

// ==================== BigDecimal 常用 ====================

/** 转为 Double（语义别名，便于链式调用）。 */
fun BigDecimal.toDoubleSafe(): Double = this.toDouble()

/** 转为 Float。 */
fun BigDecimal.toFloatSafe(): Float = this.toFloat()

/** 转为 Int（截断小数部分）。 */
fun BigDecimal.toIntSafe(): Int = this.toInt()

/**
 * 设置小数位数并四舍五入（默认 2 位）。
 */
fun BigDecimal.withScale(
    scale: Int = 2,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
): BigDecimal {
    return this.setScale(scale, roundingMode)
}

/**
 * 千分位格式化显示。
 *
 * @param scale 小数位数，默认 2
 */
fun BigDecimal.toFormattedString(scale: Int = 2): String {
    return DecimalFormat("#,##0.${"0".repeat(scale)}").format(this)
}

/** 是否等于 0（用 compareTo，避免 scale 干扰）。 */
fun BigDecimal.isZero(): Boolean = this.compareTo(BigDecimal.ZERO) == 0

/** 是否为正数。 */
fun BigDecimal.isPositive(): Boolean = this > BigDecimal.ZERO

/** 是否为负数。 */
fun BigDecimal.isNegative(): Boolean = this < BigDecimal.ZERO

/**
 * 金额格式（前缀 ¥ + 千分位）。
 */
fun BigDecimal.toCurrencyString(scale: Int = 2): String {
    return "¥${this.toFormattedString(scale)}"
}

/**
 * 百分比格式（先 ×100 再拼接 %）。
 *
 * 例：`0.156.toBigDecimal().toPercentString()` → `"15.60%"`
 */
fun BigDecimal.toPercentString(scale: Int = 2): String {
    val percent = this.multiply(BigDecimal(100)).setScale(scale, RoundingMode.HALF_UP)
    return "${percent}%"
}

/** 科学计数法字符串。 */
fun BigDecimal.toScientificString(): String {
    return DecimalFormat("0.###E0").format(this)
}

// ==================== Double 安全四则（结果为 BigDecimal） ====================

/** Double 安全加法。 */
fun Double.add(other: Double, scale: Int = 2): BigDecimal {
    return BigDecimal(this.toString())
        .add(BigDecimal(other.toString()))
        .setScale(scale, RoundingMode.HALF_UP)
}

/** Double 安全减法。 */
fun Double.subtract(other: Double, scale: Int = 2): BigDecimal {
    return BigDecimal(this.toString())
        .subtract(BigDecimal(other.toString()))
        .setScale(scale, RoundingMode.HALF_UP)
}

/** Double 安全乘法。 */
fun Double.multiply(other: Double, scale: Int = 2): BigDecimal {
    return BigDecimal(this.toString())
        .multiply(BigDecimal(other.toString()))
        .setScale(scale, RoundingMode.HALF_UP)
}

/**
 * Double 安全除法；除数为 0 时返回 [BigDecimal.ZERO]。
 */
fun Double.divide(other: Double, scale: Int = 2): BigDecimal {
    if (other == 0.0) return BigDecimal.ZERO
    return BigDecimal(this.toString())
        .divide(BigDecimal(other.toString()), scale, RoundingMode.HALF_UP)
}

// ==================== String 数值四则 ====================

/** 字符串数值安全加法。 */
fun String.add(other: String, scale: Int = 2): BigDecimal {
    return this.toBigDecimalOrZero()
        .add(other.toBigDecimalOrZero())
        .setScale(scale, RoundingMode.HALF_UP)
}

/** 字符串数值安全减法。 */
fun String.subtract(other: String, scale: Int = 2): BigDecimal {
    return this.toBigDecimalOrZero()
        .subtract(other.toBigDecimalOrZero())
        .setScale(scale, RoundingMode.HALF_UP)
}

/** 字符串数值安全乘法。 */
fun String.multiply(other: String, scale: Int = 2): BigDecimal {
    return this.toBigDecimalOrZero()
        .multiply(other.toBigDecimalOrZero())
        .setScale(scale, RoundingMode.HALF_UP)
}

/**
 * 字符串数值安全除法；除数为 0 时返回 [BigDecimal.ZERO]。
 */
fun String.divide(other: String, scale: Int = 2): BigDecimal {
    val b2 = other.toBigDecimalOrZero()
    if (b2.isZero()) return BigDecimal.ZERO
    return this.toBigDecimalOrZero().divide(b2, scale, RoundingMode.HALF_UP)
}

// ==================== 比较（infix） ====================

/** 两个 Double 按 BigDecimal 精确相等。 */
infix fun Double.eq(other: Double): Boolean {
    return BigDecimal(this.toString()).compareTo(BigDecimal(other.toString())) == 0
}

/** 两个字符串数值精确相等。 */
infix fun String.eq(other: String): Boolean {
    return this.toBigDecimalOrZero().compareTo(other.toBigDecimalOrZero()) == 0
}

/** Double 是否大于。 */
infix fun Double.gt(other: Double): Boolean {
    return BigDecimal(this.toString()) > BigDecimal(other.toString())
}

/** 字符串数值是否大于。 */
infix fun String.gt(other: String): Boolean {
    return this.toBigDecimalOrZero() > other.toBigDecimalOrZero()
}

/** Double 是否小于。 */
infix fun Double.lt(other: Double): Boolean {
    return BigDecimal(this.toString()) < BigDecimal(other.toString())
}

/** 字符串数值是否小于。 */
infix fun String.lt(other: String): Boolean {
    return this.toBigDecimalOrZero() < other.toBigDecimalOrZero()
}

// ==================== 财务常用 ====================

/**
 * 税额 = 金额 × 税率。
 */
fun BigDecimal.calculateTax(taxRate: BigDecimal, scale: Int = 2): BigDecimal {
    return this.multiply(taxRate).setScale(scale, RoundingMode.HALF_UP)
}

/**
 * 折后价 = 原价 × (1 − 折扣率)。
 *
 * @param discountRate 折扣率，如 0.1 表示九折（减 10%）
 */
fun BigDecimal.applyDiscount(discountRate: BigDecimal, scale: Int = 2): BigDecimal {
    return this.multiply(BigDecimal.ONE.subtract(discountRate))
        .setScale(scale, RoundingMode.HALF_UP)
}

/**
 * 年化收益率（简单口径）：(收益 / 本金) × (365 / 持有天数)。
 *
 * 本方法接收者为「期末金额」；[principal] 为本金。
 */
fun BigDecimal.calculateAnnualizedReturn(
    principal: BigDecimal,
    days: Int,
    scale: Int = 2
): BigDecimal {
    if (principal.isZero() || days == 0) return BigDecimal.ZERO
    val profit = this.subtract(principal)
    return profit.divide(principal, scale + 2, RoundingMode.HALF_UP)
        .multiply(BigDecimal(365))
        .divide(BigDecimal(days), scale, RoundingMode.HALF_UP)
}

/**
 * 等额本息每月还款额。
 *
 * 接收者为贷款本金；[annualRate] 为年利率（如 0.045 表示 4.5%）。
 */
fun BigDecimal.calculateMonthlyPayment(
    months: Int,
    annualRate: BigDecimal,
    scale: Int = 2
): BigDecimal {
    if (this.isZero() || months == 0) return BigDecimal.ZERO

    val monthlyRate = annualRate.divide(BigDecimal(12), 6, RoundingMode.HALF_UP)
    val ratePow = (BigDecimal.ONE + monthlyRate).pow(months)

    return this.multiply(monthlyRate)
        .multiply(ratePow)
        .divide(ratePow.subtract(BigDecimal.ONE), scale, RoundingMode.HALF_UP)
}
