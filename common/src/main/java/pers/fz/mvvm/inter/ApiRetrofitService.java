package pers.fz.mvvm.inter;

import pers.fz.mvvm.api.ApiRetrofit;

/**
 * created by fz on 2025/7/29 14:09
 * describe:
 */
public interface ApiRetrofitService {

    ApiRetrofit getRetrofit();

    void setRetrofit(ApiRetrofit retrofit);
}
