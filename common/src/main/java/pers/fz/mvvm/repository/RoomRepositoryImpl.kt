package pers.fz.mvvm.repository

import com.google.android.gms.common.api.Api
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import pers.fz.mvvm.api.ApiRetrofit
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.database.BaseRoomDao
import pers.fz.mvvm.util.log.LogUtil
import java.util.concurrent.TimeUnit

/**
 * created by fz on 2024/11/1 16:12
 * describe:
 */
open class RoomRepositoryImpl<T : Any, DB : BaseRoomDao<T>, BV : BaseView?>(
    private val roomDao: DB,
    private val baseView: BV
) : IRepository {
    /**
     * 离开页面，是否取消网络
     */
    private val compositeDisposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    private var subscription: Subscription? = null

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
     * 添加单个对象
     */
    fun insert(obj: T, showLoading: Boolean = false): Completable {
        return roomDao.insert(obj)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在插入数据,请稍后...")
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
     * 添加数组对象数据
     */
    fun insert(vararg objs: T, showLoading: Boolean = false): Completable {
        return roomDao.insert(*objs)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在插入数据,请稍后...")
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
    fun insert(objs: List<T>, showLoading: Boolean = false): Completable {
        return roomDao.insert(objs)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在插入数据,请稍后...")
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
                baseView?.showLoading("正在删除数据,请稍后...")
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
    fun deleteAll(showLoading: Boolean = false): Maybe<List<T>> {
        return roomDao.deleteAll()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在删除数据,请稍后...")
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
    fun update(vararg obj: T, showLoading: Boolean = false): Completable {
        return roomDao.update(*obj)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在更新数据,请稍后...")
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
            .map {
                if (it.isNullOrEmpty())
                    throw RuntimeException("暂未查询到数据")
                else
                    return@map it
            }
            .timeout(60, TimeUnit.SECONDS)
            .switchIfEmpty { throw RuntimeException("暂未查询到数据") }
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                this@RoomRepositoryImpl.subscription = subscription
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...")
            }
            .doFinally {
                this@RoomRepositoryImpl.subscription = null
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
    fun find(
        id: Long,
        showLoading: Boolean = false
    ): Single<T> {
        return roomDao.find(id)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...")
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
    fun find(
        id: String,
        showLoading: Boolean = false
    ): Single<T> {
        return roomDao.find(id)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...")
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
    ): Maybe<List<T>> {
        return roomDao.deleteByParams(params, value)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在删除数据,请稍后...")
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
    ): Maybe<List<T>> {
        return roomDao.deleteByParams(params)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在删除数据,请稍后...")
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
            .map {
                if (it.isNullOrEmpty())
                    throw RuntimeException("暂未查询到数据")
                else
                    return@map it
            }
            .timeout(60, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                this@RoomRepositoryImpl.subscription = onSubscribe
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...")
            }
            .doFinally {
                this@RoomRepositoryImpl.subscription
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
            .map {
                if (it.isNullOrEmpty())
                    throw RuntimeException("暂未查询到数据")
                else
                    return@map it
            }
            .timeout(60, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                this@RoomRepositoryImpl.subscription = onSubscribe
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...")
            }
            .doFinally {
                this@RoomRepositoryImpl.subscription
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
    fun doQueryByOrderAsc(
        params: Map<String, Any>,
        orderBy: String,
        showLoading: Boolean = false,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> {
        return roomDao.doQueryByOrderAsc(params, orderBy, limit, offset)
            .map {
                if (it.isNullOrEmpty())
                    throw RuntimeException("暂未查询到数据")
                else
                    return@map it
            }
            .timeout(60, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                this@RoomRepositoryImpl.subscription = onSubscribe
                if (!showLoading) {
                    return@doOnSubscribe
                }
                baseView?.showLoading("正在查询数据,请稍后...")
            }
            .doFinally {
                this@RoomRepositoryImpl.subscription
                if (!showLoading) {
                    return@doFinally
                }
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }
}

