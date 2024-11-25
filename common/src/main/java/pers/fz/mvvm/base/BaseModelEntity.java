package pers.fz.mvvm.base;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        //Observable 不能直接返回 null，所以根据泛型创建一个空对象
        if (this.data == null) {
            try {
                // 判断泛型 T 的类型并创建对应的默认值
                if (isStringType()) {
                    this.data = (T) "";
                } else if (isBooleanType()) {
                    this.data = (T) Boolean.FALSE;
                } else if (isIntegerType()) {
                    this.data = (T) Integer.valueOf(0);
                } else if (isLongType()) {
                    this.data = (T) Long.valueOf(0);
                } else if (isDoubleType()) {
                    this.data = (T) Double.valueOf(0);
                } else if (isFloatType()) {
                    this.data = (T) Float.valueOf(0);
                } else if (isListType()) {
                    this.data = (T) new ArrayList<>();
                } else if (isMapType()) {
                    this.data = (T) new HashMap<>();
                } else if (isArrayType()) {
                    try {
                        this.data = (T) Array.newInstance(data.getClass().getComponentType(), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.data = (T) new Object[0];
                    }
                } else {
                    try {
                        // 如果是其他实体类，则尝试用反射创建实例
                        this.data = (T) data.getClass().getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.data = (T) new Object();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private boolean isStringType() {
        return String.class.isAssignableFrom(data.getClass());
    }

    private boolean isBooleanType() {
        return Boolean.class.isAssignableFrom(data.getClass()) || boolean.class.isAssignableFrom(data.getClass());
    }

    private boolean isIntegerType() {
        return Integer.class.isAssignableFrom(data.getClass()) || int.class.isAssignableFrom(data.getClass());
    }

    private boolean isLongType() {
        return Long.class.isAssignableFrom(data.getClass()) || long.class.isAssignableFrom(data.getClass());
    }

    private boolean isDoubleType() {
        return Double.class.isAssignableFrom(data.getClass()) || double.class.isAssignableFrom(data.getClass());
    }

    private boolean isFloatType() {
        return Float.class.isAssignableFrom(data.getClass()) || float.class.isAssignableFrom(data.getClass());
    }

    private boolean isListType() {
        return List.class.isAssignableFrom(data.getClass());
    }

    private boolean isMapType() {
        return Map.class.isAssignableFrom(data.getClass());
    }

    private boolean isArrayType() {
        return data.getClass().isArray();
    }
}