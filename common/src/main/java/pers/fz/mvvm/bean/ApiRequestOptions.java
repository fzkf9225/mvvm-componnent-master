package pers.fz.mvvm.bean;

import androidx.annotation.NonNull;

import java.util.Map;

/**
 * Created by fz on 2023/12/1 8:54
 * describe :
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

    public static class Builder {
        private volatile ApiRequestOptions apiRequestOptions;

        public Builder() {
            if (apiRequestOptions == null) {
                synchronized (ApiRequestOptions.class) {
                    if (apiRequestOptions == null) {
                        apiRequestOptions = new ApiRequestOptions();
                    }
                }
            }
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

        public Builder setRequestParams(Map<String,?> requestParams) {
            apiRequestOptions.setRequestParams(requestParams);
            return this;
        }

        public Builder setDialogMessage(String dialogMessage) {
            apiRequestOptions.setDialogMessage(dialogMessage);
            return this;
        }

        public ApiRequestOptions build() {
            return apiRequestOptions;
        }
    }

    public static ApiRequestOptions getDefault() {
        return new Builder()
                .setShowDialog(true)
                .setDialogMessage("正在加载，请稍后...")
                .setShowToast(true)
                .build();
    }

    @NonNull
    @Override
    public String toString() {
        return "RequestConfigEntity{" +
                "isShowDialog=" + isShowDialog +
                ", isShowToast=" + isShowToast +
                ", toastMsg='" + toastMsg + '\'' +
                ", requestParams=" + requestParams +
                ", dialogMessage='" + dialogMessage + '\'' +
                '}';
    }
}
