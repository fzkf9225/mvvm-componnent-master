package pers.fz.mvvm.repository

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.reactivestreams.Subscription
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.database.BaseRoomDao

/**
 * created by fz on 2024/11/1 16:12
 * describe:
 */
class RoomRepositoryImpl<T : Any, DB : BaseRoomDao<T>, BV : BaseView?>(
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
    fun insert(obj: T): Completable {
        return roomDao.insert(obj)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                baseView?.showLoading("正在插入数据,请稍后...")
            }
            .doFinally { baseView?.hideLoading() }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 添加数组对象数据
     */
    fun insert(vararg objs: T): Completable {
        return roomDao.insert(*objs)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                baseView?.showLoading("正在插入数据,请稍后...")
            }
            .doFinally { baseView?.hideLoading() }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 添加对象集合
     */
    fun insert(objs: List<T>): Completable {
        return roomDao.insert(objs)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                baseView?.showLoading("正在插入数据,请稍后...")
            }
            .doFinally { baseView?.hideLoading() }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 根据对象中的主键删除（主键是自动增长的，无需手动赋值）
     */
    fun delete(obj: T): Completable {
        return roomDao.delete(obj)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                baseView?.showLoading("正在删除数据,请稍后...")
            }
            .doFinally { baseView?.hideLoading() }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 删除所有数据
     */
    fun deleteAll(): Completable {
        return roomDao.deleteAll()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                baseView?.showLoading("正在删除数据,请稍后...")
            }
            .doFinally { baseView?.hideLoading() }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 根据对象中的主键更新（主键是自动增长的，无需手动赋值）
     */
    fun update(vararg obj: T): Completable {
        return roomDao.update(*obj)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addDisposable(disposable)
                baseView?.showLoading("正在更新数据,请稍后...")
            }
            .doFinally { baseView?.hideLoading() }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 查询所有数据
     */
    fun findAll(): Flowable<List<T>> {
        return roomDao.findAll()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                this@RoomRepositoryImpl.subscription = subscription
                baseView?.showLoading("正在查询数据,请稍后...")
            }
            .doFinally {
                this@RoomRepositoryImpl.subscription = null
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 根据ID查询单个对象
     */
    fun find(id: Long): Single<T> {
        return roomDao.find(id)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                baseView?.showLoading("正在查询数据,请稍后...")
            }
            .doFinally { baseView?.hideLoading() }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 根据ID查询单个对象
     */
    fun find(id: String): Single<T> {
        return roomDao.find(id)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                baseView?.showLoading("正在查询数据,请稍后...")
            }
            .doFinally { baseView?.hideLoading() }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 根据参数删除数据
     */
    fun deleteByParams(params: String, value: String): Completable {
        return roomDao.deleteByParams(params, value)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                baseView?.showLoading("正在删除数据,请稍后...")
            }
            .doFinally { baseView?.hideLoading() }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 根据参数删除数据
     */
    fun deleteByParams(params: Map<String, Any>): Completable {
        return roomDao.deleteByParams(params)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                baseView?.showLoading("正在删除数据,请稍后...")
            }
            .doFinally { baseView?.hideLoading() }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 分页查询
     */
    fun doQueryByLimit(
        params: Map<String, Any>,
        limit: Int = 10,
        offset: Int = 10
    ): Flowable<List<T>> {
        return roomDao.doQueryByLimit(params, limit, offset)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                this@RoomRepositoryImpl.subscription = onSubscribe
                baseView?.showLoading("正在查询数据,请稍后...")
            }
            .doFinally {
                this@RoomRepositoryImpl.subscription
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        limit: Int = 10,
        offset: Int = 10
    ): Flowable<List<T>> {
        return roomDao.doQueryByOrderDesc(params, limit, offset)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                this@RoomRepositoryImpl.subscription = onSubscribe
                baseView?.showLoading("正在查询数据,请稍后...")
            }
            .doFinally {
                this@RoomRepositoryImpl.subscription
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderAsc(
        params: Map<String, Any>,
        limit: Int = 10,
        offset: Int = 10
    ): Flowable<List<T>> {
        return roomDao.doQueryByOrderAsc(params, limit, offset)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                this@RoomRepositoryImpl.subscription = onSubscribe
                baseView?.showLoading("正在查询数据,请稍后...")
            }
            .doFinally {
                this@RoomRepositoryImpl.subscription
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }
}

