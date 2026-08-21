package io.coderf.arklab.core.network

import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.inter.ApiRetrofitService
import io.coderf.arklab.common.repository.IRepository
import io.coderf.arklab.core.request.NoOpRequestUi
import io.coderf.arklab.core.request.RequestUi
import io.coderf.arklab.core.request.TokenRefresher
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import org.reactivestreams.Subscription

/**
 * 新版网络 Repository 基类：在 [DefaultNetworkRepository] 之上兼容 [IRepository] / [BaseViewModel]。
 *
 * 业务仓库继承本类即可接入 ViewModel 的 `createRepository` 装配与 RequestUi 注入。
 *
 * 鉴权解析（与旧栈一致，**按 ApiService 实例**，非 App 进程全局）：
 * 1. [tokenRefresher] 构造参数（局部覆盖）
 * 2. [boundApiService] → `ApiRetrofit.Builder` 上对该实例 set 的 TokenRefresher / FlowRetryService
 * 3. 都没有则不鉴权重试
 *
 * 推荐写法：把当前仓库使用的 ApiService 传入 [boundApiService]，Module 里只给需要鉴权的 Builder 配置即可。
 */
open class BaseNetworkRepository<BV : BaseView>(
    requestUi: RequestUi = NoOpRequestUi,
    tokenRefresher: TokenRefresher? = null,
    boundApiService: ApiRetrofitService? = null
) : DefaultNetworkRepository(requestUi, tokenRefresher, boundApiService), IRepository<BV> {

    private val compositeDisposable = CompositeDisposable()
    private val subscriptions = mutableListOf<Subscription>()

    @Volatile
    private var boundView: BV? = null

    override fun setBaseView(baseView: BV?) {
        boundView = baseView
    }

    override fun getBaseView(): BV? = boundView

    override fun addDisposable(disposable: Disposable?) {
        if (disposable != null) {
            compositeDisposable.add(disposable)
        }
    }

    override fun addSubscription(subscription: Subscription?) {
        if (subscription != null) {
            subscriptions.add(subscription)
        }
    }

    override fun remove() {
        compositeDisposable.clear()
        subscriptions.forEach { it.cancel() }
        subscriptions.clear()
    }

    override fun clear() {
        remove()
        boundView = null
        setRequestUi(NoOpRequestUi)
    }
}
