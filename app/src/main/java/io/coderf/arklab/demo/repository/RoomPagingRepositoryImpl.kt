package io.coderf.arklab.demo.repository

import io.coderf.arklab.common.bean.AttachmentBean
import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.common.repository.AttachmentRepositoryImpl
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.db.RoomDataRepository
import io.coderf.arklab.demo.bean.Person
import io.coderf.arklab.demo.database.PersonDao
import kotlinx.coroutines.flow.Flow

/**
 * Demo：人员表仓库（协程栈），可联合保存附件。
 *
 * 附件按自身 mobileId upsert；业务侧如需关联，应自行保存
 * [io.coderf.arklab.common.utils.common.AttachmentUtil.getIds] 返回的 ID 串。
 *
 * ```
 * repository.saveOrUpdateInfo(person, attachments)
 *     .collect { it.onSuccess { }.onError { } }
 * ```
 *
 * 分页走 [io.coderf.arklab.core.db.RoomPagingSource]。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/4
 */
class RoomPagingRepositoryImpl : RoomDataRepository<Person, PersonDao> {

    private var attachmentRepositoryImpl: AttachmentRepositoryImpl? = null

    constructor(
        roomDao: PersonDao,
        attachmentRepositoryImpl: AttachmentRepositoryImpl
    ) : super(roomDao) {
        this.attachmentRepositoryImpl = attachmentRepositoryImpl
    }

    constructor(roomDao: PersonDao) : super(roomDao)

    /**
     * 保存人员，并可选 upsert 附件（保留附件 mobileId，不反向写入业务外键）。
     */
    fun saveOrUpdateInfo(
        person: Person,
        imageList: List<AttachmentBean>?
    ): Flow<RequestResult<Unit>> {
        val options = RoomRequestOptions.Builder()
            .setShowDialog(true)
            .setDialogMessage("正在保存,请稍后...")
            .setEnableDynamicEllipsis(true)
            .setTimeoutSeconds(30)
            .build()
        return execute(options) {
            getRoomDao().insertSuspend(person)
            attachmentRepositoryImpl?.saveOrUpdate(imageList)
        }
    }
}
