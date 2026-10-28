package io.coderf.arklab.core.db

import io.coderf.arklab.common.repository.IRepository
import io.coderf.arklab.core.request.NoOpRequestUi
import io.coderf.arklab.core.request.RequestUi
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import org.reactivestreams.Subscription

/**
 * 新版 Room 协程仓库基类：在 [DefaultRoomRepository] 之上兼容 [IRepository] / BaseViewModel。
 *
 * 对齐网络层 [io.coderf.arklab.core.network.BaseNetworkRepository]。
 * 业务侧若只需 Flow CRUD，可直接继承 [RoomDataRepository]。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/28
 */
open class BaseRoomRepository(
    requestUi: RequestUi = NoOpRequestUi
) : DefaultRoomRepository(requestUi), IRepository {

    private val compositeDisposable = CompositeDisposable()
    private val subscriptions = mutableListOf<Subscription>()

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
        setRequestUi(NoOpRequestUi)
    }
}
