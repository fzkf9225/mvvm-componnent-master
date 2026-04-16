package io.coderf.arklab.usercomponent.repository

import io.coderf.arklab.userapi.bean.UserInfo
import io.coderf.arklab.usercomponent.api.UserAccountHelper
import io.coderf.arklab.usercomponent.api.UserApiService
import io.coderf.arklab.usercomponent.bean.GraphicVerificationCodeBean
import io.coderf.arklab.usercomponent.bean.RequestLoginBean
import io.coderf.arklab.usercomponent.view.UserView
import io.coderf.arklab.common.bean.ApiRequestOptions
import io.coderf.arklab.common.repository.FlowRepositoryImpl
import kotlinx.coroutines.flow.Flow

/**
 * Created by fz on 2023/12/1 10:47
 * describe :
 */
class LoginFlowRepositoryImpl constructor(apiService: UserApiService) :
    FlowRepositoryImpl<UserApiService, UserView>(apiService) {

    suspend fun getImageCode(
        randomNumber: String
    ): Flow<GraphicVerificationCodeBean> {
        return sendRequest(
            {
                apiService!!.getImageCodeSuspend(randomNumber)
            },
            ApiRequestOptions.Builder()
                .setShowDialog(false)
                .build()
        )
    }

    suspend fun login(requestLoginBean: RequestLoginBean): Flow<UserInfo> {
        return sendRequest(
            {
                val tokenBean = apiService?.getTokenSuspend(requestLoginBean)
                UserAccountHelper.setToken(tokenBean?.tokenId)
                apiService!!.getUserInfoSuspend()
            },
            ApiRequestOptions.getDefault().apply { dialogMessage = "登录中，请稍后..." }
        )
    }


    fun logout(): Flow<Any?> {
        return sendRequest(
            {
                apiService!!.logoutSuspend()
            },
            ApiRequestOptions.getDefault()
        )
    }

}
