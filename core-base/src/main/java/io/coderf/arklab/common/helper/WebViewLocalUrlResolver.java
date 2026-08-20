package io.coderf.arklab.common.helper;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

import io.coderf.arklab.common.enums.WebViewUrlTypeEnum;

/**
 * 将业务层传入的 URL 解析为 WebView 可加载的最终地址。
 * <p>
 * 网络地址直接返回；assets / 沙盒 / 下载目录映射为 {@code https://{domain}/...} 虚拟路径，
 * 由 {@link androidx.webkit.WebViewAssetLoader} 在 {@link io.coderf.arklab.common.widget.customview.ConfigurableWebView} 中拦截。
 * </p>
 *
 * <h3>本地路径约定（{@link WebViewUrlTypeEnum#LOCAL} 或 {@link WebViewUrlTypeEnum#AUTO}）</h3>
 * <ul>
 *     <li>{@code local://相对路径} — 应用 {@code filesDir} 下文件</li>
 *     <li>{@code external://相对路径} — {@code getExternalFilesDir(null)} 下文件</li>
 *     <li>{@code download://相对路径} — 系统「下载」公共目录下文件</li>
 *     <li>{@code file:///android_asset/xxx} — 兼容旧写法，转为 assets 虚拟路径</li>
 *     <li>绝对路径 {@code /storage/...} — 若文件存在则走 {@code files} 映射（需落在已注册根目录内）</li>
 * </ul>
 */
public final class WebViewLocalUrlResolver {

    /** 沙盒 files 虚拟路径前缀 */
    public static final String PREFIX_LOCAL = "local://";
    /** 应用外部私有目录虚拟路径前缀 */
    public static final String PREFIX_EXTERNAL = "external://";
    /** 系统下载目录虚拟路径前缀 */
    public static final String PREFIX_DOWNLOAD = "download://";

    /** 兼容旧版 assets 写法，见 {@link #resolveLoadUrl} */
    public static final String ASSET_LEGACY_PREFIX = "file:///android_asset/";

    /**
     * 虚拟域名 URL 协议（须为 https，否则 {@code navigator.geolocation} 等 API 不可用）。
     */
    public static final String VIRTUAL_URL_SCHEME = "https://";

    private WebViewLocalUrlResolver() {
    }

    @NonNull
    private static String virtualBase(@NonNull String domain) {
        return VIRTUAL_URL_SCHEME + domain;
    }

    /**
     * 将历史 {@code http://{domain}/...} 虚拟地址升级为 https（兼容旧缓存页内链接）。
     */
    @NonNull
    public static String upgradeToSecureVirtualUrl(@NonNull String domain, @NonNull String url) {
        String httpBase = "http://" + domain;
        if (url.regionMatches(true, 0, httpBase, 0, httpBase.length())) {
            return virtualBase(domain) + url.substring(httpBase.length());
        }
        return url;
    }

    /**
     * 根据 URL 类型解析为 WebView {@link android.webkit.WebView#loadUrl(String)} 使用的地址。
     *
     * @param context 用于定位沙盒、下载目录
     * @param domain  与 {@link androidx.webkit.WebViewAssetLoader} 一致的虚拟域名
     * @param urlType {@link WebViewUrlTypeEnum#type}
     * @param rawUrl  业务原始 URL
     * @return 可直接 load 的 URL；无法解析时返回 about:blank
     */
    @NonNull
    public static String resolveLoadUrl(
            @NonNull Context context,
            @NonNull String domain,
            int urlType,
            @NonNull String rawUrl
    ) {
        String trimmed = rawUrl.trim();
        if (TextUtils.isEmpty(trimmed)) {
            return "about:blank";
        }

        if (urlType == WebViewUrlTypeEnum.INTERNET.type) {
            return trimmed;
        }
        if (urlType == WebViewUrlTypeEnum.ASSETS.type) {
            return toAssetVirtualUrl(domain, stripLeadingSlash(trimmed));
        }
        if (urlType == WebViewUrlTypeEnum.LOCAL.type) {
            return resolveLocalVirtualUrl(context, domain, trimmed);
        }
        // AUTO：http(s) 走网络，其余智能判断
        if (isNetworkUrl(trimmed)) {
            return trimmed;
        }
        if (trimmed.startsWith(ASSET_LEGACY_PREFIX)) {
            return toAssetVirtualUrl(domain, trimmed.substring(ASSET_LEGACY_PREFIX.length()));
        }
        if (isLocalScheme(trimmed) || isAbsoluteFilePath(trimmed)) {
            return resolveLocalVirtualUrl(context, domain, trimmed);
        }
        // 默认与历史行为一致：视为 assets 相对路径
        return toAssetVirtualUrl(domain, stripLeadingSlash(trimmed));
    }

    /**
     * 是否为 http / https 网络地址。
     */
    public static boolean isNetworkUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        String lower = url.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    /**
     * 将 assets 相对路径转为虚拟 URL：{@code https://{domain}/assets/{path}}。
     */
    @NonNull
    public static String toAssetVirtualUrl(@NonNull String domain, @NonNull String assetRelativePath) {
        return virtualBase(domain) + "/assets/" + stripLeadingSlash(assetRelativePath);
    }

    /**
     * 将沙盒/下载等本地引用转为虚拟 URL。
     */
    @NonNull
    public static String resolveLocalVirtualUrl(
            @NonNull Context context,
            @NonNull String domain,
            @NonNull String rawUrl
    ) {
        String pathPart = rawUrl;
        String virtualSegment;

        if (rawUrl.startsWith(PREFIX_LOCAL)) {
            pathPart = rawUrl.substring(PREFIX_LOCAL.length());
            virtualSegment = "files";
        } else if (rawUrl.startsWith(PREFIX_EXTERNAL)) {
            pathPart = rawUrl.substring(PREFIX_EXTERNAL.length());
            virtualSegment = "external";
        } else if (rawUrl.startsWith(PREFIX_DOWNLOAD)) {
            pathPart = rawUrl.substring(PREFIX_DOWNLOAD.length());
            virtualSegment = "download";
        } else if (rawUrl.startsWith(ASSET_LEGACY_PREFIX)) {
            return toAssetVirtualUrl(domain, rawUrl.substring(ASSET_LEGACY_PREFIX.length()));
        } else if (isAbsoluteFilePath(rawUrl)) {
            // 绝对路径：尝试映射到已注册的根目录
            String mapped = mapAbsolutePathToVirtual(context, domain, rawUrl);
            if (mapped != null) {
                return mapped;
            }
            virtualSegment = "files";
            pathPart = rawUrl;
        } else {
            // 无前缀时默认 filesDir 相对路径
            virtualSegment = "files";
        }

        pathPart = stripLeadingSlash(pathPart);
        return virtualBase(domain) + "/" + virtualSegment + "/" + pathPart;
    }

    /**
     * 若绝对路径落在 files / external / download 根目录下，返回对应虚拟 URL。
     */
    @Nullable
    private static String mapAbsolutePathToVirtual(
            @NonNull Context context,
            @NonNull String domain,
            @NonNull String absolutePath
    ) {
        File file = new File(absolutePath);
        if (!file.exists()) {
            return null;
        }
        String canonical;
        try {
            canonical = file.getCanonicalPath();
        } catch (Exception e) {
            canonical = file.getAbsolutePath();
        }

        File filesDir = context.getFilesDir();
        if (filesDir != null) {
            String base = filesDir.getAbsolutePath();
            if (canonical.startsWith(base)) {
                String rel = canonical.substring(base.length()).replace('\\', '/');
                if (rel.startsWith("/")) {
                    rel = rel.substring(1);
                }
                return virtualBase(domain) + "/files/" + rel;
            }
        }

        File externalDir = context.getExternalFilesDir(null);
        if (externalDir != null) {
            String base = externalDir.getAbsolutePath();
            if (canonical.startsWith(base)) {
                String rel = canonical.substring(base.length()).replace('\\', '/');
                if (rel.startsWith("/")) {
                    rel = rel.substring(1);
                }
                return virtualBase(domain) + "/external/" + rel;
            }
        }

        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir != null) {
            String base = downloadDir.getAbsolutePath();
            if (canonical.startsWith(base)) {
                String rel = canonical.substring(base.length()).replace('\\', '/');
                if (rel.startsWith("/")) {
                    rel = rel.substring(1);
                }
                return virtualBase(domain) + "/download/" + rel;
            }
        }
        return null;
    }

    private static boolean isLocalScheme(@NonNull String url) {
        return url.startsWith(PREFIX_LOCAL)
                || url.startsWith(PREFIX_EXTERNAL)
                || url.startsWith(PREFIX_DOWNLOAD);
    }

    private static boolean isAbsoluteFilePath(@NonNull String url) {
        return url.startsWith("/") || url.matches("^[A-Za-z]:\\\\.*");
    }

    @NonNull
    private static String stripLeadingSlash(@NonNull String path) {
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
}
