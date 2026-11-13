package io.coderf.arklab.common.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import io.coderf.arklab.common.datasource.PagingSource
import io.coderf.arklab.common.repository.PagingRepositoryImpl
import io.coderf.arklab.core.bean.PagingQuery
import kotlinx.coroutines.launch

/**
 * 【Legacy】Rx 分页 ViewModel。查询参数由 [pagingQuery] 持有，经 PagingSource 快照传给 Repository。
 * 仓库由构造注入；首次刷新在 init 中延迟启动。
 * 更新 query 默认不自动请求，需显式 [refreshData] 或 [updatePagingQuery] 的 refresh=true。
 *
 * **错误 UI**：本路径不经 [io.coderf.arklab.common.base.BaseViewModel.launchRequest]；
 * 旧 Rx Source 依赖 Repository 上的 RequestUi（框架已不再 ensureRepository 自动注入）。
 * 新代码请用 [io.coderf.arklab.core.network.NetworkFlowPagingViewModel]
 *（默认 [io.coderf.arklab.core.network.NetworkPagingSource.onAppError] → Host）。
 * 若继续用本类：请自行 setRequestUi(Host) 或由页面 LoadState 处理错误。
 *
 * @param T  列表元素
 * @param Q  查询参数
 *
 * @author fz
 * @version 2.1
 * @since 1.0
 * @updated 2026/11/13
 */
abstract class PagingViewModel<T : Any, Q : PagingQuery>(
    application: Application,
    /**
     * 分页仓库：网络 Rx 栈为 [PagingRepositoryImpl]；Room 协程栈为 [io.coderf.arklab.core.db.RoomDataRepository]。
     * 默认 [createPagingData] 按 Rx 栈强转；Room Demo 请重写 [createPagingData]。
     */
    protected val pagingRepository: Any
) : BasePagingViewModel(application) {

    companion object {
        const val DEFAULT_START_PAGE = 1
        const val DEFAULT_PAGE_SIZE = 20
        const val DEFAULT_PREFETCH_DISTANCE = 3
    }

    private val itemsMediator = MediatorLiveData<PagingData<T>>()
    private var sourceLiveData: LiveData<PagingData<T>>? = null
    private var _pagingQuery: Q? = null

    init {
        viewModelScope.launch {
            refreshData()
        }
    }

    fun getItems(): LiveData<PagingData<T>> = itemsMediator

    /**
     * 当前查询参数；首次访问时通过 [createPagingQuery] 初始化。
     */
    var pagingQuery: Q
        get() {
            if (_pagingQuery == null) {
                _pagingQuery = createPagingQuery()
            }
            return _pagingQuery!!
        }
        set(value) {
            _pagingQuery = value
        }

    protected abstract fun createPagingQuery(): Q

    @JvmOverloads
    fun updatePagingQuery(query: Q, refresh: Boolean = false) {
        pagingQuery = query
        if (refresh) {
            refreshData()
        }
    }

    @Suppress("UNCHECKED_CAST")
    open fun createPagingData(): LiveData<PagingData<T>> {
        val query = pagingQuery
        val repo = pagingRepository as PagingRepositoryImpl<*, T, Q>
        return Pager(pagingConfig) {
            PagingSource(repo, startPage, query)
        }.liveData
    }

    override fun refreshData() {
        sourceLiveData?.let { itemsMediator.removeSource(it) }
        val source = createPagingData()
        sourceLiveData = source
        itemsMediator.addSource(source) { itemsMediator.value = it }
    }

    open val startPage: Int
        get() = DEFAULT_START_PAGE

    open val pagingConfig: PagingConfig
        get() = PagingConfig(
            DEFAULT_PAGE_SIZE,
            DEFAULT_PREFETCH_DISTANCE,
            false,
            DEFAULT_PAGE_SIZE
        )
}
