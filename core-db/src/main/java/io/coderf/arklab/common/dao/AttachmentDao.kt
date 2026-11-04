package io.coderf.arklab.common.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import io.coderf.arklab.common.annotation.RoomObservedEntity
import io.coderf.arklab.common.bean.AttachmentBean
import io.reactivex.rxjava3.core.Flowable

/**
 * 附件表 Room Dao。
 *
 * ## 说明
 * - 附件表只存文件实体，不反向关联业务；
 * - 业务侧通过持有 [AttachmentBean.mobileId]（或逗号拼接串）查询/更新附件；
 * - 服务端对接使用 [AttachmentBean.fileId]。
 *
 * ## 仓库层
 * 配合 [io.coderf.arklab.common.repository.AttachmentRepositoryImpl] 与
 * [io.coderf.arklab.common.database.AttachmentDatabase] 使用。
 *
 * @see RoomObservedEntity
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/4
 */
@Dao
@RoomObservedEntity(AttachmentBean::class)
abstract class AttachmentDao : AttachmentDaoRawQueryBridge() {

    /** @return 表名，与 @Entity 默认表名一致 */
    override fun getTableName(): String = AttachmentBean::class.java.simpleName

    // ==================== 按 mobileId ====================

    @Query("SELECT * FROM AttachmentBean WHERE mobileId = :mobileId LIMIT 1")
    abstract fun queryByMobileId(mobileId: String): AttachmentBean?

    @Query("SELECT * FROM AttachmentBean WHERE mobileId IN (:ids)")
    abstract fun queryByMobileIds(ids: List<String>): List<AttachmentBean>

    @Query("SELECT * FROM AttachmentBean WHERE mobileId IN (:ids)")
    abstract fun queryFlowByMobileIds(ids: List<String>): Flowable<List<AttachmentBean>>

    @Query("SELECT * FROM AttachmentBean WHERE mobileId IN (:ids)")
    abstract fun queryLiveDataByMobileIds(ids: List<String>): LiveData<List<AttachmentBean>>

    @Query("DELETE FROM AttachmentBean WHERE mobileId = :mobileId")
    abstract fun deleteByMobileId(mobileId: String): Int

    @Query("DELETE FROM AttachmentBean WHERE mobileId IN (:ids)")
    abstract fun deleteByMobileIds(ids: List<String>): Int

    // ==================== 按 fileId（服务端 ID） ====================

    @Query("SELECT * FROM AttachmentBean WHERE fileId = :fileId LIMIT 1")
    abstract fun queryByFileId(fileId: String): AttachmentBean?

    @Query("SELECT * FROM AttachmentBean WHERE fileId IN (:ids)")
    abstract fun queryByFileIds(ids: List<String>): List<AttachmentBean>

    @Query("SELECT * FROM AttachmentBean WHERE fileId IN (:ids)")
    abstract fun queryFlowByFileIds(ids: List<String>): Flowable<List<AttachmentBean>>

    @Query("SELECT * FROM AttachmentBean WHERE fileId IN (:ids)")
    abstract fun queryLiveDataByFileIds(ids: List<String>): LiveData<List<AttachmentBean>>

    @Query("DELETE FROM AttachmentBean WHERE fileId = :fileId")
    abstract fun deleteByFileId(fileId: String): Int

    @Query("DELETE FROM AttachmentBean WHERE fileId IN (:ids)")
    abstract fun deleteByFileIds(ids: List<String>): Int

    // ==================== 其它 ====================

    @Query("SELECT * FROM AttachmentBean")
    abstract fun queryAll(): List<AttachmentBean>

    @Query("SELECT * FROM AttachmentBean WHERE createUser = :createUser")
    abstract fun queryAllByUser(createUser: String?): List<AttachmentBean>
}
