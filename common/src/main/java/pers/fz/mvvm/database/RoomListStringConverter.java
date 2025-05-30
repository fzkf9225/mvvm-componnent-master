package pers.fz.mvvm.database;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.List;

/**
 * created by fz on 2024/11/6 10:52
 * describe:room数据库字符串集合类型转换器
 */
public class RoomListStringConverter {
    @TypeConverter
    public static List<String> fromString(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.asList(value.split(","));
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null) {
            return null;
        }
        return String.join(",", list);
    }
}

