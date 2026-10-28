package io.coderf.arklab.demo.viewmodel

import android.app.Application
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
import io.coderf.arklab.demo.bean.Person
import io.coderf.arklab.demo.database.PersonDatabase
import io.coderf.arklab.demo.repository.RoomPagingRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Room + Paging3 Demo（协程栈 [RoomPagingSource] / [RoomPagingRepositoryImpl]）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/10/28
 */
@HiltViewModel
class DemoRoomPagingViewModel @Inject constructor(
    application: Application
) : PagingViewModel<RoomPagingRepositoryImpl, Person, RoomPagingQuery>(application) {

    fun setKeywords(keywords: String?) {
        pagingQuery.keywords = keywords
    }

    override fun createPagingData(): LiveData<PagingData<Person>> {
        return Pager(pagingConfig) {
            RoomPagingSource(iRepository, pagingQuery)
        }.liveData
    }

    override fun createRepository(): RoomPagingRepositoryImpl {
        return RoomPagingRepositoryImpl(
            PersonDatabase.getInstance(getApplication()).personDao
        )
    }

    override fun getStartPage(): Int = 0

    override fun createPagingQuery(): RoomPagingQuery {
        return RoomPagingQuery().apply {
            keywordsKey = mutableSetOf("name")
            orderBy = "id"
        }
    }

    /** 删除一条；成功后由页面刷新列表 */
    fun deletePerson(person: Person, onResult: (RequestResult<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(iRepository.delete(person).first())
        }
    }
}
