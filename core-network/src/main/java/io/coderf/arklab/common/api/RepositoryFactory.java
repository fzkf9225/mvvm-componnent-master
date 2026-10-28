package io.coderf.arklab.common.api;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;

import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.repository.FlowRepositoryImpl;
import io.coderf.arklab.common.repository.RepositoryImpl;

/**
 * 网络侧 Repository 工厂。
 * <p>
 * 鉴权重试由 Module 挂到 {@link ApiRetrofit.Builder}，按 ApiService 实例生效，
 * 不再通过 Repository 构造参数覆盖。
 * <p>
 * Room 仓库请直接 {@code new XxxRepository(dao)}，或依赖 {@code core-db} /
 * {@code common} facade；本类不再耦合 Room，避免 network → db 反向依赖。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/6/25 11:20
 * @updated 2026/10/28
 */
public class RepositoryFactory {
    private RepositoryFactory() {
        // 私有构造，防止实例化
    }

    /**
     * 创建基础Repository
     *
     * @param repositoryClass Repository类
     * @param <R>             必须继承BaseRepository
     * @return Repository实例
     */
    public static <R extends BaseRepository> R create(@NonNull Class<R> repositoryClass) {
        try {
            return repositoryClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("创建Repository失败: " + e.getMessage());
        }
    }

    /**
     * 创建带API服务的RepositoryImpl
     *
     * @param repositoryClass RepositoryImpl类
     * @param apiService      API服务
     * @param <R>             必须继承RepositoryImpl
     * @param <API>           必须继承BaseApiService
     * @return RepositoryImpl实例
     */
    public static <R extends RepositoryImpl<API>, API extends BaseApiService> R create(
            @NonNull Class<R> repositoryClass,
            @NonNull API apiService) {
        try {
            Constructor<R> constructor = repositoryClass.getConstructor(apiService.getClass().getInterfaces()[0]);
            return constructor.newInstance(apiService);
        } catch (Exception e) {
            throw new RuntimeException("创建RepositoryImpl失败: " + e.getMessage());
        }
    }

    /**
     * 创建带API服务的FlowRepositoryImpl
     *
     * @param repositoryClass FlowRepositoryImpl类
     * @param apiService      API服务
     * @param <R>             必须继承FlowRepositoryImpl
     * @param <API>           必须继承BaseApiService
     * @return FlowRepositoryImpl实例
     */
    public static <R extends FlowRepositoryImpl<API>, API extends BaseApiService> R createFlow(
            @NonNull Class<R> repositoryClass,
            @NonNull API apiService) {
        try {
            Constructor<R> constructor = repositoryClass.getConstructor(apiService.getClass().getInterfaces()[0]);
            return constructor.newInstance(apiService);
        } catch (Exception e) {
            throw new RuntimeException("创建RepositoryImpl失败: " + e.getMessage());
        }
    }

}
