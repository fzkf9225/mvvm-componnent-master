package pers.fz.media.videocompressor;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fz
 * Date: 2017/8/16
 * Time: 15:15
 */

public class VideoCompress {
    private static final String TAG = VideoCompress.class.getSimpleName();

    public static void compressVideoHigh(Context context, Uri srcPath, String destPath, CompressListener listener) {
        ThreadExecutorVideo.getInstance().execute(new CompressRunnable(context, srcPath, destPath, listener, VideoController.COMPRESS_QUALITY_HIGH));
    }

    public static void compressVideoMedium(Context context, Uri srcPath, String destPath, CompressListener listener) {
        ThreadExecutorVideo.getInstance().execute(new CompressRunnable(context, srcPath, destPath, listener, VideoController.COMPRESS_QUALITY_MEDIUM));
    }

    public static void compressVideoLow(Context context, Uri srcPath, String destPath, CompressListener listener) {
        ThreadExecutorVideo.getInstance().execute(new CompressRunnable(context, srcPath, destPath, listener, VideoController.COMPRESS_QUALITY_LOW));
//        try {
//            Mp4ParseUtil.appendMp4List(context,srcPath,destPath,listener);
//        } catch (Exception e) {
//            Log.d("VideoController","异常："+e);
//        }
    }

    private static class CompressRunnable implements Runnable {
        private final CompressListener mListener;
        private final int mQuality;
        private final Uri srcPath;
        private final String destPath;
        private final Context context;

        public CompressRunnable(Context context, Uri srcPath, String destPath, CompressListener mListener, int mQuality) {
            this.context = context;
            this.srcPath = srcPath;
            this.destPath = destPath;
            this.mListener = mListener;
            this.mQuality = mQuality;
            if (mListener != null) {
                mListener.onStart();
            }
        }

        @Override
        public void run() {
            boolean result = false;
            try {
                result = VideoController.getInstance().convertVideo(context, srcPath, destPath, mQuality, mListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mListener != null) {
                if (result) {
                    mListener.onSuccess();
                } else {
                    mListener.onFail();
                }
            }
        }
    }
}
