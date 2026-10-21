package io.coderf.arklab.common.api;


import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 来源：
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2019/7/30
 */

public interface BaseApiService {

    /**
     * 获取创建本 API 接口的 {@link ApiRetrofit} 实例。
     * <p>
     * 由 {@code ApiRetrofit.getApiService} 返回的动态代理对象承接本方法，
     * 直接调用 Retrofit 原生代理会抛出异常。
     *
     * @return 绑定本接口的 ApiRetrofit 实例
     */
    ApiRetrofit getRetrofit();

    /**
     * 设置本 API 接口绑定的 {@link ApiRetrofit} 实例。
     *
     * @param retrofit ApiRetrofit 实例
     */
    void setRetrofit(ApiRetrofit retrofit);

    /**
     * 大文件官方建议用 @Streaming 来进行注解，不然会出现IO异常，小文件可以忽略不注入
     *
     * @param fileUrl 文件路径地址
     * @return 观察者
     */
    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(@Url String fileUrl);
    /**
     * 大文件官方建议用 @Streaming 来进行注解，不然会出现IO异常，小文件可以忽略不注入
     *
     * @param fileUrl 文件路径地址
     * @return 观察者
     */
    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(@Header("Range") String range, @Url String fileUrl);

}
