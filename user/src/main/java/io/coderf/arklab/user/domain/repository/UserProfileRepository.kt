package io.coderf.arklab.user.domain.repository

import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.userapi.bean.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * 用户资料相关数据端口；由 [io.coderf.arklab.user.repository.UserRepositoryImpl] 实现。
 */
interface UserProfileRepository {

    fun refreshUserInfo(): Flow<RequestResult<UserInfo>>
}
