package io.coderf.arklab.annotation.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 实体类验证结果
 */
public class VerifyResult {

    private boolean isSuccess;
    private String errorMsg;
    private String fieldName;
    private List<FieldVerifyError> errors;

    public VerifyResult() {
    }

    public VerifyResult(boolean isSuccess, String errorMsg) {
        this.isSuccess = isSuccess;
        this.errorMsg = errorMsg;
    }

    public VerifyResult(boolean isSuccess, String fieldName, String errorMsg) {
        this.isSuccess = isSuccess;
        this.fieldName = fieldName;
        this.errorMsg = errorMsg;
    }

    public boolean isOk() {
        return isSuccess;
    }

    public boolean isFail() {
        return !isSuccess;
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

    public static VerifyResult fail(String fieldName, String errorMsg) {
        VerifyResult result = new VerifyResult(false, fieldName, errorMsg);
        result.errors = Collections.singletonList(new FieldVerifyError(fieldName, errorMsg));
        return result;
    }

    public static VerifyResult aggregate(List<FieldVerifyError> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return ok();
        }
        VerifyResult result = new VerifyResult(false, fieldErrors.get(0).getFieldName(),
                fieldErrors.get(0).getErrorMsg());
        result.errors = new ArrayList<>(fieldErrors);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < fieldErrors.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            FieldVerifyError error = fieldErrors.get(i);
            builder.append(error.getFieldName()).append(": ").append(error.getErrorMsg());
        }
        result.errorMsg = builder.toString();
        return result;
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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public List<FieldVerifyError> getErrors() {
        if (errors == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(errors);
    }

    public void setErrors(List<FieldVerifyError> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "VerifyResult{" +
                "isSuccess=" + isSuccess +
                ", fieldName='" + fieldName + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", errors=" + errors +
                '}';
    }
}
