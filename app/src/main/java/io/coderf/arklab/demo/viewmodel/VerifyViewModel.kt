package io.coderf.arklab.demo.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.EntryPoints
import io.coderf.arklab.base.api.AppPropertiesConfig
import io.coderf.arklab.common.api.ApiRetrofit
import io.coderf.arklab.common.base.BaseViewModel
import io.coderf.arklab.common.bean.AttachmentBean
import io.coderf.arklab.common.database.AttachmentDatabase
import io.coderf.arklab.common.repository.AttachmentRepositoryImpl
import io.coderf.arklab.common.utils.log.LogUtil
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.demo.bean.Person
import io.coderf.arklab.demo.bean.PersonEditModel
import io.coderf.arklab.demo.database.PersonDatabase
import io.coderf.arklab.demo.di.AppPropertiesEntryPoint
import io.coderf.arklab.demo.repository.RoomPagingRepositoryImpl
import kotlinx.coroutines.launch

/**
 * 校验页 ViewModel：人员 + 多类型附件联合保存 / 编辑回显（协程栈）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/9
 */
class VerifyViewModel(application: Application) : BaseViewModel<RoomPagingRepositoryImpl>(application) {

    @JvmField
    val liveData = MutableLiveData<Boolean>()

    @JvmField
    val editModelLiveData = MutableLiveData<PersonEditModel>()

    private var attachmentRoomRepositoryImpl: AttachmentRepositoryImpl? = null

    override fun createRepository(): RoomPagingRepositoryImpl {
        val config: AppPropertiesConfig = EntryPoints.get(
            getApplication(),
            AppPropertiesEntryPoint::class.java
        ).appPropertiesConfig()
        attachmentRoomRepositoryImpl = AttachmentRepositoryImpl(
            AttachmentDatabase.getInstance(
                getApplication(),
                config.attachmentDataBase
            ).attachmentDao
        )
        return RoomPagingRepositoryImpl(
            PersonDatabase.getInstance(getApplication()).personDao,
            attachmentRoomRepositoryImpl!!
        )
    }

    fun loadEdit(personId: String) {
        viewModelScope.launch {
            // 用 collect 让 Flow 正常结束，避免 first() 取消导致 Loading 关不掉
            iRepository.loadEditModel(personId).collect { result ->
                when (result) {
                    is RequestResult.Success -> editModelLiveData.value = result.data
                    is RequestResult.Error -> {
                        LogUtil.logger(ApiRetrofit.TAG, "加载失败：${result.error}")
                        networkRequestUiHost.showToast(result.error.message)
                        liveData.value = false
                    }
                }
            }
        }
    }

    fun save(
        person: Person,
        images: List<AttachmentBean>?,
        videos: List<AttachmentBean>?,
        media: List<AttachmentBean>?,
        files: List<AttachmentBean>?
    ) {
        viewModelScope.launch {
            iRepository.saveOrUpdateInfo(person, images, videos, media, files).collect { result ->
                when (result) {
                    is RequestResult.Success -> liveData.value = true
                    is RequestResult.Error -> {
                        LogUtil.logger(ApiRetrofit.TAG, "错误：${result.error}")
                        networkRequestUiHost.showToast(result.error.message)
                        liveData.value = false
                    }
                }
            }
        }
    }

    /** 兼容旧调用：仅保存图片附件 */
    fun add(person: Person, imageList: List<AttachmentBean>?) {
        save(person, imageList, null, null, null)
    }
}
