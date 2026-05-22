package io.coderf.arklab.common.bean;

import androidx.annotation.NonNull;

/**
 * Room 数据仓库请求时的 UI 与行为配置，用法对齐 {@link ApiRequestOptions}。
 *
 * <p><b>典型用法：</b></p>
 * <pre>{@code
 * // 1. 静默请求（无 Loading）
 * repository.findAll(RoomRequestOptions.silent());
 *
 * // 2. 兼容老代码：仍可用 showLoading 布尔参数，内部会转成对应 Options
 * repository.insert(person, true);
 *
 * // 3. 自定义 Loading 文案与超时
 * RoomRequestOptions options = new RoomRequestOptions.Builder()
 *         .setShowDialog(true)
 *         .setDialogMessage("正在同步本地数据...")
 *         .setEnableDynamicEllipsis(true)
 *         .setTimeoutSeconds(60)
 *         .build();
 * repository.findAll(options);
 * }</pre>
 *
 * <p>Loading 仅通过 {@link io.coderf.arklab.common.repository.RoomRepositoryImpl} 所在
 * {@link io.coderf.arklab.common.base.BaseRepository#getRequestUi()} 展示，
 * 需由 {@link io.coderf.arklab.common.base.BaseViewModel} 注入，勿直接调 BaseView。</p>
 *
 * @author fz
 * @see io.coderf.arklab.common.repository.RoomRepositoryImpl
 * @see ApiRequestOptions
 */
public class RoomRequestOptions {

    /** 是否显示加载框 */
    private boolean showDialog = false;
    /** 加载框提示文案 */
    private String dialogMessage = "正在加载，请稍后...";
    /** 是否启用加载文案动态省略号 */
    private boolean enableDynamicEllipsis = false;
    /** Rx 超时时间（秒），≤0 表示不超时 */
    private long timeoutSeconds = 30L;
    /**
     * 查询类 Flowable 在结果为空时是否抛出 {@link io.coderf.arklab.common.base.BaseException}
     * （NOT_FOUND）；删除类使用 DELETE_SUCCESS，见仓库内部逻辑。
     */
    private boolean throwOnEmptyList = false;

    private RoomRequestOptions() {
    }

    /** @return 是否显示加载框 */
    public boolean isShowDialog() {
        return showDialog;
    }

    /** @return 加载框文案 */
    public String getDialogMessage() {
        return dialogMessage;
    }

    /** @return 是否动态省略号 */
    public boolean isEnableDynamicEllipsis() {
        return enableDynamicEllipsis;
    }

    /** @return 超时秒数 */
    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    /** @return 空列表是否按业务异常处理 */
    public boolean isThrowOnEmptyList() {
        return throwOnEmptyList;
    }

    /**
     * 静默模式：不显示 Loading，不抛空列表异常（除非 Builder 另行设置）。
     */
    public static RoomRequestOptions silent() {
        return new Builder().setShowDialog(false).build();
    }

    /**
     * 仅显示 Loading，使用自定义文案。
     *
     * @param message 加载提示
     */
    public static RoomRequestOptions withLoading(@NonNull String message) {
        return new Builder()
                .setShowDialog(true)
                .setDialogMessage(message)
                .build();
    }

    /**
     * 插入操作的默认配置（兼容 {@code insert(obj, showLoading)}）。
     *
     * @param showLoading 是否显示加载框
     * @param enableDynamicEllipsis 是否动态省略号
     */
    public static RoomRequestOptions insert(boolean showLoading, boolean enableDynamicEllipsis) {
        if (!showLoading) {
            return silent();
        }
        return new Builder()
                .setShowDialog(true)
                .setDialogMessage("正在插入数据,请稍后...")
                .setEnableDynamicEllipsis(enableDynamicEllipsis)
                .build();
    }

    /**
     * 删除操作的默认配置；空结果会触发删除成功业务码。
     */
    public static RoomRequestOptions delete(boolean showLoading, boolean enableDynamicEllipsis) {
        if (!showLoading) {
            return silent();
        }
        return new Builder()
                .setShowDialog(true)
                .setDialogMessage("正在删除数据,请稍后...")
                .setEnableDynamicEllipsis(enableDynamicEllipsis)
                .setThrowOnEmptyList(true)
                .build();
    }

    /** 更新操作的默认配置 */
    public static RoomRequestOptions update(boolean showLoading, boolean enableDynamicEllipsis) {
        if (!showLoading) {
            return silent();
        }
        return new Builder()
                .setShowDialog(true)
                .setDialogMessage("正在更新数据,请稍后...")
                .setEnableDynamicEllipsis(enableDynamicEllipsis)
                .build();
    }

    /** 查询操作的默认配置；空列表会 NOT_FOUND */
    public static RoomRequestOptions query(boolean showLoading, boolean enableDynamicEllipsis) {
        if (!showLoading) {
            return silent();
        }
        return new Builder()
                .setShowDialog(true)
                .setDialogMessage("正在查询数据,请稍后...")
                .setEnableDynamicEllipsis(enableDynamicEllipsis)
                .setThrowOnEmptyList(true)
                .build();
    }

    /**
     * 构建器，用于完全自定义 Room 请求行为。
     */
    public static class Builder {
        private final RoomRequestOptions options = new RoomRequestOptions();

        /** @param showDialog 是否显示加载框 */
        public Builder setShowDialog(boolean showDialog) {
            options.showDialog = showDialog;
            return this;
        }

        /** @param dialogMessage 加载文案 */
        public Builder setDialogMessage(String dialogMessage) {
            options.dialogMessage = dialogMessage;
            return this;
        }

        /** @param enableDynamicEllipsis 动态省略号 */
        public Builder setEnableDynamicEllipsis(boolean enableDynamicEllipsis) {
            options.enableDynamicEllipsis = enableDynamicEllipsis;
            return this;
        }

        /** @param timeoutSeconds 超时（秒） */
        public Builder setTimeoutSeconds(long timeoutSeconds) {
            options.timeoutSeconds = timeoutSeconds;
            return this;
        }

        /** @param throwOnEmptyList 空列表是否抛业务异常 */
        public Builder setThrowOnEmptyList(boolean throwOnEmptyList) {
            options.throwOnEmptyList = throwOnEmptyList;
            return this;
        }

        public RoomRequestOptions build() {
            return options;
        }
    }
}
