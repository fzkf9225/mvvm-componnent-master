package io.coderf.arklab.common.utils.common;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.utils.log.LogUtil;

/**
 * 实况 / Motion Photo 解析器。
 * <p>
 * 支持：
 * <ul>
 *   <li>Apple Live Photo 同目录配对：{@code IMG_xxx.JPG} + {@code IMG_xxx.MOV}（仅本地路径）</li>
 *   <li>Google Motion Photo / MicroVideo：JPEG/HEIC 内嵌尾部 MP4（XMP 偏移或 ftyp 扫描）</li>
 *   <li>本地 file / content Uri，以及 {@code http(s)} 网络地址（按字节流解析内嵌视频）</li>
 * </ul>
 * 仅有静图关键帧、已剥离视频的导出 JPG <b>无法</b>还原实况。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/9
 */
public final class MotionPhotoResolver {

    private static final String TAG = "MotionPhotoResolver";
    private static final String CACHE_DIR = "motion_photo_cache";

    private static final Pattern MICRO_VIDEO_OFFSET = Pattern.compile(
            "MicroVideoOffset(?:=|\"\\s*:)\\s*\"?(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ITEM_LENGTH = Pattern.compile(
            "Item:Length(?:=|\"\\s*:)\\s*\"?(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MOTION_PHOTO_FLAG = Pattern.compile(
            "MotionPhoto(?:=|\"\\s*:)\\s*\"?1\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MICRO_VIDEO_FLAG = Pattern.compile(
            "MicroVideo(?:=|\"\\s*:)\\s*\"?1\\b", Pattern.CASE_INSENSITIVE);

    private MotionPhotoResolver() {
    }

    /**
     * 解析结果。
     */
    public static final class ResolveResult {
        @Nullable
        public final String motionVideoPath;
        @Nullable
        public final String motionSource;
        public final boolean fromCache;

        public ResolveResult(@Nullable String motionVideoPath, @Nullable String motionSource, boolean fromCache) {
            this.motionVideoPath = motionVideoPath;
            this.motionSource = motionSource;
            this.fromCache = fromCache;
        }

        public boolean hasMotion() {
            return !TextUtils.isEmpty(motionVideoPath);
        }

        @NonNull
        public LivePhotoInfo toLivePhotoInfo() {
            if (!hasMotion()) {
                return LivePhotoInfo.EMPTY;
            }
            return LivePhotoInfo.of(true, motionVideoPath, motionSource);
        }
    }

    /**
     * 快速检查同目录是否存在 Apple Live 配对视频（不读文件内容，适合列表角标）。
     */
    public static boolean hasSidecarMotion(@Nullable String stillPath) {
        return findSidecarVideoFile(stillPath) != null;
    }

    /**
     * 同步解析：先 sidecar，再内嵌抽取。耗时可观，请在工作线程调用。
     */
    @WorkerThread
    @NonNull
    public static ResolveResult resolve(@NonNull Context context, @Nullable String stillPath) {
        if (TextUtils.isEmpty(stillPath)) {
            return new ResolveResult(null, null, false);
        }
        File sidecar = findSidecarVideoFile(stillPath);
        if (sidecar != null) {
            return new ResolveResult(sidecar.getAbsolutePath(), "apple_live_sidecar", false);
        }
        File extracted = extractEmbeddedMotionToCache(context, stillPath);
        if (extracted != null) {
            return new ResolveResult(extracted.getAbsolutePath(), "google_motion_embedded", true);
        }
        return new ResolveResult(null, null, false);
    }

    /**
     * 若附件尚无 motion 信息，尝试解析并写回 {@link AttachmentBean#setLiveInfo(String)}
     * （sidecar 或抽取缓存路径；不占用业务预留的 {@code otherInfo}）。
     *
     * @return 是否写入了新的实况信息
     */
    @WorkerThread
    public static boolean enrichAttachment(@NonNull Context context, @Nullable AttachmentBean bean) {
        if (bean == null || TextUtils.isEmpty(bean.getPath())) {
            return false;
        }
        if (LivePhotoInfo.from(bean).hasMotionVideo()) {
            return false;
        }
        ResolveResult result = resolve(context, bean.getPath());
        if (!result.hasMotion()) {
            return false;
        }
        LivePhotoInfo.of(true, result.motionVideoPath, result.motionSource).applyTo(bean);
        return true;
    }

    @Nullable
    public static File findSidecarVideoFile(@Nullable String stillPath) {
        if (TextUtils.isEmpty(stillPath)) {
            return null;
        }
        if (stillPath.startsWith("content://") || stillPath.startsWith("http://") || stillPath.startsWith("https://")) {
            return null;
        }
        String localPath = stillPath;
        if (stillPath.startsWith("file://")) {
            localPath = Uri.parse(stillPath).getPath();
        }
        if (TextUtils.isEmpty(localPath)) {
            return null;
        }
        File stillFile = new File(localPath);
        File parent = stillFile.getParentFile();
        if (parent == null || !parent.isDirectory()) {
            return null;
        }
        String name = stillFile.getName();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String[] suffixes = {".MOV", ".mov", ".MP4", ".mp4", ".M4V", ".m4v"};
        for (String suffix : suffixes) {
            File candidate = new File(parent, base + suffix);
            if (candidate.isFile() && candidate.length() > 0) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * 从 JPEG/类 JPEG Motion Photo 中抽取尾部视频到缓存目录。
     */
    @WorkerThread
    @Nullable
    public static File extractEmbeddedMotionToCache(@NonNull Context context, @Nullable String stillPath) {
        if (TextUtils.isEmpty(stillPath)) {
            return null;
        }
        try {
            File cacheFile = buildCacheFile(context, stillPath);
            if (cacheFile.exists() && cacheFile.length() > 0) {
                return cacheFile;
            }
            EmbeddedMotion embedded = locateEmbeddedMotion(context, stillPath);
            if (embedded == null || embedded.length <= 0 || embedded.startOffset < 0) {
                return null;
            }
            File parent = cacheFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                return null;
            }
            try (InputStream in = openStillStream(context, stillPath);
                 OutputStream out = new FileOutputStream(cacheFile)) {
                if (in == null) {
                    return null;
                }
                skipFully(in, embedded.startOffset);
                byte[] buffer = new byte[8192];
                long remaining = embedded.length;
                while (remaining > 0) {
                    int read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                    if (read < 0) {
                        break;
                    }
                    out.write(buffer, 0, read);
                    remaining -= read;
                }
            }
            if (!cacheFile.exists() || cacheFile.length() < 32) {
                //noinspection ResultOfMethodCallIgnored
                cacheFile.delete();
                return null;
            }
            return cacheFile;
        } catch (Exception e) {
            LogUtil.e(TAG, "extractEmbeddedMotionToCache failed: " + e);
            return null;
        }
    }

    @Nullable
    private static EmbeddedMotion locateEmbeddedMotion(@NonNull Context context, @NonNull String stillPath)
            throws Exception {
        long fileSize = queryLength(context, stillPath);
        if (fileSize <= 0) {
            return null;
        }
        String xmp = readXmpNearHead(context, stillPath, (int) Math.min(fileSize, 512 * 1024L));
        if (!TextUtils.isEmpty(xmp)) {
            Matcher micro = MICRO_VIDEO_OFFSET.matcher(xmp);
            if (micro.find()) {
                long offsetFromEnd = Long.parseLong(Objects.requireNonNull(micro.group(1)));
                if (offsetFromEnd > 0 && offsetFromEnd < fileSize) {
                    return new EmbeddedMotion(fileSize - offsetFromEnd, offsetFromEnd);
                }
            }
            boolean flagged = MOTION_PHOTO_FLAG.matcher(xmp).find() || MICRO_VIDEO_FLAG.matcher(xmp).find();
            Matcher lengthMatcher = ITEM_LENGTH.matcher(xmp);
            long videoLength = -1;
            while (lengthMatcher.find()) {
                // Container 里通常最后一个 Item:Length 对应 video/mp4；取最大合理值
                long value = Long.parseLong(Objects.requireNonNull(lengthMatcher.group(1)));
                if (value > 1024 && value < fileSize) {
                    videoLength = Math.max(videoLength, value);
                }
            }
            if (videoLength > 0 && (flagged || videoLength > fileSize / 10)) {
                return new EmbeddedMotion(fileSize - videoLength, videoLength);
            }
        }
        // 勿用「最后一个 ftyp」：视频 mdat 内常有伪匹配，会抽到无音轨残段导致实况无声
        long ftypOffset = findFirstValidFtypOffset(context, stillPath, fileSize);
        if (ftypOffset >= 4) {
            return new EmbeddedMotion(ftypOffset - 4, fileSize - (ftypOffset - 4));
        }
        return null;
    }

    /**
     * 从头扫描第一个合法 ISO BMFF {@code ftyp} box（前 4 字节 big-endian size ∈ [16,256]）。
     * Motion Photo（JPEG+MP4）上该位置即为内嵌视频起点。
     *
     * @return {@code ftyp} 四字符起始下标；未找到返回 -1
     */
    private static long findFirstValidFtypOffset(@NonNull Context context, @NonNull String stillPath, long fileSize)
            throws Exception {
        if (fileSize < 16) {
            return -1;
        }
        final byte[] ftyp = {'f', 't', 'y', 'p'};
        final int chunkSize = 1024 * 1024;
        final int overlap = 8;
        byte[] buffer = new byte[chunkSize + overlap];
        try (InputStream in = openStillStream(context, stillPath)) {
            if (in == null) {
                return -1;
            }
            long filePos = 0;
            int carry = 0;
            while (filePos < fileSize) {
                int toRead = (int) Math.min(chunkSize, fileSize - filePos);
                int filled = 0;
                while (filled < toRead) {
                    int read = in.read(buffer, carry + filled, toRead - filled);
                    if (read < 0) {
                        break;
                    }
                    filled += read;
                }
                if (filled <= 0 && carry == 0) {
                    break;
                }
                int window = carry + filled;
                int scanStart = filePos == 0 ? 4 : 0;
                for (int i = scanStart; i <= window - 4; i++) {
                    if (buffer[i] == ftyp[0] && buffer[i + 1] == ftyp[1]
                            && buffer[i + 2] == ftyp[2] && buffer[i + 3] == ftyp[3]
                            && i >= 4) {
                        int size = ((buffer[i - 4] & 0xFF) << 24)
                                | ((buffer[i - 3] & 0xFF) << 16)
                                | ((buffer[i - 2] & 0xFF) << 8)
                                | (buffer[i - 1] & 0xFF);
                        if (size >= 16 && size <= 256) {
                            return filePos - carry + i;
                        }
                    }
                }
                if (window >= overlap) {
                    System.arraycopy(buffer, window - overlap, buffer, 0, overlap);
                    carry = overlap;
                } else {
                    carry = 0;
                }
                filePos += filled;
            }
        }
        return -1;
    }

    @Nullable
    private static String readXmpNearHead(@NonNull Context context, @NonNull String stillPath, int maxBytes)
            throws Exception {
        try (InputStream in = openStillStream(context, stillPath)) {
            if (in == null) {
                return null;
            }
            byte[] buffer = new byte[maxBytes];
            int total = 0;
            while (total < maxBytes) {
                int read = in.read(buffer, total, maxBytes - total);
                if (read < 0) {
                    break;
                }
                total += read;
            }
            if (total <= 0) {
                return null;
            }
            String ascii = new String(buffer, 0, total, StandardCharsets.ISO_8859_1);
            int xmpStart = ascii.indexOf("http://ns.adobe.com/xap/1.0/");
            if (xmpStart < 0) {
                xmpStart = ascii.indexOf("<x:xmpmeta");
            }
            if (xmpStart < 0) {
                return null;
            }
            int xmpEnd = ascii.indexOf("</x:xmpmeta>", xmpStart);
            if (xmpEnd > xmpStart) {
                return ascii.substring(xmpStart, xmpEnd + "</x:xmpmeta>".length());
            }
            return ascii.substring(xmpStart, Math.min(ascii.length(), xmpStart + 64 * 1024));
        }
    }

    @Nullable
    private static InputStream openStillStream(@NonNull Context context, @NonNull String stillPath)
            throws Exception {
        if (stillPath.startsWith("content://") || stillPath.startsWith("android.resource://")) {
            return context.getContentResolver().openInputStream(Uri.parse(stillPath));
        }
        if (stillPath.startsWith("file://")) {
            String path = Uri.parse(stillPath).getPath();
            return path == null ? null : new FileInputStream(path);
        }
        if (stillPath.startsWith("http://") || stillPath.startsWith("https://")) {
            return openHttpStream(stillPath);
        }
        File file = new File(stillPath);
        if (!file.isFile()) {
            return null;
        }
        return new FileInputStream(file);
    }

    /**
     * 打开网络静图输入流。签名 URL 需在有效期内；每次解析可能发起多次请求（读头 / 扫尾 / 抽取）。
     */
    @Nullable
    private static InputStream openHttpStream(@NonNull String url) throws Exception {
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(60_000);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "*/*");
        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            return conn.getInputStream();
        }
        LogUtil.e(TAG, "openHttpStream HTTP " + code + " for " + safeUrlForLog(url));
        conn.disconnect();
        return null;
    }

    @NonNull
    private static String safeUrlForLog(@NonNull String url) {
        int q = url.indexOf('?');
        return q > 0 ? url.substring(0, q) + "?…" : url;
    }

    private static long queryLength(@NonNull Context context, @NonNull String stillPath) throws Exception {
        if (stillPath.startsWith("content://")) {
            try (android.content.res.AssetFileDescriptor afd =
                         context.getContentResolver().openAssetFileDescriptor(Uri.parse(stillPath), "r")) {
                return afd == null ? -1 : afd.getLength();
            }
        }
        if (stillPath.startsWith("http://") || stillPath.startsWith("https://")) {
            return queryHttpLength(stillPath);
        }
        String local = stillPath;
        if (stillPath.startsWith("file://")) {
            local = Uri.parse(stillPath).getPath();
        }
        if (TextUtils.isEmpty(local)) {
            return -1;
        }
        File file = new File(local);
        return file.isFile() ? file.length() : -1;
    }

    /**
     * 优先 HEAD 取 Content-Length；不支持 HEAD 或长度未知时再 GET（只读响应头）。
     */
    private static long queryHttpLength(@NonNull String url) throws Exception {
        long len = requestHttpContentLength(url, "HEAD");
        if (len > 0) {
            return len;
        }
        return requestHttpContentLength(url, "GET");
    }

    private static long requestHttpContentLength(@NonNull String url, @NonNull String method) throws Exception {
        java.net.HttpURLConnection conn = null;
        try {
            conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setConnectTimeout(15_000);
            conn.setReadTimeout(30_000);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod(method);
            conn.setRequestProperty("Accept", "*/*");
            // GET 时也只为拿长度，避免无必要时读完 body
            if ("GET".equals(method)) {
                conn.setRequestProperty("Range", "bytes=0-0");
            }
            int code = conn.getResponseCode();
            if (code >= 200 && code < 300 || code == 206) {
                long contentLength = conn.getContentLengthLong();
                if (contentLength > 0 && code != 206) {
                    return contentLength;
                }
                // Range 206 时 Content-Length 可能是 1，改读 Content-Range: bytes 0-0/total
                String contentRange = conn.getHeaderField("Content-Range");
                if (!TextUtils.isEmpty(contentRange)) {
                    int slash = contentRange.lastIndexOf('/');
                    if (slash >= 0 && slash + 1 < contentRange.length()) {
                        String total = contentRange.substring(slash + 1).trim();
                        if (!"*".equals(total)) {
                            long parsed = Long.parseLong(total);
                            if (parsed > 0) {
                                return parsed;
                            }
                        }
                    }
                }
                if (contentLength > 0) {
                    return contentLength;
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static void skipFully(@NonNull InputStream in, long count) throws Exception {
        long remaining = count;
        while (remaining > 0) {
            long skipped = in.skip(remaining);
            if (skipped > 0) {
                remaining -= skipped;
                continue;
            }
            if (in.read() < 0) {
                break;
            }
            remaining--;
        }
    }

    @NonNull
    private static File buildCacheFile(@NonNull Context context, @NonNull String stillPath) throws Exception {
        File dir = new File(context.getCacheDir(), CACHE_DIR);
        long length = -1;
        try {
            length = queryLength(context, stillPath);
        } catch (Exception ignore) {
        }
        // 长度未知时仍按 URL/路径缓存，避免网络图永远无法落盘
        String key = sha1Hex(stillPath + "_" + length);
        return new File(dir, key + ".mp4");
    }

    @NonNull
    private static String sha1Hex(@NonNull String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format(Locale.US, "%02x", b));
        }
        return sb.toString();
    }

    private static final class EmbeddedMotion {
        final long startOffset;
        final long length;

        EmbeddedMotion(long startOffset, long length) {
            this.startOffset = startOffset;
            this.length = length;
        }
    }
}
