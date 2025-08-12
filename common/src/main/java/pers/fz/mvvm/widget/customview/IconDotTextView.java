package pers.fz.mvvm.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.common.DensityUtil;

/**
 * Created by fz on 2017/4/24.
 * 底部按钮textView文本
 */
public class IconDotTextView extends ConstraintLayout {
    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mDot;
    private int imageWidth;
    private int imageHeight;
    private Drawable drawableImage;
    private String label;

    public final String DEFAULT_MORE_TEXT = "99+";

    public IconDotTextView(Context context) {
        super(context);
        init(null);
    }

    public IconDotTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context.obtainStyledAttributes(attrs, R.styleable.IconDotTextView));
    }

    public IconDotTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context.obtainStyledAttributes(attrs, R.styleable.IconDotTextView,defStyleAttr,0));
    }

    public IconDotTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context.obtainStyledAttributes(attrs, R.styleable.IconDotTextView,defStyleAttr,defStyleRes));
    }

    private void init(TypedArray a) {
        int textColor;
        float textSize;
        if (a != null) {
            textSize = a.getDimension(R.styleable.IconDotTextView_textSize, DensityUtil.dp2px(getContext(), 12f));
            textColor = a.getColor(R.styleable.IconDotTextView_textColor, ContextCompat.getColor(getContext(), R.color.autoColor));
            imageWidth = a.getDimensionPixelSize(R.styleable.IconDotTextView_imageWidth, 0);
            imageHeight = a.getDimensionPixelSize(R.styleable.IconDotTextView_imageHeight, 0);
            drawableImage = a.getDrawable(R.styleable.IconDotTextView_imageSrc);
            label = a.getString(R.styleable.IconDotTextView_label);
            a.recycle();
        } else {
            textColor = ContextCompat.getColor(getContext(), R.color.autoColor);
            textSize = DensityUtil.sp2px(getContext(), 12);
        }
        LayoutInflater.from(getContext()).inflate(R.layout.custom_textview, this, true);
        setLayoutParams(new ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mIconView = findViewById(R.id.nav_iv_icon);
        mTitleView = findViewById(R.id.nav_tv_title);
        mTitleView.setTextColor(textColor);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        if (drawableImage != null) {
            mIconView.setImageDrawable(drawableImage);
        }
        if (!TextUtils.isEmpty(label)) {
            mTitleView.setText(label);
        }
        if (imageHeight == 0 || imageWidth == 0) {
            mIconView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
        } else {
            mIconView.setLayoutParams(new ConstraintLayout.LayoutParams(imageWidth, imageHeight));
        }
        mDot = findViewById(R.id.nav_tv_dot);
    }

    public void showRedDot(int count) {
        mDot.setVisibility(count > 0 ? VISIBLE : GONE);
        if (count > 99) {
            mDot.setText(DEFAULT_MORE_TEXT);
        } else {
            mDot.setText(String.valueOf(count));
        }
    }

    public void setGlideImage(String src){
        Glide.with(getContext())
                .load(src)
                .apply(new RequestOptions().placeholder(R.mipmap.ic_default_image).error(R.mipmap.ic_default_image))
                .into(mIconView);
    }

    public void setDrawableImage(Drawable drawableImage) {
        this.drawableImage = drawableImage;
        mIconView.setImageDrawable(drawableImage);
    }

    public void setLabel(String label) {
        this.label = label;
        mTitleView.setText(label);
    }

    public void init(@DrawableRes int resId, @StringRes int strId, String tag) {
        mIconView.setImageResource(resId);
        mTitleView.setText(strId);
        mDot.setTag(getResources().getString(strId));
        mTitleView.setTag(tag);
    }

    public String getText() {
        return mDot.getTag().toString();
    }

    public String getTextString() {
        return mTitleView.getTag().toString();
    }

}
