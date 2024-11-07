package pers.fz.mvvm.repository

import androidx.lifecycle.LiveData
import io.reactivex.rxjava3.disposables.Disposable
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.database.LiveDataRoomDao

/**
 * created by fz on 2024/11/1 16:12
 * describe:
 */
open class LiveDataRoomRepositoryImpl<T : Any, DB : LiveDataRoomDao<T>, BV : BaseView?>(
    private val roomDao: DB,
    private val baseView: BV
) : RoomRepositoryImpl<T, DB, BV>(roomDao, baseView) {

    override fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun removeDisposable() {
        //默认取消所有订阅，但不会导致正在进行的任务终止，而是等待它们完成，仅仅只是取消订阅关系而已
        compositeDisposable.clear()
        //默认取消所有订阅，并取消所有正在进行的任务
//            compositeDisposable.dispose();
    }

    /**
     * 删除所有数据
     */
    fun deleteAll(): LiveData<List<T>> {
        return roomDao.deleteAll()
    }

    /**
     * 根据参数删除数据
     */
    fun deleteByParams(
        params: String, value: String,
    ): LiveData<List<T>> {
        return roomDao.deleteByParams(params, value)
    }

    /**
     * 根据参数删除数据
     */
    fun deleteByParams(
        params: Map<String, Any>,
    ): LiveData<List<T>> {
        return roomDao.deleteByParams(params)
    }

    /**
     * 查询所有数据
     */
    fun findAll(): LiveData<List<T>> {
        return roomDao.findAll()
    }

    /**
     * 根据ID查询单个对象
     */
    fun findInfoById(
        id: Long,
    ): LiveData<T> {
        return roomDao.findInfoById(id)
    }

    /**
     * 根据ID查询单个对象
     */
    fun findInfoById(
        id: String,
    ): LiveData<T> {
        return roomDao.findInfoById(id)
    }

    /**
     * 分页查询
     */
    fun doQueryByLimit(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> {
        return roomDao.doQueryByLimit(params, orderBy, limit, offset)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> {
        return roomDao.doQueryByOrderDesc(params, keywordsKey, keywords, orderBy, limit, offset)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> {
        return roomDao.doQueryByOrderDesc(params, orderBy, limit, offset)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderAsc(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> {
        return roomDao.doQueryByOrderAsc(params, orderBy, limit, offset)
    }
}

