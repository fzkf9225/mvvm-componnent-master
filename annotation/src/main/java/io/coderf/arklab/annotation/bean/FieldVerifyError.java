package io.coderf.arklab.annotation.bean;

/**
 * 单字段校验错误信息。
 */
public class FieldVerifyError {

    private final String fieldName;
    private final String errorMsg;

    public FieldVerifyError(String fieldName, String errorMsg) {
        this.fieldName = fieldName;
        this.errorMsg = errorMsg;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public String toString() {
        return fieldName + ": " + errorMsg;
    }
}
