package io.coderf.arklab.common.repository

import androidx.lifecycle.LiveData
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.bean.AttachmentBean
import io.coderf.arklab.common.dao.AttachmentDao
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Created by fz on 2023/12/1 15:25
 * describe :
 */
class AttachmentRepositoryImpl(attachmentDao: AttachmentDao, baseView: BaseView?) :
    RoomRepositoryImpl<AttachmentBean, AttachmentDao, BaseView?>(attachmentDao, baseView) {

    fun queryFlowListByMainId(mainId: String): Flowable<List<AttachmentBean>> {
        val queryParams = mapOf<String, Any>("mainId" to mainId)
        return getRoomDao().doQueryByLimit(queryParams, null, Int.MAX_VALUE, 0)
    }

    fun queryFlowList(mainId: String, fieldName: String): Flowable<List<AttachmentBean>> {
        val queryParams = mapOf<String, Any>(
            "mainId" to mainId,
            "fieldName" to fieldName
        )
        return getRoomDao().doQueryByLimit(queryParams, null, Int.MAX_VALUE, 0)
    }

    fun queryLiveDataListByMainId(mainId: String): LiveData<List<AttachmentBean>> {
        val queryParams = mapOf<String, Any>("mainId" to mainId)
        return getRoomDao().doQueryByLimitLiveData(queryParams, null, Int.MAX_VALUE, 0)
    }

    fun queryFlowLiveDataList(mainId: String, fieldName: String): LiveData<List<AttachmentBean>> {
        val queryParams = mapOf<String, Any>(
            "mainId" to mainId,
            "fieldName" to fieldName
        )
        return getRoomDao().doQueryByLimitLiveData(queryParams, null, Int.MAX_VALUE, 0)
    }

    fun queryListByMainId(mainId: String): List<AttachmentBean> {
        val queryParams = mapOf<String, Any>("mainId" to mainId)
        return getRoomDao().findPageList(queryParams, null, Int.MAX_VALUE, 0)
    }

    fun queryList(mainId: String, fieldName: String): List<AttachmentBean> {
        val queryParams = mapOf<String, Any>(
            "mainId" to mainId,
            "fieldName" to fieldName
        )
        return getRoomDao().findPageList(queryParams, null, Int.MAX_VALUE, 0)
    }

    /** 按 mainId 删除，返回影响行数 */
    fun deleteByMainIdCount(mainId: String): Single<Int> {
        val queryParams = mapOf<String, Any>("mainId" to mainId)
        return getRoomDao().deleteByParamsCount(queryParams)
    }

    /** 按 mainId + fieldName 删除，返回影响行数 */
    fun deleteCount(mainId: String, fieldName: String): Single<Int> {
        val queryParams = mapOf<String, Any>(
            "mainId" to mainId,
            "fieldName" to fieldName
        )
        return getRoomDao().deleteByParamsCount(queryParams)
    }

    @Deprecated("Use deleteByMainIdCount(mainId)", ReplaceWith("deleteByMainIdCount(mainId)"))
    fun deleteByMainId(mainId: String): Flowable<List<AttachmentBean>> {
        val queryParams = mapOf<String, Any>("mainId" to mainId)
        @Suppress("DEPRECATION")
        return getRoomDao().deleteByParams(queryParams)
    }

    @Deprecated("Use deleteCount(mainId, fieldName)", ReplaceWith("deleteCount(mainId, fieldName)"))
    fun delete(mainId: String, fieldName: String): Flowable<List<AttachmentBean>> {
        val queryParams = mapOf<String, Any>(
            "mainId" to mainId,
            "fieldName" to fieldName
        )
        @Suppress("DEPRECATION")
        return getRoomDao().deleteByParams(queryParams)
    }

    fun insert(dataList: List<AttachmentBean>?, mainId: String): Completable {
        if (dataList.isNullOrEmpty()) {
            return Completable.complete()
        }
        dataList.forEach { item ->
            item.mainId = mainId
        }
        return getRoomDao().insert(dataList.toList())
    }

    fun insert(dataList: List<AttachmentBean>?, mainId: String, fieldName: String): Completable {
        if (dataList.isNullOrEmpty()) {
            return Completable.complete()
        }
        dataList.forEach { item ->
            item.mainId = mainId
            item.fieldName = fieldName
        }
        return getRoomDao().insert(dataList)
    }

    fun saveOrUpdate(dataList: List<AttachmentBean>?, mainId: String) {
        getRoomDao().deleteByMainId(mainId)
        if (dataList.isNullOrEmpty()) {
            return
        }
        dataList.forEach { item ->
            item.mainId = mainId
            item.mobileId = UUID.randomUUID().toString().replace("-", "")
        }
        getRoomDao().insertOnly(dataList)
    }

    fun saveOrUpdate(dataList: List<AttachmentBean>?, mainId: String, fieldName: String) {
        getRoomDao().delete(mainId, fieldName)
        if (dataList.isNullOrEmpty()) {
            return
        }
        dataList.forEach { item ->
            item.mainId = mainId
            item.fieldName = fieldName
            item.mobileId = UUID.randomUUID().toString().replace("-", "")
        }
        getRoomDao().insertOnly(dataList)
    }

    suspend fun saveOrUpdateSuspend(
        dataList: List<AttachmentBean>?,
        mainId: String,
        fieldName: String?
    ) {
        withContext(Dispatchers.IO) {
            if (fieldName.isNullOrEmpty()) {
                getRoomDao().deleteByMobileId(mainId)
            } else {
                getRoomDao().delete(mainId, fieldName)
            }
            if (dataList.isNullOrEmpty()) {
                return@withContext
            }
            dataList.forEach { item ->
                item.mainId = mainId
                item.fieldName = fieldName
                item.mobileId = UUID.randomUUID().toString().replace("-", "")
            }
            getRoomDao().insertOnly(dataList)
        }
    }

}
