package io.coderf.arklab.demo.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.viewmodel.PagingViewModel
import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.bean.RoomPagingQuery
import io.coderf.arklab.core.db.RoomPagingSource
import io.coderf.arklab.speak.db.VoiceDatabase
import io.coderf.arklab.speak.db.VoiceMessage
import io.coderf.arklab.speak.db.VoiceMessageRepository
import io.coderf.arklab.speak.file.DefaultVoiceFileStore
import io.coderf.arklab.speak.model.VoiceRecordResult
import javax.inject.Inject

/**
 * core-speak Demo：Room + Paging3 协程栈。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 14:05
 */
@HiltViewModel
class SpeakDemoViewModel @Inject constructor(
    application: Application
) : PagingViewModel<VoiceMessage, RoomPagingQuery>(
    application,
    VoiceMessageRepository(
        VoiceDatabase.getInstance(application).voiceMessageDao(),
        DefaultVoiceFileStore(application)
    )
) {

    @Suppress("UNCHECKED_CAST")
    private val voiceRepository: VoiceMessageRepository
        get() = pagingRepository as VoiceMessageRepository

    override fun createPagingData(): LiveData<PagingData<VoiceMessage>> {
        return Pager(pagingConfig) {
            RoomPagingSource(voiceRepository, pagingQuery)
        }.liveData
    }

    override val startPage: Int
        get() = 0

    override fun createPagingQuery(): RoomPagingQuery {
        return RoomPagingQuery().apply { orderBy = "createdAt" }
    }

    /** 发送成功后入库并刷新分页。 */
    fun saveSend(result: VoiceRecordResult, onDone: (() -> Unit)? = null) {
        launchRequest(RequestOptions.silent()) {
            voiceRepository.insertRecord(result).collectRequest(
                onSuccess = {
                    refreshData()
                    onDone?.invoke()
                },
                onError = { onDone?.invoke() }
            )
        }
    }

    fun delete(message: VoiceMessage, onResult: ((RequestResult<Unit>) -> Unit)? = null) {
        launchRequest(RequestOptions.silent()) {
            voiceRepository.deleteMessage(message).collectRequest(
                onSuccess = {
                    refreshData()
                    onResult?.invoke(RequestResult.Success(Unit))
                },
                onError = { error -> onResult?.invoke(RequestResult.Error(error)) }
            )
        }
    }
}
