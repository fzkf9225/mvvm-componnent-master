package io.coderf.arklab.common.utils.common;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期工具类，提供日期格式化、解析、计算及区间范围等常用操作。
 * <p>内部基于 {@link java.time} 实现（minSdk 26+），对外仍保持 {@link Date} API 兼容。
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
    public static final ThreadLocal<SimpleDateFormat> defaultDateTimeFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.getDefault());
        }
    };

    /**
     * @deprecated 请使用 {@link #DEFAULT_DATE_FORMATTER}
     */
    @Deprecated
    public static final ThreadLocal<SimpleDateFormat> defaultDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DEFAULT_FORMAT_DATE, Locale.getDefault());
        }
    };

    /**
     * @deprecated 请使用 {@link #DEFAULT_TIME_FORMATTER}
     */
    @Deprecated
    public static final ThreadLocal<SimpleDateFormat> defaultTimeFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DEFAULT_FORMAT_TIME, Locale.getDefault());
        }
    };

    /**
     * @deprecated 请使用 {@link #DEFAULT_HOUR_TIME_FORMATTER}
     */
    @Deprecated
    public static final ThreadLocal<SimpleDateFormat> defaultHourTimeFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DEFAULT_HOUR_FORMAT_TIME, Locale.getDefault());
        }
    };

    private DateUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 获取日期中的「日」（1-31）。
     */
    public static int getDay(Date date) {
        return toLocalDate(date).getDayOfMonth();
    }

    /**
     * 返回日期的月份，1-12，即 yyyy-MM-dd 中的 MM。
     */
    public static int getMonth(Date date) {
        return toLocalDate(date).getMonthValue();
    }

    /**
     * 返回日期的年，即 yyyy-MM-dd 中的 yyyy。
     */
    public static int getYear(Date date) {
        return toLocalDate(date).getYear();
    }

    /**
     * 获取指定年月的天数。
     */
    public static int getDaysOfMonth(int year, int month) {
        return YearMonth.of(year, month).lengthOfMonth();
    }

    /**
     * 计算两个 yyyy-MM-dd 日期字符串相差的月数（含特殊月末处理逻辑）。
     */
    public static int calDiffMonth(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            int startYear = start.getYear();
            int startMonth = start.getMonthValue();
            int startDay = start.getDayOfMonth();
            int endYear = end.getYear();
            int endMonth = end.getMonthValue();
            int endDay = end.getDayOfMonth();
            if (startDay > endDay) {
                if (endDay == getDaysOfMonth(LocalDate.now().getYear(), 2)) {
                    return (endYear - startYear) * 12 + endMonth - startMonth;
                }
                return (endYear - startYear) * 12 + endMonth - startMonth - 1;
            }
            return (endYear - startYear) * 12 + endMonth - startMonth;
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取两个日期之间的间隔天数（忽略时分秒，按自然日计算）。
     */
    public static int getGapCount(Date startDate, Date endDate) {
        LocalDate from = toLocalDate(startDate);
        LocalDate to = toLocalDate(endDate);
        return (int) ChronoUnit.DAYS.between(from, to);
    }

    /**
     * 将 long 时间转成 yyyy-MM-dd HH:mm:ss 字符串。
     */
    public static String getDateTimeFromMillis(long timeInMillis) {
        if (timeInMillis == 0) {
            return "";
        }
        return getDateTimeFormat(new Date(timeInMillis));
    }

    /**
     * 将 long 时间转成 yyyy-MM-dd 字符串。
     */
    public static String getDateFromMillis(long timeInMillis) {
        return getDateFormat(new Date(timeInMillis));
    }

    /**
     * 将 date 转成 yyyy-MM-dd HH:mm:ss 字符串。
     */
    public static String getDateTimeFormat(Date date) {
        return date == null ? "" : DEFAULT_DATE_TIME_FORMATTER.format(toLocalDateTime(date));
    }

    /**
     * 将年月日的 int 转成 yyyy-MM-dd 的字符串。
     */
    public static String getDateFormat(int year, int month, int day) {
        return getDateFormat(getDate(year, month, day));
    }

    /**
     * 将 date 转成 yyyy-MM-dd 字符串。
     */
    public static String getDateFormat(Date date) {
        return date == null ? "" : DEFAULT_DATE_FORMATTER.format(toLocalDate(date));
    }

    /**
     * 获得 HH:mm:ss 的时间。
     */
    public static String getTimeFormat(Date date) {
        return date == null ? "" : DEFAULT_TIME_FORMATTER.format(toLocalDateTime(date));
    }

    /**
     * 获得 HH:mm 的时间。
     */
    public static String getTimeHourFormat(Date date) {
        return date == null ? "" : DEFAULT_HOUR_TIME_FORMATTER.format(toLocalDateTime(date));
    }

    /**
     * 格式化日期显示格式。
     */
    public static String dateFormat(String sdate, String format) {
        LocalDate date = LocalDate.parse(sdate);
        return date.format(DateTimeFormatter.ofPattern(format, Locale.getDefault()));
    }

    /**
     * 格式化日期显示格式。
     */
    public static String dateFormat(Date date, String format) {
        if (date == null) {
            return "";
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
     * 将 yyyy-MM-dd HH:mm:ss 格式的字符串转成 Date。
     */
    public static Date getDateByDateTimeFormat(String strDate) {
        return getDateByFormat(strDate, DEFAULT_DATE_TIME_FORMAT);
    }

    /**
     * 将 yyyy-MM-dd 格式的字符串转成 Date。
     */
    public static Date getDateByDateFormat(String strDate) {
        return getDateByFormat(strDate, DEFAULT_FORMAT_DATE);
    }

    /**
     * 将指定格式的时间字符串转成 Date 对象。
     */
    public static Date getDateByFormat(String strDate, String format) {
        return parseToDate(strDate, format);
    }

    /**
     * 将年月日的 int 转成 date；month 为 1-12。
     */
    public static Date getDate(int year, int month, int day) {
        return toDate(LocalDate.of(year, month, day));
    }

    /**
     * 求两个日期相差天数，格式 yyyy-MM-dd。
     */
    public static long getIntervalDays(String strat, String end) {
        LocalDate startDate = LocalDate.parse(strat);
        LocalDate endDate = LocalDate.parse(end);
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

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
     * 给定日期加上一定天数。
     */
    public static Date getCalcDate(Date date, int amount) {
        return toDate(toLocalDate(date).plusDays(amount));
    }

    /**
     * 计算时分秒偏移后的日期对象。
     */
    public static Date getCalcTime(Date date, int hOffset, int mOffset, int sOffset) {
        LocalDateTime base = date == null ? LocalDateTime.now() : toLocalDateTime(date);
        return toDate(base.plusHours(hOffset).plusMinutes(mOffset).plusSeconds(sOffset));
    }

    /**
     * 根据指定年月日时分秒返回 Date；month 参数为 0-11（Calendar 风格）。
     */
    public static Date getDate(int year, int month, int date, int hourOfDay, int minute, int second) {
        return toDate(LocalDateTime.of(year, month + 1, date, hourOfDay, minute, second));
    }

    /**
     * 获得年月日数据，sDate 为 yyyy-MM-dd 格式。
     */
    public static int[] getYearMonthAndDayFrom(String sDate) {
        return getYearMonthAndDayFromDate(getDateByDateFormat(sDate));
    }

    /**
     * 获得年月日数据；arr[1] 为 Calendar 风格月份（0-11）。
     */
    public static int[] getYearMonthAndDayFromDate(Date date) {
        LocalDate localDate = toLocalDate(date);
        return new int[]{localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth()};
    }

    /**
     * 获取当前毫秒时间戳字符串（10 位，秒级精度）。
     */
    public static String getTimestamp() {
        long timeStampSec = System.currentTimeMillis();
        @SuppressLint("DefaultLocale")
        String timestamp = String.format("%010d", timeStampSec);
        return timestamp;
    }

    /**
     * 将 Date 按指定格式转为字符串。
     */
    public static String dateToString(Date data, String formatType) {
        return toLocalDateTime(data).format(DateTimeFormatter.ofPattern(formatType, Locale.getDefault()));
    }

    /**
     * 将 long 时间戳按指定格式转为字符串。
     */
    public static String longToString(long currentTime, String formatType) throws ParseException {
        Date date = longToDate(currentTime, formatType);
        return dateToString(date, formatType);
    }

    /**
     * 将字符串按指定格式解析为 Date。
     */
    public static Date stringToDate(String strTime, String formatType) throws ParseException {
        try {
            return parseToDate(strTime, formatType);
        } catch (DateTimeParseException e) {
            throw new ParseException(e.getMessage(), e.getErrorIndex());
        }
    }

    /**
     * 将 long 时间戳转为 Date（经格式化后再解析，去除毫秒差异）。
     */
    public static Date longToDate(long currentTime, String formatType) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatType, Locale.getDefault());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTime), SYSTEM_ZONE);
        String formatted = formatter.format(localDateTime);
        return parseToDate(formatted, formatType);
    }

    /**
     * 将字符串按指定格式解析为 long 时间戳。
     */
    public static long stringToLong(String strTime, String formatType) throws ParseException {
        Date date = stringToDate(strTime, formatType);
        return date == null ? 0 : dateToLong(date);
    }

    /**
     * 将 Date 转为 long 毫秒时间戳。
     */
    public static long dateToLong(Date date) {
        return date.getTime();
    }

    /**
     * 计算两个 yyyy-MM 日期字符串相差多少月。
     */
    public static int getMonthSpace(String stDate, String endDate) {
        try {
            LocalDate bef = YearMonth.parse(stDate, DateTimeFormatter.ofPattern("yyyy-MM", Locale.getDefault())).atDay(1);
            LocalDate aft = YearMonth.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM", Locale.getDefault())).atDay(1);
            int result = aft.getMonthValue() - bef.getMonthValue();
            int month = (aft.getYear() - bef.getYear()) * 12;
            return Math.abs(month + result);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获取这周的起始时间和结束时间。
     */
    public static String[] getWeekScope() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String stDate = getCalcDateFormat(getToday(), -dayOfWeek + 1);
        String endDate = getCalcDateFormat(getToday(), (7 - dayOfWeek));
        if (dayOfWeek == 0) {
            stDate = getCalcDateFormat(stDate, -7);
            endDate = getCalcDateFormat(endDate, -7);
        }
        return new String[]{stDate, endDate};
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
     * 将 ISO 8601 格式转为 yyyy-MM-dd。
     */
    public static String dealDateFormat(String oldDate) {
        if (oldDate == null || oldDate.isEmpty()) {
            return "";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(oldDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return dateTime.format(DEFAULT_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return oldDate;
        }
    }

    /**
     * 将 ISO 8601 格式转为 yyyy-MM-dd HH:mm:ss。
     */
    public static String dealDateTimeFormat(String oldDate) {
        if (oldDate == null || oldDate.isEmpty()) {
            return "";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(oldDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return dateTime.format(DEFAULT_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return oldDate;
        }
    }

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
     * 安全解析日期字符串，失败时返回 null。
     */
    public static Date parseOrNull(String strDate, String format) {
        if (strDate == null || strDate.isEmpty()) {
            return null;
        }
        try {
            return parseToDate(strDate, format);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 获取当前毫秒时间戳。
     */
    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 将 Date 转为 {@link LocalDateTime}（系统默认时区）。
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(SYSTEM_ZONE).toLocalDateTime();
    }

    /**
     * 将 Date 转为 {@link LocalDate}（系统默认时区）。
     */
    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(SYSTEM_ZONE).toLocalDate();
    }

    /**
     * 将 {@link LocalDateTime} 转为 Date（系统默认时区）。
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(SYSTEM_ZONE).toInstant());
    }

    /**
     * 将 {@link LocalDate} 转为 Date（当天 00:00:00，系统默认时区）。
     */
    public static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(SYSTEM_ZONE).toInstant());
    }

    private static Date parseToDate(String strDate, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.getDefault());
        try {
            return toDate(LocalDateTime.parse(strDate, formatter));
        } catch (DateTimeParseException ex) {
            return toDate(LocalDate.parse(strDate, formatter));
        }
    }
}
