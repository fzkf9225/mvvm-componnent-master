package pers.fz.mvvm.util.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import pers.fz.mvvm.util.apiUtil.DateUtil;
import pers.fz.mvvm.util.apiUtil.StringUtil;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2021/4/12 10:35
 * describe:
 */
public class MediaUtil {
    private final String TAG = this.getClass().getSimpleName();

    public static void getPictureLocation(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            LogUtil.show("MediaUtil","纬度：" + latitude);
            LogUtil.show("MediaUtil","经度：" + longitude);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show("MediaUtil","获取图片经纬度异常:" + e);
        }
    }

    /**
     * 添加图片水印
     *
     * @param bitmap 图片
     * @param mark   水印文字，默认添加时间水印，在mark内容的上一行
     * @return 添加水印后的图片
     */
    public static Bitmap createWatermark(Bitmap bitmap, String mark) {
        if (mark == null) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        TextPaint textPaint = new TextPaint();
        // 水印颜色
        textPaint.setColor(Color.parseColor("#acacac"));
        // 水印字体大小
        textPaint.setTextSize(32);
        //抗锯齿
        textPaint.setAntiAlias(true);
        //绘制图像
        canvas.drawBitmap(bitmap, 0, 0, textPaint);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        //绘制文字
        StaticLayout myStaticLayout;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            myStaticLayout = StaticLayout.Builder.obtain(mark, 0, mark.length(), textPaint, canvas.getWidth())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0.0f, 0.8f)
                    .setIncludePad(false)
                    .build();
        } else {
            myStaticLayout = new StaticLayout(mark, textPaint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL,
                    1.0f, 0.0f, false);
        }
        canvas.drawText(DateUtil.getDateTimeFromMillis(Calendar.getInstance().getTimeInMillis()), 0,
                h - (fontMetrics.descent - fontMetrics.ascent) * myStaticLayout.getLineCount(), textPaint);
        canvas.translate(0, h - (fontMetrics.descent - fontMetrics.ascent) * myStaticLayout.getLineCount());
        myStaticLayout.draw(canvas);

        canvas.save();
        canvas.restore();
        return bmp;
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
}
