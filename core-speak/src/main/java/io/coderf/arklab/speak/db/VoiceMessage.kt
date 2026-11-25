package io.coderf.arklab.speak.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import io.coderf.arklab.common.bean.BaseDaoBean

/**
 * 语音消息元数据实体。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
@Entity(tableName = "VoiceMessage")
class VoiceMessage : BaseDaoBean() {

    @ColumnInfo
    var filePath: String? = null

    @ColumnInfo
    var durationMs: Long = 0L

    @ColumnInfo
    var sizeBytes: Long = 0L

    @ColumnInfo
    var userId: String? = null

    @ColumnInfo
    var createdAt: Long = System.currentTimeMillis()

    /** 预留转写文案；本期无 ASR，可为空。 */
    @ColumnInfo
    var transcript: String? = null
}
