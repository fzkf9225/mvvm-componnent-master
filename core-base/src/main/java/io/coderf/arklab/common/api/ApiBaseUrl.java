package io.coderf.arklab.common.api;

import androidx.annotation.NonNull;

/**
 * 下载栈使用的接口根地址。请求本身走绝对 URL 时 Retrofit 仍要求一个 baseUrl，
 * 相对地址则拼在这个根地址后面。由配置注入时写入。
 */
public final class ApiBaseUrl {

    private static volatile String value = "";

    private ApiBaseUrl() {
    }

    public static void set(@NonNull String baseUrl) {
        if (baseUrl.isEmpty()) {
            throw new IllegalArgumentException("baseUrl 为空");
        }
        value = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    @NonNull
    public static String require() {
        String current = value;
        if (current.isEmpty()) {
            throw new IllegalStateException("接口 baseUrl 尚未注入");
        }
        return current;
    }
}
