package pers.fz.mvvm.repository

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.database.BaseRoomDao

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
    protected val compositeDisposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }


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

}

