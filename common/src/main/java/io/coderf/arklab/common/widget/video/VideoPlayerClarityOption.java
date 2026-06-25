package io.coderf.arklab.common.widget.video;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 清晰度选项。GSY 通过切换播放地址实现清晰度切换，可附带请求头。
 */
public record VideoPlayerClarityOption(String name, String url, @NonNull String description,
                                       @NonNull Map<String, String> headers) {

    public VideoPlayerClarityOption(@NonNull String name, @NonNull String url) {
        this(name, url, "", Collections.emptyMap());
    }

    public VideoPlayerClarityOption(@NonNull String name,
                                    @NonNull String url,
                                    @NonNull String description) {
        this(name, url, description, Collections.emptyMap());
    }

    public VideoPlayerClarityOption(@NonNull String name,
                                    @NonNull String url,
                                    @NonNull String description,
                                    @Nullable Map<String, String> headers) {
        this.name = name;
        this.url = url;
        this.description = description;
        this.headers = headers == null ? Collections.emptyMap() : new HashMap<>(headers);
    }

    @Override
    @NonNull
    public String name() {
        return name;
    }

    @Override
    @NonNull
    public String url() {
        return url;
    }

    @Override
    @NonNull
    public Map<String, String> headers() {
        return Collections.unmodifiableMap(headers);
    }

    public boolean hasHeaders() {
        return !headers.isEmpty();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof VideoPlayerClarityOption)) {
            return false;
        }
        VideoPlayerClarityOption other = (VideoPlayerClarityOption) obj;
        return name.equals(other.name) && url.equals(other.url);
    }
}
