package io.coderf.arklab.common.converter;

import android.net.Uri;

import androidx.room.TypeConverter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * room数据库标量类型转换器
 *
 * <p>注意：UUID 字段存库时须使用标准 36 位格式（含横线），
 * 若存的是去除横线的紧凑格式（如 AttachmentBean.mobileId），解析会抛异常。</p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/14 10:00
 */
public class RoomScalarConverters {

    /** Uri -> 字符串 */
    @TypeConverter
    public static String uriToString(Uri uri) {
        return uri == null ? null : uri.toString();
    }

    /** 字符串 -> Uri */
    @TypeConverter
    public static Uri stringToUri(String value) {
        return value == null ? null : Uri.parse(value);
    }

    /** UUID -> 字符串（标准36位格式） */
    @TypeConverter
    public static String uuidToString(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    /** 字符串（标准36位格式） -> UUID */
    @TypeConverter
    public static UUID stringToUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    /** BigDecimal -> 字符串（toPlainString，避免科学计数法） */
    @TypeConverter
    public static String bigDecimalToString(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    /** 字符串 -> BigDecimal */
    @TypeConverter
    public static BigDecimal stringToBigDecimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
