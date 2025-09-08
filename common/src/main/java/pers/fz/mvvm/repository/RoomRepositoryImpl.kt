package pers.fz.mvvm.repository

import androidx.lifecycle.LiveData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import pers.fz.mvvm.base.BaseException
import pers.fz.mvvm.base.BaseRepository
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.dao.BaseRoomDao
import java.util.concurrent.TimeUnit

/**
 * created by fz on 2024/11/1 16:12
 * describe:
 */
open class RoomRepositoryImpl<T : Any, DB : BaseRoomDao<T>, BV : BaseView?>(
    private val roomDao: DB,
    baseView: BV
) : BaseRepository<BV>(baseView) {
    var enableDynamicEllipsis : Boolean = false

    fun getRoomDao() = roomDao
    /**
     * 添加单个对象
     */
    fun insert( showLoading: Boolean = false,obj: T): Completable {
        return roomDao.insert(obj)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在插入数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 添加对象集合
     */
    fun insert(showLoading: Boolean = false,objs: List<T>): Completable {
        return roomDao.insert(objs.toList())
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在插入数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally { baseView?.hideLoading() }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 根据对象中的主键删除（主键是自动增长的，无需手动赋值）
     */
    fun delete(obj: T, showLoading: Boolean = false): Completable {
        return roomDao.delete(obj)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在删除数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }
    /**
     * 根据对象中的主键更新（主键是自动增长的，无需手动赋值）
     */
    fun update(showLoading: Boolean = false,obj: T): Completable {
        return roomDao.update(obj)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在更新数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }
    /**
     * 根据对象中的主键更新（主键是自动增长的，无需手动赋值）
     */
    fun update(showLoading: Boolean = false,obj: List<T>): Completable {
        return roomDao.update(obj)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在更新数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 删除所有数据
     */
    fun deleteAll(showLoading: Boolean = false): Flowable<List<T>> {
        return roomDao.deleteAll()
            .defaultIfEmpty(emptyList())
            .doOnNext {
                if (it.isEmpty()) {
                    throw BaseException(
                        BaseException.DELETE_SUCCESS_MSG,
                        BaseException.DELETE_SUCCESS
                    )
                }
                //下面这行代码是因为room返回flowable的时候他不执行doFinally
                if (!showLoading) {
                    return@doOnNext
                }
                baseView?.hideLoading()
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addSubscription(disposable)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在删除数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 根据参数删除数据
     */
    fun deleteByParams(
        params: String, value: String,
        showLoading: Boolean = false
    ): Flowable<List<T>> {
        return roomDao.deleteByParams(params, value)
            .defaultIfEmpty(emptyList())
            .doOnNext {
                if (it.isEmpty()) {
                    throw BaseException(
                        BaseException.DELETE_SUCCESS_MSG,
                        BaseException.DELETE_SUCCESS
                    )
                }
                //下面这行代码是因为room返回flowable的时候他不执行doFinally
                if (!showLoading) {
                    return@doOnNext
                }
                baseView?.hideLoading()
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在删除数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 根据参数删除数据
     */
    fun deleteByParams(
        params: Map<String, Any>,
        showLoading: Boolean = false
    ): Flowable<List<T>> {
        return roomDao.deleteByParams(params)
            .defaultIfEmpty(emptyList())
            .doOnNext {
                if (it.isEmpty()) {
                    throw BaseException(
                        BaseException.DELETE_SUCCESS_MSG,
                        BaseException.DELETE_SUCCESS
                    )
                }
                //下面这行代码是因为room返回flowable的时候他不执行doFinally
                if (!showLoading) {
                    return@doOnNext
                }
                baseView?.hideLoading()
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在删除数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 查询所有数据
     */
    fun findAll(showLoading: Boolean = false): Flowable<List<T>> {
        return roomDao.findAll()
            .defaultIfEmpty(emptyList())
            .doOnNext {
                if (it.isEmpty()) {
                    throw BaseException(BaseException.NOT_FOUND_MSG, BaseException.NOT_FOUND)
                }
                //下面这行代码是因为room返回flowable的时候他不执行doFinally
                if (!showLoading) {
                    return@doOnNext
                }
                baseView?.hideLoading()
            }
            .timeout(30, TimeUnit.SECONDS)
            .switchIfEmpty { throw RuntimeException("暂未查询到数据") }
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 根据ID查询单个对象
     */
    fun findInfoById(
        id: Long,
        showLoading: Boolean = false
    ): Single<T> {
        return roomDao.findInfoById(id)
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 根据ID查询单个对象
     */
    fun findInfoById(
        id: String,
        showLoading: Boolean = false
    ): Single<T> {
        return roomDao.findInfoById(id)
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }
    /**
     * 根据ID查询单个对象
     */
    fun findInfoById(
        primaryKey: String,
        id: Long,
        showLoading: Boolean = false
    ): Single<T> {
        return roomDao.findInfoById(primaryKey,id)
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 根据ID查询单个对象
     */
    fun findInfoById(
        primaryKey: String,
        id: String,
        showLoading: Boolean = false
    ): Single<T> {
        return roomDao.findInfoById(primaryKey,id)
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }
    /**
     * 分页查询
     */
    fun doQueryByLimit(
        params: Map<String, Any>,
        orderBy: String,
        showLoading: Boolean = false,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> {
        return roomDao.doQueryByLimit(params, orderBy, limit, offset)
            .defaultIfEmpty(emptyList())
            .doOnNext {
                if (it.isEmpty()) {
                    throw BaseException(BaseException.NOT_FOUND_MSG, BaseException.NOT_FOUND)
                }
                //下面这行代码是因为room返回flowable的时候他不执行doFinally
                if (!showLoading) {
                    return@doOnNext
                }
                baseView?.hideLoading()
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        showLoading: Boolean = false,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> {
        return roomDao.doQueryByOrderDesc(params, keywordsKey, keywords, orderBy, limit, offset)
            .defaultIfEmpty(emptyList())
            .doOnNext {
                if (it.isEmpty()) {
                    throw BaseException(BaseException.NOT_FOUND_MSG, BaseException.NOT_FOUND)
                }
                //下面这行代码是因为room返回flowable的时候他不执行doFinally
                if (!showLoading) {
                    return@doOnNext
                }
                baseView?.hideLoading()
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        orderBy: String,
        showLoading: Boolean = false,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> {
        return roomDao.doQueryByOrderDesc(params, orderBy, limit, offset)
            .defaultIfEmpty(emptyList())
            .doOnNext {
                if (it.isEmpty()) {
                    throw BaseException(BaseException.NOT_FOUND_MSG, BaseException.NOT_FOUND)
                }
                //下面这行代码是因为room返回flowable的时候他不执行doFinally
                if (!showLoading) {
                    return@doOnNext
                }
                baseView?.hideLoading()
            }
            .timeout(30, TimeUnit.SECONDS)
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 降序分页查询
     */
    fun findPageList(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> {
        return roomDao.findPageList(params, orderBy, limit, offset)
    }

    /**
     * 降序分页查询
     */
    fun findPageList(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> {
        return roomDao.findPageList(params, keywordsKey, keywords, orderBy, limit, offset)
    }

    /**
     * 降序分页查询
     */
    fun findPageListAsc(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> {
        return roomDao.findPageListAsc(params, orderBy, limit, offset)
    }

    /**
     * 降序分页查询
     */
    fun findPageListAsc(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> {
        return roomDao.findPageListAsc(params, keywordsKey, keywords, orderBy, limit, offset)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderAsc(
        params: Map<String, Any>,
        orderBy: String,
        showLoading: Boolean = false,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> {
        return roomDao.doQueryByOrderAsc(params, orderBy, limit, offset)
            .defaultIfEmpty(emptyList())
            .doOnNext {
                if (it.isEmpty()) {
                    throw BaseException(BaseException.NOT_FOUND_MSG, BaseException.NOT_FOUND)
                }
                //下面这行代码是因为room返回flowable的时候他不执行doFinally
                if (!showLoading) {
                    return@doOnNext
                }
                baseView?.hideLoading()
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...",enableDynamicEllipsis)
            }
            .doFinally {
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }


    /**
     * 删除所有数据
     */
    fun deleteAllLiveData(): LiveData<List<T>> {
        return roomDao.deleteAllLiveData()
    }

    /**
     * 根据参数删除数据
     */
    fun deleteByParamsLiveData(
        params: String, value: String,
    ): LiveData<List<T>> {
        return roomDao.deleteByParamsLiveData(params, value)
    }

    /**
     * 根据参数删除数据
     */
    fun deleteByParamsLiveData(
        params: Map<String, Any>,
    ): LiveData<List<T>> {
        return roomDao.deleteByParamsLiveData(params)
    }

    /**
     * 查询所有数据
     */
    fun findAllLiveData(): LiveData<List<T>> {
        return roomDao.findAllLiveData()
    }

    /**
     * 根据ID查询单个对象
     */
    fun findInfoByIdLiveData(
        id: Long,
    ): LiveData<T> {
        return roomDao.findInfoByIdLiveData(id)
    }

    /**
     * 根据ID查询单个对象
     */
    fun findInfoByIdLiveData(
        id: String,
    ): LiveData<T> {
        return roomDao.findInfoByIdLiveData(id)
    }

    /**
     * 分页查询
     */
    fun doQueryByLimitLiveData(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> {
        return roomDao.doQueryByLimitLiveData(params, orderBy, limit, offset)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDescLiveData(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> {
        return roomDao.doQueryByOrderDescLiveData(
            params,
            keywordsKey,
            keywords,
            orderBy,
            limit,
            offset
        )
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDescLiveData(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> {
        return roomDao.doQueryByOrderDescLiveData(params, orderBy, limit, offset)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderAscLiveData(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> {
        return roomDao.doQueryByOrderAscLiveData(params, orderBy, limit, offset)
    }
}

