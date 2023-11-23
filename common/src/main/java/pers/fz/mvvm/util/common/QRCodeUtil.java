package pers.fz.mvvm.util.common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.ColorInt;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Vector;

import pers.fz.mvvm.util.zxing.BitmapLuminanceSource;

/**
 * Created by fz on 2023/11/9 13:35
 * describe :
 */
public class QRCodeUtil {

    private static final Collection<BarcodeFormat> PRODUCT_FORMATS;
    private static final Collection<BarcodeFormat> ONE_D_FORMATS;
    private static final Collection<BarcodeFormat> QR_CODE_FORMATS = EnumSet
            .of(BarcodeFormat.QR_CODE);
    private static final Collection<BarcodeFormat> DATA_MATRIX_FORMATS = EnumSet
            .of(BarcodeFormat.DATA_MATRIX);

    static {
        PRODUCT_FORMATS = EnumSet.of(BarcodeFormat.UPC_A, BarcodeFormat.UPC_E,
                BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
                BarcodeFormat.RSS_14, BarcodeFormat.RSS_EXPANDED);
        ONE_D_FORMATS = EnumSet.of(BarcodeFormat.CODE_39,
                BarcodeFormat.CODE_93, BarcodeFormat.CODE_128,
                BarcodeFormat.ITF, BarcodeFormat.CODABAR);
        ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
    }

    public static Bitmap createQrCode(String content) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            return barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 400, 400);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap createQrCode(String content, int width, int height) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            return barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, width, height);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap createQRCodeBitmap(String content) {
        return createQRCodeBitmap(content, 400, 400, null, null, null, 0xff000000, 0xffffffff, null, 0.2f);
    }

    public static Bitmap createQRCodeBitmap(String content, @ColorInt int colorBlack) {
        return createQRCodeBitmap(content, 400, 400, null, null, null, colorBlack, 0xffffffff, null, 0.2f);
    }

    public static Bitmap createQRCodeBitmap(String content, @ColorInt int colorBlack, @ColorInt int colorWhite) {
        return createQRCodeBitmap(content, 400, 400, null, null, null, colorBlack, colorWhite, null, 0.2f);
    }

    public static Bitmap createQRCodeBitmap(String content, String margin, @ColorInt int colorBlack, @ColorInt int colorWhite) {
        return createQRCodeBitmap(content, 400, 400, null, null, margin, colorBlack, colorWhite, null, 0.2f);
    }


    public static Bitmap createQRCodeBitmap(String content, int width, int height, String margin, @ColorInt int colorBlack, @ColorInt int colorWhite) {
        return createQRCodeBitmap(content, width, height, null, null, margin, colorBlack, colorWhite, null, 0.2f);
    }

    public static Bitmap createQRCodeBitmap(String content, Bitmap logoBitmap, float logoPercent) {
        return createQRCodeBitmap(content, 400, 400, null, null, null, 0xff000000, 0xffffffff, logoBitmap, logoPercent);
    }

    public static Bitmap createQRCodeBitmap(String content, @ColorInt int colorBlack, @ColorInt int colorWhite, Bitmap logoBitmap, float logoPercent) {
        return createQRCodeBitmap(content, 400, 400, null, null, null, colorBlack, colorWhite, logoBitmap, logoPercent);
    }

    public static Bitmap createQRCodeBitmap(String content, @ColorInt int colorBlack,  Bitmap logoBitmap, float logoPercent) {
        return createQRCodeBitmap(content, 400, 400, null, null, null, colorBlack, 0xffffffff, logoBitmap, logoPercent);
    }

    public static Bitmap createQRCodeBitmap(String content, String margin, @ColorInt int colorBlack, @ColorInt int colorWhite, Bitmap logoBitmap, float logoPercent) {
        return createQRCodeBitmap(content, 400, 400, null, null, margin, colorBlack, colorWhite, logoBitmap, logoPercent);
    }

    public static Bitmap createQRCodeBitmap(String content, int width, int height, String margin, @ColorInt int colorBlack, @ColorInt int colorWhite, Bitmap logoBitmap, float logoPercent) {
        return createQRCodeBitmap(content, width, height, null, null, margin, colorBlack, colorWhite, logoBitmap, logoPercent);
    }

    /**
     * 创建二维码位图 (支持自定义配置和自定义样式)
     *
     * @param content         字符串内容
     * @param width           位图宽度,要求>=0(单位:px)
     * @param height          位图高度,要求>=0(单位:px)
     * @param characterSet    字符集/字符转码格式 (支持格式:{@link CharacterSetECI })。传null时,zxing源码默认使用 "ISO-8859-1"
     * @param errorCorrection 容错级别 (支持级别:{@link ErrorCorrectionLevel })。传null时,zxing源码默认使用 "L"
     * @param margin          空白边距 (可修改,要求:整型且>=0), 传null时,zxing源码默认使用"4"。
     * @param colorBlack      黑色色块的自定义颜色值
     * @param colorWhite      白色色块的自定义颜色值
     * @param logoBitmap      logo小图片
     * @param logoPercent     logo小图片在二维码图片中的占比大小,范围[0F,1F],超出范围->默认使用0.2F。
     * @return
     */
    public static Bitmap createQRCodeBitmap(String content, int width, int height, EncodeHintType characterSet, EncodeHintType errorCorrection, String margin, @ColorInt int colorBlack, @ColorInt int colorWhite, Bitmap logoBitmap, float logoPercent) {
        /* 1.参数合法性判断  */
        if (width < 0 || height < 0) {
            // 宽和高都需要>=0
            return null;
        }
        try {
            /* 2.设置二维码相关配置,生成BitMatrix(位矩阵)对象  */
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            if (characterSet != null) {
                hints.put(EncodeHintType.CHARACTER_SET, characterSet);
                // 字符转码格式设置
            } else {
                hints.put(EncodeHintType.CHARACTER_SET, CharacterSetECI.UTF8);
            }
            if (errorCorrection != null) {
                hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrection);
                // 容错级别设置
            } else {
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            }
            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin);
                // 空白边距设置
            } else {
                hints.put(EncodeHintType.MARGIN, 0);
            }
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            /*3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值  */
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = colorBlack;
                        // 黑色色块像素设置
                    } else {
                        pixels[y * width + x] = colorWhite;
                        // 白色色块像素设置
                    }
                }
            }
            /* 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,之后返回Bitmap对象  */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            /* 5.为二维码添加logo小图标 */
            if (logoBitmap != null) {
                return addLogo(bitmap, logoBitmap, logoPercent);
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap addLogo(Bitmap srcBitmap, Bitmap logoBitmap, float logoPercent) {
        /* 1.参数合法性判断  */
        if (srcBitmap == null || logoBitmap == null) {
            return null;
        }
        float percent = logoPercent;
        if (logoPercent < 0F || logoPercent > 1F) {
            percent = 0.2F;
        }
        /* 2. 获取原图片和Logo图片各自的宽、高值 */
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();
        int logoWidth = logoBitmap.getWidth();
        int logoHeight = logoBitmap.getHeight();
        /* 3. 计算画布缩放的宽高比 */
        float scaleWidth = srcWidth * percent / logoWidth;
        float scaleHeight = srcHeight * percent / logoHeight;
        /* 4. 使用Canvas绘制,合成图片 */
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(srcBitmap, 0f, 0f, null);
        canvas.scale(scaleWidth, scaleHeight, (srcWidth / 2f), (srcHeight / 2f));
        canvas.drawBitmap(logoBitmap, srcWidth * 1f / 2 - logoWidth / 2, srcHeight * 1f / 2 - logoHeight / 2, null);
        return bitmap;
    }

    /**
     * 获取解码结果
     *
     * @param bitmap
     * @return
     */
    public static Result getRawResult(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        MultiFormatReader multiFormatReader = new MultiFormatReader();

        // 解码的参数
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>(
                2);
        // 可以解析的编码类型
        Vector<BarcodeFormat> decodeFormats = new Vector<>();

        // 这里设置可扫描的类型，我这里选择了都支持
        decodeFormats.addAll(ONE_D_FORMATS);
        decodeFormats.addAll(QR_CODE_FORMATS);
        decodeFormats.addAll(DATA_MATRIX_FORMATS);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        // 设置继续的字符编码格式为UTF8
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8");

        // 设置解析配置参数
        multiFormatReader.setHints(hints);
        try {
            Result result = multiFormatReader.decodeWithState(new BinaryBitmap(
                    new HybridBinarizer(new BitmapLuminanceSource(bitmap))));
            Log.d("QRCodeUtil", "识别结果：" + (result == null));
            return result;
        } catch (NotFoundException e) {
            e.printStackTrace();
            Log.d("QRCodeUtil", "识别异常：" + e);
        }
        return null;
    }

}
