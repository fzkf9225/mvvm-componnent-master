package io.coderf.arklab.user.repository

import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.network.BaseNetworkRepository
import io.coderf.arklab.user.api.UserApiService
import io.coderf.arklab.user.domain.repository.UserProfileRepository
import io.coderf.arklab.userapi.bean.UserInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 用户资料仓库（[BaseNetworkRepository]）。
 * 鉴权：优先构造 [tokenRefresher]，否则用 [api] 所属 ApiRetrofit.Builder 上的配置。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/13
 */
class UserRepositoryImpl @Inject constructor(
    private val apiService: UserApiService,
) : BaseNetworkRepository(
    boundApiService = apiService
),
    UserProfileRepository {

    override fun refreshUserInfo(): Flow<RequestResult<UserInfo>> {
        return request(
            RequestOptions.builder()
                .showLoading(false)
                .deliverErrorToUi(false)
                .build()
        ) {
            apiService.getUserInfoSuspend()
        }
    }
}
