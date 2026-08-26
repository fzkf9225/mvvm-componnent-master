package io.coderf.arklab.common.utils.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 日期工具类，提供日期格式化、解析、计算及区间范围等常用操作。
 * <p>内部基于 {@link java.time} 实现（minSdk 26+），对外仍保持 {@link Date} API 兼容。
 * <p>约定：
 * <ul>
 *   <li>无特殊说明时，{@code month} 均为 1–12（非 Calendar 的 0–11）。</li>
 *   <li>时区默认使用 {@link ZoneId#systemDefault()}，关键方法提供 {@link ZoneId} 重载。</li>
 *   <li>格式化：{@code date == null} 返回空串；严格解析失败抛异常；宽松解析用 {@code *OrNull}/{@code *OrDefault}。</li>
 * </ul>
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @updated 2026/8/26 15:57
 */
public final class DateUtil {

    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    /**
     * yyyyMMddHHmmss字符串
     */
    public static final String DATE_TIME_FORMAT = "yyyyMMddHHmmss";
    /**
     * yyyyMMddHHmm字符串
     */
    public static final String DATE_TIME_NO_MS_FORMAT = "yyyyMMddHHmm";
    /**
     * yyyy-MM-dd HH:mm:ss字符串
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * yyyy-MM-dd字符串
     */
    public static final String DEFAULT_FORMAT_DATE = "yyyy-MM-dd";
    /**
     * HH:mm:ss字符串
     */
    public static final String DEFAULT_FORMAT_TIME = "HH:mm:ss";
    /**
     * HH:mm字符串
     */
    public static final String DEFAULT_HOUR_FORMAT_TIME = "HH:mm";

    /** java.time 格式化器（线程安全，推荐直接使用） */
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT, Locale.getDefault());
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DEFAULT_FORMAT_DATE, Locale.getDefault());
    public static final DateTimeFormatter DEFAULT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(DEFAULT_FORMAT_TIME, Locale.getDefault());
    public static final DateTimeFormatter DEFAULT_HOUR_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(DEFAULT_HOUR_FORMAT_TIME, Locale.getDefault());

    /**
     * @deprecated 请使用 {@link #DEFAULT_DATE_TIME_FORMATTER}
     */
    @Deprecated
    public static final ThreadLocal<SimpleDateFormat> defaultDateTimeFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.getDefault()));

    /**
     * @deprecated 请使用 {@link #DEFAULT_DATE_FORMATTER}
     */
    @Deprecated
    public static final ThreadLocal<SimpleDateFormat> defaultDateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat(DEFAULT_FORMAT_DATE, Locale.getDefault()));

    /**
     * @deprecated 请使用 {@link #DEFAULT_TIME_FORMATTER}
     */
    @Deprecated
    public static final ThreadLocal<SimpleDateFormat> defaultTimeFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat(DEFAULT_FORMAT_TIME, Locale.getDefault()));

    /**
     * @deprecated 请使用 {@link #DEFAULT_HOUR_TIME_FORMATTER}
     */
    @Deprecated
    public static final ThreadLocal<SimpleDateFormat> defaultHourTimeFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat(DEFAULT_HOUR_FORMAT_TIME, Locale.getDefault()));

    private DateUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ========================= 年月日提取 =========================

    /**
     * 获取日期中的「日」（1-31）。date 为 null 时返回 0。
     */
    public static int getDay(Date date) {
        if (date == null) {
            return 0;
        }
        return toLocalDate(date).getDayOfMonth();
    }

    /**
     * 返回日期的月份，1-12，即 yyyy-MM-dd 中的 MM。date 为 null 时返回 0。
     */
    public static int getMonth(Date date) {
        if (date == null) {
            return 0;
        }
        return toLocalDate(date).getMonthValue();
    }

    /**
     * 返回日期的年，即 yyyy-MM-dd 中的 yyyy。date 为 null 时返回 0。
     */
    public static int getYear(Date date) {
        if (date == null) {
            return 0;
        }
        return toLocalDate(date).getYear();
    }

    /**
     * 获取指定年月的天数。month 为 1-12。
     */
    public static int getDaysOfMonth(int year, int month) {
        return YearMonth.of(year, month).lengthOfMonth();
    }

    // ========================= 月差 / 日差 =========================

    /**
     * 计算两个 yyyy-MM-dd 日期字符串相差的月数（含月末对齐逻辑）。
     * <p>若起始日大于结束日，且结束日不是当月最后一天，则月数减 1；
     * 若结束日是当月最后一天，则视为整月，不减 1。
     */
    public static int calDiffMonth(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate, DEFAULT_DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DEFAULT_DATE_FORMATTER);
            int startYear = start.getYear();
            int startMonth = start.getMonthValue();
            int startDay = start.getDayOfMonth();
            int endYear = end.getYear();
            int endMonth = end.getMonthValue();
            int endDay = end.getDayOfMonth();
            if (startDay > endDay) {
                // 结束日为当月最后一天时，按整月计，不减 1
                if (endDay == end.lengthOfMonth()) {
                    return (endYear - startYear) * 12 + endMonth - startMonth;
                }
                return (endYear - startYear) * 12 + endMonth - startMonth - 1;
            }
            return (endYear - startYear) * 12 + endMonth - startMonth;
        } catch (DateTimeParseException e) {
            return 0;
        }
    }

    /**
     * 获取两个日期之间的间隔天数（忽略时分秒，按自然日计算）。
     * 任一为 null 时返回 0。
     */
    public static int getGapCount(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        LocalDate from = toLocalDate(startDate);
        LocalDate to = toLocalDate(endDate);
        return (int) ChronoUnit.DAYS.between(from, to);
    }

    /**
     * 求两个日期相差天数，格式 yyyy-MM-dd。解析失败返回 0。
     */
    public static long getIntervalDays(String strat, String end) {
        try {
            LocalDate startDate = LocalDate.parse(strat, DEFAULT_DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(end, DEFAULT_DATE_FORMATTER);
            return ChronoUnit.DAYS.between(startDate, endDate);
        } catch (DateTimeParseException e) {
            return 0;
        }
    }

    /**
     * 计算两个日期字符串相差多少月（绝对值）。
     * <p>支持 {@code yyyy-MM} 与 {@code yyyy-MM-dd} 两种格式。解析失败返回 -1。
     */
    public static int getMonthSpace(String stDate, String endDate) {
        try {
            LocalDate bef = toYearMonthStart(stDate);
            LocalDate aft = toYearMonthStart(endDate);
            int result = aft.getMonthValue() - bef.getMonthValue();
            int month = (aft.getYear() - bef.getYear()) * 12;
            return Math.abs(month + result);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 将 yyyy-MM 或 yyyy-MM-dd 解析为当月 1 号。
     */
    private static LocalDate toYearMonthStart(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new DateTimeParseException("empty date", dateStr == null ? "" : dateStr, 0);
        }
        if (dateStr.length() >= 10) {
            return LocalDate.parse(dateStr.substring(0, 10), DEFAULT_DATE_FORMATTER).withDayOfMonth(1);
        }
        return YearMonth.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM", Locale.getDefault())).atDay(1);
    }

    // ========================= 格式化：Date / long → String =========================

    /**
     * 将 long 毫秒时间转成 yyyy-MM-dd HH:mm:ss 字符串。0 返回空串（兼容旧行为）。
     */
    public static String getDateTimeFromMillis(long timeInMillis) {
        if (timeInMillis == 0) {
            return "";
        }
        return getDateTimeFormat(new Date(timeInMillis));
    }

    /**
     * 将 long 毫秒时间转成 yyyy-MM-dd 字符串。
     */
    public static String getDateFromMillis(long timeInMillis) {
        return getDateFormat(new Date(timeInMillis));
    }

    /**
     * 将 date 转成 yyyy-MM-dd HH:mm:ss 字符串。null 返回空串。
     */
    public static String getDateTimeFormat(Date date) {
        return date == null ? "" : DEFAULT_DATE_TIME_FORMATTER.format(toLocalDateTime(date));
    }

    /**
     * 将年月日的 int 转成 yyyy-MM-dd 的字符串。month 为 1-12。
     */
    public static String getDateFormat(int year, int month, int day) {
        return getDateFormat(getDate(year, month, day));
    }

    /**
     * 将 date 转成 yyyy-MM-dd 字符串。null 返回空串。
     */
    public static String getDateFormat(Date date) {
        return date == null ? "" : DEFAULT_DATE_FORMATTER.format(toLocalDate(date));
    }

    /**
     * 获得 HH:mm:ss 的时间。null 返回空串。
     */
    public static String getTimeFormat(Date date) {
        return date == null ? "" : DEFAULT_TIME_FORMATTER.format(toLocalDateTime(date));
    }

    /**
     * 获得 HH:mm 的时间。null 返回空串。
     */
    public static String getTimeHourFormat(Date date) {
        return date == null ? "" : DEFAULT_HOUR_TIME_FORMATTER.format(toLocalDateTime(date));
    }

    /**
     * 格式化日期显示格式。sdate 按 yyyy-MM-dd 解析；失败返回原串。
     */
    public static String dateFormat(String sdate, String format) {
        if (sdate == null || sdate.isEmpty()) {
            return "";
        }
        try {
            LocalDate date = LocalDate.parse(sdate, DEFAULT_DATE_FORMATTER);
            return date.format(DateTimeFormatter.ofPattern(format, Locale.getDefault()));
        } catch (DateTimeParseException e) {
            return sdate;
        }
    }

    /**
     * 格式化日期显示格式。null 返回空串。
     */
    public static String dateFormat(Date date, String format) {
        if (date == null) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            return getDateTimeFormat(date);
        }
        return toLocalDateTime(date).format(DateTimeFormatter.ofPattern(format, Locale.getDefault()));
    }

    /**
     * 将 date 转成字符串；format 为空时使用默认 yyyy-MM-dd HH:mm:ss。
     */
    public static String dateSimpleFormat(Date date, SimpleDateFormat format) {
        if (date == null) {
            return "";
        }
        if (format == null) {
            return DEFAULT_DATE_TIME_FORMATTER.format(toLocalDateTime(date));
        }
        return format.format(date);
    }

    /**
     * 将 Date 按指定格式转为字符串。null 返回空串。
     */
    public static String dateToString(Date data, String formatType) {
        if (data == null) {
            return "";
        }
        if (formatType == null || formatType.isEmpty()) {
            return getDateTimeFormat(data);
        }
        return toLocalDateTime(data).format(DateTimeFormatter.ofPattern(formatType, Locale.getDefault()));
    }

    /**
     * 将 long 毫秒时间戳按指定格式转为字符串。
     */
    public static String longToString(long currentTime, String formatType) throws ParseException {
        Date date = longToDate(currentTime, formatType);
        return dateToString(date, formatType);
    }

    /**
     * 将 long 毫秒时间戳按指定格式转为字符串（系统默认时区）。不抛受检异常。
     */
    public static String format(long millis, String pattern) {
        return format(millis, pattern, SYSTEM_ZONE);
    }

    /**
     * 将 long 毫秒时间戳按指定格式、指定时区转为字符串。
     */
    public static String format(long millis, String pattern, ZoneId zoneId) {
        if (pattern == null || pattern.isEmpty()) {
            pattern = DEFAULT_DATE_TIME_FORMAT;
        }
        ZoneId zone = zoneId != null ? zoneId : SYSTEM_ZONE;
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zone);
        return ldt.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()));
    }

    /**
     * 将 Date 按指定格式、指定时区转为字符串。null 返回空串。
     */
    public static String format(Date date, String pattern, ZoneId zoneId) {
        if (date == null) {
            return "";
        }
        return format(date.getTime(), pattern, zoneId);
    }

    // ========================= 解析：String → Date =========================

    /**
     * 将 yyyy-MM-dd HH:mm:ss 格式的字符串转成 Date。失败抛 DateTimeParseException。
     */
    public static Date getDateByDateTimeFormat(String strDate) {
        return getDateByFormat(strDate, DEFAULT_DATE_TIME_FORMAT);
    }

    /**
     * 将 yyyy-MM-dd 格式的字符串转成 Date。失败抛 DateTimeParseException。
     */
    public static Date getDateByDateFormat(String strDate) {
        return getDateByFormat(strDate, DEFAULT_FORMAT_DATE);
    }

    /**
     * 将指定格式的时间字符串转成 Date 对象。失败抛 DateTimeParseException。
     */
    public static Date getDateByFormat(String strDate, String format) {
        return parseToDate(strDate, format);
    }

    /**
     * 将字符串按指定格式解析为 Date。失败抛 ParseException。
     */
    public static Date stringToDate(String strTime, String formatType) throws ParseException {
        try {
            return parseToDate(strTime, formatType);
        } catch (DateTimeParseException e) {
            throw new ParseException(e.getMessage(), e.getErrorIndex() >= 0 ? e.getErrorIndex() : 0);
        }
    }

    /**
     * 安全解析日期字符串，失败或空串时返回 null。
     */
    public static Date parseOrNull(String strDate, String format) {
        if (strDate == null || strDate.isEmpty() || format == null || format.isEmpty()) {
            return null;
        }
        try {
            return parseToDate(strDate, format);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 安全解析日期字符串，失败或空串时返回 defaultValue。
     */
    public static Date parseOrDefault(String strDate, String format, Date defaultValue) {
        Date parsed = parseOrNull(strDate, format);
        return parsed != null ? parsed : defaultValue;
    }

    // ========================= long ↔ Date ↔ String =========================

    /**
     * 将 long 毫秒时间戳转为 Date。
     * <p>若 formatType 仅含日期或到分钟等，会按该精度截断后再转 Date（与历史「去毫秒差异」意图一致）。
     */
    public static Date longToDate(long currentTime, String formatType) throws ParseException {
        try {
            ZoneId zone = SYSTEM_ZONE;
            LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTime), zone);
            if (formatType != null && !formatType.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatType, Locale.getDefault());
                String formatted = formatter.format(ldt);
                return parseToDate(formatted, formatType);
            }
            return Date.from(Instant.ofEpochMilli(currentTime));
        } catch (DateTimeParseException e) {
            throw new ParseException(e.getMessage(), e.getErrorIndex() >= 0 ? e.getErrorIndex() : 0);
        }
    }

    /**
     * 将毫秒时间戳直接转为 Date（不截断）。
     */
    public static Date fromMillis(long millis) {
        return new Date(millis);
    }

    /**
     * 将秒级时间戳转为 Date。
     */
    public static Date fromSeconds(long seconds) {
        return new Date(seconds * 1000L);
    }

    /**
     * 将字符串按指定格式解析为 long 毫秒时间戳。失败抛 ParseException；解析结果为 null 时返回 0。
     */
    public static long stringToLong(String strTime, String formatType) throws ParseException {
        Date date = stringToDate(strTime, formatType);
        return date == null ? 0 : dateToLong(date);
    }

    /**
     * 将 Date 转为 long 毫秒时间戳。null 返回 0。
     */
    public static long dateToLong(Date date) {
        return date == null ? 0L : date.getTime();
    }

    /**
     * 将 Date 转为秒级时间戳。null 返回 0。
     */
    public static long toSeconds(Date date) {
        return date == null ? 0L : date.getTime() / 1000L;
    }

    /**
     * 将毫秒时间戳转为秒。
     */
    public static long toSeconds(long millis) {
        return millis / 1000L;
    }

    // ========================= 构造 Date =========================

    /**
     * 将年月日的 int 转成 date；month 为 1-12。
     */
    public static Date getDate(int year, int month, int day) {
        return toDate(LocalDate.of(year, month, day));
    }

    /**
     * 根据指定年月日时分秒返回 Date；month 为 1-12（与三参数重载一致）。
     * <p>注意：旧实现曾使用 Calendar 风格 0-11，现已统一为 1-12。
     */
    public static Date getDate(int year, int month, int date, int hourOfDay, int minute, int second) {
        return toDate(LocalDateTime.of(year, month, date, hourOfDay, minute, second));
    }

    /**
     * 获得年月日数据，sDate 为 yyyy-MM-dd 格式。
     */
    public static int[] getYearMonthAndDayFrom(String sDate) {
        return getYearMonthAndDayFromDate(getDateByDateFormat(sDate));
    }

    /**
     * 获得年月日数据；arr[1] 为 Calendar 风格月份（0-11），以兼容旧调用方。
     */
    public static int[] getYearMonthAndDayFromDate(Date date) {
        if (date == null) {
            return new int[]{0, 0, 0};
        }
        LocalDate localDate = toLocalDate(date);
        return new int[]{localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth()};
    }

    // ========================= 当前时间 / 相对日期 =========================

    /**
     * 获得当前年份。
     */
    public static int getCurrentYear() {
        return LocalDate.now().getYear();
    }

    /**
     * 获得当前月份（1-12）。
     */
    public static int getCurrentMonth() {
        return LocalDate.now().getMonthValue();
    }

    /**
     * 获得当月几号。
     */
    public static int getDayOfMonth() {
        return LocalDate.now().getDayOfMonth();
    }

    /**
     * 获得今天的日期（yyyy-MM-dd）。
     */
    public static String getToday() {
        return LocalDate.now().format(DEFAULT_DATE_FORMATTER);
    }

    /**
     * 获得昨天的日期（yyyy-MM-dd）。
     */
    public static String getYesterday() {
        return LocalDate.now().minusDays(1).format(DEFAULT_DATE_FORMATTER);
    }

    /**
     * 获得前天的日期（yyyy-MM-dd）。
     */
    public static String getBeforeYesterday() {
        return LocalDate.now().minusDays(2).format(DEFAULT_DATE_FORMATTER);
    }

    /**
     * 获得几天之前或几天之后的日期。
     */
    public static String getOtherDay(int diff) {
        return LocalDate.now().plusDays(diff).format(DEFAULT_DATE_FORMATTER);
    }

    /**
     * 给定 yyyy-MM-dd 字符串，加上一定天数后返回 yyyy-MM-dd。
     */
    public static String getCalcDateFormat(String sDate, int amount) {
        Date date = getCalcDate(getDateByDateFormat(sDate), amount);
        return getDateFormat(date);
    }

    /**
     * 给定日期加上一定天数。date 为 null 时以今天为基准。
     */
    public static Date getCalcDate(Date date, int amount) {
        LocalDate base = date == null ? LocalDate.now() : toLocalDate(date);
        return toDate(base.plusDays(amount));
    }

    /**
     * 计算时分秒偏移后的日期对象。date 为 null 时以当前时刻为基准。
     */
    public static Date getCalcTime(Date date, int hOffset, int mOffset, int sOffset) {
        LocalDateTime base = date == null ? LocalDateTime.now() : toLocalDateTime(date);
        return toDate(base.plusHours(hOffset).plusMinutes(mOffset).plusSeconds(sOffset));
    }

    /**
     * 获取当前毫秒时间戳字符串（完整毫秒值，通常 13 位）。
     * <p>用于签名等场景，请勿改为秒级，以免破坏既有协议。
     * 若需要秒级字符串，请使用 {@link #getTimestampSeconds()}。
     */
    public static String getTimestamp() {
        long timeStampMs = System.currentTimeMillis();
        return String.valueOf(timeStampMs);
    }

    /**
     * 获取当前秒级时间戳字符串（通常 10 位）。
     */
    public static String getTimestampSeconds() {
        long sec = System.currentTimeMillis() / 1000L;
        return String.valueOf(sec);
    }

    /**
     * 获取当前毫秒时间戳。
     */
    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前秒级时间戳。
     */
    public static long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * 获取当前时间戳的格式化字符串（yyyy-MM-dd HH:mm:ss）。
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DEFAULT_DATE_TIME_FORMATTER);
    }

    /**
     * 获取年月日时分秒字符串，用于文件名（yyyyMMddHHmmssSSS）。
     */
    public static String getCurrentTime() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS", Locale.getDefault())
                .format(LocalDateTime.now());
    }

    // ========================= 范围：周 / 月 / 年 / 季 =========================

    /**
     * 获取本周的起始和结束日期（周一至周日，yyyy-MM-dd）。
     */
    public static String[] getWeekScope() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return new String[]{
                monday.format(DEFAULT_DATE_FORMATTER),
                sunday.format(DEFAULT_DATE_FORMATTER)
        };
    }

    /**
     * 获取指定日期所在周的起始和结束日期（周一至周日）。
     */
    public static String[] getWeekScope(Date date) {
        LocalDate base = date == null ? LocalDate.now() : toLocalDate(date);
        LocalDate monday = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = base.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return new String[]{
                monday.format(DEFAULT_DATE_FORMATTER),
                sunday.format(DEFAULT_DATE_FORMATTER)
        };
    }

    /**
     * 获取这个月起始时间和结束时间。
     */
    public static String[] getMonthScope() {
        LocalDate now = LocalDate.now();
        return new String[]{
                now.withDayOfMonth(1).format(DEFAULT_DATE_FORMATTER),
                now.withDayOfMonth(now.lengthOfMonth()).format(DEFAULT_DATE_FORMATTER)
        };
    }

    /**
     * 获取上个月起始时间和结束时间。
     */
    public static String[] getLastMonthScope() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        return new String[]{
                lastMonth.withDayOfMonth(1).format(DEFAULT_DATE_FORMATTER),
                lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).format(DEFAULT_DATE_FORMATTER)
        };
    }

    /**
     * 获取今年的起始时间和结束时间。
     */
    public static String[] getYearScope() {
        int year = getCurrentYear();
        return new String[]{year + "-01-01", year + "-12-31"};
    }

    /**
     * 获取当前季度的起始和结束日期（yyyy-MM-dd）。
     */
    public static String[] getQuarterScope() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int startMonth = ((month - 1) / 3) * 3 + 1;
        LocalDate start = LocalDate.of(now.getYear(), startMonth, 1);
        LocalDate end = start.plusMonths(3).minusDays(1);
        return new String[]{
                start.format(DEFAULT_DATE_FORMATTER),
                end.format(DEFAULT_DATE_FORMATTER)
        };
    }

    // ========================= 起止时刻 =========================

    /**
     * 当天 00:00:00.000（系统默认时区）。
     */
    public static Date startOfDay(Date date) {
        if (date == null) {
            return null;
        }
        return toDate(toLocalDate(date));
    }

    /**
     * 当天 23:59:59.999（系统默认时区）。
     */
    public static Date endOfDay(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime end = toLocalDate(date).atTime(23, 59, 59, 999_000_000);
        return toDate(end);
    }

    /**
     * 当月第一天 00:00:00。
     */
    public static Date startOfMonth(Date date) {
        if (date == null) {
            return null;
        }
        return toDate(toLocalDate(date).withDayOfMonth(1));
    }

    /**
     * 当月最后一天 23:59:59.999。
     */
    public static Date endOfMonth(Date date) {
        if (date == null) {
            return null;
        }
        LocalDate ld = toLocalDate(date);
        LocalDateTime end = ld.withDayOfMonth(ld.lengthOfMonth()).atTime(23, 59, 59, 999_000_000);
        return toDate(end);
    }

    /**
     * 按精度截断（如 DAYS / HOURS / MINUTES / SECONDS）。null 返回 null。
     */
    public static Date truncateTo(Date date, ChronoUnit unit) {
        if (date == null || unit == null) {
            return date;
        }
        LocalDateTime ldt = toLocalDateTime(date).truncatedTo(unit);
        return toDate(ldt);
    }

    // ========================= 同一天 / 相对判断 =========================

    /**
     * 判断两个日期是否为同一天（忽略时分秒）。
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return toLocalDate(date1).equals(toLocalDate(date2));
    }

    /**
     * 判断指定日期是否为今天。
     */
    public static boolean isToday(Date date) {
        return isSameDay(date, new Date());
    }

    /**
     * 判断指定日期是否为昨天。
     */
    public static boolean isYesterday(Date date) {
        if (date == null) {
            return false;
        }
        return toLocalDate(date).equals(LocalDate.now().minusDays(1));
    }

    /**
     * 判断指定日期是否为明天。
     */
    public static boolean isTomorrow(Date date) {
        if (date == null) {
            return false;
        }
        return toLocalDate(date).equals(LocalDate.now().plusDays(1));
    }

    /**
     * 判断两个日期是否为同一月（忽略日与时分秒）。
     */
    public static boolean isSameMonth(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        LocalDate a = toLocalDate(date1);
        LocalDate b = toLocalDate(date2);
        return a.getYear() == b.getYear() && a.getMonthValue() == b.getMonthValue();
    }

    /**
     * 判断两个日期是否为同一年。
     */
    public static boolean isSameYear(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return toLocalDate(date1).getYear() == toLocalDate(date2).getYear();
    }

    /**
     * date1 是否严格在 date2 之前（按时间戳比较）。任一 null 返回 false。
     */
    public static boolean isBefore(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.getTime() < date2.getTime();
    }

    /**
     * date1 是否严格在 date2 之后。任一 null 返回 false。
     */
    public static boolean isAfter(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.getTime() > date2.getTime();
    }

    /**
     * 判断 target 是否在 [start, end] 闭区间内（按时间戳）。任一 null 返回 false。
     */
    public static boolean isBetween(Date target, Date start, Date end) {
        if (target == null || start == null || end == null) {
            return false;
        }
        long t = target.getTime();
        return t >= start.getTime() && t <= end.getTime();
    }

    // ========================= 友好时间 / 年龄 =========================

    /**
     * 相对当前时间的友好描述，例如：刚刚、3分钟前、昨天 14:30、2024-01-01。
     */
    public static String getFriendlyTime(Date date) {
        if (date == null) {
            return "";
        }
        long now = System.currentTimeMillis();
        long diff = now - date.getTime();
        if (diff < 0) {
            // 未来时间，直接返回完整日期时间
            return getDateTimeFormat(date);
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        if (seconds < 60) {
            return "刚刚";
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes < 60) {
            return minutes + "分钟前";
        }
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours < 24 && isToday(date)) {
            return hours + "小时前";
        }
        if (isYesterday(date)) {
            return "昨天 " + getTimeHourFormat(date);
        }
        if (isSameYear(date, new Date())) {
            return dateFormat(date, "MM-dd HH:mm");
        }
        return getDateFormat(date);
    }

    /**
     * 按周岁计算年龄。birthday 为 null 返回 0。
     */
    public static int getAge(Date birthday) {
        if (birthday == null) {
            return 0;
        }
        return getAge(toLocalDate(birthday));
    }

    /**
     * 按周岁计算年龄。
     */
    public static int getAge(LocalDate birthday) {
        if (birthday == null) {
            return 0;
        }
        return (int) ChronoUnit.YEARS.between(birthday, LocalDate.now());
    }

    // ========================= ISO 处理 =========================

    /**
     * 将 ISO 8601 格式转为 yyyy-MM-dd。
     * 支持 LocalDateTime、带偏移、带 Z、纯日期。失败返回原串；空返回空串。
     */
    public static String dealDateFormat(String oldDate) {
        if (oldDate == null || oldDate.isEmpty()) {
            return "";
        }
        Date parsed = parseIsoOrNull(oldDate);
        if (parsed == null) {
            return oldDate;
        }
        return getDateFormat(parsed);
    }

    /**
     * 将 ISO 8601 格式转为 yyyy-MM-dd HH:mm:ss。
     * 支持 LocalDateTime、带偏移、带 Z、纯日期。失败返回原串；空返回空串。
     */
    public static String dealDateTimeFormat(String oldDate) {
        if (oldDate == null || oldDate.isEmpty()) {
            return "";
        }
        Date parsed = parseIsoOrNull(oldDate);
        if (parsed == null) {
            return oldDate;
        }
        return getDateTimeFormat(parsed);
    }

    /**
     * 解析常见 ISO-8601 字符串为 Date（系统默认时区解释无偏移时间）。
     * 支持：{@code 2024-01-01T12:00:00}、{@code 2024-01-01T12:00:00Z}、
     * {@code 2024-01-01T12:00:00+08:00}、{@code 2024-01-01}。失败返回 null。
     */
    public static Date parseIsoOrNull(String iso) {
        if (iso == null || iso.isEmpty()) {
            return null;
        }
        String text = iso.trim();
        try {
            // 带偏移或 Z
            if (text.endsWith("Z") || text.contains("+") || text.lastIndexOf('-') > 10) {
                try {
                    OffsetDateTime odt = OffsetDateTime.parse(text);
                    return Date.from(odt.toInstant());
                } catch (DateTimeParseException ignored) {
                    Instant instant = Instant.parse(text);
                    return Date.from(instant);
                }
            }
            // 纯日期
            if (text.length() <= 10) {
                LocalDate ld = LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
                return toDate(ld);
            }
            // 本地日期时间
            LocalDateTime ldt = LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return toDate(ldt);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 将 Date 转为 ISO_LOCAL_DATE_TIME 字符串（无偏移）。null 返回空串。
     */
    public static String toIsoLocalDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return toLocalDateTime(date).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * 将 Date 转为带系统默认时区偏移的 ISO 字符串。null 返回空串。
     */
    public static String toIso(Date date) {
        if (date == null) {
            return "";
        }
        return ZonedDateTime.ofInstant(date.toInstant(), SYSTEM_ZONE)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    // ========================= 时长格式化 =========================

    /**
     * 将毫秒数格式化为 "XX时XX分XX秒" 格式。
     */
    public static String formatDuration(long milliseconds) {
        if (milliseconds <= 0) {
            return "0秒";
        }
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("时");
        }
        if (minutes > 0 || hours > 0) {
            sb.append(minutes).append("分");
        }
        sb.append(seconds).append("秒");
        return sb.toString();
    }

    /**
     * 将毫秒数格式化为 "XX时XX分" 格式（省略秒）。
     */
    public static String formatDurationWithoutSeconds(long milliseconds) {
        if (milliseconds <= 0) {
            return "0分";
        }
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("时");
        }
        sb.append(minutes).append("分");
        return sb.toString();
    }

    /**
     * 将毫秒数格式化为 "HH:mm:ss" 格式。
     */
    public static String formatDurationAsTime(long milliseconds) {
        if (milliseconds <= 0) {
            return "00:00:00";
        }
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * 将毫秒数格式化为智能格式（自动选择显示精度）。
     */
    public static String formatDurationSmart(long milliseconds) {
        if (milliseconds <= 0) {
            return "0秒";
        }
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d时%d分", hours, minutes);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(), "%d分%d秒", minutes, seconds);
        }
        return String.format(Locale.getDefault(), "%d秒", seconds);
    }

    /**
     * 将毫秒数格式化为带「天」的时长，例如 "1天2时3分"。
     */
    public static String formatDurationWithDays(long milliseconds) {
        if (milliseconds <= 0) {
            return "0秒";
        }
        long totalSeconds = milliseconds / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("时");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("分");
        }
        if (days == 0 && hours == 0) {
            sb.append(seconds).append("秒");
        }
        return sb.toString();
    }

    // ========================= java.time 转换（含 null 安全） =========================

    /**
     * 将 Date 转为 {@link LocalDateTime}（系统默认时区）。null 返回 null。
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(SYSTEM_ZONE).toLocalDateTime();
    }

    /**
     * 将 Date 转为 {@link LocalDateTime}（指定时区）。null 返回 null。
     */
    public static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
        if (date == null) {
            return null;
        }
        ZoneId zone = zoneId != null ? zoneId : SYSTEM_ZONE;
        return date.toInstant().atZone(zone).toLocalDateTime();
    }

    /**
     * 将 Date 转为 {@link LocalDate}（系统默认时区）。null 返回 null。
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(SYSTEM_ZONE).toLocalDate();
    }

    /**
     * 将 Date 转为 {@link LocalDate}（指定时区）。null 返回 null。
     */
    public static LocalDate toLocalDate(Date date, ZoneId zoneId) {
        if (date == null) {
            return null;
        }
        ZoneId zone = zoneId != null ? zoneId : SYSTEM_ZONE;
        return date.toInstant().atZone(zone).toLocalDate();
    }

    /**
     * 将 {@link LocalDateTime} 转为 Date（系统默认时区）。null 返回 null。
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(SYSTEM_ZONE).toInstant());
    }

    /**
     * 将 {@link LocalDateTime} 转为 Date（指定时区）。null 返回 null。
     */
    public static Date toDate(LocalDateTime localDateTime, ZoneId zoneId) {
        if (localDateTime == null) {
            return null;
        }
        ZoneId zone = zoneId != null ? zoneId : SYSTEM_ZONE;
        return Date.from(localDateTime.atZone(zone).toInstant());
    }

    /**
     * 将 {@link LocalDate} 转为 Date（当天 00:00:00，系统默认时区）。null 返回 null。
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(SYSTEM_ZONE).toInstant());
    }

    /**
     * 将 {@link LocalDate} 转为 Date（当天 00:00:00，指定时区）。null 返回 null。
     */
    public static Date toDate(LocalDate localDate, ZoneId zoneId) {
        if (localDate == null) {
            return null;
        }
        ZoneId zone = zoneId != null ? zoneId : SYSTEM_ZONE;
        return Date.from(localDate.atStartOfDay(zone).toInstant());
    }

    // ========================= 内部解析 =========================

    /**
     * 按 pattern 解析字符串为 Date。
     * 优先按 LocalDateTime 解析；失败则按 LocalDate（当天 00:00:00）。
     */
    private static Date parseToDate(String strDate, String format) {
        if (strDate == null || strDate.isEmpty()) {
            throw new DateTimeParseException("empty date string", strDate == null ? "" : strDate, 0);
        }
        if (format == null || format.isEmpty()) {
            throw new DateTimeParseException("empty format", strDate, 0);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.getDefault());
        boolean hasTime = format.indexOf('H') >= 0
                || format.indexOf('h') >= 0
                || format.indexOf('m') >= 0
                || format.indexOf('s') >= 0
                || format.indexOf('S') >= 0
                || format.indexOf('a') >= 0
                || format.indexOf('K') >= 0
                || format.indexOf('k') >= 0;
        if (hasTime) {
            try {
                return toDate(LocalDateTime.parse(strDate, formatter));
            } catch (DateTimeParseException ex) {
                // 兼容：pattern 含时间但字符串只有日期时，尝试纯日期
                return toDate(LocalDate.parse(strDate, DateTimeFormatter.ofPattern(
                        stripTimePattern(format), Locale.getDefault())));
            }
        }
        try {
            return toDate(LocalDate.parse(strDate, formatter));
        } catch (DateTimeParseException ex) {
            return toDate(LocalDateTime.parse(strDate, formatter));
        }
    }

    /**
     * 粗略去掉 pattern 中的时间部分，用于回退解析纯日期。
     */
    private static String stripTimePattern(String format) {
        String cleaned = format
                .replaceAll("[HhKksSaS]+", "")
                .replaceAll("[:：.\\s]+$", "")
                .replaceAll("[:：]\\s*$", "")
                .trim();
        if (cleaned.isEmpty()) {
            return DEFAULT_FORMAT_DATE;
        }
        return cleaned;
    }
}
