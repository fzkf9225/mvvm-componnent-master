package io.coderf.arklab.demo.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.network.NetworkFlowPagingViewModel
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.bean.RequestNotificationBean
import io.coderf.arklab.demo.repository.DemoFlowPagingRepositoryImpl
import javax.inject.Inject

/**
 * 新闻分页 ViewModel（新版 [NetworkFlowPagingViewModel]）。
 *
 * 列表翻页走默认 [createPagingSource]（AppError → Host）；点击详情走 Flow 重载
 * [launchRequest]（内部强制 Host 错误处理），Loading 可取消。
 *
 * @author fz
 * @version 1.3
 * @since 1.0
 * @updated 2026/11/13
 */
@HiltViewModel
class DemoFlowPagingViewModel @Inject constructor(
    application: Application,
    private val flowPagingRepository: DemoFlowPagingRepositoryImpl
) : NetworkFlowPagingViewModel<NotificationMessageBean, RequestNotificationBean>(
    application,
    flowPagingRepository
) {

    /**
     * 点击行后的一次性提示（position + title）；页面 observe 后展示 Toast。
     */
    private val _itemClickToast = MutableLiveData<ItemClickToast>()
    val itemClickToast: LiveData<ItemClickToast> = _itemClickToast

    /**
     * 按 id 拉详情；Flow 重载强制 Error 进 Host，成功只写 Toast 载荷。
     */
    fun loadInfoById(id: String, position: Int) {
        launchRequest(
            options = RequestOptions.cancellable(),
            flow = { flowPagingRepository.getInfoById(id) }
        ) { bean ->
            _itemClickToast.value = ItemClickToast(position, bean.title)
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
