package com.casic.otitan.common.widget.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatImageView;

import com.casic.otitan.common.listener.CaptureListener;
import com.casic.otitan.common.listener.TypeListener;


/**
 * created fz on 2024/10/22 19:56
 * describe：
 */
public class CaptureLayout extends FrameLayout {
    /**
     * 拍照按钮监听
     */
    private CaptureListener captureListener;
    /**
     * 拍照或录制后接结果按钮监听
     */
    private TypeListener typeListener;
    /**
     * 左边按钮监听
     */
    private OnClickListener leftClickListener;
    /**
     * 右边按钮监听
     */
    private OnClickListener rightClickListener;

    /**
     * 拍照按钮
     */
    private CaptureButton btnCapture;
    /**
     * 确认按钮
     */
    private TypeButton btnConfirm;
    /**
     * 取消按钮
     */
    private TypeButton btnCancel;
    /**
     * 左边自定义按钮
     */
    private AppCompatImageView ivCustomLeft;
    /**
     * 右边自定义按钮
     */
    private AppCompatImageView ivCustomRight;

    private final int layoutWidth;
    private final int layoutHeight;
    private final int buttonSize;
    private int iconLeft = 0;
    private int iconRight = 0;

    public CaptureLayout(Context context) {
        this(context, null);
    }

    public CaptureLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        DisplayMetrics appDisplayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutWidth = appDisplayMetrics.widthPixels;
        } else {
            layoutWidth = appDisplayMetrics.widthPixels / 2;
        }
        buttonSize = (int) (layoutWidth / 4.5f);
        layoutHeight = buttonSize + (buttonSize / 5) * 2 + 100;

        initView();
        initEvent();
    }

    public void setTypeListener(TypeListener typeListener) {
        this.typeListener = typeListener;
    }

    public void setCaptureListener(CaptureListener captureListener) {
        this.captureListener = captureListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(layoutWidth, layoutHeight);
    }

    public void initEvent() {
        //默认TypeButton为隐藏
        ivCustomRight.setVisibility(GONE);
        btnCancel.setVisibility(GONE);
        btnConfirm.setVisibility(GONE);
    }

    public void startTypeBtnAnimator() {
        //拍照录制结果后的动画
        if (this.iconLeft != 0) {
            ivCustomLeft.setVisibility(GONE);
        }
        if (this.iconRight != 0) {
            ivCustomRight.setVisibility(GONE);
        }
        btnCapture.setVisibility(GONE);
        btnCancel.setVisibility(VISIBLE);
        btnConfirm.setVisibility(VISIBLE);
        btnCancel.setClickable(false);
        btnConfirm.setClickable(false);
        ObjectAnimator animatorCancel = ObjectAnimator.ofFloat(btnCancel, "translationX", (float) layoutWidth / 4, 0);
        ObjectAnimator animatorConfirm = ObjectAnimator.ofFloat(btnConfirm, "translationX", (float) -layoutWidth / 4, 0);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorCancel, animatorConfirm);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                btnCancel.setClickable(true);
                btnConfirm.setClickable(true);
            }
        });
        set.setDuration(300);
        set.start();
    }

    public CaptureButton getBtnCapture() {
        return btnCapture;
    }

    public AppCompatImageView getIvCustomLeft() {
        return ivCustomLeft;
    }

    public AppCompatImageView getIvCustomRight() {
        return ivCustomRight;
    }

    public int getIconLeft() {
        return iconLeft;
    }

    public int getIconRight() {
        return iconRight;
    }

    public TypeButton getBtnCancel() {
        return btnCancel;
    }

    public TypeButton getBtnConfirm() {
        return btnConfirm;
    }

    private void initView() {
        setWillNotDraw(false);
        //拍照按钮
        btnCapture = new CaptureButton(getContext(), buttonSize);
        LayoutParams btnCaptureParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        btnCaptureParam.gravity = Gravity.CENTER;
        btnCapture.setLayoutParams(btnCaptureParam);
        btnCapture.setCaptureListener(new CaptureListener() {
            @Override
            public void takePictures() {
                if (captureListener != null) {
                    captureListener.takePictures();
                }
            }

            @Override
            public void recordShort(long time) {
                if (captureListener != null) {
                    captureListener.recordShort(time);
                }
            }

            @Override
            public void recordStart() {
                if (captureListener != null) {
                    captureListener.recordStart();
                }
            }

            @Override
            public void recordEnd(long time) {
                if (captureListener != null) {
                    captureListener.recordEnd(time);
                }
            }

            @Override
            public void recordZoom(float zoom) {
                if (captureListener != null) {
                    captureListener.recordZoom(zoom);
                }
            }

            @Override
            public void recordError() {
                if (captureListener != null) {
                    captureListener.recordError();
                }
            }
        });

        //取消按钮
        btnCancel = new TypeButton(getContext(), TypeButton.TYPE_CANCEL, buttonSize);
        final LayoutParams btnCancelParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        btnCancelParam.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        btnCancelParam.setMargins((layoutWidth / 4) - buttonSize / 2, 0, 0, 0);
        btnCancel.setLayoutParams(btnCancelParam);
        btnCancel.setOnClickListener(view -> {
            if (typeListener != null) {
                typeListener.cancel();
            }
        });

        //确认按钮
        btnConfirm = new TypeButton(getContext(), TypeButton.TYPE_CONFIRM, buttonSize);
        LayoutParams btnConfirmParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        btnConfirmParam.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        btnConfirmParam.setMargins(0, 0, (layoutWidth / 4) - buttonSize / 2, 0);
        btnConfirm.setLayoutParams(btnConfirmParam);
        btnConfirm.setOnClickListener(view -> {
            if (typeListener != null) {
                typeListener.confirm();
            }
        });

        LayoutParams btnReturnParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        btnReturnParam.gravity = Gravity.CENTER_VERTICAL;
        btnReturnParam.setMargins(layoutWidth / 6, 0, 0, 0);
        //左边自定义按钮
        ivCustomLeft = new AppCompatImageView(getContext());
        LayoutParams ivCustomParamLeft = new LayoutParams((int) (buttonSize / 2.5f), (int) (buttonSize / 2.5f));
        ivCustomParamLeft.gravity = Gravity.CENTER_VERTICAL;
        ivCustomParamLeft.setMargins(layoutWidth / 6, 0, 0, 0);
        ivCustomLeft.setLayoutParams(ivCustomParamLeft);
        ivCustomLeft.setOnClickListener(v -> {
            if (leftClickListener == null) {
                return;
            }
            leftClickListener.onClick(v);
        });

        //右边自定义按钮
        ivCustomRight = new AppCompatImageView(getContext());
        LayoutParams ivCustomParamRight = new LayoutParams((int) (buttonSize / 2.5f), (int) (buttonSize / 2.5f));
        ivCustomParamRight.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        ivCustomParamRight.setMargins(0, 0, layoutWidth / 6, 0);
        ivCustomRight.setLayoutParams(ivCustomParamRight);
        ivCustomRight.setOnClickListener(v -> {
            if (rightClickListener == null) {
                return;
            }
            rightClickListener.onClick(v);
        });

        this.addView(btnCapture);
        this.addView(btnCancel);
        this.addView(btnConfirm);
        this.addView(ivCustomLeft);
        this.addView(ivCustomRight);

    }

    public void resetCaptureLayout() {
        btnCapture.resetState();
        btnCancel.setVisibility(GONE);
        btnConfirm.setVisibility(GONE);
        btnCapture.setVisibility(VISIBLE);
        if (this.iconLeft != 0) {
            ivCustomLeft.setVisibility(VISIBLE);
        }
        if (this.iconRight != 0) {
            ivCustomRight.setVisibility(VISIBLE);
        }
    }


    public void setButtonFeatures(int state) {
        btnCapture.setButtonFeatures(state);
    }

    public void setIconSrc(int iconLeft, int iconRight) {
        this.iconLeft = iconLeft;
        this.iconRight = iconRight;
        if (this.iconLeft != 0) {
            ivCustomLeft.setImageResource(iconLeft);
            ivCustomLeft.setVisibility(VISIBLE);
        } else {
            ivCustomLeft.setVisibility(GONE);
        }
        if (this.iconRight != 0) {
            ivCustomRight.setImageResource(iconRight);
            ivCustomRight.setVisibility(VISIBLE);
        } else {
            ivCustomRight.setVisibility(GONE);
        }
    }

    public void setLeftClickListener(OnClickListener leftClickListener) {
        this.leftClickListener = leftClickListener;
    }

    public void setRightClickListener(OnClickListener rightClickListener) {
        this.rightClickListener = rightClickListener;
    }
}
