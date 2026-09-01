package io.coderf.arklab.common.inter;

import io.coderf.arklab.common.api.ApiRetrofit;

/**
 * ApiRetrofitService 接口。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/7/29 14:09
 */
public interface ApiRetrofitService {

    ApiRetrofit getRetrofit();

    void setRetrofit(ApiRetrofit retrofit);
}
