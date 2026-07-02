package io.coderf.arklab.user.domain.usecase

import androidx.lifecycle.MutableLiveData
import io.coderf.arklab.common.inter.RequestUiCallback
import io.coderf.arklab.userapi.bean.UserInfo
import io.coderf.arklab.user.api.UserApiService
import io.coderf.arklab.user.domain.repository.UserProfileRepository
import io.coderf.arklab.user.repository.UserRepositoryImpl
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

/**
 * 刷新当前用户资料。
 *
 * **推荐**：当前页面 ViewModel 的 [io.coderf.arklab.common.base.BaseViewModel.iRepository] 已是 [UserRepositoryImpl] 时，
 * 使用 [execute]，这样会走 [io.coderf.arklab.common.base.BaseViewModel.attachRepositoryRequestUi] 注入的 [RequestUiCallback]（错误提示等一致）。
 *
 * **任意其它 ViewModel**（主仓库不是 [UserRepositoryImpl]）：注入本 UseCase + [UserApiService]，调用 [executeWithApiService]，
 * 内部会临时创建 [UserRepositoryImpl]；该请求本身无 loading（见仓库实现），错误处理在无 [RequestUiCallback] 时可能静默，需要时可传 [requestUi]。
 */
class RefreshUserProfileUseCase @Inject constructor() {

    fun execute(
        repository: UserProfileRepository,
        liveData: MutableLiveData<UserInfo>
    ): Disposable = repository.refreshUserInfo(liveData)

    /**
     * 不依赖页面主 [iRepository] 类型时的刷新入口。
     *
     * @param requestUi 可选；传入后临时仓库会 [io.coderf.arklab.common.base.BaseRepository.setRequestUi]，便于与当前页统一错误/Toast。
     */
    fun executeWithApiService(
        api: UserApiService,
        liveData: MutableLiveData<UserInfo>,
        requestUi: RequestUiCallback? = null
    ): Disposable {
        val repo = UserRepositoryImpl(api)
        if (requestUi != null) {
            repo.setRequestUi(requestUi)
        }
        return repo.refreshUserInfo(liveData)
    }
}
