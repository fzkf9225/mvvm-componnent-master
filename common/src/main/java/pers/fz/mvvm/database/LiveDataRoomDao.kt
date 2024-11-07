package pers.fz.mvvm.database

import androidx.lifecycle.LiveData
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import pers.fz.mvvm.api.ApiRetrofit
import pers.fz.mvvm.bean.BaseDaoBean
import pers.fz.mvvm.util.log.LogUtil

/**
 * created by fz on 2024/11/1 16:28
 * describe:
 */
abstract class LiveDataRoomDao<T : Any> : BaseRoomDao<T>() {

    fun deleteAll(): LiveData<List<T>> {
        val query = SimpleSQLiteQuery("delete from ${getTableName()}")
        return doQueryByParams(query)
    }

    /**
     * [params] 列名
     * [value] 列的值
     */
    fun deleteByParams(params: String, value: String): LiveData<List<T>> {
        val query = SimpleSQLiteQuery("delete from ${getTableName()} where $params='${value}'")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryByParams(query)
    }

    /**
     * 根据多个参数删除数据
     */
    fun deleteByParams(params: Map<String, Any>): LiveData<List<T>> {
        val conditions = params.entries.joinToString(" and ") { entry ->
            if (entry.value is String) {
                "${entry.key}='${entry.value}'"
            } else {
                "${entry.key}=${entry.value}"
            }
        }
        val query = SimpleSQLiteQuery("delete from ${getTableName()} where $conditions")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryByParams(query)
    }

    fun findAll(): LiveData<List<T>> {
        val query = SimpleSQLiteQuery("select * from ${getTableName()}")
        return doQueryByParams(query)
    }

    fun findInfoById(id: Long): LiveData<T> {
        val query =
            SimpleSQLiteQuery("select * from ${getTableName()} where id = ?", arrayOf<Any>(id))
        return doFind(query)
    }

    fun findInfoById(id: String): LiveData<T> {
        val query =
            SimpleSQLiteQuery("select * from ${getTableName()} where id = ?", arrayOf<Any>(id))
        return doFind(query)
    }

    /**
     * 分页查询，支持传入多个字段，但必须要按照顺序传入
     * key = value，key = value 的形式，一一对应（可以使用 stringbuilder 去构造一下，这里就不演示了）
     */
    fun doQueryByLimit(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> {
        val conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        val order = if (orderBy.isNullOrEmpty()) "" else " ORDER BY id "
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order limit $limit offset $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryByParams(query)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 10
    ): LiveData<List<T>> {
        val conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY id desc "
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order  LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryByParams(query)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 10
    ): LiveData<List<T>> {
        var conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        if (!keywordsKey.isNullOrEmpty()) {
            conditions =
                if (conditions.isBlank()) ("$conditions WHERE " + keywordsKey.joinToString(" or ") { entry ->
                    "$entry like '%${keywords ?: ""}%'"
                }) else ("$conditions and (" + keywordsKey.joinToString(" or ") { entry ->
                    "$entry like '%${keywords ?: ""}%'"
                } + ")")
        }
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY id DESC "
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order  LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryByParams(query)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderAsc(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 10
    ): LiveData<List<T>> {
        val conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY id asc "
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions  $order LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryByParams(query)
    }

    /**
     * 占位方法如果你需要调用这个方法最好是重写一下BaseDaoBean修改为你的实体表，不写observedEntities会无法编译
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doFind(query: SupportSQLiteQuery): LiveData<T>

    /**
     * 占位方法如果你需要调用这个方法最好是重写一下BaseDaoBean修改为你的实体表，不写observedEntities会无法编译
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doQueryByParams(query: SupportSQLiteQuery): LiveData<List<T>>

}