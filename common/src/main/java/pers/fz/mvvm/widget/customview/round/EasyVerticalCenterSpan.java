package pers.fz.mvvm.widget.customview.round;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

/**
 * Created by fz on 2023/5/23 16:32
 * describe :
 */
public class EasyVerticalCenterSpan extends ReplacementSpan {
    private final float fontSizeSp;    //字体大小sp
    private final float paintColor;

    public EasyVerticalCenterSpan(float fontSizeSp, float paintColor) {
        this.fontSizeSp = fontSizeSp;
        this.paintColor = paintColor;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt
            fm) {
        paint.setTextSize(fontSizeSp);
        text = text.subSequence(start, end);
        return (int) paint.measureText(text.toString());
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int
            y, int bottom, Paint paint) {
        text = text.subSequence(start, end);
        paint.setTextSize(fontSizeSp);
        paint.setColor((int) paintColor);
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        canvas.drawText(text.toString(), x, y - (((y + fm.descent + y + fm.ascent) >> 1) - ((bottom + top) >> 1)), paint);    //此处重新计算y坐标，使字体居中
    }
}

