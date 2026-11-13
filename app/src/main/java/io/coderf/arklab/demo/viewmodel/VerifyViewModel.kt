package io.coderf.arklab.demo.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.EntryPoints
import io.coderf.arklab.base.api.AppPropertiesConfig
import io.coderf.arklab.common.api.ApiRetrofit
import io.coderf.arklab.common.base.BaseViewModel
import io.coderf.arklab.common.bean.AttachmentBean
import io.coderf.arklab.common.database.AttachmentDatabase
import io.coderf.arklab.common.repository.AttachmentRepositoryImpl
import io.coderf.arklab.common.utils.log.LogUtil
import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.request.LoadingCancelPolicy
import io.coderf.arklab.demo.bean.Person
import io.coderf.arklab.demo.bean.PersonEditModel
import io.coderf.arklab.demo.database.PersonDatabase
import io.coderf.arklab.demo.di.AppPropertiesEntryPoint
import io.coderf.arklab.demo.repository.RoomPagingRepositoryImpl

/**
 * 校验页 ViewModel：人员 + 多类型附件联合保存 / 编辑回显（协程栈）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/13
 */
class VerifyViewModel(application: Application) : BaseViewModel(application) {

    @JvmField
    val liveData = MutableLiveData<Boolean>()

    @JvmField
    val editModelLiveData = MutableLiveData<PersonEditModel>()

    private val roomRepository: RoomPagingRepositoryImpl

    init {
        val config: AppPropertiesConfig = EntryPoints.get(
            getApplication(),
            AppPropertiesEntryPoint::class.java
        ).appPropertiesConfig()
        val attachmentRoomRepositoryImpl = AttachmentRepositoryImpl(
            AttachmentDatabase.getInstance(
                getApplication(),
                config.attachmentDataBase
            ).attachmentDao
        )
        roomRepository = RoomPagingRepositoryImpl(
            PersonDatabase.getInstance(getApplication()).personDao,
            attachmentRoomRepositoryImpl
        )
    }

    fun loadEdit(personId: String) {
        launchRequest(
            RequestOptions(
                loadingMessage = "正在加载",
                loadingCancelPolicy = LoadingCancelPolicy.CancelRequest,
                deliverErrorToUi = false
            )
        ) {
            roomRepository.loadEditModel(personId).collectRequest(
                onSuccess = { editModelLiveData.value = it },
                onError = { error ->
                    LogUtil.logger(ApiRetrofit.TAG, "加载失败：$error")
                    networkRequestUiHost.showToast(error.message)
                    liveData.value = false
                }
            )
        }
    }

    fun save(
        person: Person,
        images: List<AttachmentBean>?,
        videos: List<AttachmentBean>?,
        media: List<AttachmentBean>?,
        files: List<AttachmentBean>?
    ) {
        launchRequest(
            RequestOptions(
                loadingMessage = "正在保存,请稍后...",
                loadingCancelPolicy = LoadingCancelPolicy.CancelRequest,
                deliverErrorToUi = false
            )
        ) {
            roomRepository.saveOrUpdateInfo(person, images, videos, media, files).collectRequest(
                onSuccess = { liveData.value = true },
                onError = { error ->
                    LogUtil.logger(ApiRetrofit.TAG, "错误：$error")
                    networkRequestUiHost.showToast(error.message)
                    liveData.value = false
                }
            )
        }
    }

    /** 兼容旧调用：仅保存图片附件 */
    fun add(person: Person, imageList: List<AttachmentBean>?) {
        save(person, imageList, null, null, null)
    }
}
