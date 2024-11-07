package pers.fz.mvvm.database

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.rxjava3.core.Completable

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
     * 添加单个对象
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Transaction
    abstract fun insert(obj: T): Completable

    /**
     * 添加数组对象数据
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Transaction
    abstract fun insert(vararg objs: T): Completable

    /**
     * 添加对象集合
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Transaction
    abstract fun insert(personList: List<T>): Completable

    /**
     * 根据对象中的主键删除（主键是自动增长的，无需手动赋值）
     */
    @Delete
    @Transaction
    abstract fun delete(obj: T): Completable

    /**
     * 根据对象中的主键更新（主键是自动增长的，无需手动赋值）
     */
    @Update
    @Transaction
    abstract fun update(vararg obj: T): Completable


}