package com.casic.titan.demo.bean;

import java.lang.reflect.Type;

import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/10/26 18:57
 * describe :
 */
public class Boy extends Man<Boy>{
    public void show(){
        Type type = getClass().getGenericSuperclass();
        LogUtil.show("MainActivity", "type:" + type);
    }
}
