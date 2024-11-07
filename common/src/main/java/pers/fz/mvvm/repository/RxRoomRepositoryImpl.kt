package pers.fz.mvvm.repository

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.schedulers.Schedulers
import org.reactivestreams.Publisher
import org.reactivestreams.Subscription
import pers.fz.mvvm.base.BaseException
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.database.RxRoomDao
import java.util.concurrent.TimeUnit

/**
 * created by fz on 2024/11/1 16:12
 * describe:
 */
open class RxRoomRepositoryImpl<T : Any, DB : RxRoomDao<T>, BV : BaseView?>(
    private val roomDao: DB,
    private val baseView: BV
) : RoomRepositoryImpl<T, DB, BV>(roomDao, baseView) {

    private val subscriptionList by lazy {
        ArrayList<Subscription>()
    }

    private fun addSubscription(subscription: Subscription) {
        subscriptionList.add(subscription)
    }

    private fun removeSubscription(subscription: Subscription) {
        subscriptionList.isNotEmpty().let {
            subscriptionList.remove(subscription)
        }
    }

    private fun removeDisposable(disposable: Disposable) {
        if (compositeDisposable.isDisposed) {
            return
        }
        compositeDisposable.remove(disposable)
    }

    override fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun removeDisposable() {
        //默认取消所有订阅，但不会导致正在进行的任务终止，而是等待它们完成，仅仅只是取消订阅关系而已
        compositeDisposable.clear()
        //默认取消所有订阅，并取消所有正在进行的任务
//            compositeDisposable.dispose();
        subscriptionList.isNotEmpty().let {
            for (subscription in subscriptionList) {
                subscription.cancel()
            }
        }
        subscriptionList.clear()
    }

    /**
     * 删除所有数据
     */
    fun deleteAll(showLoading: Boolean = false): Flowable<List<T>> {
        return roomDao.deleteAll()
            .defaultIfEmpty(emptyList())
            .map {
                if (it.isEmpty())
                    throw BaseException(BaseException.DELETE_SUCCESS_MSG, BaseException.DELETE_SUCCESS)
                else
                    return@map it
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                addSubscription(disposable)
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
        params: String, value: String,
        showLoading: Boolean = false
    ): Flowable<List<T>> {
        return roomDao.deleteByParams(params, value)
            .defaultIfEmpty(emptyList())
            .map {
                if (it.isEmpty())
                    throw BaseException(BaseException.DELETE_SUCCESS_MSG, BaseException.DELETE_SUCCESS)
                else
                    return@map it
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
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
    ): Flowable<List<T>> {
        return roomDao.deleteByParams(params)
            .defaultIfEmpty(emptyList())
            .map {
                if (it.isEmpty())
                    throw BaseException(BaseException.DELETE_SUCCESS_MSG, BaseException.DELETE_SUCCESS)
                else
                    return@map it
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
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
     * 查询所有数据
     */
    fun findAll(showLoading: Boolean = false): Flowable<List<T>> {
        return roomDao.findAll()
            .defaultIfEmpty(emptyList())
            .map {
                if (it.isEmpty())
                    throw BaseException(BaseException.NOT_FOUND_MSG, BaseException.NOT_FOUND)
                else
                    return@map it
            }
            .timeout(30, TimeUnit.SECONDS)
            .switchIfEmpty { throw RuntimeException("暂未查询到数据") }
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
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
            .map {
                if (it.isEmpty())
                    throw BaseException(BaseException.NOT_FOUND_MSG, BaseException.NOT_FOUND)
                else
                    return@map it
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
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
            .map {
                if (it.isEmpty())
                    throw BaseException(BaseException.NOT_FOUND_MSG, BaseException.NOT_FOUND)
                else
                    return@map it
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
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
            .map {
                if (it.isEmpty())
                    throw BaseException(BaseException.NOT_FOUND_MSG, BaseException.NOT_FOUND)
                else
                    return@map it
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
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
            .map {
                if (it.isEmpty())
                    throw BaseException(BaseException.NOT_FOUND_MSG, BaseException.NOT_FOUND)
                else
                    return@map it
            }
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addSubscription(onSubscribe)
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
}

