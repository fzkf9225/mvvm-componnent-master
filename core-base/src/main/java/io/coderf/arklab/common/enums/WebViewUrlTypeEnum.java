package io.coderf.arklab.common.enums;


/**
 * created by fz on 2025/6/27 09:08
 * describe:WebView的地址类型
 */
public enum WebViewUrlTypeEnum {
    /**
     * 网络地址：http / https，由系统 WebView 直接加载。
     */
    INTERNET(1, "网络地址"),
    /**
     * 打包在 APK assets 目录下的静态页，经 {@link androidx.webkit.WebViewAssetLoader} 映射加载。
     */
    ASSETS(2, "本地assets"),
    /**
     * 自动判断：以 http(s) 开头走网络，否则按 assets 相对路径处理。
     */
    AUTO(3, "智能判断"),
    /**
     * 应用沙盒 / 外部私有目录 / 系统下载目录中的 html、pdf 等文件。
     * <p>支持协议前缀：{@code local://}、{@code external://}、{@code download://}，
     * 或直接传相对于 {@code filesDir} 的路径。</p>
     */
    LOCAL(4, "应用本地存储"),
    ;

    private WebViewUrlTypeEnum(int type, String describe) {
        this.type = type;
        this.describe = describe;
    }

    public final int type;
    public final String describe;

}

