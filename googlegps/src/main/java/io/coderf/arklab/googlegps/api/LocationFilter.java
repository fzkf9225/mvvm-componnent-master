package io.coderf.arklab.googlegps.api;
import android.location.Location;

import androidx.annotation.NonNull;

import io.coderf.arklab.googlegps.service.Session;

/**
 * 自定义点位过滤器
 *
 * <p>在内置过滤链（时间倒退、最小间隔、跳点、精度、最小距离、静止抖动）全部通过后执行，
 * 按 {@link GpsSettingConfig#addLocationFilter(LocationFilter)} 的添加顺序依次调用，
 * 任一过滤器返回 false 即丢弃该点并短路后续过滤器。</p>
 *
 * <p>实现内抛出的异常会被服务捕获并按"放行"处理，不会中断定位流程。</p>
 *
 * <pre>{@code
 * GpsSettingConfig.getInstance().addLocationFilter((loc, session, config) -> {
 *     Location prev = session.getCurrentLocationInfo();
 *     return prev == null || loc.distanceTo(prev) < 500;
 * });
 * }</pre>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/15
 */
public interface LocationFilter {

    /**
     * 判断点位是否保留
     *
     * @param location 已通过内置过滤链的当前点
     * @param session  当前会话，可读取上一个点、历史点、累计里程等上下文
     * @param config   当前生效的配置
     * @return true 保留该点；false 丢弃该点
     */
    boolean accept(@NonNull Location location, @NonNull Session session, @NonNull GpsSettingConfig config);

    /**
     * 过滤器名称，用于日志及 {@link GpsCallback#onLocationFiltered} 的原因文案
     */
    @NonNull
    default String getName() {
        return getClass().getSimpleName();
    }
}
