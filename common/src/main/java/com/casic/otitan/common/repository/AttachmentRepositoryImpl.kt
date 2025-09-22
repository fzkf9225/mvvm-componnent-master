package com.casic.otitan.common.repository

import androidx.lifecycle.LiveData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.casic.otitan.common.base.BaseView
import com.casic.otitan.common.bean.AttachmentBean
import com.casic.otitan.common.dao.AttachmentDao
import java.util.UUID

/**
 * Created by fz on 2023/12/1 15:25
 * describe :
 */
class AttachmentRepositoryImpl(attachmentDao: AttachmentDao, baseView: BaseView?) :
    RoomRepositoryImpl<AttachmentBean, AttachmentDao, BaseView?>(attachmentDao, baseView) {

    fun queryFlowListByMainId(mainId: String): Flowable<List<AttachmentBean>> {
        val queryParams = HashMap<String, String>()
        queryParams.put("mainId", mainId)
        return getRoomDao().doQueryByLimit(queryParams, null, Int.MAX_VALUE, 0)
    }

    fun queryFlowList(mainId: String, fieldName: String): Flowable<List<AttachmentBean>> {
        val queryParams = HashMap<String, String>()
        queryParams.put("mainId", mainId)
        queryParams.put("fieldName", fieldName)
        return getRoomDao().doQueryByLimit(queryParams, null, Int.MAX_VALUE, 0)
    }

    fun queryLiveDataListByMainId(mainId: String): LiveData<List<AttachmentBean>> {
        val queryParams = HashMap<String, String>()
        queryParams.put("mainId", mainId)
        return getRoomDao().doQueryByLimitLiveData(queryParams, null, Int.MAX_VALUE, 0)
    }

    fun queryFlowLiveDataList(mainId: String, fieldName: String): LiveData<List<AttachmentBean>> {
        val queryParams = HashMap<String, String>()
        queryParams.put("mainId", mainId)
        queryParams.put("fieldName", fieldName)
        return getRoomDao().doQueryByLimitLiveData(queryParams, null, Int.MAX_VALUE, 0)
    }

    fun queryListByMainId(mainId: String): List<AttachmentBean> {
        val queryParams = HashMap<String, String>()
        queryParams.put("mainId", mainId)
        return getRoomDao().findPageList(queryParams, null, 10, 0)
    }

    fun queryList(mainId: String, fieldName: String): List<AttachmentBean> {
        val queryParams = HashMap<String, String>()
        queryParams.put("mainId", mainId)
        queryParams.put("fieldName", fieldName)
        return getRoomDao().findPageList(queryParams, null, Int.MAX_VALUE, 0)
    }

    fun deleteByMainId(mainId: String): Flowable<List<AttachmentBean>> {
        val queryParams = HashMap<String, String>()
        queryParams.put("mainId", mainId)
        return getRoomDao().deleteByParams(queryParams)
    }

    fun delete(mainId: String, fieldName: String): Flowable<List<AttachmentBean>> {
        val queryParams = HashMap<String, String>()
        queryParams.put("mainId", mainId)
        queryParams.put("fieldName", fieldName)
        return getRoomDao().deleteByParams(queryParams)
    }

    fun insert(dataList: List<AttachmentBean>?, mainId: String): Completable {
        if (dataList.isNullOrEmpty()) {
            return Completable.complete()
        }
        dataList.forEach { item ->
            {
                item.mainId = mainId
            }
        }
        return getRoomDao().insert(dataList.toList())
    }

    fun insert(dataList: List<AttachmentBean>?, mainId: String, fieldName: String): Completable {
        if (dataList.isNullOrEmpty()) {
            return Completable.complete()
        }
        dataList.forEach { item ->
            {
                item.mainId = mainId
                item.fieldName = fieldName
            }
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