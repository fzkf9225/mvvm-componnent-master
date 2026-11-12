package io.coderf.arklab.demo.repository

import io.coderf.arklab.common.bean.AttachmentBean
import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.common.repository.AttachmentRepositoryImpl
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.db.RoomDataRepository
import io.coderf.arklab.core.request.LoadingCancelPolicy
import io.coderf.arklab.demo.bean.Person
import io.coderf.arklab.demo.bean.PersonEditModel
import io.coderf.arklab.demo.database.PersonDao
import kotlinx.coroutines.flow.Flow

/**
 * Demo：人员表仓库（协程栈），可联合保存附件。
 *
 * 附件按自身 mobileId upsert；业务侧通过 [Person.imageIds] 等字段保存
 * [io.coderf.arklab.common.utils.common.AttachmentUtil.getIds] 返回的 ID 串。
 *
 * ```
 * repository.saveOrUpdateInfo(person, images, videos, media, files)
 *     .collect { it.onSuccess { }.onError { } }
 * ```
 *
 * 分页走 [io.coderf.arklab.core.db.RoomPagingSource]。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/9
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
     * 保存/更新人员，并用最新附件列表替换旧 ID 集合后回写到 Person。
     */
    fun saveOrUpdateInfo(
        person: Person,
        images: List<AttachmentBean>? = null,
        videos: List<AttachmentBean>? = null,
        media: List<AttachmentBean>? = null,
        files: List<AttachmentBean>? = null
    ): Flow<RequestResult<Unit>> {
        val options = RoomRequestOptions.Builder()
            .setShowDialog(true)
            .setDialogMessage("正在保存,请稍后...")
            .setEnableDynamicEllipsis(true)
            .setTimeoutSeconds(30)
            .setLoadingCancelPolicy(LoadingCancelPolicy.CancelRequest)
            .build()
        return execute(options) {
            val attachmentRepo = attachmentRepositoryImpl
            if (attachmentRepo != null) {
                person.imageIds = attachmentRepo.replaceByMobileIds(images, person.imageIds)
                person.videoIds = attachmentRepo.replaceByMobileIds(videos, person.videoIds)
                person.mediaIds = attachmentRepo.replaceByMobileIds(media, person.mediaIds)
                person.fileIds = attachmentRepo.replaceByMobileIds(files, person.fileIds)
            }
            getRoomDao().upsertSuspend(person)
        }
    }

    /**
     * 按主键加载人员及其关联的多类型附件，供编辑页回显。
     */
    fun loadEditModel(personId: String): Flow<RequestResult<PersonEditModel>> {
        val options = RoomRequestOptions.Builder()
            .setShowDialog(true)
            // 动态省略号会自行追加 "."，文案不要自带 "..."
            .setDialogMessage("正在加载")
            .setEnableDynamicEllipsis(true)
            .setLoadingCancelPolicy(LoadingCancelPolicy.CancelRequest)
            .build()
        return execute(options) {
            val person = getRoomDao().findInfoByIdSuspend(personId)
            val attachmentRepo = attachmentRepositoryImpl
            PersonEditModel(
                person = person,
                images = attachmentRepo?.queryListByMobileIds(person.imageIds).orEmpty(),
                videos = attachmentRepo?.queryListByMobileIds(person.videoIds).orEmpty(),
                media = attachmentRepo?.queryListByMobileIds(person.mediaIds).orEmpty(),
                files = attachmentRepo?.queryListByMobileIds(person.fileIds).orEmpty()
            )
        }
    }
}
