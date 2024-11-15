package pers.fz.media.imgcompressor;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import pers.fz.media.MediaUtil;


/**
 * Created by guizhigang on 16/5/25.
 */
public class ImgCompressor {
    @SuppressLint("StaticFieldLeak")
    private volatile static ImgCompressor instance = null;
    private final Context context;
    private CompressListener compressListener;
    private static final int DEFAULT_OUTWIDTH = 720;
    private static final int DEFAULT_OUTHEIGHT = 1080;
    private static final int DEFAULT_MAXFILESIZE = 1024;//KB

    private ImgCompressor(Context context) {
        this.context = context;
    }

    public static ImgCompressor getInstance(Context context) {
        if (instance == null) {
            synchronized (ImgCompressor.class) {
                if (instance == null) {
                    instance = new ImgCompressor(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public ImgCompressor withListener(CompressListener compressListener) {
        this.compressListener = compressListener;
        return this;
    }

    /**
     * Can't compress a recycled bitmap
     *
     * @param srcImageUri 原始图片的uri路径
     * @param outWidth    期望的输出图片的宽度
     * @param outHeight   期望的输出图片的高度
     * @param maxFileSize 期望的输出图片的最大占用的存储空间
     * @return
     */
    public Uri compressImage( Uri srcImageUri, String outputPath, int outWidth, int outHeight, int maxFileSize) {

        //进行大小缩放来达到压缩的目的
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            if (compressListener != null) {
                compressListener.onCompressFail(new Exception("打开内容解析器失败！"));
                return null;
            }
        }
        ParcelFileDescriptor parcelFileDescriptor;
        try {
            parcelFileDescriptor = contentResolver.openFileDescriptor(srcImageUri,"r");
            if (parcelFileDescriptor == null) {
                if (compressListener != null) {
                    compressListener.onCompressFail(new Exception("权限不足！"));
                }
                return null;
            }
            BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor(), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (compressListener != null) {
                compressListener.onCompressFail(new FileNotFoundException("文件不存在或已删除"));
            }
            return null;
        }
        //根据原始图片的宽高比和期望的输出图片的宽高比计算最终输出的图片的宽和高
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        float maxWidth = outWidth;
        float maxHeight = outHeight;
        float srcRatio = srcWidth / srcHeight;
        float outRatio = maxWidth / maxHeight;
        float actualOutWidth = srcWidth;
        float actualOutHeight = srcHeight;

        if (srcWidth > maxWidth || srcHeight > maxHeight) {
            //如果输入比率小于输出比率,则最终输出的宽度以maxHeight为准()
            //比如输入比为10:20 输出比是300:10 如果要保证输出图片的宽高比和原始图片的宽高比相同,则最终输出图片的高为10
            //宽度为10/20 * 10 = 5  最终输出图片的比率为5:10 和原始输入的比率相同

            //同理如果输入比率大于输出比率,则最终输出的高度以maxHeight为准()
            //比如输入比为20:10 输出比是5:100 如果要保证输出图片的宽高比和原始图片的宽高比相同,则最终输出图片的宽为5
            //高度需要根据输入图片的比率计算获得 为5 / 20/10= 2.5  最终输出图片的比率为5:2.5 和原始输入的比率相同
            if (srcRatio < outRatio) {
                actualOutHeight = maxHeight;
                actualOutWidth = actualOutHeight * srcRatio;
            } else if (srcRatio > outRatio) {
                actualOutWidth = maxWidth;
                actualOutHeight = actualOutWidth / srcRatio;
            } else {
                actualOutWidth = maxWidth;
                actualOutHeight = maxHeight;
            }
        }
        options.inSampleSize = computSampleSize(options, actualOutWidth, actualOutHeight);
        options.inJustDecodeBounds = false;
        Bitmap scaledBitmap;
        try {
            parcelFileDescriptor = contentResolver.openFileDescriptor(srcImageUri,"r");
            if (parcelFileDescriptor == null) {
                if (compressListener != null) {
                    compressListener.onCompressFail(new Exception("权限不足！"));
                }
                return null;
            }
            scaledBitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor(), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (compressListener != null) {
                compressListener.onCompressFail(new FileNotFoundException("文件不存在或已删除"));
            }
            return null;
        }
        if (scaledBitmap == null) {
            return null;//压缩失败
        }
        //生成最终输出的bitmap
        Bitmap actualOutBitmap = Bitmap.createScaledBitmap(scaledBitmap, (int) actualOutWidth, (int) actualOutHeight, true);
        if (actualOutBitmap != scaledBitmap) {
            scaledBitmap.recycle();
        }

        //处理图片旋转问题
        ExifInterface exif;
        try {
            exif = new ExifInterface(parcelFileDescriptor.getFileDescriptor());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }
            actualOutBitmap = Bitmap.createBitmap(actualOutBitmap, 0, 0,
                    actualOutBitmap.getWidth(), actualOutBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
            if (compressListener != null) {
                compressListener.onCompressFail(e);
            }
            return null;
        }

        //进行有损压缩
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options_ = 100;
        actualOutBitmap.compress(Bitmap.CompressFormat.JPEG, options_, baos);//质量压缩方法，把压缩后的数据存放到baos中 (100表示不压缩，0表示压缩到最小)

        int baosLength = baos.toByteArray().length;

        while (baosLength / 1024 > maxFileSize) {//循环判断如果压缩后图片是否大于maxMemmorrySize,大于继续压缩
            baos.reset();//重置baos即让下一次的写入覆盖之前的内容
            options_ = Math.max(0, options_ - 10);//图片质量每次减少10
            actualOutBitmap.compress(Bitmap.CompressFormat.JPEG, options_, baos);//将压缩后的图片保存到baos中
            baosLength = baos.toByteArray().length;
            if (options_ == 0)//如果图片的质量已降到最低则，不再进行压缩
            {
                break;
            }
        }
        actualOutBitmap.recycle();
        //将bitmap保存到指定路径
        FileOutputStream fos = null;
        String fileName = MediaUtil.getNoRepeatFileName(outputPath, "IMG_", ".jpg");
        File outputFile = new File(outputPath, fileName + ".jpg");
        try {
            fos = new FileOutputStream(outputFile);
            //包装缓冲流,提高写入速度
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);
            bufferedOutputStream.write(baos.toByteArray());
            bufferedOutputStream.flush();
        } catch (IOException e) {
            if (compressListener != null) {
                compressListener.onCompressFail(e);
            }
            return null;
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //兼容android7.0 使用共享文件的形式
        return FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider", outputFile);
    }

    private static int computSampleSize(BitmapFactory.Options options, float reqWidth, float reqHeight) {
        float srcWidth = options.outWidth;//20
        float srcHeight = options.outHeight;//10
        int sampleSize = 1;
        if (srcWidth > reqWidth || srcHeight > reqHeight) {
            int withRatio = Math.round(srcWidth / reqWidth);
            int heightRatio = Math.round(srcHeight / reqHeight);
            sampleSize = Math.min(withRatio, heightRatio);
        }
        return sampleSize;
    }

    public void starCompress( Uri srcImageUri, String outPath, int outWidth, int outHeight, int maxFileSize) {
        ThreadExecutor.getInstance().execute(new CompressRunnable(srcImageUri, outPath, outWidth, outHeight, maxFileSize));
    }

    public static class CompressResult implements Parcelable {
        public static final int RESULT_OK = 0;
        public static final int RESULT_ERROR = 1;
        private int status = RESULT_OK;
        private Uri srcPath;
        private Uri outPath;

        public CompressResult() {

        }

        protected CompressResult(Parcel in) {
            status = in.readInt();
            srcPath = in.readParcelable(Uri.class.getClassLoader());
            outPath = in.readParcelable(Uri.class.getClassLoader());
        }

        public static final Creator<CompressResult> CREATOR = new Creator<CompressResult>() {
            @Override
            public CompressResult createFromParcel(Parcel in) {
                return new CompressResult(in);
            }

            @Override
            public CompressResult[] newArray(int size) {
                return new CompressResult[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(status);
            dest.writeParcelable(srcPath, flags);
            dest.writeParcelable(outPath, flags);
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public Uri getSrcPath() {
            return srcPath;
        }

        public void setSrcPath(Uri srcPath) {
            this.srcPath = srcPath;
        }

        public Uri getOutPath() {
            return outPath;
        }

        public void setOutPath(Uri outPath) {
            this.outPath = outPath;
        }
    }

    /**
     * 压缩结果回到监听类
     */
    public interface CompressListener {
        void onCompressStart();

        void onCompressEnd(CompressResult imageOutPath);

        void onCompressFail(Exception exception);
    }


    private class CompressRunnable implements Runnable {
        private final Uri srcPath;
        private final int outWidth;
        private final int outHeight;
        private final int maxFileSize;
        private final String outPath;

        public CompressRunnable( Uri srcPath, String outPath, int outWidth, int outHeight, int maxFileSize) {
            this.srcPath = srcPath;
            this.outPath = outPath;
            this.outWidth = outWidth;
            this.outHeight = outHeight;
            this.maxFileSize = maxFileSize;
            if (compressListener != null) {
                compressListener.onCompressStart();
            }
        }

        @Override
        public void run() {
            CompressResult compressResult = new CompressResult();
            Uri outPutPath = null;
            try {
                outPutPath = compressImage( srcPath, outPath, outWidth, outHeight, maxFileSize);
            } catch (Exception e) {
                e.printStackTrace();
                if (compressListener != null) {
                    compressListener.onCompressFail(e);
                }
            }
            if (outPutPath == null) {
                if (compressListener != null) {
                    compressListener.onCompressFail(new Exception("图片压缩异常！"));
                }
            }
            compressResult.setSrcPath(srcPath);
            compressResult.setOutPath(outPutPath);
            compressResult.setStatus(CompressResult.RESULT_OK);
            if (compressListener != null) {
                compressListener.onCompressEnd(compressResult);
            }
        }
    }

    /**
     * @author fz
     * @dec 开启线程, 进行压缩
     * @date 2023/4/26 16:23
     */
    public static class ThreadExecutor extends ThreadPoolExecutor {
        private static final int CORE_POOL_SIZE = 3;
        //以CPU总数*2作为线程池上限
        private static final int MAXI_MUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
        private static final int KEEP_ALIVE_TIME = 10;
        private static volatile ThreadExecutor executor;

        private static final ThreadFactory sThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "ThreadExecutor #" + mCount.getAndIncrement());
            }
        };

        public ThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        //单例模式
        public static ThreadExecutor getInstance() {
            if (null == executor) {
                synchronized (ThreadExecutor.class) {
                    if (null == executor) {
                        executor = new ThreadExecutor(CORE_POOL_SIZE, MAXI_MUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                                sThreadFactory);
                    }
                }
            }
            return executor;
        }
    }

}
