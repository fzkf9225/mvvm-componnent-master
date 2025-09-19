package pers.fz.mvvm.utils.common

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ==================== Context 相关扩展 ====================
/**
 * 显示Toast
 * @param message 消息内容
 * @param duration 显示时长，默认短时间
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * 显示Toast
 * @param resId 字符串资源ID
 * @param duration 显示时长，默认短时间
 */
fun Context.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}
// ==================== dp、sp、px 相关扩展 ====================
/**
 * DP转PX
 */
fun Number.dp2px(context: Context): Int {
    return DensityUtil.dp2px(context, toFloat())
}

/**
 * sp转px
 */
fun Number.sp2px(context: Context): Int {
    return DensityUtil.sp2px(context, toFloat())
}

/**
 * px转dp
 */
fun Number.px2dp(context: Context): Int {
    return DensityUtil.px2dp(context, toFloat())
}

/**
 * px转sp
 */
fun Number.px2sp(context: Context): Int {
    return DensityUtil.px2sp(context, toFloat())
}

// ==================== asset 相关扩展 ====================
/**
 * 快速读取Assets文件
 */
fun Context.readAssetsFile(fileName: String): String {
    return assets.open(fileName).bufferedReader().use { it.readText() }
}

// ==================== 时间 相关扩展 ====================
/**
 * 时间戳转格式化字符串
 */
fun Long.toDateTimeString(pattern: String = "yyyy-MM-dd HH:mm"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
}

/**
 * 获取日期的年份
 */
val Date.year: Int
    get() = Calendar.getInstance().apply { time = this@year }.get(Calendar.YEAR)

/**
 * 获取日期的月份 (1-12)
 */
val Date.month: Int
    get() = Calendar.getInstance().apply { time = this@month }.get(Calendar.MONTH) + 1

/**
 * 获取日期的天数
 */
val Date.day: Int
    get() = Calendar.getInstance().apply { time = this@day }.get(Calendar.DATE)

/**
 * 将Date格式化为字符串
 */
fun Date.format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}

/**
 * 日期加减天数
 */
fun Date.addDays(days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DAY_OF_MONTH, days)
    return calendar.time
}

/**
 * 日期加减月份
 */
fun Date.addMonths(months: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.MONTH, months)
    return calendar.time
}

/**
 * 时间戳转日期字符串
 */
fun Long.toDateString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
}

/**
 * 时间戳转Date对象
 */
fun Long.toDate(): Date {
    return Date(this)
}

/**
 * 判断时间戳是否是今天
 */
fun Long.isToday(): Boolean {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val targetDay = Calendar.getInstance().apply {
        timeInMillis = this@isToday
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    return today == targetDay
}

/**
 * 时间戳转相对时间（刚刚、几分钟前、几小时前等）
 */
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
        diff < 30 * 24 * 60 * 60 * 1000L -> "${diff / (24 * 60 * 60 * 1000)}天前"
        else -> this.toDateString("yyyy-MM-dd")
    }
}

/**
 * 字符串转Date对象
 */
fun String.toDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): Date? {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}

/**
 * 日期字符串格式转换
 */
fun String.convertDateFormat(
    fromPattern: String = "yyyy-MM-dd HH:mm:ss",
    toPattern: String = "yyyy-MM-dd"
): String {
    return try {
        val date = SimpleDateFormat(fromPattern, Locale.getDefault()).parse(this)
        return if (date != null) {
            SimpleDateFormat(toPattern, Locale.getDefault()).format(date)
        } else {
            this
        }
    } catch (e: Exception) {
        this
    }
}

/**
 * 判断字符串日期是否是今天
 */
fun String.isToday(datePattern: String = "yyyy-MM-dd"): Boolean {
    return try {
        val date = SimpleDateFormat(datePattern, Locale.getDefault()).parse(this)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val target = Calendar.getInstance().apply {
            time = date ?: Date()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        today == target
    } catch (e: Exception) {
        false
    }
}

// ==================== BigDecimal 扩展函数 ====================

/**
 * BigDecimal 扩展函数
 */

/**
 * 安全转换为BigDecimal，空字符串或null转为0
 */
fun String?.toBigDecimalOrZero(): BigDecimal {
    return if (this.isNullOrBlank()) BigDecimal.ZERO else BigDecimal(this)
}

/**
 * 安全转换为BigDecimal，可指定默认值
 */
fun String?.toBigDecimalOrDefault(defaultValue: BigDecimal = BigDecimal.ZERO): BigDecimal {
    return try {
        if (this.isNullOrBlank()) defaultValue else BigDecimal(this)
    } catch (e: Exception) {
        defaultValue
    }
}

/**
 * BigDecimal转为Double
 */
fun BigDecimal.toDoubleSafe(): Double {
    return this.toDouble()
}

/**
 * BigDecimal转为Float
 */
fun BigDecimal.toFloatSafe(): Float {
    return this.toFloat()
}

/**
 * BigDecimal转为Int
 */
fun BigDecimal.toIntSafe(): Int {
    return this.toInt()
}

/**
 * 设置精度并四舍五入
 */
fun BigDecimal.withScale(scale: Int = 2, roundingMode: RoundingMode = RoundingMode.HALF_UP): BigDecimal {
    return this.setScale(scale, roundingMode)
}

/**
 * 格式化显示（带千分位）
 */
fun BigDecimal.toFormattedString(scale: Int = 2): String {
    return DecimalFormat("#,##0.${"0".repeat(scale)}").format(this)
}

/**
 * 判断是否为0
 */
fun BigDecimal.isZero(): Boolean {
    return this.compareTo(BigDecimal.ZERO) == 0
}

/**
 * 判断是否为正数
 */
fun BigDecimal.isPositive(): Boolean {
    return this > BigDecimal.ZERO
}

/**
 * 判断是否为负数
 */
fun BigDecimal.isNegative(): Boolean {
    return this < BigDecimal.ZERO
}

// ==================== 数字运算 扩展函数 ====================
/**
 * Double安全加法
 */
fun Double.add(other: Double, scale: Int = 2): BigDecimal {
    return BigDecimal(this.toString()).add(BigDecimal(other.toString())).setScale(scale, RoundingMode.HALF_UP)
}

/**
 * Double安全减法
 */
fun Double.subtract(other: Double, scale: Int = 2): BigDecimal {
    return BigDecimal(this.toString()).subtract(BigDecimal(other.toString())).setScale(scale, RoundingMode.HALF_UP)
}

/**
 * Double安全乘法
 */
fun Double.multiply(other: Double, scale: Int = 2): BigDecimal {
    return BigDecimal(this.toString()).multiply(BigDecimal(other.toString())).setScale(scale, RoundingMode.HALF_UP)
}

/**
 * Double安全除法
 */
fun Double.divide(other: Double, scale: Int = 2): BigDecimal {
    if (other == 0.0) return BigDecimal.ZERO
    return BigDecimal(this.toString()).divide(BigDecimal(other.toString()), scale, RoundingMode.HALF_UP)
}

/**
 * String安全加法
 */
fun String.add(other: String, scale: Int = 2): BigDecimal {
    val b1 = this.toBigDecimalOrZero()
    val b2 = other.toBigDecimalOrZero()
    return b1.add(b2).setScale(scale, RoundingMode.HALF_UP)
}

/**
 * String安全减法
 */
fun String.subtract(other: String, scale: Int = 2): BigDecimal {
    val b1 = this.toBigDecimalOrZero()
    val b2 = other.toBigDecimalOrZero()
    return b1.subtract(b2).setScale(scale, RoundingMode.HALF_UP)
}

/**
 * String安全乘法
 */
fun String.multiply(other: String, scale: Int = 2): BigDecimal {
    val b1 = this.toBigDecimalOrZero()
    val b2 = other.toBigDecimalOrZero()
    return b1.multiply(b2).setScale(scale, RoundingMode.HALF_UP)
}

/**
 * String安全除法
 */
fun String.divide(other: String, scale: Int = 2): BigDecimal {
    val b1 = this.toBigDecimalOrZero()
    val b2 = other.toBigDecimalOrZero()
    if (b2.isZero()) return BigDecimal.ZERO
    return b1.divide(b2, scale, RoundingMode.HALF_UP)
}

// ==================== 集合数学运算 扩展函数 ====================

/**
 * 计算List的平均值
 */
fun List<String>.average(scale: Int = 2): BigDecimal {
    if (this.isEmpty()) return BigDecimal.ZERO
    val sum = this.fold(BigDecimal.ZERO) { acc, s -> acc + s.toBigDecimalOrZero() }
    return sum.divide(BigDecimal(this.size), scale, RoundingMode.HALF_UP)
}

/**
 * 计算List的和
 */
fun List<String>.sum(scale: Int = 2): BigDecimal {
    return this.fold(BigDecimal.ZERO) { acc, s -> acc + s.toBigDecimalOrZero() }
        .setScale(scale, RoundingMode.HALF_UP)
}

/**
 * 计算List的最大值
 */
fun List<String>.max(): BigDecimal? {
    if (this.isEmpty()) return null
    return this.mapNotNull { it.toBigDecimalOrNull() }.maxOrNull()
}

/**
 * 计算List的最小值
 */
fun List<String>.min(): BigDecimal? {
    if (this.isEmpty()) return null
    return this.mapNotNull { it.toBigDecimalOrNull() }.minOrNull()
}

/**
 * Double列表求和
 */
fun List<Double>.sumBigDecimal(scale: Int = 2): BigDecimal {
    return this.fold(BigDecimal.ZERO) { acc, d -> acc + BigDecimal(d.toString()) }
        .setScale(scale, RoundingMode.HALF_UP)
}


/**
 * 比较和格式化扩展函数
 */

/**
 * 比较两个Double值
 */
infix fun Double.eq(other: Double): Boolean {
    return BigDecimal(this.toString()).compareTo(BigDecimal(other.toString())) == 0
}

/**
 * 比较两个String数值
 */
infix fun String.eq(other: String): Boolean {
    return this.toBigDecimalOrZero().compareTo(other.toBigDecimalOrZero()) == 0
}

/**
 * 判断Double是否大于
 */
infix fun Double.gt(other: Double): Boolean {
    return BigDecimal(this.toString()) > BigDecimal(other.toString())
}

/**
 * 判断String数值是否大于
 */
infix fun String.gt(other: String): Boolean {
    return this.toBigDecimalOrZero() > other.toBigDecimalOrZero()
}

/**
 * 判断Double是否小于
 */
infix fun Double.lt(other: Double): Boolean {
    return BigDecimal(this.toString()) < BigDecimal(other.toString())
}

/**
 * 判断String数值是否小于
 */
infix fun String.lt(other: String): Boolean {
    return this.toBigDecimalOrZero() < other.toBigDecimalOrZero()
}

/**
 * 格式化数字为金额格式（带¥符号）
 */
fun BigDecimal.toCurrencyString(scale: Int = 2): String {
    return "¥${this.toFormattedString(scale)}"
}

/**
 * 百分比格式化
 */
fun BigDecimal.toPercentString(scale: Int = 2): String {
    val percent = this.multiply(BigDecimal(100)).setScale(scale, RoundingMode.HALF_UP)
    return "${percent}%"
}

/**
 * 科学计数法显示
 */
fun BigDecimal.toScientificString(): String {
    return DecimalFormat("0.###E0").format(this)
}

// ==================== 财务相关 扩展函数 ====================
/**
 * 计算税率（金额 * 税率）
 */
fun BigDecimal.calculateTax(taxRate: BigDecimal, scale: Int = 2): BigDecimal {
    return this.multiply(taxRate).setScale(scale, RoundingMode.HALF_UP)
}

/**
 * 计算折扣后价格
 */
fun BigDecimal.applyDiscount(discountRate: BigDecimal, scale: Int = 2): BigDecimal {
    return this.multiply(BigDecimal.ONE.subtract(discountRate)).setScale(scale, RoundingMode.HALF_UP)
}

/**
 * 计算年化收益率
 */
fun BigDecimal.calculateAnnualizedReturn(
    principal: BigDecimal,
    days: Int,
    scale: Int = 2
): BigDecimal {
    if (principal.isZero() || days == 0) return BigDecimal.ZERO
    val profit = this.subtract(principal)
    val annualized = profit.divide(principal, scale + 2, RoundingMode.HALF_UP)
        .multiply(BigDecimal(365))
        .divide(BigDecimal(days), scale, RoundingMode.HALF_UP)
    return annualized
}

/**
 * 等额本息计算每月还款额
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

