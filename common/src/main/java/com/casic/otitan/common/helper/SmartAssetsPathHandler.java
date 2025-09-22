package com.casic.otitan.common.helper;

import android.content.Context;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.webkit.WebViewAssetLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * created by fz on 2025/8/26 9:07
 * describe:
 */
public class SmartAssetsPathHandler  implements WebViewAssetLoader.PathHandler {
    private final Context context;
    private final String prefix;
    private final Map<String, String> mimeTypeMap;

    public SmartAssetsPathHandler(Context context) {
        this(context, "");
    }

    public SmartAssetsPathHandler(Context context, String prefix) {
        this.context = context;
        this.prefix = prefix;
        this.mimeTypeMap = createMimeTypeMap();
    }

    @Override
    public WebResourceResponse handle(@NonNull String path) {
        try {
            String assetPath = buildAssetPath(path);
            String mimeType = determineMimeType(path);
            String encoding = determineEncoding(mimeType);

            java.io.InputStream inputStream = context.getAssets().open(assetPath);
            WebResourceResponse response = new WebResourceResponse(mimeType, encoding, inputStream);

            // 设置缓存头
            Map<String, String> headers = new HashMap<>();
            headers.put("Cache-Control", "max-age=3600");
            response.setResponseHeaders(headers);

            return response;
        } catch (FileNotFoundException e) {
            return handleFileNotFound(path);
        } catch (IOException e) {
            return handleIOException(e, path);
        }
    }

    private String buildAssetPath(String path) {
        String cleanPath = path;
        if (cleanPath.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }

        if (prefix != null && !prefix.isEmpty()) {
            // 移除可能重复的前缀
            if (cleanPath.startsWith(prefix + "/")) {
                cleanPath = cleanPath.substring(prefix.length() + 1);
            }
            return prefix + "/" + cleanPath;
        }
        return cleanPath;
    }

    private String determineMimeType(String path) {
        String extension = getFileExtension(path).toLowerCase();
        String mimeType = mimeTypeMap.get(extension);

        if (mimeType == null) {
            mimeType = URLConnection.guessContentTypeFromName(path);
        }

        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        return mimeType;
    }

    private String determineEncoding(String mimeType) {
        if (mimeType != null) {
            if (mimeType.startsWith("text/") ||
                    mimeType.equals("application/json") ||
                    mimeType.equals("application/javascript")) {
                return "UTF-8";
            }
        }
        return null;
    }

    private WebResourceResponse handleFileNotFound(String path) {
        return null;
    }

    private WebResourceResponse handleIOException(IOException e, String path) {
        return null;
    }

    private String getFileExtension(String path) {
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < path.length() - 1) {
            return path.substring(lastDotIndex);
        }
        return "";
    }

    protected Map<String, String> createMimeTypeMap() {
        Map<String, String> map = new HashMap<>();
        map.put(".js", "application/javascript");
        map.put(".css", "text/css");
        map.put(".html", "text/html");
        map.put(".htm", "text/html");
        map.put(".json", "application/json");
        map.put(".png", "image/png");
        map.put(".jpg", "image/jpeg");
        map.put(".jpeg", "image/jpeg");
        map.put(".gif", "image/gif");
        map.put(".svg", "image/svg+xml");
        map.put(".ico", "image/x-icon");
        map.put(".ttf", "font/ttf");
        map.put(".otf", "font/otf");
        map.put(".woff", "font/woff");
        map.put(".woff2", "font/woff2");
        map.put(".eot", "application/vnd.ms-fontobject");
        map.put(".xml", "application/xml");
        map.put(".txt", "text/plain");
        map.put(".pdf", "application/pdf");
        map.put(".mp4", "video/mp4");
        map.put(".webm", "video/webm");
        map.put(".mp3", "audio/mpeg");
        map.put(".wav", "audio/wav");
        return map;
    }
}

