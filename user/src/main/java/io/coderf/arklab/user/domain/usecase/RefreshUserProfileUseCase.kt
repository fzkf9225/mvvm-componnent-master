package io.coderf.arklab.user.domain.usecase

import io.coderf.arklab.core.bean.RequestResult
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
 * **其它 ViewModel**：用 [executeWithApiService]，再经 `launchRequest` 收集（UI 在 ViewModel）。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/13
 */
class RefreshUserProfileUseCase @Inject constructor() {

    fun execute(repository: UserProfileRepository): Flow<RequestResult<UserInfo>> =
        repository.refreshUserInfo()

    fun executeWithApiService(api: UserApiService): Flow<RequestResult<UserInfo>> =
        UserRepositoryImpl(api).refreshUserInfo()
}
