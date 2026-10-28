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
 * <p>
 * 每次 {@code postXxx} 会同时写入对应分路 LiveData 与统一通道，业务可逐步迁到单路观察。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/10/28
 */
class MediaResultPublisher {

    /** 选择/拍照/录像等结果（历史分路，兼容旧 observe） */
    private val pickLiveData = MutableLiveData<MediaBean>()

    /** 压缩结果（历史分路） */
    private val compressLiveData = MutableLiveData<MediaBean>()

    /** 水印结果（历史分路） */
    private val waterMarkLiveData = MutableLiveData<MediaBean>()

    /** 统一结果 LiveData：Pick / Compress / WaterMark */
    private val unifiedLiveData = MutableLiveData<MediaResult>()

    /**
     * 统一结果 Flow（协程友好）。
     * extraBufferCapacity=1 + DROP_OLDEST：无订阅者时最多缓存 1 条，避免阻塞压缩线程。
     */
    private val unifiedFlow = MutableSharedFlow<MediaResult>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** @see pickLiveData */
    fun getPickLiveData(): MutableLiveData<MediaBean> = pickLiveData

    /** @see compressLiveData */
    fun getCompressLiveData(): MutableLiveData<MediaBean> = compressLiveData

    /** @see waterMarkLiveData */
    fun getWaterMarkLiveData(): MutableLiveData<MediaBean> = waterMarkLiveData

    /** 统一结果 LiveData，推荐新代码使用 */
    fun getMediaResultLiveData(): LiveData<MediaResult> = unifiedLiveData

    /** 统一结果 SharedFlow，Kotlin 协程侧使用 */
    fun getMediaResultFlow(): SharedFlow<MediaResult> = unifiedFlow.asSharedFlow()

    /** 分发选择类结果：分路 + 统一通道 */
    fun postPick(mediaBean: MediaBean) {
        pickLiveData.postValue(mediaBean)
        dispatch(MediaResult.Pick(mediaBean))
    }

    /** 分发压缩结果：分路 + 统一通道 */
    fun postCompress(mediaBean: MediaBean) {
        compressLiveData.postValue(mediaBean)
        dispatch(MediaResult.Compress(mediaBean))
    }

    /** 分发水印结果：分路 + 统一通道 */
    fun postWaterMark(mediaBean: MediaBean) {
        waterMarkLiveData.postValue(mediaBean)
        dispatch(MediaResult.WaterMark(mediaBean))
    }

    /** 写入统一 LiveData / Flow */
    private fun dispatch(result: MediaResult) {
        unifiedLiveData.postValue(result)
        unifiedFlow.tryEmit(result)
    }
}
