package io.coderf.arklab.demo.viewmodel

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.viewmodel.PagingViewModel
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.bean.RoomPagingQuery
import io.coderf.arklab.core.db.RoomPagingSource
import io.coderf.arklab.demo.bean.LocalChatMessage
import io.coderf.arklab.demo.database.LocalChatDatabase
import io.coderf.arklab.demo.repository.LocalChatPagingRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Room + Paging3 本地发消息 Demo（协程栈）：insert 后 [refreshData] 重建 PagingSource。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/10/28
 */
@HiltViewModel
class DemoLocalChatPagingViewModel @Inject constructor(
    application: Application
) : PagingViewModel<LocalChatPagingRepositoryImpl, LocalChatMessage, RoomPagingQuery>(application) {

    companion object {
        private const val SEED_COUNT = 48
    }

    private val seedStarted = AtomicBoolean(false)

    override fun createPagingData(): LiveData<PagingData<LocalChatMessage>> {
        return Pager(pagingConfig) {
            RoomPagingSource(iRepository, pagingQuery)
        }.liveData
    }

    override fun createRepository(): LocalChatPagingRepositoryImpl {
        return LocalChatPagingRepositoryImpl(
            LocalChatDatabase.getInstance(getApplication()).localChatMessageDao
        )
    }

    override fun getStartPage(): Int = 0

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
        viewModelScope.launch {
            when (val countResult = iRepository.countAll().first()) {
                is RequestResult.Success -> {
                    if (countResult.data > 0L) {
                        onReady?.invoke()
                        return@launch
                    }
                    when (val insertResult = iRepository.insert(buildSeedMessages()).first()) {
                        is RequestResult.Success -> onReady?.invoke()
                        is RequestResult.Error -> {
                            seedStarted.set(false)
                            onError?.invoke(
                                insertResult.error.cause ?: Exception(insertResult.error.message)
                            )
                        }
                    }
                }
                is RequestResult.Error -> {
                    seedStarted.set(false)
                    onError?.invoke(countResult.error.cause ?: Exception(countResult.error.message))
                }
            }
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
        viewModelScope.launch {
            when (val result = iRepository.insert(message).first()) {
                is RequestResult.Success -> {
                    refreshData()
                    onSuccess?.invoke()
                }
                is RequestResult.Error -> {
                    onError?.invoke(result.error.cause ?: Exception(result.error.message))
                }
            }
        }
    }

    fun deleteMessage(item: LocalChatMessage, onResult: (RequestResult<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(iRepository.delete(item).first())
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
