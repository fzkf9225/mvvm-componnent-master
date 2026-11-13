package io.coderf.arklab.demo.viewmodel

import android.app.Application
import android.text.TextUtils
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
import io.coderf.arklab.demo.bean.LocalChatMessage
import io.coderf.arklab.demo.database.LocalChatDatabase
import io.coderf.arklab.demo.repository.LocalChatPagingRepositoryImpl
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Room + Paging3 本地发消息 Demo（协程栈）：insert 后 [refreshData] 重建 PagingSource。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/13
 */
@HiltViewModel
class DemoLocalChatPagingViewModel @Inject constructor(
    application: Application
) : PagingViewModel<LocalChatMessage, RoomPagingQuery>(
    application,
    LocalChatPagingRepositoryImpl(
        LocalChatDatabase.getInstance(application).localChatMessageDao
    )
) {

    companion object {
        private const val SEED_COUNT = 48
    }

    private val seedStarted = AtomicBoolean(false)

    @Suppress("UNCHECKED_CAST")
    private val chatRepository: LocalChatPagingRepositoryImpl
        get() = pagingRepository as LocalChatPagingRepositoryImpl

    override fun createPagingData(): LiveData<PagingData<LocalChatMessage>> {
        return Pager(pagingConfig) {
            RoomPagingSource(chatRepository, pagingQuery)
        }.liveData
    }

    override val startPage: Int
        get() = 0

    override fun createPagingQuery(): RoomPagingQuery {
        return RoomPagingQuery().apply { orderBy = "createdAt" }
    }

    /**
     * 空库时写入一批历史消息，便于演示向上翻页。
     */
    fun ensureSeedHistory(onReady: (() -> Unit)?, onError: ((Throwable) -> Unit)?) {
        if (!seedStarted.compareAndSet(false, true)) {
            onReady?.invoke()
            return
        }
        launchRequest(RequestOptions.silent()) {
            chatRepository.countAll().collectRequest(
                onSuccess = { count ->
                    if (count > 0L) {
                        onReady?.invoke()
                        return@collectRequest
                    }
                    chatRepository.insert(buildSeedMessages()).collectRequest(
                        onSuccess = { onReady?.invoke() },
                        onError = { error ->
                            seedStarted.set(false)
                            onError?.invoke(error.cause ?: Exception(error.message))
                        }
                    )
                },
                onError = { error ->
                    seedStarted.set(false)
                    onError?.invoke(error.cause ?: Exception(error.message))
                }
            )
        }
    }

    /** 本地发送一条消息 */
    fun sendMessage(
        text: String,
        onSuccess: (() -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        val content = text.trim()
        if (TextUtils.isEmpty(content)) return
        val message = LocalChatMessage(content, System.currentTimeMillis(), true)
        launchRequest(RequestOptions.silent()) {
            chatRepository.insert(message).collectRequest(
                onSuccess = {
                    refreshData()
                    onSuccess?.invoke()
                },
                onError = { error ->
                    onError?.invoke(error.cause ?: Exception(error.message))
                }
            )
        }
    }

    fun deleteMessage(item: LocalChatMessage, onResult: (RequestResult<Unit>) -> Unit) {
        launchRequest(RequestOptions.silent()) {
            chatRepository.delete(item).collectRequest(
                onSuccess = { onResult(RequestResult.Success(Unit)) },
                onError = { error -> onResult(RequestResult.Error(error)) }
            )
        }
    }

    private fun buildSeedMessages(): List<LocalChatMessage> {
        val now = System.currentTimeMillis()
        val peer = arrayOf(
            "你好，这是本地 Room 预置的历史消息。",
            "向上滑动可以加载更早的分页记录。",
            "底部输入框发送会 insert 到 Room，再刷新 Paging。",
            "不涉及真实网络通信，仅演示单条插入。",
            "Paging3 本身不支持随意插 item，落库后 refresh 即可。"
        )
        val mine = arrayOf(
            "收到，我先翻翻历史。",
            "明白，发送走本地库。",
            "这条也会按时间排到最底部。",
            "好的，继续。"
        )
        return List(SEED_COUNT) { i ->
            val createdAt = now - (SEED_COUNT - i) * 60_000L
            val isMine = i % 3 == 2
            val content = if (isMine) {
                mine[(i / 3) % mine.size]
            } else {
                peer[i % peer.size] + " (#${i + 1})"
            }
            LocalChatMessage(content, createdAt, isMine)
        }
    }
}
