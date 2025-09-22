package com.casic.otitan.common.widget.camera;

import static com.casic.otitan.common.widget.camera.CameraView.Companion.Mode.BUTTON_STATE_BOTH;
import static com.casic.otitan.common.widget.camera.CameraView.Companion.Mode.BUTTON_STATE_ONLY_CAPTURE;
import static com.casic.otitan.common.widget.camera.CameraView.Companion.Mode.BUTTON_STATE_ONLY_RECORDER;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.casic.otitan.common.listener.CaptureListener;


/**
 * created fz on 2024/10/22 19:56
 * describe：
 */
public class CaptureButton extends View {

    private int state;              //当前按钮状态
    private int button_state;       //按钮可执行的功能状态（拍照,录制,两者）

    public static final int STATE_IDLE = 0x001;        //空闲状态
    public static final int STATE_PRESS = 0x002;       //按下状态
    public static final int STATE_LONG_PRESS = 0x003;  //长按状态
    public static final int STATE_RECORDING = 0x004; //录制状态
    public static final int STATE_BAN = 0x005;         //禁止状态

    private float eventY;  //Touch_Event_Down时候记录的Y值

    private Paint mPaint;

    private float strokeWidth;          //进度条宽度
    private int outsideAddSize;       //长按外圆半径变大的Size
    private int insideReduceSize;     //长安内圆缩小的Size

    //中心坐标
    private float centerX;
    private float centerY;

    private float buttonRadius;            //按钮半径
    private float buttonOutsideRadius;    //外圆半径
    private float buttonInsideRadius;     //内圆半径
    private int buttonSize;                //按钮大小

    private float progress;         //录制视频的进度
    private int duration;           //录制视频最大时间长度
    private int minDuration;       //最短录制时间限制
    private int recordedTime;      //记录当前录制的时间

    private RectF rectF;

    private LongPressRunnable longPressRunnable;    //长按后处理的逻辑Runnable
    private CaptureListener captureListener;        //按钮回调接口
    private RecordCountDownTimer timer;             //计时器

    public CaptureButton(Context context) {
        super(context);
    }

    public CaptureButton(Context context, int size) {
        super(context);
        this.buttonSize = size;
        buttonRadius = size / 2.0f;

        buttonOutsideRadius = buttonRadius;
        buttonInsideRadius = buttonRadius * 0.75f;

        strokeWidth = (float) size / 15;
        outsideAddSize = size / 5;
        insideReduceSize = size / 8;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        progress = 0;
        longPressRunnable = new LongPressRunnable();
        //初始化为空闲状态
        state = STATE_IDLE;
        //初始化按钮为可录制可拍照
        button_state = BUTTON_STATE_BOTH;
        //默认最长录制时间为30s
        duration = 30 * 1000;
        //默认最短录制时间为0s
        minDuration = 0;

        centerX = (float) (buttonSize + outsideAddSize * 2) / 2;
        centerY = (float) (buttonSize + outsideAddSize * 2) / 2;

        rectF = new RectF(centerX - (buttonRadius + outsideAddSize - strokeWidth / 2), centerY - (buttonRadius + outsideAddSize - strokeWidth / 2), centerX + (buttonRadius + outsideAddSize - strokeWidth / 2), centerY + (buttonRadius + outsideAddSize - strokeWidth / 2));

        timer = new RecordCountDownTimer(duration, duration / 360);    //录制定时器
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(buttonSize + outsideAddSize * 2, buttonSize + outsideAddSize * 2);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);
        //外圆（半透明灰色）
        //外圆背景色
        int outsideColor = 0xEEDCDCDC;
        mPaint.setColor(outsideColor);
        canvas.drawCircle(centerX, centerY, buttonOutsideRadius, mPaint);
        //内圆（白色）
        //内圆背景色
        int insideColor = 0xFFFFFFFF;
        mPaint.setColor(insideColor);
        canvas.drawCircle(centerX, centerY, buttonInsideRadius, mPaint);

        //如果状态为录制状态，则绘制录制进度条
        if (state == STATE_RECORDING) {
            //进度条颜色
            int progressColor = 0xEE16AE16;
            mPaint.setColor(progressColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(strokeWidth);
            canvas.drawArc(rectF, -90, progress, false, mPaint);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (event.getPointerCount() > 1 || state != STATE_IDLE) {
                return true;
            }
            //记录Y值
            eventY = event.getY();
            //修改当前状态为点击按下
            state = STATE_PRESS;

            //判断按钮状态是否为可录制状态
            if ((button_state == BUTTON_STATE_ONLY_RECORDER || button_state == BUTTON_STATE_BOTH)) {
                //同时延长500启动长按后处理的逻辑Runnable
                postDelayed(longPressRunnable, 500);
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (captureListener != null && state == STATE_RECORDING && (button_state == BUTTON_STATE_ONLY_RECORDER || button_state == BUTTON_STATE_BOTH)) {
                //记录当前Y值与按下时候Y值的差值，调用缩放回调接口
                captureListener.recordZoom(eventY - event.getY());
            }
        } else if (action == MotionEvent.ACTION_UP) {
            //根据当前按钮的状态进行相应的处理
            handlerUnPressByState();
        }
        return true;
    }

    /**
     * 当手指松开按钮时候处理的逻辑
     */
    private void handlerUnPressByState() {
        //移除长按逻辑的Runnable
        removeCallbacks(longPressRunnable);
        //根据当前状态处理
        //当前是点击按下
        if (state == STATE_PRESS) {
            if (captureListener != null && (button_state == BUTTON_STATE_ONLY_CAPTURE || button_state == BUTTON_STATE_BOTH)) {
                startCaptureAnimation(buttonInsideRadius);
            } else {
                state = STATE_IDLE;
            }
            //当前是长按状态
        } else if (state == STATE_RECORDING) {
            //停止计时器
            timer.cancel();
            //录制结束
            recordEnd();
        }
    }

    /**
     * 录制结束
     */
    private void recordEnd() {
        if (captureListener != null) {
            if (recordedTime < minDuration) {
                //回调录制时间过短
                captureListener.recordShort(recordedTime);
            } else {
                //回调录制结束
                captureListener.recordEnd(recordedTime);
            }
        }
        resetRecordAnim();  //重制按钮状态
    }

    /**
     * 重制状态
     */
    private void resetRecordAnim() {
        state = STATE_BAN;
        //重制进度
        progress = 0;
        invalidate();
        //还原按钮初始状态动画
        startRecordAnimation(buttonOutsideRadius, buttonRadius, buttonInsideRadius, buttonRadius * 0.75f);
    }

    /**
     * 内圆动画
     *
     * @param insideStart 内圈开始
     */
    private void startCaptureAnimation(float insideStart) {
        ValueAnimator insideAnim = ValueAnimator.ofFloat(insideStart, insideStart * 0.75f, insideStart);
        insideAnim.addUpdateListener(animation -> {
            buttonInsideRadius = (float) animation.getAnimatedValue();
            invalidate();
        });
        insideAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //回调拍照接口
                captureListener.takePictures();
                state = STATE_BAN;
            }
        });
        insideAnim.setDuration(100);
        insideAnim.start();
    }

    /**
     * 内外圆动画
     *
     * @param outsideStart 外圈开始
     * @param outsideEnd   外圈结束
     * @param insideStart  内环开始
     * @param insideEnd    内圈结束
     */
    private void startRecordAnimation(float outsideStart, float outsideEnd, float insideStart, float insideEnd) {
        ValueAnimator outsideAnim = ValueAnimator.ofFloat(outsideStart, outsideEnd);
        ValueAnimator insideAnim = ValueAnimator.ofFloat(insideStart, insideEnd);
        //外圆动画监听
        outsideAnim.addUpdateListener(animation -> {
            buttonOutsideRadius = (float) animation.getAnimatedValue();
            invalidate();
        });
        //内圆动画监听
        insideAnim.addUpdateListener(animation -> {
            buttonInsideRadius = (float) animation.getAnimatedValue();
            invalidate();
        });
        AnimatorSet set = getAnimatorSet();
        set.playTogether(outsideAnim, insideAnim);
        set.setDuration(100);
        set.start();
    }

    @NonNull
    private AnimatorSet getAnimatorSet() {
        AnimatorSet set = new AnimatorSet();
        //当动画结束后启动录像Runnable并且回调录像开始接口
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //设置为录制状态
                if (state == STATE_LONG_PRESS) {
                    if (captureListener != null) {
                        captureListener.recordStart();
                    }
                    state = STATE_RECORDING;
                    timer.start();
                }
            }
        });
        return set;
    }


    /**
     * 更新进度条
     *
     * @param millisUntilFinished 时间
     */
    private void updateProgress(long millisUntilFinished) {
        recordedTime = (int) (duration - millisUntilFinished);
        progress = 360f - millisUntilFinished / (float) duration * 360f;
        invalidate();
    }

    /**
     * 录制视频计时器
     */
    private class RecordCountDownTimer extends CountDownTimer {
        RecordCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            updateProgress(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            updateProgress(0);
            recordEnd();
        }
    }

    /**
     * 长按线程
     */
    private class LongPressRunnable implements Runnable {
        @Override
        public void run() {
            state = STATE_LONG_PRESS;   //如果按下后经过500毫秒则会修改当前状态为长按状态
            //启动按钮动画，外圆变大，内圆缩小
            startRecordAnimation(buttonOutsideRadius, buttonOutsideRadius + outsideAddSize, buttonInsideRadius, buttonInsideRadius - insideReduceSize);
        }
    }

    /**
     * 设置最长录制时间
     *
     * @param duration 录制最长时间
     */
    public void setDuration(int duration) {
        this.duration = duration;
        timer = new RecordCountDownTimer(duration, duration / 360);    //录制定时器
    }

    /**
     * 设置最短录制时间，不建议使用因为短按位拍照，这不冲突
     *
     * @param duration 最短录像时间
     */
    public void setMinDuration(int duration) {
        this.minDuration = duration;
    }

    /**
     * 设置回调接口
     *
     * @param captureListener 回调
     */
    public void setCaptureListener(CaptureListener captureListener) {
        this.captureListener = captureListener;
    }

    /**
     * 设置按钮功能（拍照和录像）
     *
     * @param state 按钮状态
     */
    public void setButtonFeatures(int state) {
        this.button_state = state;
    }

    /**
     * 获取当前按钮支持状态
     *
     * @return 按钮状态
     */
    public int getButtonState() {
        return button_state;
    }

    /**
     * 是否空闲状态
     *
     * @return true为空闲
     */
    public boolean isIdle() {
        return state == STATE_IDLE;
    }

    /**
     * 设置状态
     */
    public void resetState() {
        state = STATE_IDLE;
    }
}