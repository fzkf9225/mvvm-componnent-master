package io.coderf.arklab.common.repository

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.base.BaseRepository
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.common.inter.RequestUiCallback
import java.util.concurrent.TimeUnit

/**
 * Room 仓库层 RxJava 链路的统一封装（内部类，不对外直接使用）。
 *
 * ## 职责
 * - `subscribeOn(IO)` + `observeOn(MainThread)`；
 * - 按 [RoomRequestOptions] 控制 [RequestUiCallback] 的 show/hide Loading；
 * - 注册 Disposable / Subscription 到 [BaseRepository]；
 * - 查询空列表、删除空结果等业务异常与 [RepositoryImpl.sendRequest] 行为对齐。
 *
 * ## 调用方
 * 仅由 [RoomRepositoryImpl] 委托调用；业务代码请使用 Repository 公开 API。
 *
 * @author fz
 * @see RoomRepositoryImpl
 * @see RepositoryImpl
 */
internal object RoomRepositorySupport {

    /**
     * 包装 [Completable]：插入、更新、按主键删除等写操作。
     *
     * @param repository 用于注册 Disposable 与获取 RequestUi
     * @param source Room Dao 返回的 Completable
     * @param options UI 与超时配置
     */
    fun <BV : BaseView?> applyCompletable(
        repository: BaseRepository<BV>,
        source: Completable,
        options: RoomRequestOptions
    ): Completable {
        var chain = source.subscribeOn(Schedulers.io())
        if (options.timeoutSeconds > 0) {
            chain = chain.timeout(options.timeoutSeconds, TimeUnit.SECONDS)
        }
        return chain
            .doOnSubscribe { repository.addDisposable(it) }
            .doOnSubscribe { showLoading(repository, options) }
            .doFinally { hideLoading(repository, options) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 包装 [Single]：按 id 查询单条等。
     */
    fun <T : Any, BV : BaseView?> applySingle(
        repository: BaseRepository<BV>,
        source: Single<T>,
        options: RoomRequestOptions
    ): Single<T> {
        var chain = source.subscribeOn(Schedulers.io())
        if (options.timeoutSeconds > 0) {
            chain = chain.timeout(options.timeoutSeconds, TimeUnit.SECONDS)
        }
        return chain
            .doOnSubscribe { repository.addDisposable(it) }
            .doOnSubscribe { showLoading(repository, options) }
            .doFinally { hideLoading(repository, options) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 包装查询类 [Flowable]&lt;List&gt;。
     * 若 [RoomRequestOptions.isThrowOnEmptyList] 为 true 且结果为空，抛出 [BaseException.ErrorType.NOT_FOUND]。
     * Room 的 Flowable 有时不触发 doFinally，因此在 onNext 中也会尝试关闭 Loading。
     */
    fun <T : Any, BV : BaseView?> applyListFlowable(
        repository: BaseRepository<BV>,
        source: Flowable<List<T>>,
        options: RoomRequestOptions
    ): Flowable<List<T>> {
        var chain = source
            .defaultIfEmpty(emptyList())
            .subscribeOn(Schedulers.io())
        if (options.timeoutSeconds > 0) {
            chain = chain.timeout(options.timeoutSeconds, TimeUnit.SECONDS)
        }
        if (options.isThrowOnEmptyList) {
            chain = chain.doOnNext { list ->
                if (list.isEmpty()) {
                    throw BaseException(BaseException.ErrorType.NOT_FOUND)
                }
                if (options.isShowDialog) {
                    hideLoading(repository, options)
                }
            }
        }
        return chain
            .doOnSubscribe { repository.addSubscription(it) }
            .doOnSubscribe { showLoading(repository, options) }
            .doFinally { hideLoading(repository, options) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 包装条件删除类 [Flowable]&lt;List&gt;。
     * 结果为空时抛出 [BaseException.ErrorType.DELETE_SUCCESS]（与老逻辑一致）。
     */
    fun <T : Any, BV : BaseView?> applyDeleteFlowable(
        repository: BaseRepository<BV>,
        source: Flowable<List<T>>,
        options: RoomRequestOptions
    ): Flowable<List<T>> {
        var chain = source
            .defaultIfEmpty(emptyList())
            .subscribeOn(Schedulers.io())
        if (options.timeoutSeconds > 0) {
            chain = chain.timeout(options.timeoutSeconds, TimeUnit.SECONDS)
        }
        return chain
            .doOnNext { list ->
                if (list.isEmpty()) {
                    throw BaseException(BaseException.ErrorType.DELETE_SUCCESS)
                }
                if (options.isShowDialog) {
                    hideLoading(repository, options)
                }
            }
            .doOnSubscribe { repository.addSubscription(it) }
            .doOnSubscribe { showLoading(repository, options) }
            .doFinally { hideLoading(repository, options) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /** 根据 options 显示 Loading（仅当 RequestUi 非空且 showDialog 为 true） */
    private fun <BV : BaseView?> showLoading(
        repository: BaseRepository<BV>,
        options: RoomRequestOptions
    ) {
        if (!options.isShowDialog) return
        repository.getRequestUi()?.showLoading(
            options.dialogMessage,
            options.isEnableDynamicEllipsis
        )
    }

    /** 隐藏 Loading */
    private fun <BV : BaseView?> hideLoading(
        repository: BaseRepository<BV>,
        options: RoomRequestOptions
    ) {
        if (!options.isShowDialog) return
        repository.getRequestUi()?.hideLoading()
    }
}
