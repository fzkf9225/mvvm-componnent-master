package io.coderf.arklab.common.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.coderf.arklab.core.request.LoadingCancelPolicy;

/**
 * Room 数据仓库请求时的行为配置，属于 {@code core-db}。
 * <p>
 * <b>协程栈</b>（{@link io.coderf.arklab.core.db.BaseRoomRepository}）：仅超时 / 空列表等字段生效；
 * {@code showDialog} / {@code deliverErrorToUi} / Toast 字段<strong>无效</strong>，UI 由 ViewModel
 * {@code launchRequest} + {@code collectSuccess} 使用网络侧 {@link io.coderf.arklab.core.bean.RequestOptions}。
 * <p>
 * <b>Rx 栈</b>（{@link io.coderf.arklab.common.repository.RoomRepositoryImpl}）：Loading 仍经
 * {@link io.coderf.arklab.core.request.RequestUi}；错误展示由订阅方负责。
 *
 * <p><b>典型用法：</b></p>
 * <pre>{@code
 * // 协程：仓库 silent；UI 在 ViewModel
 * repository.execute(RoomRequestOptions.silent(), () -> dao.findAllSuspend());
 * // ViewModel: launchRequest(RequestOptions.cancellable()) { repo.execute(...).collectSuccess { } }
 *
 * // Rx 存量：showLoading 仍走 RequestUi
 * repository.insert(person, true);
 * }</pre>
 *
 * @see io.coderf.arklab.common.repository.RoomRepositoryImpl
 * @see io.coderf.arklab.core.db.BaseRoomRepository
 * @see ApiRequestOptions
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/13
 */
public class RoomRequestOptions {

    /** 是否显示加载框 */
    private boolean showDialog = false;
    /** 加载框提示文案 */
    private String dialogMessage = "正在加载，请稍后...";
    /** 是否启用加载文案动态省略号 */
    private boolean enableDynamicEllipsis = true;
    /**
     * Loading 用户取消策略；默认 {@link LoadingCancelPolicy#None}。
     */
    @NonNull
    private LoadingCancelPolicy loadingCancelPolicy = LoadingCancelPolicy.None;
    /**
     * 超时时间（秒）。≤0 表示不超时。
     * Rx 走 {@code timeout(seconds)}；协程栈走 {@code withTimeout(seconds)}。
     */
    private long timeoutSeconds = 15L;
    /**
     * 查询类结果为空列表时是否抛出 {@link io.coderf.arklab.common.base.BaseException}
     * （NOT_FOUND）。条件删除请使用返回影响行数的 {@code delete*Count} API。
     */
    private boolean throwOnEmptyList = false;
    /**
     * 是否把错误交给 RequestUi（协程栈）。默认 true。
     * Rx 栈不读取本字段，错误仍由订阅方处理。
     */
    private boolean deliverErrorToUi = true;
    /**
     * 是否弹出错误 Toast（对齐 {@link ApiRequestOptions#isShowToast()}）。
     * 仅在 {@link #deliverErrorToUi} 为 true 时由协程栈生效。
     */
    private boolean showErrorToast = true;
    /**
     * 覆盖错误 Toast 文案（对齐 {@link ApiRequestOptions#getToastMsg()}）；
     * null / 空则用异常原文。
     */
    @Nullable
    private String errorToastMessage;

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

    /**
     * @return Loading 用户取消策略
     */
    @NonNull
    public LoadingCancelPolicy getLoadingCancelPolicy() {
        return loadingCancelPolicy;
    }

    /** @return 超时秒数；≤0 表示不超时 */
    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    /** @return 空列表是否按业务异常处理 */
    public boolean isThrowOnEmptyList() {
        return throwOnEmptyList;
    }

    /** @return 协程栈是否把错误交给 RequestUi */
    public boolean isDeliverErrorToUi() {
        return deliverErrorToUi;
    }

    /** @return 是否弹出错误 Toast */
    public boolean isShowErrorToast() {
        return showErrorToast;
    }

    /** @return 覆盖用的错误 Toast 文案，可为 null */
    @Nullable
    public String getErrorToastMessage() {
        return errorToastMessage;
    }

    /**
     * 与 Builder 字段默认值一致：无 Loading、15s 超时、错误交 UI。
     */
    public static RoomRequestOptions defaults() {
        return new Builder().build();
    }

    /**
     * 静默模式：无 Loading；协程栈不把错误交给 RequestUi（对齐网络 RequestOptions.silent()）。
     * 不抛空列表异常（除非 Builder 另行设置）。
     */
    public static RoomRequestOptions silent() {
        return new Builder()
                .setShowDialog(false)
                .setDeliverErrorToUi(false)
                .build();
    }

    /**
     * 保留 Loading / 错误回调，但不弹 Toast（对齐网络 RequestOptions.noToast()）。
     */
    public static RoomRequestOptions noToast() {
        return new Builder().setShowErrorToast(false).build();
    }

    /**
     * 不设置超时（适合大批量写入 / 复杂查询）。
     */
    public static RoomRequestOptions noTimeout() {
        return new Builder().setTimeoutSeconds(0).build();
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
     * 可取消 Loading：取消请求后留在当前页（对齐网络 {@code RequestOptions.cancellable()}）。
     */
    @NonNull
    public static RoomRequestOptions cancellable() {
        return new Builder()
                .setShowDialog(true)
                .setLoadingCancelPolicy(LoadingCancelPolicy.CancelRequest)
                .build();
    }

    /**
     * 可取消 Loading：取消后离开当前页（对齐网络 {@code RequestOptions.cancellableExit()}）。
     */
    @NonNull
    public static RoomRequestOptions cancellableExit() {
        return new Builder()
                .setShowDialog(true)
                .setLoadingCancelPolicy(LoadingCancelPolicy.CancelRequestAndExit)
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
     * 删除操作的默认配置（配合 {@code delete*Count} 等返回影响行数的 API）。
     */
    public static RoomRequestOptions delete(boolean showLoading, boolean enableDynamicEllipsis) {
        if (!showLoading) {
            return silent();
        }
        return new Builder()
                .setShowDialog(true)
                .setDialogMessage("正在删除数据,请稍后...")
                .setEnableDynamicEllipsis(enableDynamicEllipsis)
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

    /**
     * 查询操作的默认配置；空列表会 NOT_FOUND。
     * 无 Loading 时仍保留 {@code throwOnEmptyList=true}（不再误用 silent()）。
     */
    public static RoomRequestOptions query(boolean showLoading, boolean enableDynamicEllipsis) {
        return new Builder()
                .setShowDialog(showLoading)
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

        /**
         * @param loadingCancelPolicy Loading 用户取消策略
         */
        public Builder setLoadingCancelPolicy(@NonNull LoadingCancelPolicy loadingCancelPolicy) {
            options.loadingCancelPolicy = loadingCancelPolicy != null
                    ? loadingCancelPolicy
                    : LoadingCancelPolicy.None;
            return this;
        }

        /**
         * @param timeoutSeconds 超时（秒）；≤0 表示不超时
         */
        public Builder setTimeoutSeconds(long timeoutSeconds) {
            options.timeoutSeconds = timeoutSeconds;
            return this;
        }

        /** @param throwOnEmptyList 空列表是否抛业务异常 */
        public Builder setThrowOnEmptyList(boolean throwOnEmptyList) {
            options.throwOnEmptyList = throwOnEmptyList;
            return this;
        }

        /** @param deliverErrorToUi 协程栈是否把错误交给 RequestUi */
        public Builder setDeliverErrorToUi(boolean deliverErrorToUi) {
            options.deliverErrorToUi = deliverErrorToUi;
            return this;
        }

        /** @param showErrorToast 是否弹出错误 Toast */
        public Builder setShowErrorToast(boolean showErrorToast) {
            options.showErrorToast = showErrorToast;
            return this;
        }

        /** @param errorToastMessage 覆盖错误 Toast 文案，null 表示用异常原文 */
        public Builder setErrorToastMessage(@Nullable String errorToastMessage) {
            options.errorToastMessage = errorToastMessage;
            return this;
        }

        public RoomRequestOptions build() {
            return options;
        }
    }
}
