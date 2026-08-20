package io.coderf.arklab.common.dao

import androidx.room.Dao
import androidx.room.Query
import io.coderf.arklab.common.annotation.RoomObservedEntity
import io.coderf.arklab.common.bean.AttachmentBean

/**
 * 附件表 Room Dao 示例。
 *
 * ## 说明
 * - 使用 [@RoomObservedEntity] + [AttachmentDaoRawQueryBridge]（KSP 生成），无需手写 6 个 RawQuery；
 * - 通用增删改查、动态条件查询继承自 [BaseRoomDao]；
 * - 本类仅补充附件业务相关的固定 [@Query] 方法。
 *
 * ## 仓库层
 * 配合 [io.coderf.arklab.common.repository.AttachmentRepositoryImpl] 与
 * [io.coderf.arklab.common.database.AttachmentDatabase] 使用。
 *
 * @author fz
 * @see RoomObservedEntity
 */
@Dao
@RoomObservedEntity(AttachmentBean::class)
abstract class AttachmentDao : AttachmentDaoRawQueryBridge() {

    /** @return 表名，与 @Entity 默认表名一致 */
    override fun getTableName(): String = AttachmentBean::class.java.simpleName

    /** 按 mobileId 删除附件 */
    @Query("Delete FROM AttachmentBean WHERE mobileId = :mobileId")
    abstract fun deleteByMobileId(mobileId: String)

    /** 按 mainId 删除附件 */
    @Query("Delete FROM AttachmentBean WHERE mainId = :mainId")
    abstract fun deleteByMainId(mainId: String)

    /** 按 mainId + fieldName 删除 */
    @Query("Delete FROM AttachmentBean WHERE mainId = :mainId and fieldName = :fieldName")
    abstract fun delete(mainId: String, fieldName: String)

    /** 查询全部附件 */
    @Query("Select * FROM AttachmentBean")
    abstract fun queryAll(): List<AttachmentBean>

    /** 按创建人查询 */
    @Query("Select * FROM AttachmentBean where createUser = :createUser")
    abstract fun queryAllByUser(createUser: String?): List<AttachmentBean>

    /** 按 mainId 查询列表 */
    @Query("Select * FROM AttachmentBean WHERE mainId = :mainId")
    abstract fun queryByMainId(mainId: String): List<AttachmentBean>

    /** 按 mainId、fieldName 查询 */
    @Query("Select * FROM AttachmentBean WHERE mainId = :mainId and fieldName = :fieldName")
    abstract fun queryList(mainId: String, fieldName: String): List<AttachmentBean>

    /** 按 mainId、createUser 查询 */
    @Query("Select * FROM AttachmentBean WHERE mainId = :mainId and createUser = :createUser")
    abstract fun queryByMainId(mainId: String, createUser: String?): List<AttachmentBean>

    /** 按 mainId、fieldName、createUser 查询 */
    @Query("Select * FROM AttachmentBean WHERE mainId = :mainId and fieldName = :fieldName and createUser = :createUser")
    abstract fun queryList(mainId: String, fieldName: String, createUser: String?): List<AttachmentBean>
}
