package com.casic.otitan.usercomponent.repository

import com.casic.otitan.userapi.bean.UserInfo
import com.casic.otitan.usercomponent.api.UserAccountHelper
import com.casic.otitan.usercomponent.api.UserApiService
import com.casic.otitan.usercomponent.bean.GraphicVerificationCodeBean
import com.casic.otitan.usercomponent.bean.RequestLoginBean
import com.casic.otitan.usercomponent.view.UserView
import com.casic.otitan.common.bean.ApiRequestOptions
import com.casic.otitan.common.repository.FlowRepositoryImpl
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
