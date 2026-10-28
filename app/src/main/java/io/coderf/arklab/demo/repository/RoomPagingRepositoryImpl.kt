package io.coderf.arklab.demo.repository

import io.coderf.arklab.common.bean.AttachmentBean
import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.common.repository.AttachmentRepositoryImpl
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.db.RoomDataRepository
import io.coderf.arklab.demo.bean.Person
import io.coderf.arklab.demo.database.PersonDao
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Demo：人员表仓库（协程栈），可联合保存附件。
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
 * @updated 2026/10/28
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
     * 保存人员，并可选同步写入附件表（附件仍用 [AttachmentRepositoryImpl] 同步 API，在 IO 上执行）。
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
            attachmentRepositoryImpl?.saveOrUpdate(
                imageList,
                UUID.randomUUID().toString().replace("-", "")
            )
        }
    }
}
