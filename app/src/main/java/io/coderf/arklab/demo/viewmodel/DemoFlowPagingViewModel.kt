package io.coderf.arklab.demo.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.base.launchUiRequest
import io.coderf.arklab.core.network.NetworkFlowPagingViewModel
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.bean.RequestNotificationBean
import io.coderf.arklab.demo.repository.DemoFlowPagingRepositoryImpl
import javax.inject.Inject

/**
 * 新闻分页 ViewModel（新版 [NetworkFlowPagingViewModel]）。
 * <p>
 * 点击详情走 [launchUiRequest]，以便 Loading 取消协议能真正取消请求。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/12
 */
@HiltViewModel
class DemoFlowPagingViewModel @Inject constructor(
    application: Application,
    private val apiServiceHelper: ApiServiceHelper
) : NetworkFlowPagingViewModel<DemoFlowPagingRepositoryImpl, NotificationMessageBean, RequestNotificationBean>(
    application
) {

    /**
     * 点击行后的一次性提示（position + title）；页面 observe 后展示 Toast。
     */
    private val _itemClickToast = MutableLiveData<ItemClickToast>()
    val itemClickToast: LiveData<ItemClickToast> = _itemClickToast

    override fun createRepository(): DemoFlowPagingRepositoryImpl {
        return DemoFlowPagingRepositoryImpl(apiServiceHelper)
    }

    /**
     * 按 id 拉详情；必须经 [launchUiRequest] 登记 Job，取消 Loading 才会打断请求。
     *
     * @param id       条目 id
     * @param position 列表行号（仅用于 Toast 文案）
     */
    fun loadInfoById(id: String, position: Int) {
        launchUiRequest {
            iRepository.getInfoById(id).collect { result ->
                result.onSuccess { bean ->
                    _itemClickToast.value = ItemClickToast(position, bean.title)
                }
            }
        }
    }

    override fun createPagingQuery() = RequestNotificationBean().apply {
        type = "5"
    }

    /**
     * 点击详情成功后的 Toast 载荷。
     *
     * @property position 列表行号
     * @property title    详情标题
     */
    data class ItemClickToast(
        val position: Int,
        val title: String?
    )
}
