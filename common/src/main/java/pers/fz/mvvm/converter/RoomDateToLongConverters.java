package pers.fz.mvvm.converter;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * created by fz on 2024/11/7 9:11
 * describe:room数据库Date类型转换器
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

