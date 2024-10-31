package pers.fz.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


/**
 * Created by fz on 2021/4/12 10:35
 * describe:
 */
public class MediaUtil {
    private final String TAG = this.getClass().getSimpleName();

    public static String getPictureLongitude(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getPictureLatitude(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getPictureLocation(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            String[] strings =  new String[2];
            ExifInterface exif = new ExifInterface(filePath);
            strings[0] = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            strings[1] = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            return strings;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Bitmap createWatermark(Bitmap originalBitmap, String watermarkText) {
        return createWatermark(originalBitmap, watermarkText, 100);
    }

    /**
     * 添加图片水印
     *
     * @param originalBitmap 图片
     * @param watermarkText  水印文字，默认添加时间水印，在mark内容的上一行
     * @return 添加水印后的图片
     */
    public static Bitmap createWatermark(Bitmap originalBitmap, String watermarkText, int alpha) {
        if (watermarkText == null) {
            return originalBitmap;
        }
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 255) {
            alpha = 255;
        }
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        Paint p = new Paint();
        // 水印颜色,先不透明
        p.setColor(Color.argb(255, 169, 169, 169));
        // 水印字体大小
        p.setTextSize(32);
        // 抗锯齿
        p.setAntiAlias(true);
        // 绘制图像
        canvas.drawBitmap(originalBitmap, 0, 0, p);

        // 绘制文字
        canvas.save();
        canvas.rotate(-30);
        float textWidth = p.measureText(watermarkText);
        int index = 0;
        for (int positionY = height / 10; positionY <= height; positionY += height / 10 + 80) {
            float fromX = -width + (index++ % 2) * textWidth;
            for (float positionX = fromX; positionX < width; positionX += textWidth * 2) {
                int spacing = 0;
                // 保存文字透明度// 间距
                p.setAlpha(alpha);
                canvas.drawText(watermarkText, positionX, positionY + spacing, p);
                // 恢复文字透明度
            }
        }
        canvas.restore();
        return resultBitmap;
    }

    /**
     * @param absolutePath 照片的绝对路劲
     * @return 重新调整方向之后的bitmap图片
     * @author yukaida
     */
    public static Bitmap orientation(String absolutePath) {
        Bitmap bitmapOr = BitmapFactory.decodeFile(absolutePath);
        try {
            ExifInterface exif = new ExifInterface(absolutePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            bitmapOr = Bitmap.createBitmap(bitmapOr, 0, 0, bitmapOr.getWidth(), bitmapOr.getHeight(), matrix, true);
            // rotating bitmap
            return bitmapOr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存bitmap
     *
     * @param bmp      源图片
     * @param filePath 源文件路径，保存的话直接替换源文件
     */
    public static void saveBitmap(Bitmap bmp, String filePath) {
        try { // 获取SDCard指定目录下
            File dirFile = new File(filePath);
            //目录转化成文件夹
            if (!dirFile.getParentFile().exists()) {
                //如果不存在，那就建立这个文件夹
                dirFile.getParentFile().mkdirs();
            }                          //文件夹有啦，就可以保存图片啦
            FileOutputStream out = new FileOutputStream(dirFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getDefaultBasePath(Context mContext) {
        String packageName = mContext.getPackageName();
        String[] packageArr = packageName.split("\\.");
        if (packageArr.length == 0) {
            return "";
        }
        if (packageArr.length == 1) {
            return packageArr[0];
        }
        return packageArr[1];
    }

    public static String getLastPath(String path, String defaultPath) {
        if (TextUtils.isEmpty(path)) {
            return defaultPath;
        }
        String[] pathArr = path.split(File.separator);
        if (pathArr == null) {
            return defaultPath;
        }
        if (pathArr.length == 0) {
            return defaultPath;
        }
        if (pathArr.length == 1) {
            return pathArr[0];
        }
        if (File.separator.equals(pathArr[pathArr.length - 1])) {
            return TextUtils.isEmpty(pathArr[pathArr.length - 2]) ? defaultPath : pathArr[pathArr.length - 2];
        }
        return TextUtils.isEmpty(pathArr[pathArr.length - 1]) ? defaultPath : pathArr[pathArr.length - 1];
    }


    /**
     * 获取basePath下不重复的文件名
     *
     * @param basePath  基础目录
     * @param prefix    默认前缀
     * @param extension 扩展名
     * @return 文件名，不带后缀名的
     */
    public static String getNoRepeatFileName(String basePath, String prefix, String extension) {
        File baseFile = new File(basePath);
        if (!baseFile.exists()) {
            boolean isCreated = baseFile.mkdirs();
        }
        String fileName = prefix + MediaUtil.dateFormat(new Date(), MediaUtil.DATE_TIME_FORMAT) + "_" + new Random().nextInt(1000);
        File file = new File(baseFile, fileName + extension);
        int index = 0;
        //防止重名
        while (file.exists()) {
            index += 1;
            file = new File(baseFile, fileName + "_" + index + extension);
        }
        return fileName;
    }

    /**
     * 获取basePath下不重复的文件名
     *
     * @return 如果存储路径中有重复的则自动+1，如果没有则返回文件名
     */
    public static String autoRenameFileName(String baseSavePath, String oldName) {
        try {
            // 检查文件是否有后缀名
            if (!oldName.contains(".")) {
                oldName += "." + oldName.split("\\.")[oldName.split("\\.").length - 1];
            }

            // 拼接完整的文件路径
            String filePath = baseSavePath + File.separator + oldName;

            // 判断文件是否存在
            File file = new File(filePath);
            if (!file.exists()) {
                return oldName;
            }

            // 文件已存在，查找可用的文件名
            int count = 1;
            while (true) {
                String newFileName = oldName.split("\\.")[0] + count + "." + oldName.split("\\.")[oldName.split("\\.").length - 1];
                String newFilePath = baseSavePath + File.separator + newFileName;
                File newFile = new File(newFilePath);
                if (!newFile.exists()) {
                    return newFileName;
                }
                count++;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return oldName;
    }

    /**
     * 格式化日期显示格式
     *
     * @param date   Date对象
     * @param format 格式化后日期格式
     * @return 格式化后的日期显示
     */
    public static String dateFormat(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return dateSimpleFormat(date, formatter);
    }

    /**
     * 将date转成字符串
     *
     * @param date   Date
     * @param format SimpleDateFormat
     *               <br>
     *               注： SimpleDateFormat为空时，采用默认的yyyy-MM-dd HH:mm:ss格式
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String dateSimpleFormat(Date date, SimpleDateFormat format) {
        if (format == null) {
            synchronized (MediaUtil.class) {
                format = defaultDateTimeFormat.get();
            }
        }
        return (date == null ? "" : format.format(date));
    }


    /**
     * yyyy-MM-dd HH:mm:ss格式
     */
    public static final ThreadLocal<SimpleDateFormat> defaultDateTimeFormat = new ThreadLocal<>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
        }
    };

    /**
     * yyyy-MM-dd HH:mm:ss字符串
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * yyyyMMddHHmmss字符串
     */
    public static final String DATE_TIME_FORMAT = "yyyyMMddHHmmss";
}
