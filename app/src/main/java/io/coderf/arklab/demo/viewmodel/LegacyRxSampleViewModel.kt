package io.coderf.arklab.demo.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.base.BaseRxViewModel
import io.coderf.arklab.core.bean.RequestOptions
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 【已过时】Rx 存量写法示例，**新功能禁止模仿**。
 *
 * 正确路径：`BaseViewModel` + `launchRequest` + `collectSuccess` + Flow Repository。
 *
 * 若必须暂时用 Rx：继承 [BaseRxViewModel]，用 [subscribeRequest] 走 Host Loading/错误；
 * **不要**再依赖已删除的 `ensureRepository` 给 `RepositoryImpl` 注入 RequestUi。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
@Deprecated(
    message = "示例：Rx 存量写法；新代码使用 BaseViewModel.launchRequest + Flow",
    replaceWith = ReplaceWith("BaseViewModel")
)
@HiltViewModel
class LegacyRxSampleViewModel @Inject constructor(
    application: Application
) : BaseRxViewModel(application) {

    private val _demoText = MutableLiveData<String>()
    val demoText: LiveData<String> = _demoText

    /**
     * 演示：subscribeRequest 自动 Loading；取消遮罩会 dispose。
     * 真实业务请换成 Flow + launchRequest。
     */
    @Deprecated("迁移到 launchRequest + Flow")
    fun loadDemo() {
        @Suppress("DEPRECATION")
        subscribeRequest(
            source = Observable.just("Rx 存量示例（请迁移 Flow）")
                .delay(800, TimeUnit.MILLISECONDS),
            options = RequestOptions.defaults()
        ) { text ->
            _demoText.value = text
        }
    }
}
