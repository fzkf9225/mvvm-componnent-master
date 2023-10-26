package com.casic.titan.demo.bean;

/**
 * Created by fz on 2023/10/26 18:56
 * describe :
 */
public class Man<T>{
    private T t;

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }
}
