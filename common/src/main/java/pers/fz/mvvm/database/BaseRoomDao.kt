package pers.fz.mvvm.database

import androidx.paging.PagingSource
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Query

/**
 * created by fz on 2024/11/1 16:28
 * describe:
 */
abstract class BaseRoomDao<T : Any> {

    /**
     * 获取表名一般来说是 实体类名名字，可以用反射实现但是不建议
     */
    abstract fun getTableName();

    /**
     * 获取表名，该方法不建议使用
     */
//    val tableName: String
//        get() {
//            val clazz = (javaClass.superclass.genericSuperclass as ParameterizedType)
//                .actualTypeArguments[0] as Class<*>
//            val tableName = clazz.simpleName
//            return tableName
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

    fun deleteAll(): Completable {
        val query = SimpleSQLiteQuery("delete from ${getTableName()}")
        return doDeleteAll(query)
    }

    fun findAll(): Flowable<List<T>> {
        val query = SimpleSQLiteQuery("select * from ${getTableName()}")
        return doFindAll(query)
    }

    fun find(id: Long): Single<T> {
        val query = SimpleSQLiteQuery("select * from ${getTableName()} where id = ?", arrayOf<Any>(id))
        return doFind(query)
    }
    fun find(id: String): Single<T> {
        val query = SimpleSQLiteQuery("select * from ${getTableName()} where id = ?", arrayOf<Any>(id))
        return doFind(query)
    }
    /**
     * [params] 列名
     * [value] 列的值
     */
    fun deleteByParams(params: String, value: String): Completable {
        val query = SimpleSQLiteQuery("delete from ${getTableName()} where $params='${value}'")
        return doDeleteByParams(query)
    }


    /**
     * 根据多个参数删除数据
     */
    fun deleteByParams(params: Map<String, Any>): Completable {
        val conditions = params.entries.joinToString(" and ") { entry ->
            if (entry.value is String) {
                "${entry.key}='${entry.value}'"
            } else {
                "${entry.key}=${entry.value}"
            }
        }
        val query = SimpleSQLiteQuery("delete from ${getTableName()} where $conditions")
        return doDeleteByParams(query)
    }

    /**
     * 分页查询，支持传入多个字段，但必须要按照顺序传入
     * key = value，key = value 的形式，一一对应（可以使用 stringbuilder 去构造一下，这里就不演示了）
     */
    fun doQueryByLimit(params: Map<String, Any>, limit: Int = 10, offset: Int = 0): Flowable<List<T>> {
        val conditions = params.entries.joinToString(" and ") { entry ->
            if (entry.value is String) {
                "${entry.key}='${entry.value}'"
            } else {
                "${entry.key}=${entry.value}"
            }
        }
        val query = SimpleSQLiteQuery("SELECT * FROM ${getTableName()} WHERE $conditions limit $limit offset $offset")
        return doQueryByLimit(query)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDesc(params: Map<String, Any>, limit: Int = 10, offset: Int = 10): Flowable<List<T>> {
        val conditions = params.entries.joinToString(" and ") { entry ->
            if (entry.value is String) {
                "${entry.key}='${entry.value}'"
            } else {
                "${entry.key}=${entry.value}"
            }
        }
        val query = SimpleSQLiteQuery("SELECT * FROM ${getTableName()} ORDER BY $conditions desc limit $limit offset $offset")
        return doQueryByLimit(query)
    }
    /**
     * 降序分页查询
     */
    fun doQueryByOrderAsc(params: Map<String, Any>, limit: Int = 10, offset: Int = 10): Flowable<List<T>> {
        val conditions = params.entries.joinToString(" and ") { entry ->
            if (entry.value is String) {
                "${entry.key}='${entry.value}'"
            } else {
                "${entry.key}=${entry.value}"
            }
        }
        val query = SimpleSQLiteQuery("SELECT * FROM ${getTableName()} ORDER BY $conditions asc limit $limit offset $offset")
        return doQueryByLimit(query)
    }

    @RawQuery
    protected abstract fun doFindAll(query: SupportSQLiteQuery): Flowable<List<T>>

    @RawQuery
    protected abstract  fun doFind(query: SupportSQLiteQuery): Single<T>

    @RawQuery
    protected abstract fun doDeleteAll(query: SupportSQLiteQuery): Completable

    @RawQuery
    protected abstract fun doDeleteByParams(query: SupportSQLiteQuery): Completable

    @RawQuery
    protected abstract fun doQueryByLimit(query: SupportSQLiteQuery): Flowable<List<T>>

    @RawQuery
    protected abstract fun doQueryByOrder(query: SupportSQLiteQuery): Flowable<List<T>>
}