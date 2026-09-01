package io.coderf.arklab.user.domain.usecase

import io.coderf.arklab.common.utils.encode.SM3Utils
import javax.inject.Inject

/**
 * 登录密码 SM3 哈希（与原有 [LoginViewModel] 逻辑一致）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
class HashLoginPasswordUseCase @Inject constructor() {

    operator fun invoke(plainPassword: String): String =
        SM3Utils.hashToHex(SM3Utils.hash(plainPassword.toByteArray()))
}
