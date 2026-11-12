package io.coderf.arklab.common.bean;

import androidx.annotation.NonNull;

import java.util.Map;

import io.coderf.arklab.core.request.LoadingCancelPolicy;

/**
 * 旧栈 Rx 请求选项（与新栈 {@code RequestOptions} 对齐 Loading / Toast / 取消策略）。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2023/12/1 8:54
 * @updated 2026/11/12
 */
public class ApiRequestOptions {
    /**
     * 是否展示加载框
     */
    private boolean isShowDialog = true;
    /**
     * 是否展示吐司提示
     */
    private boolean isShowToast = true;
    /**
     * 默认吐司提示，如果这个设置了值则就会提示这个不会提示返回的错误内容了
     */
    private String toastMsg;
    /**
     * 需要返回的数据
     */
    private Map<String,?> requestParams;
    /**
     * 默认加载提示内容
     */
    private String dialogMessage = "正在加载，请稍后...";
    /**
     * 是否启用动态省略号效果
     */
    private boolean enableDynamicEllipsis = true;
    /**
     * Loading 用户取消策略；默认 {@link LoadingCancelPolicy#None}（兼容历史：不可取消）。
     */
    @NonNull
    private LoadingCancelPolicy loadingCancelPolicy = LoadingCancelPolicy.None;

    private ApiRequestOptions() {
    }

    public boolean isShowDialog() {
        return isShowDialog;
    }

    public void setShowDialog(boolean showDialog) {
        isShowDialog = showDialog;
    }

    public boolean isShowToast() {
        return isShowToast;
    }

    public void setShowToast(boolean showToast) {
        isShowToast = showToast;
    }

    public String getToastMsg() {
        return toastMsg;
    }

    public void setToastMsg(String toastMsg) {
        this.toastMsg = toastMsg;
    }

    public Map<String,?> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(Map<String,?> requestParams) {
        this.requestParams = requestParams;
    }

    public String getDialogMessage() {
        return dialogMessage;
    }

    public void setDialogMessage(String dialogMessage) {
        this.dialogMessage = dialogMessage;
    }

    public boolean isEnableDynamicEllipsis() {
        return enableDynamicEllipsis;
    }

    public void setEnableDynamicEllipsis(boolean enableDynamicEllipsis) {
        this.enableDynamicEllipsis = enableDynamicEllipsis;
    }

    /**
     * @return Loading 用户取消策略
     */
    @NonNull
    public LoadingCancelPolicy getLoadingCancelPolicy() {
        return loadingCancelPolicy;
    }

    /**
     * @param loadingCancelPolicy Loading 用户取消策略，null 时回落为 {@link LoadingCancelPolicy#None}
     */
    public void setLoadingCancelPolicy(LoadingCancelPolicy loadingCancelPolicy) {
        this.loadingCancelPolicy = loadingCancelPolicy != null
                ? loadingCancelPolicy
                : LoadingCancelPolicy.None;
    }

    public static class Builder {
        private final ApiRequestOptions apiRequestOptions;

        public Builder() {
            // 每次 Builder 独立实例，避免多次 build / 多请求之间串改配置
            apiRequestOptions = new ApiRequestOptions();
        }

        public Builder setShowDialog(boolean showDialog) {
            apiRequestOptions.setShowDialog(showDialog);
            return this;
        }

        public Builder setShowToast(boolean showToast) {
            apiRequestOptions.setShowToast(showToast);
            return this;
        }

        public Builder setToastMsg(String toastMsg) {
            apiRequestOptions.setToastMsg(toastMsg);
            return this;
        }

        public Builder enableDynamicEllipsis(boolean enableDynamicEllipsis) {
            apiRequestOptions.setEnableDynamicEllipsis(enableDynamicEllipsis);
            return this;
        }

        public Builder setRequestParams(Map<String,?> requestParams) {
            apiRequestOptions.setRequestParams(requestParams);
            return this;
        }

        public Builder setDialogMessage(String dialogMessage) {
            apiRequestOptions.setDialogMessage(dialogMessage);
            return this;
        }

        /**
         * @param loadingCancelPolicy Loading 用户取消策略
         */
        public Builder setLoadingCancelPolicy(@NonNull LoadingCancelPolicy loadingCancelPolicy) {
            apiRequestOptions.setLoadingCancelPolicy(loadingCancelPolicy);
            return this;
        }

        public ApiRequestOptions build() {
            return apiRequestOptions;
        }
    }

    /** 默认：有 Loading、有 Toast、Loading 不可取消。 */
    public static ApiRequestOptions getDefault() {
        return new Builder()
                .setShowDialog(true)
                .setDialogMessage("正在加载，请稍后...")
                .setShowToast(true)
                .setLoadingCancelPolicy(LoadingCancelPolicy.None)
                .build();
    }

    /**
     * 可取消 Loading：取消请求后留在当前页（对齐新栈 {@code RequestOptions.cancellable()}）。
     */
    @NonNull
    public static ApiRequestOptions cancellable() {
        return new Builder()
                .setShowDialog(true)
                .setDialogMessage("正在加载，请稍后...")
                .setShowToast(true)
                .setLoadingCancelPolicy(LoadingCancelPolicy.CancelRequest)
                .build();
    }

    /**
     * 可取消 Loading：取消后离开当前页（对齐新栈 {@code RequestOptions.cancellableExit()}）。
     */
    @NonNull
    public static ApiRequestOptions cancellableExit() {
        return new Builder()
                .setShowDialog(true)
                .setDialogMessage("正在加载，请稍后...")
                .setShowToast(true)
                .setLoadingCancelPolicy(LoadingCancelPolicy.CancelRequestAndExit)
                .build();
    }

    @NonNull
    @Override
    public String toString() {
        return "ApiRequestOptions{" +
                "isShowDialog=" + isShowDialog +
                ", isShowToast=" + isShowToast +
                ", enableDynamicEllipsis=" + enableDynamicEllipsis +
                ", loadingCancelPolicy=" + loadingCancelPolicy +
                ", toastMsg='" + toastMsg + '\'' +
                ", requestParams=" + requestParams +
                ", dialogMessage='" + dialogMessage + '\'' +
                '}';
    }
}
