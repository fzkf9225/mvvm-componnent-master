package com.casic.titan.commonui.api

import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

/**
 * created by fz on 2025/9/9 9:30
 * describe:附件上传，未初始化，需要单独初始化，使用retrofit代理
 */
interface FileApiService {
    /**
     * 单文件上传
     * @param url 接口相对路径
     * @PartMap partMap: Map<String?, RequestBody?>?,
     */
    @Multipart
    @POST
    fun performUpload(@Url url: String, @Part filePart: MultipartBody.Part): Observable<Map<String, Any?>>
}