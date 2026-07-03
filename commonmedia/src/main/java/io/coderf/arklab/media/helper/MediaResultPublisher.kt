package io.coderf.arklab.media.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.coderf.arklab.media.bean.MediaBean
import io.coderf.arklab.media.bean.MediaResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 统一分发 [MediaResult]：LiveData + SharedFlow，并兼容旧版分路 LiveData。
 */
class MediaResultPublisher {

    private val pickLiveData = MutableLiveData<MediaBean>()
    private val compressLiveData = MutableLiveData<MediaBean>()
    private val waterMarkLiveData = MutableLiveData<MediaBean>()
    private val unifiedLiveData = MutableLiveData<MediaResult>()
    private val unifiedFlow = MutableSharedFlow<MediaResult>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun getPickLiveData(): MutableLiveData<MediaBean> = pickLiveData

    fun getCompressLiveData(): MutableLiveData<MediaBean> = compressLiveData

    fun getWaterMarkLiveData(): MutableLiveData<MediaBean> = waterMarkLiveData

    fun getMediaResultLiveData(): LiveData<MediaResult> = unifiedLiveData

    fun getMediaResultFlow(): SharedFlow<MediaResult> = unifiedFlow.asSharedFlow()

    fun postPick(mediaBean: MediaBean) {
        pickLiveData.postValue(mediaBean)
        dispatch(MediaResult.Pick(mediaBean))
    }

    fun postCompress(mediaBean: MediaBean) {
        compressLiveData.postValue(mediaBean)
        dispatch(MediaResult.Compress(mediaBean))
    }

    fun postWaterMark(mediaBean: MediaBean) {
        waterMarkLiveData.postValue(mediaBean)
        dispatch(MediaResult.WaterMark(mediaBean))
    }

    private fun dispatch(result: MediaResult) {
        unifiedLiveData.postValue(result)
        unifiedFlow.tryEmit(result)
    }
}
