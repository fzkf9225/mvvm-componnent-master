package pers.fz.mvvm.helper;

import android.content.Context;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.webkit.WebViewAssetLoader;

import java.io.IOException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * created by fz on 2025/8/26 9:07
 * describe:
 */
public class JavaScriptAssetsPathHandler  implements WebViewAssetLoader.PathHandler{
    private final Context context;
    private final String prefix;
    private final Map<String, String> mimeTypeMap;

    public JavaScriptAssetsPathHandler(Context context) {
        this(context, "");
    }

    public JavaScriptAssetsPathHandler(Context context, String prefix) {
        this.context = context;
        this.prefix = prefix;
        this.mimeTypeMap = createMimeTypeMap();
    }

    @Override
    public WebResourceResponse handle(@NonNull String path) {
        String assetPath = !prefix.isEmpty() ? prefix + "/" + path : path;

        try {
            String extension = getFileExtension(path);
            String mimeType = mimeTypeMap.get(extension);
            if (mimeType == null) {
                mimeType = URLConnection.guessContentTypeFromName(path);
            }
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            java.io.InputStream inputStream = context.getAssets().open(assetPath);
            return new WebResourceResponse(mimeType, "UTF-8", inputStream);
        } catch (IOException e) {
            return null;
        }
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
        return map;
    }
}

