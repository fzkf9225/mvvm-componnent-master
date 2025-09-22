package com.casic.otitan.demo.bean;

import javax.inject.Inject;

/**
 * Created by fz on 2024/5/31 14:28
 * describe :
 */
public class HiltTestBean {
    private String name;
    private String password;

    @Inject
    public HiltTestBean() {
        this.name = "张三";
        this.password = "123456";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "HiltTestBean{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
