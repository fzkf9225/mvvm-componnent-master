package io.coderf.arklab.common.widget.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * created fz on 2024/10/22 19:56
 * 自定义相机中的取消和确认按钮
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class TypeButton extends View {
    /**
     * 取消按钮，绘制返回箭头
     */
    public static final int TYPE_CANCEL = 0x001;
    /**
     * 确认按钮，绘制绿色勾
     */
    public static final int TYPE_CONFIRM = 0x002;
    /**
     * 按钮类型，见 {@link #TYPE_CANCEL}、{@link #TYPE_CONFIRM}
     */
    private int button_type;
    /**
     * 按钮边长
     */
    private int button_size;

    /**
     * 圆心 X 坐标
     */
    private float center_X;
    /**
     * 圆心 Y 坐标
     */
    private float center_Y;
    /**
     * 圆形背景半径
     */
    private float button_radius;

    /**
     * 按钮绘制画笔
     */
    private Paint mPaint;
    /**
     * 箭头或对勾路径
     */
    private Path path;
    /**
     * 图标线宽
     */
    private float strokeWidth;

    /**
     * 图标相对按钮尺寸的基准偏移
     */
    private float index;
    /**
     * 取消箭头圆弧区域
     */
    private RectF rectF;

    public TypeButton(Context context) {
        super(context);
    }

    /**
     * 按类型和尺寸初始化确认或取消按钮
     *
     * @param type 按钮类型
     * @param size 按钮边长
     */
    public TypeButton(Context context, int type, int size) {
        super(context);
        this.button_type = type;
        button_size = size;
        button_radius = size / 2.0f;
        center_X = size / 2.0f;
        center_Y = size / 2.0f;

        mPaint = new Paint();
        path = new Path();
        strokeWidth = size / 50f;
        index = button_size / 12f;
        rectF = new RectF(center_X, center_Y - index, center_X + index * 2, center_Y + index);
    }

    /**
     * 按给定边长测量为正方形
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(button_size, button_size);
    }

    /**
     * 取消类型绘制返回箭头，确认类型绘制绿色对勾
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //如果类型为取消，则绘制内部为返回箭头
        if (button_type == TYPE_CANCEL) {
            mPaint.setAntiAlias(true);
            mPaint.setColor(0xEEDCDCDC);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(center_X, center_Y, button_radius, mPaint);

            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(strokeWidth);

            path.moveTo(center_X - index / 7, center_Y + index);
            path.lineTo(center_X + index, center_Y + index);

            path.arcTo(rectF, 90, -180);
            path.lineTo(center_X - index, center_Y - index);
            canvas.drawPath(path, mPaint);
            mPaint.setStyle(Paint.Style.FILL);
            path.reset();
            path.moveTo(center_X - index, (float) (center_Y - index * 1.5));
            path.lineTo(center_X - index, (float) (center_Y - index / 2.3));
            path.lineTo((float) (center_X - index * 1.6), center_Y - index);
            path.close();
            canvas.drawPath(path, mPaint);
        }
        //如果类型为确认，则绘制绿色勾
        if (button_type == TYPE_CONFIRM) {
            mPaint.setAntiAlias(true);
            mPaint.setColor(0xFFFFFFFF);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(center_X, center_Y, button_radius, mPaint);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0xFF00CC00);
            mPaint.setStrokeWidth(strokeWidth);

            path.moveTo(center_X - button_size / 6f, center_Y);
            path.lineTo(center_X - button_size / 21.2f, center_Y + button_size / 7.7f);
            path.lineTo(center_X + button_size / 4.0f, center_Y - button_size / 8.5f);
            path.lineTo(center_X - button_size / 21.2f, center_Y + button_size / 9.4f);
            path.close();
            canvas.drawPath(path, mPaint);
        }
    }
}
