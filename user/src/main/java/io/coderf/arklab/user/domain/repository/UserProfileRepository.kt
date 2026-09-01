package io.coderf.arklab.user.domain.repository

import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.userapi.bean.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * 用户资料相关数据端口；由 [io.coderf.arklab.user.repository.UserRepositoryImpl] 实现。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
interface UserProfileRepository {

    fun refreshUserInfo(): Flow<RequestResult<UserInfo>>
}
