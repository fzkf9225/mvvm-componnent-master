package io.coderf.arklab.ui.widget.calendar.internal

import android.annotation.SuppressLint
import io.coderf.arklab.core.utils.DateUtil
import io.coderf.arklab.core.utils.NumberUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * 日历日期字符串与 [LocalDate] 互转工具。
 * <p>
 * 对外 API 边界仍为 `yyyy-MM-dd`；内部选中/可选判断优先用 [LocalDate] 避免 ParseException 散落。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/27 16:04
 */
object CalendarDateFormats {

    /** 与 [DateUtil.DEFAULT_FORMAT_DATE] 一致的格式化器 */
    @SuppressLint("ConstantLocale")
    val DATE_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern(DateUtil.DEFAULT_FORMAT_DATE, Locale.getDefault())

    /**
     * 解析日期字符串；空白或格式错误返回 null。
     *
     * @param date yyyy-MM-dd，可为 null
     */
    @JvmStatic
    fun parseOrNull(date: String?): LocalDate? {
        if (date.isNullOrBlank()) return null
        return try {
            LocalDate.parse(date.trim(), DATE_FORMATTER)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    /**
     * [LocalDate] → yyyy-MM-dd。
     */
    @JvmStatic
    fun format(date: LocalDate): String = date.format(DATE_FORMATTER)

    /**
     * 年月日整数 → yyyy-MM-dd（月、日补零）。
     */
    @JvmStatic
    fun format(year: Int, month: Int, day: Int): String {
        return "$year-${NumberUtil.formatMonthOrDay(month)}-${NumberUtil.formatMonthOrDay(day)}"
    }

    /** 今天的 [LocalDate] */
    @JvmStatic
    fun today(): LocalDate = LocalDate.now()

    /** 今天的 yyyy-MM-dd 字符串 */
    @JvmStatic
    fun todayString(): String = format(today())
}
