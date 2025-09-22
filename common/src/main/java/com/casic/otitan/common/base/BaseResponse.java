package com.casic.otitan.common.base;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;


public class BaseResponse<T>{
    public BaseResponse() {
    }

    public BaseResponse(String code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public BaseResponse(String code, String message, T data) {
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
    @SerializedName(value = "message", alternate = {"msg", "error_description"})
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

    @NonNull
    @Override
    public String toString() {
        return "BaseResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

}