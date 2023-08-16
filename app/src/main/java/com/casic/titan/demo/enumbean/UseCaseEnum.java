package com.casic.titan.demo.enumbean;

import android.os.Bundle;

import com.casic.titan.demo.activity.DialogActivity;
import com.casic.titan.demo.activity.DownloadActivity;
import com.casic.titan.demo.activity.MediaActivity;
import com.casic.titan.demo.activity.MediaCompressActivity;
import com.casic.titan.demo.activity.WightActivity;
import com.casic.titan.demo.bean.UseCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fz on 2023/8/14 10:19
 * describe :
 */
public enum UseCaseEnum {
    /**
     * 自定义组件
     */
    WIGHT(WightActivity.class,"自定义组件","自定义实现的一些View、ViewGroup",null),
    DIALOG(DialogActivity.class,"自定义dialog","自定义实现的一些Vdialog",null),
    MEDIA(MediaActivity.class,"媒体访问","自定义媒体dialog可以实现选择图片、视频等",null),
    DOWNLOAD(DownloadActivity.class,"文件下载","自定义断点续传下载功能",null),
    COMPRESS(MediaCompressActivity.class,"文件压缩","展示文件压缩能力",null),

    ;

    UseCaseEnum(Class<?> clx, String name, String describe, Bundle args) {
        this.clx = clx;
        this.name = name;
        this.describe = describe;
        this.args = args;
    }

    private final Class<?> clx;
    private final String name;
    private final String describe;
    private final Bundle args;

    public Class<?> getClx() {
        return clx;
    }

    public String getName() {
        return name;
    }

    public String getDescribe() {
        return describe;
    }

    public Bundle getArgs() {
        return args;
    }

    /**
     * toList
     * @return 示例合集
     */
    public static List<UseCase> toUseCaseList() {
        List<UseCase> useCases = new ArrayList<>();
        for (UseCaseEnum useCaseEnum : UseCaseEnum.values()) {
            useCases.add(new UseCase(useCaseEnum.getClx(), useCaseEnum.name, useCaseEnum.describe, useCaseEnum.args));
        }
        return useCases;
    }
}
