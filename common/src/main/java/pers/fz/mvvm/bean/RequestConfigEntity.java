package pers.fz.mvvm.bean;

import java.util.Map;

/**
 * Created by fz on 2023/12/1 8:54
 * describe :
 */
public class RequestConfigEntity {
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

    private RequestConfigEntity() {
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
        private volatile RequestConfigEntity requestConfigEntity;

        public Builder() {
            if (requestConfigEntity == null) {
                synchronized (RequestConfigEntity.class) {
                    if (requestConfigEntity == null) {
                        requestConfigEntity = new RequestConfigEntity();
                    }
                }
            }
        }

        public Builder setShowDialog(boolean showDialog) {
            requestConfigEntity.setShowDialog(showDialog);
            return this;
        }

        public Builder setShowToast(boolean showToast) {
            requestConfigEntity.setShowToast(showToast);
            return this;
        }

        public Builder setToastMsg(String toastMsg) {
            requestConfigEntity.setToastMsg(toastMsg);
            return this;
        }

        public Builder setRequestParams(Map<String,?> requestParams) {
            requestConfigEntity.setRequestParams(requestParams);
            return this;
        }

        public Builder setDialogMessage(String dialogMessage) {
            requestConfigEntity.setDialogMessage(dialogMessage);
            return this;
        }

        public RequestConfigEntity build() {
            return requestConfigEntity;
        }
    }

    public static RequestConfigEntity getDefault() {
        return new Builder()
                .setShowDialog(true)
                .setDialogMessage("正在加载，请稍后...")
                .setShowToast(true)
                .build();
    }

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
