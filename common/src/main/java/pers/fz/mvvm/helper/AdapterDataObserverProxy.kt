package pers.fz.mvvm.helper

import androidx.recyclerview.widget.RecyclerView
import pers.fz.mvvm.api.ApiRetrofit
import pers.fz.mvvm.util.log.LogUtil


/**
 * created by fz on 2024/10/31 15:42
 * describe:
 */
class AdapterDataObserverProxy(
    private val adapterDataObserver: RecyclerView.AdapterDataObserver,
    private val headerCount: Int
) : RecyclerView.AdapterDataObserver() {

    override fun onChanged() {
        super.onChanged()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        super.onItemRangeChanged(positionStart + headerCount, itemCount)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        super.onItemRangeChanged(positionStart + headerCount, itemCount, payload)
    }

    //    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//        adapterDataObserver.onItemRangeInserted(positionStart + headerCount, itemCount)
//    }
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        LogUtil.show(ApiRetrofit.TAG, "positionStart:" + positionStart + ",itemCount:" + itemCount)
        super.onItemRangeInserted(positionStart + headerCount, itemCount)
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        super.onItemRangeRemoved(positionStart + headerCount, itemCount)
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        super.onItemRangeMoved(
            fromPosition + headerCount,
            toPosition + headerCount,
            itemCount
        )
    }
}