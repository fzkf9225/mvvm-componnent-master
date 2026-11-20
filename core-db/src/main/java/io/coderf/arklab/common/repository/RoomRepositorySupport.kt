package io.coderf.arklab.common.repository

import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.base.BaseRepository
import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.core.db.RoomLog
import io.coderf.arklab.core.request.LoadingCancelPolicy
import io.coderf.arklab.core.request.LoadingTicket
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Room 仓库 RxJava 链路的统一封装（内部类，不对外直接使用）。
 *
 * ## 职责
 * - `subscribeOn(IO)` + `observeOn(MainThread)`；
 * - 按 [RoomRequestOptions] 控制 [io.coderf.arklab.core.request.RequestUi] 的 show/hide Loading（引用计数）；
 * - 注册 Disposable / Subscription 到 [BaseRepository]，可取消策略非 None 时额外登记可取消组；
 * - 查询空列表、删除空结果等业务异常与 [RepositoryImpl.sendRequest] 行为对齐；
 * - Debug 下经 [RoomLog] 打印操作、耗时与结果（对齐网络 [io.coderf.arklab.common.api.ApiRetrofit.printLog]）。
 *
 * ## 调用方
 * 仅由 [RoomRepositoryImpl] 委托调用；业务代码请使用 Repository 公开 API。
 *
 * @see RoomRepositoryImpl
 * @see RepositoryImpl
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/12
 */
internal object RoomRepositorySupport {

    /**
     * 包装 [Completable]：插入、更新、按主键删除等写操作。
     *
     * @param repository 用于注册 Disposable 与获取 RequestUi
     * @param source Room Dao 返回的 Completable
     * @param options UI 与超时配置
     * @param tableName 表名，仅用于日志
     * @param operation 操作名，仅用于日志
     * @param payload 写入实体或参数，仅用于日志
     */
    fun applyCompletable(
        repository: BaseRepository,
        source: Completable,
        options: RoomRequestOptions,
        tableName: String,
        operation: String,
        payload: Any? = null
    ): Completable {
        var chain = source.subscribeOn(Schedulers.io())
        if (options.timeoutSeconds > 0) {
            chain = chain.timeout(options.timeoutSeconds, TimeUnit.SECONDS)
        }
        val session = arrayOfNulls<RoomLog.Session>(1)
        val loading = LoadingLease()
        return chain
            .doOnSubscribe { disposable ->
                repository.addDisposable(disposable)
                trackCancellableDisposable(repository, disposable, options)
            }
            .doOnSubscribe {
                session[0] = RoomLog.begin(
                    repository.javaClass.simpleName, operation, tableName, options, payload
                )
            }
            .doOnComplete { session[0]?.success("complete") }
            .doOnError { session[0]?.error(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { loading.show(repository, options) }
            .doFinally { loading.release(repository, options) }
    }

    /**
     * 包装 [Single]：按 id 查询单条等。
     */
    fun <T : Any> applySingle(
        repository: BaseRepository,
        source: Single<T>,
        options: RoomRequestOptions,
        tableName: String,
        operation: String,
        payload: Any? = null
    ): Single<T> {
        var chain = source.subscribeOn(Schedulers.io())
        if (options.timeoutSeconds > 0) {
            chain = chain.timeout(options.timeoutSeconds, TimeUnit.SECONDS)
        }
        val session = arrayOfNulls<RoomLog.Session>(1)
        val loading = LoadingLease()
        return chain
            .doOnSubscribe { disposable ->
                repository.addDisposable(disposable)
                trackCancellableDisposable(repository, disposable, options)
            }
            .doOnSubscribe {
                session[0] = RoomLog.begin(
                    repository.javaClass.simpleName, operation, tableName, options, payload
                )
            }
            .doOnSuccess { session[0]?.success(it) }
            .doOnError { session[0]?.error(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { loading.show(repository, options) }
            .doFinally { loading.release(repository, options) }
    }

    /**
     * 包装查询类 [Flowable]&lt;List&gt;。
     * 若 [RoomRequestOptions.isThrowOnEmptyList] 为 true 且结果为空，抛出 [BaseException.ErrorType.NOT_FOUND]。
     * Room 的 Flowable 有时不触发 doFinally，因此在 onNext 中也会尝试关闭 Loading（仅释放一次）。
     */
    fun <T : Any> applyListFlowable(
        repository: BaseRepository,
        source: Flowable<List<T>>,
        options: RoomRequestOptions,
        tableName: String,
        operation: String,
        payload: Any? = null
    ): Flowable<List<T>> {
        var chain = source
            .defaultIfEmpty(emptyList())
            .subscribeOn(Schedulers.io())
        if (options.timeoutSeconds > 0) {
            chain = chain.timeout(options.timeoutSeconds, TimeUnit.SECONDS)
        }
        val loading = LoadingLease()
        if (options.isThrowOnEmptyList) {
            chain = chain.doOnNext { list ->
                if (list.isEmpty()) {
                    throw BaseException(BaseException.ErrorType.NOT_FOUND)
                }
                if (options.isShowDialog) {
                    loading.release(repository, options)
                }
            }
        }
        val session = arrayOfNulls<RoomLog.Session>(1)
        return chain
            .doOnSubscribe { subscription ->
                repository.addSubscription(subscription)
                trackCancellableSubscription(repository, subscription, options)
            }
            .doOnSubscribe {
                session[0] = RoomLog.begin(
                    repository.javaClass.simpleName, operation, tableName, options, payload
                )
            }
            .doOnNext { session[0]?.success(it) }
            .doOnError { session[0]?.error(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { loading.show(repository, options) }
            .doFinally { loading.release(repository, options) }
    }

    /**
     * 同一次 Room 请求的 Loading 票据。onNext 与 doFinally 都可能关闭，只关一次。
     */
    private class LoadingLease {
        private val ticket = AtomicReference(LoadingTicket.NONE)
        private val opened = AtomicBoolean(false)

        fun show(repository: BaseRepository, options: RoomRequestOptions) {
            if (!options.isShowDialog || !opened.compareAndSet(false, true)) {
                return
            }
            val ui = repository.requestUi
            if (ui == null) {
                opened.set(false)
                return
            }
            val policy = options.loadingCancelPolicy ?: LoadingCancelPolicy.None
            ticket.set(
                ui.openLoading(
                    options.dialogMessage ?: "",
                    options.isEnableDynamicEllipsis,
                    policy
                )
            )
        }

        fun release(repository: BaseRepository, options: RoomRequestOptions) {
            if (!options.isShowDialog || !opened.compareAndSet(true, false)) {
                return
            }
            repository.requestUi?.closeLoading(ticket.get())
        }
    }

    private fun trackCancellableDisposable(
        repository: BaseRepository,
        disposable: Disposable,
        options: RoomRequestOptions
    ) {
        if (!options.isShowDialog) return
        repository.trackCancellableIfNeeded(disposable, options.loadingCancelPolicy)
    }

    private fun trackCancellableSubscription(
        repository: BaseRepository,
        subscription: Subscription,
        options: RoomRequestOptions
    ) {
        if (!options.isShowDialog) return
        repository.trackCancellableIfNeeded(subscription, options.loadingCancelPolicy)
    }
}
