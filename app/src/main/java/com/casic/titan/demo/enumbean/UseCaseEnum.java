package com.casic.titan.demo.enumbean;

import android.os.Bundle;

import com.casic.titan.demo.activity.CoordinatorActivity;
import com.casic.titan.demo.activity.DemoPagingActivity;
import com.casic.titan.demo.activity.DemoSmartPagingActivity;
import com.casic.titan.demo.activity.DialogActivity;
import com.casic.titan.demo.activity.DownloadActivity;
import com.casic.titan.demo.activity.GoogleGPSActivity;
import com.casic.titan.demo.activity.HiltActivity;
import com.casic.titan.demo.activity.KotlinActivity;
import com.casic.titan.demo.activity.MediaActivity;
import com.casic.titan.demo.activity.MediaCompressActivity;
import com.casic.titan.demo.activity.RecyclerViewSampleActivity;
import com.casic.titan.demo.activity.ScanQrCodeActivity;
import com.casic.titan.demo.activity.VerifyActivity;
import com.casic.titan.demo.activity.ViewPagerSampleActivity;
import com.casic.titan.demo.activity.WightActivity;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.googlegps.GpsLoggingService;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.activity.VideoPlayerActivity;

/**
 * Created by fz on 2023/8/14 10:19
 * describe :
 */
public enum UseCaseEnum {
    /**
     * 自定义组件
     */
    WIGHT(WightActivity.class, "自定义组件", "自定义实现的一些View、ViewGroup", null),
    DIALOG(DialogActivity.class, "自定义dialog", "自定义实现的一些Vdialog", null),
    MEDIA(MediaActivity.class, "媒体访问", "自定义媒体dialog可以实现选择图片、视频等", null),
    DOWNLOAD(DownloadActivity.class, "文件下载", "自定义断点续传下载功能", null),
    COMPRESS(MediaCompressActivity.class, "文件压缩", "展示文件压缩能力", null),
    RECYCLER_VIEW(RecyclerViewSampleActivity.class, "列表示例", "自定义封装列表", null),
    PAGING(DemoPagingActivity.class, "Paging列表示例", "Jetpack Paging3列表封装", null),
    SMART_PAGING(DemoSmartPagingActivity.class, "SmartPaging列表示例", "Smart库+Jetpack Paging3综合列表封装", null),
    VIEW_PAGER(ViewPagerSampleActivity.class, "ViewPagerFragment", "侧滑标签页面", null),
    VIDEO_PLAYER(VideoPlayerActivity.class, "视频播放器", "在线视频播放器，集成自GSYVideoPlayer", getVideoBundle()),
    COORDINATOR(CoordinatorActivity.class, "CoordinatorLayout沉浸式", "侧滑标签页面", null),
    KOTLIN(KotlinActivity.class, "kotlin父类测试", "测试kotlin父类代码", null),
    ANNOTATION(VerifyActivity.class, "自定义注解测试", "通过自定义注解验证实体类参数问题", null),
    SCAN_QR_CODE(ScanQrCodeActivity.class, "二维码能力", "基于ZXING的扫描二维码、识别图片中二维码、生成二维码图片和生成带logo的二维码图片", null),
    GOOGLE_GPS(GoogleGPSActivity.class, "谷歌GPS", "基于Github上的gpslogger工具的gps定位辅助类", null),
    HILT(HiltActivity.class, "Hilt依赖注入", "官方的Hilt依赖注入demo演示", null),

    ;

    private static Bundle getVideoBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(VideoPlayerActivity.VIDEO_TITLE, "测试标题");
        bundle.putBoolean(VideoPlayerActivity.CACHE_ENABLE, false);
        //自己加地址试试吧,这个地址不行，因为他不是视频链接
        bundle.putString(VideoPlayerActivity.VIDEO_PATH, "https://www.bilibili.com/video/BV1sX4y1L7BV?t=15.2");
        return bundle;
    }

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
     *
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
