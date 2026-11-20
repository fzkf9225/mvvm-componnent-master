package io.coderf.arklab.core.request;

import androidx.annotation.NonNull;

/**
 * 一次 Loading 占用的票据。
 * <p>
 * {@link io.coderf.arklab.common.base.NetworkRequestUiHost#openLoading} 发出，
 * {@link io.coderf.arklab.common.base.NetworkRequestUiHost#closeLoading} 按票据关闭。
 * 并发请求完成顺序不一致时，不能靠先进先出猜测该关哪一次。
 * <p>
 * {@link #NONE} 表示这次没有真正占住 Host 上的会话（例如未展示 Loading）。
 *
 * @author fz
 * @version 1.0
 * @since 2.0.0
 */
public final class LoadingTicket {

    /** 未占用会话。关闭它是空操作。 */
    @NonNull
    public static final LoadingTicket NONE = new LoadingTicket();

    private LoadingTicket() {
    }

    /**
     * 新建一张尚未登记的票据。只有放进 Host 会话表之后，关闭才会生效。
     */
    @NonNull
    public static LoadingTicket acquire() {
        return new LoadingTicket();
    }

    /** 是否对应一次真实的 Loading 占用。 */
    public boolean isActive() {
        return this != NONE;
    }
}
