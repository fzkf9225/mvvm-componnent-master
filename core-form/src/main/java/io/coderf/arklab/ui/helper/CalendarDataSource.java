package io.coderf.arklab.ui.helper;

import io.coderf.arklab.ui.widget.calendar.data.CalendarMonthRepository;

/**
 * 日历预热门面（Java 友好入口）。
 * <p>
 * 实际缓存与生成逻辑位于 {@link CalendarMonthRepository}；
 * 本类不再暴露可写全局列表。
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @created 2024/12/2
 * @updated 2026/11/27
 */
public final class CalendarDataSource {

    private CalendarDataSource() {
    }

    /**
     * Application 启动预热（fire-and-forget），默认 ±10 年区间。
     */
    public static void preload() {
        CalendarMonthRepository.preload();
    }

    /**
     * 「今天」所在月在缓存列表中的下标；尚未加载时返回 0。
     */
    public static int currentMonthIndex() {
        return CalendarMonthRepository.currentMonthIndex();
    }
}
