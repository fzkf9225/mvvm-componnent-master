package com.casic.otitan.wscomponent;

public interface Callback<T> {
    void onEvent(String code, String msg, T t);
}
