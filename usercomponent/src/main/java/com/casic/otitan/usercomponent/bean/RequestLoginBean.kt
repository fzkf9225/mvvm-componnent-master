package com.casic.otitan.usercomponent.bean

/**
 * Create by CherishTang on 2024/11/8 0018
 * describe:
 */
data class RequestLoginBean(
    var accountId: String? = null,
    var code: String? = null,
    var num: String? = null,
    var openId: String? = null,
    var password: String? = null,
    var userName: String? = null,
    var phoneCode: String? = null,
) {
    constructor(
        code: String,
        num: String,
        password: String? = null,
        userName: String
    ) : this(null, code, num, null, password, userName,"873990")

    constructor(userName: String? = null) : this(null, null, null, null, null, userName,"873990")
    constructor() : this(null, null, null, null, null, null,"873990")

}
