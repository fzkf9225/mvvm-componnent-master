package pers.fz.mvvm.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import pers.fz.mvvm.api.ApiRetrofit;

/**
 * created by fz on 2025/7/29 14:15
 * describe:
 */
public class ApiServiceWrapper<T> implements InvocationHandler {
    private final T retrofitProxy;      // Retrofit 动态代理对象
    private final ApiRetrofit apiRetrofit; // 持有的 ApiRetrofit 实例

    private ApiServiceWrapper(T retrofitProxy, ApiRetrofit apiRetrofit) {
        this.retrofitProxy = retrofitProxy;
        this.apiRetrofit = apiRetrofit;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 处理 getRetrofit/setRetrofit 方法
        if (method.getName().equals("getRetrofit")) {
            return apiRetrofit;
        } else if (method.getName().equals("setRetrofit")) {
            throw new UnsupportedOperationException("Cannot set Retrofit on a wrapped service");
        }
        // 其他方法转发给 Retrofit 代理
        return method.invoke(retrofitProxy, args);
    }

    // 静态方法：创建装饰后的实例
    @SuppressWarnings("unchecked")
    public static <T> T wrap(T retrofitProxy, ApiRetrofit apiRetrofit, Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class<?>[]{serviceClass},
                new ApiServiceWrapper<>(retrofitProxy, apiRetrofit)
        );
    }
}

