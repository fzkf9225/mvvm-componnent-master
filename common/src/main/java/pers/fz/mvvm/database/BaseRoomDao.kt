package pers.fz.mvvm.database

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import pers.fz.mvvm.api.ApiRetrofit
import pers.fz.mvvm.bean.BaseDaoBean
import pers.fz.mvvm.util.log.LogUtil

/**
 * created by fz on 2024/11/1 16:28
 * describe:
 */
abstract class BaseRoomDao<T : Any> {

    /**
     * 获取表名一般来说是 实体类名名字，可以用反射实现但是不建议
     */
    abstract fun getTableName(): String;

    /**
     * 获取表名，该方法不建议使用
     */
//    val tableName: String
//        get() {
//            val clazz = (javaClass.superclass.genericSuperclass as ParameterizedType)
//                .actualTypeArguments[0] as Class<*>
//            return clazz.simpleName
//        }
    /**
     * 添加单个对象
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insert(obj: T): Completable

    /**
     * 添加数组对象数据
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insert(vararg objs: T): Completable

    /**
     * 添加对象集合
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insert(personList: List<T>): Completable

    /**
     * 根据对象中的主键删除（主键是自动增长的，无需手动赋值）
     */
    @Delete
    abstract fun delete(obj: T): Completable

    /**
     * 根据对象中的主键更新（主键是自动增长的，无需手动赋值）
     */
    @Update
    abstract fun update(vararg obj: T): Completable

    fun deleteAll(): Maybe<List<T>> {
        val query = SimpleSQLiteQuery("delete from ${getTableName()}")
        return doDeleteByParams(query)
    }

    fun findAll(): Flowable<List<T>> {
        val query = SimpleSQLiteQuery("select * from ${getTableName()}")
        return doQueryByParams(query)
    }

    fun find(id: Long): Single<T> {
        val query =
            SimpleSQLiteQuery("select * from ${getTableName()} where id = ?", arrayOf<Any>(id))
        return doFind(query)
    }

    fun find(id: String): Single<T> {
        val query =
            SimpleSQLiteQuery("select * from ${getTableName()} where id = ?", arrayOf<Any>(id))
        return doFind(query)
    }

    /**
     * [params] 列名
     * [value] 列的值
     */
    fun deleteByParams(params: String, value: String): Maybe<List<T>> {
        val query = SimpleSQLiteQuery("delete from ${getTableName()} where $params='${value}'")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doDeleteByParams(query)
    }

    /**
     * 根据多个参数删除数据
     */
    fun deleteByParams(params: Map<String, Any>): Maybe<List<T>> {
        val conditions = params.entries.joinToString(" and ") { entry ->
            if (entry.value is String) {
                "${entry.key}='${entry.value}'"
            } else {
                "${entry.key}=${entry.value}"
            }
        }
        val query = SimpleSQLiteQuery("delete from ${getTableName()} where $conditions")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doDeleteByParams(query)
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
    ): Flowable<List<T>> {
        val conditions = if (params.isEmpty()) "" else (" WHERE "+params.entries.joinToString(" and ") { entry ->
            if (entry.value is String) {
                "${entry.key}='${entry.value}'"
            } else {
                "${entry.key}=${entry.value}"
            }
        })
        val order = if (orderBy.isNullOrEmpty()) "" else " ORDER BY id "
        val query = SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order limit $limit offset $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryByParams(query)
    }

    /**
     * 降序分页查询
     */
    fun findPageList(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 10
    ): List<T> {
        val conditions = if (params.isEmpty()) "" else (" WHERE "+params.entries.joinToString(" and ") { entry ->
            if (entry.value is String) {
                "${entry.key}='${entry.value}'"
            } else {
                "${entry.key}=${entry.value}"
            }
        })
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY id DESC"
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return findPageList(query)
    }

    /**
     * 降序分页查询
     */
    fun findPageListAsc(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 10
    ): List<T> {
        val conditions = if (params.isEmpty()) "" else (" WHERE "+params.entries.joinToString(" and ") { entry ->
            if (entry.value is String) {
                "${entry.key}='${entry.value}'"
            } else {
                "${entry.key}=${entry.value}"
            }
        })
        val order = if (orderBy.isNullOrEmpty()) "" else " ORDER BY id ASC"
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return findPageList(query)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 10
    ): Flowable<List<T>> {
        val conditions = if (params.isEmpty()) "" else (" WHERE "+params.entries.joinToString(" and ") { entry ->
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
    fun doQueryByOrderAsc(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 10
    ): Flowable<List<T>> {
        val conditions = if (params.isEmpty()) "" else (" WHERE "+params.entries.joinToString(" and ") { entry ->
            if (entry.value is String) {
                "${entry.key}='${entry.value}'"
            } else {
                "${entry.key}=${entry.value}"
            }
        })
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY id asc "
        val query = SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions  $order LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryByParams(query)
    }

    /**
     * 占位方法如果你需要调用这个方法最好是重写一下BaseDaoBean修改为你的实体表，不写observedEntities会无法编译
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doFind(query: SupportSQLiteQuery): Single<T>

    /**
     * 占位方法如果你需要调用这个方法最好是重写一下BaseDaoBean修改为你的实体表，不写observedEntities会无法编译
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doDeleteByParams(query: SupportSQLiteQuery): Maybe<List<T>>
    /**
     * 占位方法如果你需要调用这个方法最好是重写一下BaseDaoBean修改为你的实体表，不写observedEntities会无法编译
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doQueryByParams(query: SupportSQLiteQuery): Flowable<List<T>>
    /**
     * 占位方法如果你需要调用这个方法最好是重写一下BaseDaoBean修改为你的实体表，不写observedEntities会无法编译
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun findPageList(query: SupportSQLiteQuery): List<T>

}