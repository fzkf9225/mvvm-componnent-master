package io.coderf.arklab.user.repository

import io.coderf.arklab.core.network.BaseNetworkRepository
import io.coderf.arklab.core.request.RequestOptions
import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.core.request.TokenRefresher
import io.coderf.arklab.user.api.UserApiService
import io.coderf.arklab.user.domain.repository.UserProfileRepository
import io.coderf.arklab.userapi.bean.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * 用户资料仓库（新版 [BaseNetworkRepository]）。
 * 鉴权：优先构造 [tokenRefresher]，否则用 [api] 所属 ApiRetrofit.Builder 上的配置。
 */
class UserRepositoryImpl(
    private val apiService: UserApiService,
) : BaseNetworkRepository<io.coderf.arklab.common.base.BaseView>(
    boundApiService = apiService
),
    UserProfileRepository {

    override fun refreshUserInfo(): Flow<RequestResult<UserInfo>> {
        return request(
            RequestOptions.builder()
                .showLoading(false)
                .build()
        ) {
            apiService.getUserInfoSuspend()
        }
    }
}
