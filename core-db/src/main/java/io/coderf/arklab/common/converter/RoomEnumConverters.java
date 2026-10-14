package io.coderf.arklab.common.converter;

import androidx.room.TypeConverter;

import io.coderf.arklab.common.enums.AttachmentTypeEnum;
import io.coderf.arklab.common.enums.UploadStatusEnum;

/**
 * room数据库枚举类型转换器
 *
 * <p>说明：Room 泛型枚举转换器（@ProvidedTypeConverter）在 Java 实体的生成代码场景下存在
 * 类型擦除限制，故本类为具体枚举各提供一对转换方法；
 * 持久化沿用枚举的 typeValue，与存量数据保持一致，新增枚举照此模式添加。</p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/14 10:00
 */
public class RoomEnumConverters {

    /** 附件类型枚举 -> 字符串(typeValue) */
    @TypeConverter
    public static String attachmentTypeToString(AttachmentTypeEnum value) {
        return value == null ? null : value.typeValue;
    }

    /** 字符串(typeValue) -> 附件类型枚举 */
    @TypeConverter
    public static AttachmentTypeEnum stringToAttachmentType(String value) {
        return value == null ? null : AttachmentTypeEnum.getMediaType(value);
    }

    /** 上传状态枚举 -> 整型(typeValue) */
    @TypeConverter
    public static Integer uploadStatusToInteger(UploadStatusEnum value) {
        return value == null ? null : value.typeValue;
    }

    /** 整型(typeValue) -> 上传状态枚举 */
    @TypeConverter
    public static UploadStatusEnum integerToUploadStatus(Integer value) {
        return value == null ? null : UploadStatusEnum.getInfo(value);
    }
}
