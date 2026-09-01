package io.coderf.arklab.core.utils.ext

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 日期 / 时间相关扩展。
 *
 * 覆盖时间戳、[Date]、日期字符串的格式化、加减与相对时间展示。
 * 从 core-base Extensions 迁移；无 Android R 依赖。
 */

// ==================== Long（时间戳） ====================

/**
 * 时间戳转格式化字符串。
 *
 * @param pattern 格式，默认 `"yyyy-MM-dd HH:mm"`
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
fun Long.toDateTimeString(pattern: String = "yyyy-MM-dd HH:mm"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
}

/**
 * 时间戳转日期字符串。
 *
 * @param pattern 格式，默认 `"yyyy-MM-dd HH:mm:ss"`
 */
fun Long.toDateString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
}

/**
 * 时间戳转 [Date]。
 */
fun Long.toDate(): Date = Date(this)

/**
 * 判断时间戳是否属于「今天」（按本地日历日，忽略时分秒）。
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
 * 时间戳转相对时间文案（刚刚、几分钟前、几小时前、几天前，否则回落为日期）。
 *
 * 文案为中文；若需多语言可再包一层资源。
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

// ==================== Date ====================

/**
 * 获取日期的年份。
 */
val Date.year: Int
    get() = Calendar.getInstance().apply { time = this@year }.get(Calendar.YEAR)

/**
 * 获取日期的月份（1–12）。
 */
val Date.month: Int
    get() = Calendar.getInstance().apply { time = this@month }.get(Calendar.MONTH) + 1

/**
 * 获取日期的「日」（1–31）。
 */
val Date.day: Int
    get() = Calendar.getInstance().apply { time = this@day }.get(Calendar.DATE)

/**
 * 将 [Date] 格式化为字符串。
 *
 * @param pattern 格式，默认 `"yyyy-MM-dd HH:mm:ss"`
 */
fun Date.format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}

/**
 * 日期加减天数，返回新 [Date]（不修改原对象）。
 */
fun Date.addDays(days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DAY_OF_MONTH, days)
    return calendar.time
}

/**
 * 日期加减月份，返回新 [Date]（不修改原对象）。
 */
fun Date.addMonths(months: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.MONTH, months)
    return calendar.time
}

// ==================== String（日期字符串） ====================

/**
 * 字符串按指定格式解析为 [Date]；失败返回 null。
 *
 * @param pattern 解析格式，默认 `"yyyy-MM-dd HH:mm:ss"`
 */
fun String.toDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): Date? {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)
    } catch (_: Exception) {
        null
    }
}

/**
 * 日期字符串格式转换；解析失败则原样返回。
 *
 * @param fromPattern 源格式
 * @param toPattern   目标格式
 */
fun String.convertDateFormat(
    fromPattern: String = "yyyy-MM-dd HH:mm:ss",
    toPattern: String = "yyyy-MM-dd"
): String {
    return try {
        val date = SimpleDateFormat(fromPattern, Locale.getDefault()).parse(this)
        if (date != null) {
            SimpleDateFormat(toPattern, Locale.getDefault()).format(date)
        } else {
            this
        }
    } catch (_: Exception) {
        this
    }
}

/**
 * 判断字符串表示的日期是否为今天。
 *
 * @param datePattern 解析格式，默认 `"yyyy-MM-dd"`
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
    } catch (_: Exception) {
        false
    }
}
