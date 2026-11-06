package io.coderf.arklab.media.compressor.video;

import android.content.Context;
import android.net.Uri;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fz
 * Date: 2017/8/16
 * Time: 15:15
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/10/22
 */

public class VideoCompress {
    private static final String TAG = VideoCompress.class.getSimpleName();

    public static void compressVideoHigh(Context context, Uri srcPath, String destPath, CompressListener listener) {
        compressVideoHigh(context, srcPath, destPath, listener, 0L);
    }

    public static void compressVideoMedium(Context context, Uri srcPath, String destPath, CompressListener listener) {
        compressVideoMedium(context, srcPath, destPath, listener, 0L);
    }

    public static void compressVideoLow(Context context, Uri srcPath, String destPath, CompressListener listener) {
        compressVideoLow(context, srcPath, destPath, listener, 0L);
    }

    public static void compressVideoHigh(Context context, Uri srcPath, String destPath, CompressListener listener, long timeoutMs) {
        ThreadExecutorVideo.getInstance().execute(
                new CompressRunnable(context, srcPath, destPath, listener, VideoController.COMPRESS_QUALITY_HIGH, timeoutMs));
    }

    public static void compressVideoMedium(Context context, Uri srcPath, String destPath, CompressListener listener, long timeoutMs) {
        ThreadExecutorVideo.getInstance().execute(
                new CompressRunnable(context, srcPath, destPath, listener, VideoController.COMPRESS_QUALITY_MEDIUM, timeoutMs));
    }

    public static void compressVideoLow(Context context, Uri srcPath, String destPath, CompressListener listener, long timeoutMs) {
        ThreadExecutorVideo.getInstance().execute(
                new CompressRunnable(context, srcPath, destPath, listener, VideoController.COMPRESS_QUALITY_LOW, timeoutMs));
    }

    private static class CompressRunnable implements Runnable {
        private final CompressListener mListener;
        private final int mQuality;
        private final Uri srcPath;
        private final String destPath;
        private final Context context;
        private final long timeoutMs;

        public CompressRunnable(Context context, Uri srcPath, String destPath, CompressListener mListener, int mQuality, long timeoutMs) {
            this.context = context;
            this.srcPath = srcPath;
            this.destPath = destPath;
            this.mListener = mListener;
            this.mQuality = mQuality;
            this.timeoutMs = timeoutMs > 0 ? timeoutMs : 0L;
            if (mListener != null) {
                mListener.onStart();
            }
        }

        @Override
        public void run() {
            final AtomicBoolean finished = new AtomicBoolean(false);
            if (timeoutMs <= 0) {
                doCompress(finished);
                return;
            }

            // 独立单线程执行实际压缩，避免与 ThreadExecutorVideo 池嵌套提交死锁；
            // 当前线程仅负责超时等待，超时后取消任务并回调失败。
            ExecutorService workExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "VideoCompress-Work");
                t.setDaemon(true);
                return t;
            });
            Future<?> future = workExecutor.submit(() -> doCompress(finished));
            try {
                future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                if (finished.compareAndSet(false, true) && mListener != null) {
                    mListener.onResult(false, "视频压缩超时（" + timeoutMs + "ms），请重试或更换视频");
                }
            } catch (ExecutionException e) {
                // doCompress 内部已回调，这里兜底
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                if (finished.compareAndSet(false, true) && mListener != null) {
                    mListener.onResult(false, cause.getMessage() != null ? cause.getMessage() : "视频压缩异常");
                }
            } catch (InterruptedException e) {
                future.cancel(true);
                Thread.currentThread().interrupt();
                if (finished.compareAndSet(false, true) && mListener != null) {
                    mListener.onResult(false, "视频压缩被中断");
                }
            } finally {
                workExecutor.shutdownNow();
            }
        }

        private void doCompress(AtomicBoolean finished) {
            try {
                VideoController.getInstance().convertVideo(context, srcPath, destPath, mQuality, mListener);
                if (Thread.currentThread().isInterrupted()) {
                    if (finished.compareAndSet(false, true) && mListener != null) {
                        mListener.onResult(false, "视频压缩被中断");
                    }
                    return;
                }
                if (finished.compareAndSet(false, true) && mListener != null) {
                    mListener.onResult(true, "压缩视频成功！");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (finished.compareAndSet(false, true) && mListener != null) {
                    mListener.onResult(false, e.getMessage() != null ? e.getMessage() : "视频压缩失败");
                }
            }
        }
    }
}
