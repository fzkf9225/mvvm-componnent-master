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
import io.coderf.arklab.demo.database.PersonDatabase
import io.coderf.arklab.demo.di.AppPropertiesEntryPoint
import io.coderf.arklab.demo.repository.RoomPagingRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 校验页 ViewModel：人员 + 附件联合保存（协程栈）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/10/28
 */
class VerifyViewModel(application: Application) : BaseViewModel<RoomPagingRepositoryImpl>(application) {

    @JvmField
    val liveData = MutableLiveData<Boolean>()

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

    fun add(person: Person, imageList: List<AttachmentBean>?) {
        viewModelScope.launch {
            when (val result = iRepository.saveOrUpdateInfo(person, imageList).first()) {
                is RequestResult.Success -> liveData.postValue(true)
                is RequestResult.Error -> {
                    LogUtil.logger(ApiRetrofit.TAG, "错误：${result.error}")
                    networkRequestUiHost.showToast(result.error.message)
                    liveData.postValue(false)
                }
            }
        }
    }
}
