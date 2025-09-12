package com.casic.titan.commonui.widght.tickview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Created by 陈岗不姓陈 on 2017/10/17.
 * <p>
 * 自定义view -- checkbox
 * 一个打钩的小动画
 */

public class TickCheckedView extends View {
    //打钩的画笔
    private Paint paint,paintFauiler;
    //打钩的画笔
    private float mPaintWidth = 4f;
    //控件大小
    float totalWidth = 0f;
    private int step = 1;//绘制第一根先还是第二根线
    private float startX = 0f;
    private float startY = 0f;
    private boolean isDrawCheck = true;

    public TickCheckedView(Context context) {
        this(context, null);
    }

    public TickCheckedView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        initAttrs(context);
    }

    private void initAttrs(Context context) {
        paint = new Paint();
        paintFauiler = new Paint();
        //设置画笔颜色
        paint.setColor(0x1c50b5);
        paintFauiler.setColor(0xEA3F25);
        //设置画笔的宽度
        paint.setStrokeWidth(mPaintWidth);
        paintFauiler.setStrokeWidth(mPaintWidth);
        //设置图形为空心
        paint.setStyle(Paint.Style.STROKE);
        paintFauiler.setStyle(Paint.Style.STROKE);
        //消除锯齿
        paint.setAntiAlias(true);
        paintFauiler.setAntiAlias(true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        totalWidth = getMeasuredWidth();
        startX = totalWidth / 5;
        startY = totalWidth * 2 / 3;
    }

    public TickCheckedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isDrawCheck){
            //画第一根线
            canvas.drawLine(totalWidth / 5, totalWidth * 2 / 3,
                    totalWidth / 2, totalWidth * 9 / 10, paint);
            //画第二根线
            canvas.drawLine(totalWidth / 2,
                    totalWidth * 9 / 10, totalWidth, totalWidth / 8, paint);
        }else{
            //画第一根线
            canvas.drawLine(0, 0,
                    totalWidth , totalWidth, paintFauiler);
            //画第二根线
            canvas.drawLine(0,
                    totalWidth , totalWidth, 0, paintFauiler);
        }

    }

    public void draw(boolean isDrawCheck) {
        this.isDrawCheck = isDrawCheck;
        invalidate();
    }
}
