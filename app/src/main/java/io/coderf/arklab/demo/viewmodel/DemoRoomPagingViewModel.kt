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
import io.coderf.arklab.demo.bean.Person
import io.coderf.arklab.demo.database.PersonDatabase
import io.coderf.arklab.demo.repository.RoomPagingRepositoryImpl
import javax.inject.Inject

/**
 * Room + Paging3 Demo（协程栈 [RoomPagingSource] / [RoomPagingRepositoryImpl]）。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/13
 */
@HiltViewModel
class DemoRoomPagingViewModel @Inject constructor(
    application: Application
) : PagingViewModel<Person, RoomPagingQuery>(
    application,
    RoomPagingRepositoryImpl(
        PersonDatabase.getInstance(application).personDao
    )
) {

    @Suppress("UNCHECKED_CAST")
    private val personRepository: RoomPagingRepositoryImpl
        get() = pagingRepository as RoomPagingRepositoryImpl

    fun setKeywords(keywords: String?) {
        pagingQuery.keywords = keywords
    }

    override fun createPagingData(): LiveData<PagingData<Person>> {
        return Pager(pagingConfig) {
            RoomPagingSource(personRepository, pagingQuery)
        }.liveData
    }

    override val startPage: Int
        get() = 0

    override fun createPagingQuery(): RoomPagingQuery {
        return RoomPagingQuery().apply {
            keywordsKey = mutableSetOf("name")
            orderBy = "id"
        }
    }

    /** 删除一条；成功后由页面刷新列表（silent 无遮罩） */
    fun deletePerson(person: Person, onResult: (RequestResult<Unit>) -> Unit) {
        launchRequest(RequestOptions.silent()) {
            personRepository.delete(person).collectRequest(
                onSuccess = { onResult(RequestResult.Success(Unit)) },
                onError = { error -> onResult(RequestResult.Error(error)) }
            )
        }
    }
}
