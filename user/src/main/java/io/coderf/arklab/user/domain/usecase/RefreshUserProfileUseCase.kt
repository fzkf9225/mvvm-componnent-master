package io.coderf.arklab.user.domain.usecase

import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.core.request.RequestUi
import io.coderf.arklab.core.request.TokenRefresher
import io.coderf.arklab.user.api.UserApiService
import io.coderf.arklab.user.domain.repository.UserProfileRepository
import io.coderf.arklab.user.repository.UserRepositoryImpl
import io.coderf.arklab.userapi.bean.UserInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 刷新当前用户资料。
 *
 * **推荐**：当前页面 ViewModel 主仓库已是 [UserRepositoryImpl] 时用 [execute]。
 * **其它 ViewModel**：用 [executeWithApiService]，可传入 [requestUi] / [tokenRefresher]。
 */
class RefreshUserProfileUseCase @Inject constructor() {

    fun execute(repository: UserProfileRepository): Flow<RequestResult<UserInfo>> =
        repository.refreshUserInfo()

    fun executeWithApiService(
        api: UserApiService,
        requestUi: RequestUi? = null
    ): Flow<RequestResult<UserInfo>> {
        val repo = UserRepositoryImpl(api)
        if (requestUi != null) {
            repo.setRequestUi(requestUi)
        }
        return repo.refreshUserInfo()
    }
}
