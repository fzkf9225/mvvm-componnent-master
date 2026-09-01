package io.coderf.arklab.common.converter;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * room数据库Date类型转换器
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/7 9:11
 */
public class RoomDateToLongConverters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}

