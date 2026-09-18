package io.coderf.arklab.common.glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 从图片 URL 生成稳定缓存 key：只删除配置的 query 名，其余 query / path 原样保留。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/18
 */
final class UrlCacheKeyNormalizer {

    private UrlCacheKeyNormalizer() {
    }

    @Nullable
    static String normalize(@Nullable String url, @Nullable Collection<String> ignoredQueryKeys) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        Set<String> ignored = toIgnoredSet(ignoredQueryKeys);
        if (ignored.isEmpty()) {
            return url;
        }
        int queryStart = url.indexOf('?');
        if (queryStart < 0) {
            return url;
        }
        int fragmentStart = url.indexOf('#', queryStart);
        String path = url.substring(0, queryStart);
        String query = fragmentStart < 0
                ? url.substring(queryStart + 1)
                : url.substring(queryStart + 1, fragmentStart);
        String fragment = fragmentStart < 0 ? "" : url.substring(fragmentStart);
        if (query.isEmpty()) {
            return url;
        }

        String[] pairs = query.split("&", -1);
        List<String> kept = new ArrayList<>(pairs.length);
        boolean removedAny = false;
        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String rawKey = eq < 0 ? pair : pair.substring(0, eq);
            if (ignored.contains(canonicalizeQueryKey(rawKey))) {
                removedAny = true;
                continue;
            }
            kept.add(pair);
        }
        if (!removedAny) {
            return url;
        }

        StringBuilder builder = new StringBuilder(path.length() + query.length() + fragment.length());
        builder.append(path);
        if (!kept.isEmpty()) {
            builder.append('?');
            for (int i = 0; i < kept.size(); i++) {
                if (i > 0) {
                    builder.append('&');
                }
                builder.append(kept.get(i));
            }
        }
        builder.append(fragment);
        return builder.toString();
    }

    @NonNull
    private static Set<String> toIgnoredSet(@Nullable Collection<String> ignoredQueryKeys) {
        if (ignoredQueryKeys == null || ignoredQueryKeys.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> ignored = new HashSet<>();
        for (String key : ignoredQueryKeys) {
            String canonical = canonicalizeQueryKey(key);
            if (!canonical.isEmpty()) {
                ignored.add(canonical);
            }
        }
        return ignored;
    }

    @NonNull
    private static String canonicalizeQueryKey(@Nullable String key) {
        if (key == null) {
            return "";
        }
        String trimmed = key.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return urlDecode(trimmed).toLowerCase(Locale.ROOT);
    }

    @NonNull
    private static String urlDecode(@NonNull String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception ignored) {
            return value;
        }
    }
}
