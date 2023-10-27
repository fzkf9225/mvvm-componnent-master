package pers.fz.mvvm.base;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BaseModelEntity<T> implements Serializable {
    public BaseModelEntity() {
    }

    public BaseModelEntity(String code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public BaseModelEntity(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 类型：Number  必有字段  备注：错误标识,根据该字段判断服务器操作是否成功
     */
    @SerializedName(value = "code", alternate = {"error"})
    private String code;
    /**
     * 类型：String  必有字段  备注：错误信息
     */
    @SerializedName(value = "message", alternate = {"msg","error_description"})
    private String message;
    @SerializedName("data")
    private T data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaseModelEntity{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}