package io.coderf.arklab.core.db

import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.common.dao.BaseRoomDao
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.request.NoOpRequestUi
import io.coderf.arklab.core.request.RequestUi
import kotlinx.coroutines.flow.Flow

/**
 * 带实体类型的 Room 协程数据仓库：在 [BaseRoomRepository] 上封装常用 CRUD。
 *
 * 与 Rx 栈 [io.coderf.arklab.common.repository.RoomRepositoryImpl] **并行独立**，内部只调 Dao 的
 * Room 原生 `*Suspend`，无 Rx / blocking 桥接。
 *
 * ```
 * class PersonRepository(dao: PersonDao) : RoomDataRepository<Person, PersonDao>(dao)
 *
 * // ViewModel
 * repository.insert(person, RoomRequestOptions.withLoading("保存中..."))
 *     .collect { result -> result.onSuccess { }.onError { } }
 * ```
 *
 * @param T  实体类型
 * @param DB 继承 [BaseRoomDao] 的 Dao
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/28
 */
open class RoomDataRepository<T : Any, DB : BaseRoomDao<T>>(
    private val roomDao: DB,
    requestUi: RequestUi = NoOpRequestUi
) : BaseRoomRepository(requestUi) {

    fun getRoomDao(): DB = roomDao

    private fun <R> runOp(
        options: RoomRequestOptions,
        operation: String,
        payload: Any? = null,
        block: suspend () -> R
    ): Flow<RequestResult<R>> =
        executeInternal(options, roomDao.getTableName(), operation, payload, block)

    // ==================== 写 ====================

    fun insert(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Unit>> =
        runOp(options, "INSERT", obj) { roomDao.insertSuspend(obj) }

    fun insert(
        objs: List<T>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Unit>> =
        runOp(options, "INSERT_LIST", objs) { roomDao.insertSuspend(objs) }

    fun upsert(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Unit>> =
        runOp(options, "UPSERT", obj) { roomDao.upsertSuspend(obj) }

    fun upsert(
        objs: List<T>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Unit>> =
        runOp(options, "UPSERT_LIST", objs) { roomDao.upsertSuspend(objs) }

    fun update(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Unit>> =
        runOp(options, "UPDATE", obj) { roomDao.updateSuspend(obj) }

    fun update(
        objs: List<T>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Unit>> =
        runOp(options, "UPDATE_LIST", objs) { roomDao.updateSuspend(objs) }

    fun delete(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Unit>> =
        runOp(options, "DELETE", obj) { roomDao.deleteSuspend(obj) }

    fun deleteAllCount(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Int>> =
        runOp(options, "DELETE_ALL") { roomDao.deleteAllRowsSuspend() }

    fun deleteByParamsCount(
        params: Map<String, Any>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Int>> =
        runOp(options, "DELETE_BY_PARAMS", params) { roomDao.deleteByParamsRowsSuspend(params) }

    fun deleteByParamsCount(
        column: String,
        value: Any,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Int>> =
        runOp(options, "DELETE_BY_PARAMS", mapOf(column to value)) {
            roomDao.deleteByParamsRowsSuspend(column, value)
        }

    // ==================== 读 ====================

    fun findAll(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<List<T>>> =
        runOp(options, "FIND_ALL") { roomDao.findAllSuspend() }

    fun findInfoById(
        id: String,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<T>> =
        runOp(options, "FIND_BY_ID", id) { roomDao.findInfoByIdSuspend(id) }

    fun findInfoById(
        id: Long,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<T>> =
        runOp(options, "FIND_BY_ID", id) { roomDao.findInfoByIdSuspend(id) }

    fun findInfoById(
        primaryKey: String,
        id: String,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<T>> =
        runOp(options, "FIND_BY_ID", mapOf(primaryKey to id)) {
            roomDao.findInfoByIdSuspend(primaryKey, id)
        }

    fun findInfoById(
        primaryKey: String,
        id: Long,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<T>> =
        runOp(options, "FIND_BY_ID", mapOf(primaryKey to id)) {
            roomDao.findInfoByIdSuspend(primaryKey, id)
        }

    fun findByIn(
        column: String,
        values: Collection<*>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<List<T>>> =
        runOp(options, "FIND_BY_IN", mapOf(column to values)) {
            roomDao.findByInSuspend(column, values)
        }

    fun countAll(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Long>> =
        runOp(options, "COUNT_ALL") { roomDao.countAllSuspend() }

    fun count(
        params: Map<String, Any>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Long>> =
        runOp(options, "COUNT", params) { roomDao.countSuspend(params) }

    fun exists(
        params: Map<String, Any>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Boolean>> =
        runOp(options, "EXISTS", params) { roomDao.existsSuspend(params) }

    fun findPageList(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<List<T>>> =
        runOp(options, "FIND_PAGE", params) {
            roomDao.findPageListSuspend(params, orderBy, limit, offset)
        }

    fun findPageList(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<List<T>>> =
        runOp(options, "FIND_PAGE", params) {
            roomDao.findPageListSuspend(params, keywordsKey, keywords, orderBy, limit, offset)
        }

    fun findPageListAsc(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<List<T>>> =
        runOp(options, "FIND_PAGE_ASC", params) {
            roomDao.findPageListAscSuspend(params, orderBy, limit, offset)
        }
}
