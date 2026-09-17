package io.coderf.arklab.media.compressor.image;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Parcel;
import android.text.TextUtils;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.coderf.arklab.media.utils.MediaUtil;


/**
 * ImgCompressor 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 16/5/25
 */
public class ImgCompressor {
    @SuppressLint("StaticFieldLeak")
    private volatile static ImgCompressor instance = null;
    private final Context context;
    private CompressListener compressListener;
    private static final int DEFAULT_MAXFILESIZE = 1024;//KB
    private static final String DEFAULT_FILE_PROVIDER_SUFFIX = ".FileProvider";

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
    public Uri compressImage(Uri srcImageUri, String outputPath, int outWidth, int outHeight, int maxFileSize) {
        return compressImage(srcImageUri, outputPath, outWidth, outHeight, maxFileSize, ".jpg");
    }

    /**
     * @param srcImageUri  原始图片的uri路径
     * @param outputPath   输出目录
     * @param outWidth     期望的输出图片的宽度
     * @param outHeight    期望的输出图片的高度
     * @param maxFileSize  期望的输出图片的最大占用的存储空间，单位 kb
     * @param fileExtension 输出文件扩展名，如 .jpg；编码格式会根据扩展名自动选择
     */
    public Uri compressImage(Uri srcImageUri, String outputPath, int outWidth, int outHeight, int maxFileSize,
                             String fileExtension) {
        return compressImage(srcImageUri, outputPath, outWidth, outHeight, maxFileSize, fileExtension,
                context.getPackageName() + DEFAULT_FILE_PROVIDER_SUFFIX);
    }

    /**
     * @param fileProviderAuthority FileProvider authority，空则使用 {@code packageName.FileProvider}
     */
    public Uri compressImage(Uri srcImageUri, String outputPath, int outWidth, int outHeight, int maxFileSize,
                             String fileExtension, String fileProviderAuthority) {

        //进行大小缩放来达到压缩的目的
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            if (compressListener != null) {
                compressListener.onCompressFail(new Exception("打开内容解析器失败！"));
            }
            return null;
        }
        try (ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(srcImageUri, "r")) {
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
        } catch (IOException e) {
            e.printStackTrace();
            if (compressListener != null) {
                compressListener.onCompressFail(e);
            }
            return null;
        }
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        float[] actualOut = resolveOutputSize(srcWidth, srcHeight, outWidth, outHeight);
        float actualOutWidth = actualOut[0];
        float actualOutHeight = actualOut[1];
        options.inSampleSize = computSampleSize(options, actualOutWidth, actualOutHeight);
        options.inJustDecodeBounds = false;
        Bitmap scaledBitmap;
        try (ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(srcImageUri, "r")) {
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
        } catch (IOException e) {
            e.printStackTrace();
            if (compressListener != null) {
                compressListener.onCompressFail(e);
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
        try (InputStream inputStream = contentResolver.openInputStream(srcImageUri)) {
            exif = new ExifInterface(Objects.requireNonNull(inputStream));
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
        Bitmap.CompressFormat compressFormat = MediaUtil.compressFormatFromExtension(fileExtension);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options_ = 100;
        actualOutBitmap.compress(compressFormat, options_, baos);//质量压缩方法，把压缩后的数据存放到baos中 (100表示不压缩，0表示压缩到最小)

        int baosLength = baos.toByteArray().length;

        while (baosLength / 1024 > maxFileSize) {//循环判断如果压缩后图片是否大于maxMemmorrySize,大于继续压缩
            baos.reset();//重置baos即让下一次的写入覆盖之前的内容
            options_ = Math.max(0, options_ - 10);//图片质量每次减少10
            actualOutBitmap.compress(compressFormat, options_, baos);//将压缩后的图片保存到baos中
            baosLength = baos.toByteArray().length;
            if (options_ == 0)//如果图片的质量已降到最低则，不再进行压缩
            {
                break;
            }
        }
        actualOutBitmap.recycle();
        //将bitmap保存到指定路径
        FileOutputStream fos = null;
        String normalizedExtension = TextUtils.isEmpty(fileExtension) ? ".jpg"
                : (fileExtension.startsWith(".") ? fileExtension : "." + fileExtension);
        String fileName = MediaUtil.getNoRepeatFileName(outputPath, "IMG_", normalizedExtension);
        File outputFile = new File(outputPath, fileName + normalizedExtension);
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
        String authority = TextUtils.isEmpty(fileProviderAuthority)
                ? context.getPackageName() + DEFAULT_FILE_PROVIDER_SUFFIX
                : fileProviderAuthority;
        return FileProvider.getUriForFile(context, authority, outputFile);
    }

    /**
     * 计算压缩输出宽高，始终保持原图宽高比。
     * <ul>
     *   <li>宽高都未配置（&lt;=0）：按原图像素自动分档等比缩放</li>
     *   <li>只配一边：该边作为上限，另一边按原比例</li>
     *   <li>两边都配：落入指定框内，等比缩放</li>
     * </ul>
     */
    private static float[] resolveOutputSize(float srcWidth, float srcHeight, int outWidth, int outHeight) {
        if (srcWidth <= 0 || srcHeight <= 0) {
            return new float[]{Math.max(1f, srcWidth), Math.max(1f, srcHeight)};
        }
        if (outWidth <= 0 && outHeight <= 0) {
            int sample = computeAutoSampleSize(srcWidth, srcHeight);
            return new float[]{
                    Math.max(1f, srcWidth / sample),
                    Math.max(1f, srcHeight / sample)
            };
        }
        float srcRatio = srcWidth / srcHeight;
        if (outWidth <= 0) {
            if (srcHeight > outHeight) {
                return new float[]{Math.max(1f, outHeight * srcRatio), outHeight};
            }
            return new float[]{srcWidth, srcHeight};
        }
        if (outHeight <= 0) {
            if (srcWidth > outWidth) {
                return new float[]{outWidth, Math.max(1f, outWidth / srcRatio)};
            }
            return new float[]{srcWidth, srcHeight};
        }
        float actualOutWidth = srcWidth;
        float actualOutHeight = srcHeight;
        if (srcWidth > (float) outWidth || srcHeight > (float) outHeight) {
            float outRatio = (float) outWidth / (float) outHeight;
            if (srcRatio < outRatio) {
                actualOutHeight = (float) outHeight;
                actualOutWidth = actualOutHeight * srcRatio;
            } else if (srcRatio > outRatio) {
                actualOutWidth = (float) outWidth;
                actualOutHeight = actualOutWidth / srcRatio;
            } else {
                actualOutWidth = (float) outWidth;
                actualOutHeight = (float) outHeight;
            }
        }
        return new float[]{Math.max(1f, actualOutWidth), Math.max(1f, actualOutHeight)};
    }

    /**
     * 按原图长短边自动计算下采样倍数，保持宽高比。
     * 小图不缩小；中大图按长边分档（约 2 / 4 / 长边÷1280）。
     */
    private static int computeAutoSampleSize(float srcWidth, float srcHeight) {
        int width = Math.round(srcWidth);
        int height = Math.round(srcHeight);
        if ((width & 1) == 1) {
            width++;
        }
        if ((height & 1) == 1) {
            height++;
        }
        int longSide = Math.max(width, height);
        int shortSide = Math.min(width, height);
        if (longSide <= 0 || shortSide <= 0) {
            return 1;
        }
        float scale = (float) shortSide / (float) longSide;
        if (scale > 0.5625f) {
            if (longSide < 1664) {
                return 1;
            }
            if (longSide < 4990) {
                return 2;
            }
            if (longSide < 10240) {
                return 4;
            }
            int sample = longSide / 1280;
            return sample <= 0 ? 1 : sample;
        }
        if (scale > 0.5f) {
            int sample = longSide / 1280;
            return sample <= 0 ? 1 : sample;
        }
        int sample = (int) Math.ceil(longSide / (1280.0 / scale));
        return sample <= 0 ? 1 : sample;
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

    public void starCompress(Uri srcImageUri, String outPath, int outWidth, int outHeight, int maxFileSize) {
        starCompress(srcImageUri, outPath, outWidth, outHeight, maxFileSize, ".jpg");
    }

    /**
     * @param fileExtension 输出文件扩展名，如 .jpg；传 null 或空则默认 .jpg
     */
    public void starCompress(Uri srcImageUri, String outPath, int outWidth, int outHeight, int maxFileSize,
                             String fileExtension) {
        starCompress(srcImageUri, outPath, outWidth, outHeight, maxFileSize, fileExtension,
                context.getPackageName() + DEFAULT_FILE_PROVIDER_SUFFIX);
    }

    public void starCompress(Uri srcImageUri, String outPath, int outWidth, int outHeight, int maxFileSize,
                             String fileExtension, String fileProviderAuthority) {
        CompressListener listener = this.compressListener;
        ThreadExecutor.getInstance().execute(
                new CompressRunnable(srcImageUri, outPath, outWidth, outHeight, maxFileSize, fileExtension,
                        fileProviderAuthority, listener));
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
        private final String fileExtension;
        private final String fileProviderAuthority;
        private final CompressListener listener;

        public CompressRunnable(Uri srcPath, String outPath, int outWidth, int outHeight, int maxFileSize,
                                String fileExtension, String fileProviderAuthority, CompressListener listener) {
            this.srcPath = srcPath;
            this.outPath = outPath;
            this.outWidth = outWidth;
            this.outHeight = outHeight;
            this.maxFileSize = maxFileSize;
            this.fileExtension = fileExtension;
            this.fileProviderAuthority = fileProviderAuthority;
            this.listener = listener;
            if (listener != null) {
                listener.onCompressStart();
            }
        }

        @Override
        public void run() {
            CompressResult compressResult = new CompressResult();
            Uri outPutPath = null;
            try {
                outPutPath = compressImage(srcPath, outPath, outWidth, outHeight, maxFileSize, fileExtension,
                        fileProviderAuthority);
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onCompressFail(e);
                }
            }
            if (outPutPath == null) {
                if (listener != null) {
                    listener.onCompressFail(new Exception("图片压缩异常！"));
                }
            }
            compressResult.setSrcPath(srcPath);
            compressResult.setOutPath(outPutPath);
            compressResult.setStatus(CompressResult.RESULT_OK);
            if (listener != null) {
                listener.onCompressEnd(compressResult);
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
