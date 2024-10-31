package pers.fz.mvvm.repository

import io.reactivex.rxjava3.core.Observable
import pers.fz.mvvm.api.ErrorConsumer
import pers.fz.mvvm.base.BaseException
import pers.fz.mvvm.base.BaseModelEntity
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.bean.RequestConfigEntity
import pers.fz.mvvm.inter.RetryService

/**
 * Created by fz on 2023/12/1 11:14
 * describe :
 */
abstract class PagingKtRepositoryImpl<T : Any, BV : BaseView?>(
    retryService: RetryService?,
    baseView: BV
) :
    RepositoryImpl<BV>(retryService, baseView) {
    var requestConfigEntity: RequestConfigEntity? = null
        get() {
            if (field == null) {
                field = RequestConfigEntity.Builder().setShowDialog(false).build()
            }
            return field
        }
        private set

    abstract suspend fun requestPaging(currentPage: Int, pageSize: Int): Observable<List<T>>?

    fun catchException(): ErrorConsumer {
        return ErrorConsumer(baseView, requestConfigEntity)
    }

    fun onError(exception: Exception) {
        baseView?.onErrorCode(BaseModelEntity<Any?>(BaseException.OTHER, exception.message))
    }
}
