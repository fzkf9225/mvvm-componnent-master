package io.coderf.arklab.ui.widget.calendar.data

import io.coderf.arklab.core.utils.DateUtil
import io.coderf.arklab.ui.widget.calendar.internal.CalendarDateFormats
import io.coderf.arklab.ui.widget.calendar.model.CalendarDay
import io.coderf.arklab.ui.widget.calendar.model.CalendarMonth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * 进程级只读月份缓存。
 * <p>
 * 多 [io.coderf.arklab.ui.widget.calendar.CalendarView] 共享同一份不可变数据；
 * 各 View 各自持有 [io.coderf.arklab.ui.widget.calendar.model.CalendarSelection]，互不影响。
 * </p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/27 16:04
 */
object CalendarMonthRepository {

    /**
     * 一次加载结果快照。
     *
     * @property months 不可变月份列表
     * @property currentMonthIndex 「今天」所在月在 [months] 中的下标，供 ViewPager 定位
     * @property rangeStart 生成区间起点（yyyy-MM-dd）
     * @property rangeEnd 生成区间终点（yyyy-MM-dd）
     */
    data class Snapshot(
        val months: List<CalendarMonth>,
        val currentMonthIndex: Int,
        val rangeStart: String,
        val rangeEnd: String,
    )

    /** 保证并发 ensureLoaded 只构建一次 */
    private val mutex = Mutex()

    /** 当前进程缓存；range 变化时重建 */
    @Volatile
    private var snapshot: Snapshot? = null

    /** Application preload 使用的独立作用域（不绑定界面生命周期） */
    private val preloadScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * 读取已缓存快照；尚未加载时返回 null。
     */
    @JvmStatic
    fun cachedOrNull(): Snapshot? = snapshot

    /**
     * 「今天」所在月索引；未加载时返回 0。
     */
    @JvmStatic
    fun currentMonthIndex(): Int = snapshot?.currentMonthIndex ?: 0

    /**
     * Application 启动预热（fire-and-forget）。
     *
     * @param startDate 区间起点，默认今天前 10 年
     * @param endDate 区间终点，默认今天后 10 年
     */
    @JvmStatic
    @JvmOverloads
    fun preload(
        startDate: String = defaultStart(),
        endDate: String = defaultEnd(),
    ) {
        preloadScope.launch {
            ensureLoaded(startDate, endDate)
        }
    }

    /**
     * 确保指定区间已加载；命中缓存且区间相同则直接返回。
     *
     * @param startDate 区间起点 yyyy-MM-dd
     * @param endDate 区间终点 yyyy-MM-dd
     */
    suspend fun ensureLoaded(
        startDate: String = defaultStart(),
        endDate: String = defaultEnd(),
    ): Snapshot {
        // 无锁快路径：区间未变则直接复用
        snapshot?.let { cached ->
            if (cached.rangeStart == startDate && cached.rangeEnd == endDate) {
                return cached
            }
        }
        return mutex.withLock {
            // 双重检查，避免并发重复构建
            snapshot?.let { cached ->
                if (cached.rangeStart == startDate && cached.rangeEnd == endDate) {
                    return@withLock cached
                }
            }
            val built = withContext(Dispatchers.Default) {
                buildSnapshot(startDate, endDate)
            }
            snapshot = built
            built
        }
    }

    /**
     * 按起止日期生成全部月份及当前月索引。
     */
    private fun buildSnapshot(startDate: String, endDate: String): Snapshot {
        // getMonthSpace 为相差月数，生成列表需 +1
        val totalMonth = DateUtil.getMonthSpace(startDate, endDate) + 1
        val currentYear = DateUtil.getCurrentYear()
        val currentMonth = DateUtil.getCurrentMonth()
        val months = ArrayList<CalendarMonth>(totalMonth.coerceAtLeast(0))
        var currentIndex = 0
        val startYm = YearMonth.from(
            CalendarDateFormats.parseOrNull(startDate)?.withDayOfMonth(1)
                ?: LocalDate.now().withDayOfMonth(1)
        )
        for (offset in 0 until totalMonth.coerceAtLeast(0)) {
            val ym = startYm.plusMonths(offset.toLong())
            if (ym.year == currentYear && ym.monthValue == currentMonth) {
                currentIndex = offset
            }
            months.add(buildMonth(ym))
        }
        return Snapshot(
            months = months,
            currentMonthIndex = currentIndex,
            rangeStart = startDate,
            rangeEnd = endDate,
        )
    }

    /**
     * 构建单月：周日为一周之首的 leading 占位 + 本月各日。
     */
    private fun buildMonth(ym: YearMonth): CalendarMonth {
        val first = ym.atDay(1)
        // java.time：Mon=1…Sun=7；周日为首时 leading = value % 7（与旧 Calendar.DAY_OF_WEEK-1 一致）
        val leadingBlanks = first.dayOfWeek.value % 7
        val days = ArrayList<CalendarDay>(leadingBlanks + ym.lengthOfMonth())
        repeat(leadingBlanks) {
            days.add(CalendarDay(ym.year, ym.monthValue))
        }
        for (day in 1..ym.lengthOfMonth()) {
            val date = ym.atDay(day)
            val weekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
            days.add(
                CalendarDay(
                    year = ym.year,
                    month = ym.monthValue,
                    dayOfMonth = day,
                    isWeekend = weekend,
                )
            )
        }
        return CalendarMonth(ym.year, ym.monthValue, days)
    }

    /** 默认预热起点：今天往前约 10 年 */
    @JvmStatic
    fun defaultStart(): String = DateUtil.getCalcDateFormat(DateUtil.getToday(), -10 * 365)

    /** 默认预热终点：今天往后约 10 年 */
    @JvmStatic
    fun defaultEnd(): String = DateUtil.getCalcDateFormat(DateUtil.getToday(), 10 * 365)
}
