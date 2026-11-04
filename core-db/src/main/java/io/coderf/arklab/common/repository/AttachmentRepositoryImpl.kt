package io.coderf.arklab.common.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.coderf.arklab.common.bean.AttachmentBean
import io.coderf.arklab.common.dao.AttachmentDao
import io.coderf.arklab.common.utils.common.AttachmentUtil
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 附件仓库：按本地 [AttachmentBean.mobileId] / 服务端 [AttachmentBean.fileId] 增删查改。
 *
 * 业务侧应持有附件 ID（集合或逗号拼接串），附件表不再反向关联业务。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/12/1 15:25
 * @updated 2026/11/4
 */
class AttachmentRepositoryImpl(attachmentDao: AttachmentDao) :
    RoomRepositoryImpl<AttachmentBean, AttachmentDao>(attachmentDao) {

    // ==================== 查询：mobileId ====================

    fun queryByMobileId(mobileId: String?): AttachmentBean? {
        if (mobileId.isNullOrBlank()) {
            return null
        }
        return getRoomDao().queryByMobileId(mobileId)
    }

    fun queryListByMobileIds(ids: List<String>?): List<AttachmentBean> {
        val normalized = AttachmentUtil.normalizeIds(ids)
        if (normalized.isEmpty()) {
            return emptyList()
        }
        return getRoomDao().queryByMobileIds(normalized)
    }

    fun queryListByMobileIds(idsCsv: String?): List<AttachmentBean> {
        return queryListByMobileIds(AttachmentUtil.parseIds(idsCsv))
    }

    fun queryFlowListByMobileIds(ids: List<String>?): Flowable<List<AttachmentBean>> {
        val normalized = AttachmentUtil.normalizeIds(ids)
        if (normalized.isEmpty()) {
            return Flowable.just(emptyList())
        }
        return getRoomDao().queryFlowByMobileIds(normalized)
    }

    fun queryFlowListByMobileIds(idsCsv: String?): Flowable<List<AttachmentBean>> {
        return queryFlowListByMobileIds(AttachmentUtil.parseIds(idsCsv))
    }

    fun queryLiveDataListByMobileIds(ids: List<String>?): LiveData<List<AttachmentBean>> {
        val normalized = AttachmentUtil.normalizeIds(ids)
        if (normalized.isEmpty()) {
            return MutableLiveData(emptyList())
        }
        return getRoomDao().queryLiveDataByMobileIds(normalized)
    }

    fun queryLiveDataListByMobileIds(idsCsv: String?): LiveData<List<AttachmentBean>> {
        return queryLiveDataListByMobileIds(AttachmentUtil.parseIds(idsCsv))
    }

    // ==================== 查询：fileId ====================

    fun queryByFileId(fileId: String?): AttachmentBean? {
        if (fileId.isNullOrBlank()) {
            return null
        }
        return getRoomDao().queryByFileId(fileId)
    }

    fun queryListByFileIds(ids: List<String>?): List<AttachmentBean> {
        val normalized = AttachmentUtil.normalizeIds(ids)
        if (normalized.isEmpty()) {
            return emptyList()
        }
        return getRoomDao().queryByFileIds(normalized)
    }

    fun queryListByFileIds(idsCsv: String?): List<AttachmentBean> {
        return queryListByFileIds(AttachmentUtil.parseIds(idsCsv))
    }

    fun queryFlowListByFileIds(ids: List<String>?): Flowable<List<AttachmentBean>> {
        val normalized = AttachmentUtil.normalizeIds(ids)
        if (normalized.isEmpty()) {
            return Flowable.just(emptyList())
        }
        return getRoomDao().queryFlowByFileIds(normalized)
    }

    fun queryFlowListByFileIds(idsCsv: String?): Flowable<List<AttachmentBean>> {
        return queryFlowListByFileIds(AttachmentUtil.parseIds(idsCsv))
    }

    fun queryLiveDataListByFileIds(ids: List<String>?): LiveData<List<AttachmentBean>> {
        val normalized = AttachmentUtil.normalizeIds(ids)
        if (normalized.isEmpty()) {
            return MutableLiveData(emptyList())
        }
        return getRoomDao().queryLiveDataByFileIds(normalized)
    }

    fun queryLiveDataListByFileIds(idsCsv: String?): LiveData<List<AttachmentBean>> {
        return queryLiveDataListByFileIds(AttachmentUtil.parseIds(idsCsv))
    }

    // ==================== 删除 ====================

    fun deleteByMobileId(mobileId: String?): Int {
        if (mobileId.isNullOrBlank()) {
            return 0
        }
        return getRoomDao().deleteByMobileId(mobileId)
    }

    fun deleteByMobileIds(ids: List<String>?): Int {
        val normalized = AttachmentUtil.normalizeIds(ids)
        if (normalized.isEmpty()) {
            return 0
        }
        return getRoomDao().deleteByMobileIds(normalized)
    }

    fun deleteByMobileIds(idsCsv: String?): Int {
        return deleteByMobileIds(AttachmentUtil.parseIds(idsCsv))
    }

    fun deleteByMobileIdsCount(ids: List<String>?): Single<Int> {
        return Single.fromCallable { deleteByMobileIds(ids) }
    }

    fun deleteByMobileIdsCount(idsCsv: String?): Single<Int> {
        return Single.fromCallable { deleteByMobileIds(idsCsv) }
    }

    fun deleteByFileId(fileId: String?): Int {
        if (fileId.isNullOrBlank()) {
            return 0
        }
        return getRoomDao().deleteByFileId(fileId)
    }

    fun deleteByFileIds(ids: List<String>?): Int {
        val normalized = AttachmentUtil.normalizeIds(ids)
        if (normalized.isEmpty()) {
            return 0
        }
        return getRoomDao().deleteByFileIds(normalized)
    }

    fun deleteByFileIds(idsCsv: String?): Int {
        return deleteByFileIds(AttachmentUtil.parseIds(idsCsv))
    }

    // ==================== 写入（保留 mobileId，不再重建） ====================

    fun insert(dataList: List<AttachmentBean>?): Completable {
        if (dataList.isNullOrEmpty()) {
            return Completable.complete()
        }
        return getRoomDao().insert(dataList)
    }

    /**
     * 按主键 upsert；保留已有 [AttachmentBean.mobileId]，不强制重建。
     */
    fun saveOrUpdate(dataList: List<AttachmentBean>?) {
        if (dataList.isNullOrEmpty()) {
            return
        }
        getRoomDao().upsertOnly(dataList)
    }

    suspend fun saveOrUpdateSuspend(dataList: List<AttachmentBean>?) {
        withContext(Dispatchers.IO) {
            if (dataList.isNullOrEmpty()) {
                return@withContext
            }
            getRoomDao().upsertOnly(dataList)
        }
    }

    /**
     * 用新列表整体替换旧 ID 集合：删除不在新列表中的旧附件，再 upsert 新列表。
     *
     * @param dataList 最新附件列表（其 mobileId 将作为业务侧应保存的 ID）
     * @param previousIds 业务侧原先持有的 mobileId 集合（可为逗号串解析结果）
     * @return 最新列表的 mobileId 逗号拼接串，便于回写业务字段
     */
    fun replaceByMobileIds(
        dataList: List<AttachmentBean>?,
        previousIds: List<String>?
    ): String {
        val newIds = AttachmentUtil.normalizeIds(AttachmentUtil.getIdList(dataList))
        val oldIds = AttachmentUtil.normalizeIds(previousIds)
        val removed = oldIds.filterNot { newIds.contains(it) }
        if (removed.isNotEmpty()) {
            getRoomDao().deleteByMobileIds(removed)
        }
        if (!dataList.isNullOrEmpty()) {
            getRoomDao().upsertOnly(dataList)
        }
        return AttachmentUtil.getIds(dataList).orEmpty()
    }

    fun replaceByMobileIds(
        dataList: List<AttachmentBean>?,
        previousIdsCsv: String?
    ): String {
        return replaceByMobileIds(dataList, AttachmentUtil.parseIds(previousIdsCsv))
    }

    suspend fun replaceByMobileIdsSuspend(
        dataList: List<AttachmentBean>?,
        previousIds: List<String>?
    ): String {
        return withContext(Dispatchers.IO) {
            replaceByMobileIds(dataList, previousIds)
        }
    }

    suspend fun replaceByMobileIdsSuspend(
        dataList: List<AttachmentBean>?,
        previousIdsCsv: String?
    ): String {
        return withContext(Dispatchers.IO) {
            replaceByMobileIds(dataList, previousIdsCsv)
        }
    }
}
