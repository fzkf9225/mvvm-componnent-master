package io.coderf.arklab.common.converter;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * room数据库java.time类型转换器（minSdk 26 及以上可直接使用）
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/14 10:00
 */
public class RoomJavaTimeConverters {

    /** 时间戳(毫秒) -> LocalDateTime，按系统默认时区解析 */
    @TypeConverter
    public static LocalDateTime localDateTimeFromLong(Long value) {
        return value == null ? null
                : LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }

    /** LocalDateTime -> 时间戳(毫秒)，按系统默认时区换算 */
    @TypeConverter
    public static Long localDateTimeToLong(LocalDateTime value) {
        return value == null ? null
                : value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /** ISO-8601字符串(yyyy-MM-dd) -> LocalDate */
    @TypeConverter
    public static LocalDate localDateFromString(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    /** LocalDate -> ISO-8601字符串(yyyy-MM-dd) */
    @TypeConverter
    public static String localDateToString(LocalDate value) {
        return value == null ? null : value.toString();
    }
}
