package pers.fz.mvvm.annotations;

/**
 * Created by fz on 2023/9/5 16:59
 * describe :实体类验证结果
 */
public class VerifyResult {
    private boolean isSuccess;

    private String errorMsg;

    public VerifyResult() {
    }

    public VerifyResult(boolean isSuccess, String errorMsg) {
        this.isSuccess = isSuccess;
        this.errorMsg = errorMsg;
    }

    public boolean isOk() {
        return this.isSuccess;
    }

    public static VerifyResult ok() {
        return new VerifyResult(true, null);
    }

    public static VerifyResult ok(String errorMsg) {
        return new VerifyResult(true, errorMsg);
    }

    public static VerifyResult fail(String errorMsg) {
        return new VerifyResult(false, errorMsg);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "VerifyResult{" +
                "isSuccess=" + isSuccess +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
