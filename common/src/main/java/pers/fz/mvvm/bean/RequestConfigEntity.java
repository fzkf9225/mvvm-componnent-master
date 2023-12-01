package pers.fz.mvvm.bean;

/**
 * Created by fz on 2023/12/1 8:54
 * describe :
 */
public class RequestConfigEntity {
    private boolean isShowDialog = true;
    private boolean isShowToast = true;
    private String toastMsg;
    private Object requestParams;
    private String dialogMessage = "正在加载，请稍后...";

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

    public Object getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(Object requestParams) {
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

        public Builder setRequestParams(Object requestParams) {
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
