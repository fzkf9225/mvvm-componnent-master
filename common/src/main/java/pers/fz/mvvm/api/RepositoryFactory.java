package pers.fz.mvvm.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Constructor;

import pers.fz.mvvm.base.BaseRepository;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.database.BaseRoomDao;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.RepositoryImpl;
import pers.fz.mvvm.repository.RoomRepositoryImpl;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * created by fz on 2025/6/25 11:20
 * describe:
 */
public class RepositoryFactory {
    private RepositoryFactory() {
        // 私有构造，防止实例化
    }

    /**
     * 创建基础Repository
     *
     * @param repositoryClass Repository类
     * @param <R>             必须继承RepositoryImpl
     * @return Repository实例
     */
    public static <R extends BaseRepository<?>> R create(@NonNull Class<R> repositoryClass) {
        try {
            return repositoryClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("创建Repository失败: " + e.getMessage());
        }
    }

    /**
     * 创建带BaseView的Repository
     *
     * @param repositoryClass Repository类
     * @param baseView        基础视图
     * @param <R>             必须继承RepositoryImpl
     * @param <V>             必须继承BaseView
     * @return Repository实例
     */
    public static <R extends BaseRepository<V>, V extends BaseView> R create(
            @NonNull Class<R> repositoryClass,
            @Nullable V baseView) {
        try {
            return repositoryClass.getConstructor(BaseView.class).newInstance(baseView);
        } catch (Exception e) {
            throw new RuntimeException("创建Repository失败: " + e.getMessage());
        }
    }

    /**
     * 创建带重试服务的Repository
     *
     * @param repositoryClass Repository类
     * @param retryService    重试服务
     * @param <R>             必须继承RepositoryImpl
     * @return Repository实例
     */
    public static <R extends BaseRepository<?>> R create(
            @NonNull Class<R> repositoryClass,
            @Nullable RetryService retryService) {
        try {
            return repositoryClass.getConstructor(RetryService.class).newInstance(retryService);
        } catch (Exception e) {
            throw new RuntimeException("创建Repository失败: " + e.getMessage());
        }
    }

    /**
     * 创建带BaseView和重试服务的Repository
     *
     * @param repositoryClass Repository类
     * @param retryService    重试服务
     * @param baseView        基础视图
     * @param <R>             必须继承RepositoryImpl
     * @param <V>             必须继承BaseView
     * @return Repository实例
     */
    public static <R extends BaseRepository<V>, V extends BaseView> R create(
            @NonNull Class<R> repositoryClass,
            @Nullable RetryService retryService,
            @Nullable V baseView) {
        try {
            return repositoryClass.getConstructor(RetryService.class, BaseView.class)
                    .newInstance(retryService, baseView);
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
     * @param <V>             必须继承BaseView
     * @return RepositoryImpl实例
     */
    public static <R extends RepositoryImpl<API, V>, API extends BaseApiService, V extends BaseView> R create(
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
     * 创建带BaseView和API服务的RepositoryImpl
     *
     * @param repositoryClass RepositoryImpl类
     * @param baseView        基础视图
     * @param apiService      API服务
     * @param <R>             必须继承RepositoryImpl
     * @param <API>           必须继承BaseApiService
     * @param <V>             必须继承BaseView
     * @return RepositoryImpl实例
     */
    public static <R extends RepositoryImpl<API, V>, API extends BaseApiService, V extends BaseView> R create(
            @NonNull Class<R> repositoryClass,
            @Nullable V baseView,
            @NonNull API apiService) {
        try {
            return repositoryClass.getConstructor(BaseView.class, apiService.getClass().getInterfaces()[0])
                    .newInstance(baseView, apiService);
        } catch (Exception e) {
            throw new RuntimeException("创建RepositoryImpl失败: " + e.getMessage());
        }
    }

    /**
     * 创建带重试服务和API服务的RepositoryImpl
     *
     * @param repositoryClass RepositoryImpl类
     * @param retryService    重试服务
     * @param apiService      API服务
     * @param <R>             必须继承RepositoryImpl
     * @param <API>           必须继承BaseApiService
     * @param <V>             必须继承BaseView
     * @return RepositoryImpl实例
     */
    public static <R extends RepositoryImpl<API, V>, API extends BaseApiService, V extends BaseView> R create(
            @NonNull Class<R> repositoryClass,
            @Nullable RetryService retryService,
            @NonNull API apiService) {
        try {
            return repositoryClass.getConstructor(RetryService.class, apiService.getClass().getInterfaces()[0])
                    .newInstance(retryService, apiService);
        } catch (Exception e) {
            throw new RuntimeException("创建RepositoryImpl失败: " + e.getMessage());
        }
    }

    /**
     * 创建带BaseView、重试服务和API服务的RepositoryImpl
     *
     * @param repositoryClass RepositoryImpl类
     * @param retryService    重试服务
     * @param baseView        基础视图
     * @param apiService      API服务
     * @param <R>             必须继承RepositoryImpl
     * @param <API>           必须继承BaseApiService
     * @param <V>             必须继承BaseView
     * @return RepositoryImpl实例
     */
    public static <R extends RepositoryImpl<API, V>, API extends BaseApiService, V extends BaseView> R create(
            @NonNull Class<R> repositoryClass,
            @Nullable RetryService retryService,
            @Nullable V baseView,
            @NonNull API apiService) {
        try {
            return repositoryClass.getConstructor(RetryService.class, BaseView.class, apiService.getClass().getInterfaces()[0])
                    .newInstance(retryService, baseView, apiService);
        } catch (Exception e) {
            throw new RuntimeException("创建RepositoryImpl失败: " + e.getMessage());
        }
    }

    /**
     * 创建带RoomDao和BaseView的RoomRepositoryImpl
     *
     * @param repositoryClass RoomRepositoryImpl类
     * @param roomDao         Room数据库访问对象
     * @param baseView        基础视图
     * @param <R>             必须继承RoomRepositoryImpl
     * @param <T>             实体类型
     * @param <DB>            必须继承BaseRoomDao
     * @param <V>             必须继承BaseView
     * @return RoomRepositoryImpl实例
     */
    public static <R extends RoomRepositoryImpl<T, DB, V>, T, DB extends BaseRoomDao<T>, V extends BaseView> R create(
            @NonNull Class<R> repositoryClass,
            @NonNull DB roomDao,
            @Nullable V baseView) {
        try {
            return repositoryClass.getConstructor(BaseRoomDao.class, BaseView.class)
                    .newInstance(roomDao, baseView);
        } catch (Exception e) {
            throw new RuntimeException("创建RoomRepositoryImpl失败: " + e.getMessage());
        }
    }

    /**
     * 创建带RoomDao、BaseView和重试服务的RoomRepositoryImpl
     *
     * @param repositoryClass RoomRepositoryImpl类
     * @param roomDao         Room数据库访问对象
     * @param baseView        基础视图
     * @param retryService    重试服务
     * @param <R>             必须继承RoomRepositoryImpl
     * @param <T>             实体类型
     * @param <DB>            必须继承BaseRoomDao
     * @param <V>             必须继承BaseView
     * @return RoomRepositoryImpl实例
     */
    public static <R extends RoomRepositoryImpl<T, DB, V>, T, DB extends BaseRoomDao<T>, V extends BaseView> R create(
            @NonNull Class<R> repositoryClass,
            @NonNull DB roomDao,
            @Nullable V baseView,
            @Nullable RetryService retryService) {
        try {
            R repository = repositoryClass.getConstructor(BaseRoomDao.class, BaseView.class)
                    .newInstance(roomDao, baseView);
            repository.setRetryService(retryService);
            return repository;
        } catch (Exception e) {
            throw new RuntimeException("创建RoomRepositoryImpl失败: " + e.getMessage());
        }
    }

}

