package io.coderf.arklab.media.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import io.coderf.arklab.media.utils.MediaUtil;

/**
 * 图片裁剪工具方法：解码、EXIF 纠正、按矩阵裁剪、圆形蒙版、保存文件。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/31 15:51
 */
public final class CropUtil {

    /**
     * 解码时最长边上限，避免 OOM
     */
    private static final int MAX_DECODE_SIZE = 4096;

    /**
     * 私有构造，禁止实例化
     */
    private CropUtil() {
    }

    /**
     * 从 Uri 或 File 路径加载并按 EXIF 纠正方向的 Bitmap
     *
     * @param context    上下文
     * @param sourceUri  源 Uri，可空
     * @param sourcePath 源文件路径，可空
     * @return Bitmap，失败返回 null
     */
    @Nullable
    public static Bitmap loadBitmap(@NonNull Context context,
                                    @Nullable Uri sourceUri,
                                    @Nullable String sourcePath) {
        try {
            if (sourceUri != null) {
                return decodeUri(context, sourceUri);
            }
            if (!TextUtils.isEmpty(sourcePath)) {
                Bitmap oriented = MediaUtil.orientation(sourcePath);
                if (oriented != null) {
                    return oriented;
                }
                return decodeFilePath(sourcePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解码 Uri 图片并纠正 EXIF 方向
     *
     * @param context 上下文
     * @param uri     图片 Uri
     * @return Bitmap
     */
    @Nullable
    private static Bitmap decodeUri(@NonNull Context context, @NonNull Uri uri) throws Exception {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(is, null, bounds);
        }
        int sample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_DECODE_SIZE);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap;
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(is, null, options);
        }
        if (bitmap == null) {
            return null;
        }
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is != null) {
                ExifInterface exif = new ExifInterface(is);
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
            }
        } catch (Exception ignored) {
        }
        return applyExifOrientation(bitmap, orientation);
    }

    /**
     * 解码本地文件路径
     *
     * @param path 文件绝对路径
     * @return Bitmap
     */
    @Nullable
    private static Bitmap decodeFilePath(@NonNull String path) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        int sample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_DECODE_SIZE);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 计算 inSampleSize
     *
     * @param width     原图宽
     * @param height    原图高
     * @param maxSide   最长边限制
     * @return inSampleSize
     */
    private static int calculateInSampleSize(int width, int height, int maxSide) {
        int inSampleSize = 1;
        int max = Math.max(width, height);
        while (max / inSampleSize > maxSide) {
            inSampleSize *= 2;
        }
        return Math.max(1, inSampleSize);
    }

    /**
     * 按 EXIF orientation 旋转/翻转 Bitmap
     *
     * @param bitmap      原图
     * @param orientation EXIF orientation
     * @return 纠正后的 Bitmap
     */
    @NonNull
    private static Bitmap applyExifOrientation(@NonNull Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1, -1);
                break;
            default:
                return bitmap;
        }
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (result != bitmap) {
            bitmap.recycle();
        }
        return result;
    }

    /**
     * 根据 ImageView 矩阵与裁剪框，从源图裁出区域
     *
     * @param sourceBitmap 源图
     * @param imageMatrix  ImageView 当前矩阵
     * @param cropRect     裁剪框（View 坐标）
     * @param cropShape    裁剪形状
     * @param maxOutputSize 最长边限制，&lt;=0 不限制
     * @return 裁剪结果 Bitmap
     */
    @Nullable
    public static Bitmap cropBitmap(@NonNull Bitmap sourceBitmap,
                                    @NonNull Matrix imageMatrix,
                                    @NonNull RectF cropRect,
                                    @NonNull CropShapeEnum cropShape,
                                    int maxOutputSize) {
        Matrix inverse = new Matrix();
        if (!imageMatrix.invert(inverse)) {
            return null;
        }
        RectF srcRect = new RectF(cropRect);
        inverse.mapRect(srcRect);

        int left = Math.max(0, Math.round(srcRect.left));
        int top = Math.max(0, Math.round(srcRect.top));
        int right = Math.min(sourceBitmap.getWidth(), Math.round(srcRect.right));
        int bottom = Math.min(sourceBitmap.getHeight(), Math.round(srcRect.bottom));
        int width = right - left;
        int height = bottom - top;
        if (width <= 0 || height <= 0) {
            return null;
        }

        Bitmap cropped = Bitmap.createBitmap(sourceBitmap, left, top, width, height);
        if (maxOutputSize > 0) {
            int maxSide = Math.max(cropped.getWidth(), cropped.getHeight());
            if (maxSide > maxOutputSize) {
                float scale = maxOutputSize * 1f / maxSide;
                int outW = Math.max(1, Math.round(cropped.getWidth() * scale));
                int outH = Math.max(1, Math.round(cropped.getHeight() * scale));
                Bitmap scaled = Bitmap.createScaledBitmap(cropped, outW, outH, true);
                if (scaled != cropped) {
                    cropped.recycle();
                }
                cropped = scaled;
            }
        }

        if (cropShape == CropShapeEnum.CIRCLE) {
            Bitmap circle = toCircleBitmap(cropped);
            if (circle != cropped) {
                cropped.recycle();
            }
            return circle;
        }
        return cropped;
    }

    /**
     * 将正方形（或接近）Bitmap 裁成圆形透明 PNG 用图
     *
     * @param source 源图
     * @return 圆形 Bitmap
     */
    @NonNull
    public static Bitmap toCircleBitmap(@NonNull Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Path path = new Path();
        path.addCircle(size / 2f, size / 2f, size / 2f, Path.Direction.CW);
        canvas.drawPath(path, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        int left = (source.getWidth() - size) / 2;
        int top = (source.getHeight() - size) / 2;
        canvas.drawBitmap(source, -left, -top, paint);
        return output;
    }

    /**
     * 保存 Bitmap 到文件
     *
     * @param bitmap   图片
     * @param outFile  目标文件
     * @param isPng    true 使用 PNG，否则 JPEG
     * @return true 保存成功
     */
    public static boolean saveBitmap(@NonNull Bitmap bitmap, @NonNull File outFile, boolean isPng) {
        File parent = outFile.getParentFile();
        if (parent != null && !parent.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile);
            if (isPng) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos);
            }
            fos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 解析输出文件：目录 + 文件名（自动补扩展名、防空）
     *
     * @param outputDir      输出目录
     * @param outputFileName 配置的文件名，可空
     * @param cropShape      裁剪形状（决定默认扩展名）
     * @return 输出 File
     */
    @NonNull
    public static File resolveOutputFile(@NonNull String outputDir,
                                         @Nullable String outputFileName,
                                         @NonNull CropShapeEnum cropShape) {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        boolean preferPng = cropShape == CropShapeEnum.CIRCLE;
        String extension = preferPng
                ? ImageCropBuilder.DEFAULT_OUTPUT_EXTENSION_PNG
                : ImageCropBuilder.DEFAULT_OUTPUT_EXTENSION_JPG;
        String name;
        if (TextUtils.isEmpty(outputFileName)) {
            name = "CROP_" + MediaUtil.getCurrentTime() + extension;
        } else {
            name = outputFileName;
            if (!name.contains(".")) {
                name = name + extension;
            }
        }
        return new File(dir, name);
    }
}
