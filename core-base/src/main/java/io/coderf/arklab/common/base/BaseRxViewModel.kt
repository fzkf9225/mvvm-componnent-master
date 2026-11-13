package io.coderf.arklab.common.base

import android.app.Application
import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.request.AppError
import io.coderf.arklab.core.request.RequestErrorMapper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Rx 存量 ViewModel 基类（已过时）。
 *
 * 新功能请使用 [BaseViewModel.launchRequest] + Flow Repository。
 * 本类仅供仍使用 Rx Observable/Single/Flowable 的页面：自动 Loading/错误 + 可取消。
 *
 * 不要在本类恢复 createRepository / 泛型 Repository；Rx Repository 由业务 Hilt 注入或自行持有。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
@Deprecated(
    message = "使用 BaseViewModel.launchRequest + Flow Repository；本类仅供 Rx 存量",
    replaceWith = ReplaceWith("BaseViewModel")
)
open class BaseRxViewModel(
    application: Application
) : BaseViewModel(application) {

    private val rxDisposables = CompositeDisposable()

    override fun cancelUiRequests() {
        rxDisposables.clear()
        super.cancelUiRequests()
    }

    override fun onCleared() {
        rxDisposables.clear()
        super.onCleared()
    }

    /**
     * 订阅 [Observable]，按 [options] 自动 Loading / 错误；Disposable 可随 Loading 取消 dispose。
     *
     * @deprecated 迁移到 launchRequest + Flow
     */
    @Deprecated("迁移到 launchRequest + Flow")
    protected fun <T : Any> subscribeRequest(
        source: Observable<T>,
        options: RequestOptions = RequestOptions.defaults(),
        onNext: (T) -> Unit
    ): Disposable {
        showLoadingIfNeeded(options)
        val disposable = source
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { hideLoadingIfNeeded(options) }
            .subscribe(
                { value -> onNext(value) },
                { throwable -> deliverRxError(throwable, options) }
            )
        rxDisposables.add(disposable)
        return disposable
    }

    /**
     * @deprecated 迁移到 launchRequest + Flow
     */
    @Deprecated("迁移到 launchRequest + Flow")
    protected fun <T : Any> subscribeRequest(
        source: Single<T>,
        options: RequestOptions = RequestOptions.defaults(),
        onSuccess: (T) -> Unit
    ): Disposable {
        showLoadingIfNeeded(options)
        val disposable = source
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { hideLoadingIfNeeded(options) }
            .subscribe(
                { value -> onSuccess(value) },
                { throwable -> deliverRxError(throwable, options) }
            )
        rxDisposables.add(disposable)
        return disposable
    }

    /**
     * @deprecated 迁移到 launchRequest + Flow
     */
    @Deprecated("迁移到 launchRequest + Flow")
    protected fun <T : Any> subscribeRequest(
        source: Flowable<T>,
        options: RequestOptions = RequestOptions.defaults(),
        onNext: (T) -> Unit
    ): Disposable {
        showLoadingIfNeeded(options)
        val disposable = source
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { hideLoadingIfNeeded(options) }
            .subscribe(
                { value -> onNext(value) },
                { throwable -> deliverRxError(throwable, options) }
            )
        rxDisposables.add(disposable)
        return disposable
    }

    private fun showLoadingIfNeeded(options: RequestOptions) {
        if (options.showLoading) {
            networkRequestUiHost.showLoading(
                options.loadingMessage,
                options.enableDynamicEllipsis,
                options.loadingCancelPolicy
            )
        }
    }

    private fun hideLoadingIfNeeded(options: RequestOptions) {
        if (options.showLoading) {
            networkRequestUiHost.hideLoading()
        }
    }

    private fun deliverRxError(throwable: Throwable, options: RequestOptions) {
        val error = RequestErrorMapper.toAppError(throwable)
        if (options.deliverErrorToUi && error !is AppError.Cancelled) {
            networkRequestUiHost.showError(
                RequestErrorMapper.applyToastPolicy(error, options)
            )
        }
    }
}
