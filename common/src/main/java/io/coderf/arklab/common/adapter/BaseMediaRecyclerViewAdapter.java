package io.coderf.arklab.common.adapter;

import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.utils.common.DensityUtil;

import java.util.List;

/**
 * 图片、视频、媒体adapter适配器基类
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/3/5 9:32
 */
public abstract class BaseMediaRecyclerViewAdapter<T, VDB extends ViewDataBinding> extends BaseRecyclerViewAdapter<T, VDB> {

    /**
     * 最大上传数量
     */
    protected int defaultMaxCount = -1;
    /**
     * 背景颜色，默认为null也就是不添加任何北京
     */
    protected @ColorInt Integer bgColor;
    /**
     * 圆角大小，默认为8dp
     */
    protected float radius = 0;
    /**
     * 占位图图片
     */
    protected Drawable placeholderImage;
    /**
     * 错误时占位图片
     */
    protected Drawable errorImage;
    /**
     * 右上角占位按钮图片
     */
    protected Drawable clearImage;
    /**
     * 默认占位图图片
     */
    protected Drawable placeholderDefaultImage;
    /**
     * 默认错误时占位图片
     */
    protected Drawable errorDefaultImage;
    /**
     * 默认右上角占位按钮图片
     */
    protected Drawable clearDefaultImage;
    /**
     * 默认右上角占位按钮图片宽高
     */
    protected int clearImageWidth;
    /**
     * 默认右上角占位按钮图片宽高
     */
    protected int clearImageHeight;
    /**
     * 默认右上角占位按钮图片topMargin，这个理论上是个-值
     */
    protected int clearImageTopMargin;
    /**
     * 默认右上角占位按钮图片endMargin，这个理论上是个-值
     */
    protected int clearImageEndMargin;

    public BaseMediaRecyclerViewAdapter() {
        initArguments();
    }

    public BaseMediaRecyclerViewAdapter(List<T> list) {
        super(list);
        initArguments();
    }

    public BaseMediaRecyclerViewAdapter(int defaultMaxCount) {
        this.defaultMaxCount = defaultMaxCount;
        initArguments();
    }

    protected void initArguments() {
        if (Config.getInstance().getApplication() != null) {
            radius = DensityUtil.dp2px(Config.getInstance().getApplication(), 8f);
            clearImageWidth = DensityUtil.dp2px(Config.getInstance().getApplication(), 24f);
            clearImageHeight = DensityUtil.dp2px(Config.getInstance().getApplication(), 24f);
            clearImageTopMargin = DensityUtil.dp2px(Config.getInstance().getApplication(), -12f);
            clearImageEndMargin = DensityUtil.dp2px(Config.getInstance().getApplication(), -12f);
            clearImage = ContextCompat.getDrawable(Config.getInstance().getApplication(), R.drawable.ib_clear_image_selector);
            placeholderDefaultImage = ContextCompat.getDrawable(Config.getInstance().getApplication(), R.mipmap.ic_default_image);
            errorDefaultImage = ContextCompat.getDrawable(Config.getInstance().getApplication(), R.mipmap.ic_default_image);
        }
    }

    public int getDefaultMaxCount() {
        return defaultMaxCount;
    }

    public void setDefaultMaxCount(int defaultMaxCount) {
        this.defaultMaxCount = defaultMaxCount;
    }

    public Integer getBgColor() {
        return bgColor;
    }

    public void setBgColor(Integer bgColor) {
        this.bgColor = bgColor;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public Drawable getPlaceholderImage() {
        return placeholderImage;
    }

    public void setPlaceholderImage(Drawable placeholderImage) {
        this.placeholderImage = placeholderImage;
    }

    public Drawable getErrorImage() {
        return errorImage;
    }

    public void setErrorImage(Drawable errorImage) {
        this.errorImage = errorImage;
    }

    public Drawable getClearImage() {
        return clearImage;
    }

    public void setClearImage(Drawable clearImage) {
        this.clearImage = clearImage;
    }

    public int getClearImageWidth() {
        return clearImageWidth;
    }

    public void setClearImageWidth(int clearImageWidth) {
        this.clearImageWidth = clearImageWidth;
    }

    public int getClearImageHeight() {
        return clearImageHeight;
    }

    public void setClearImageHeight(int clearImageHeight) {
        this.clearImageHeight = clearImageHeight;
    }

    public float getClearImageTopMargin() {
        return clearImageTopMargin;
    }

    public void setClearImageTopMargin(int clearImageTopMargin) {
        this.clearImageTopMargin = clearImageTopMargin;
    }

    public Drawable getPlaceholderDefaultImage() {
        return placeholderDefaultImage;
    }

    public void setPlaceholderDefaultImage(Drawable placeholderDefaultImage) {
        this.placeholderDefaultImage = placeholderDefaultImage;
    }

    public Drawable getErrorDefaultImage() {
        return errorDefaultImage;
    }

    public void setErrorDefaultImage(Drawable errorDefaultImage) {
        this.errorDefaultImage = errorDefaultImage;
    }

    public Drawable getClearDefaultImage() {
        return clearDefaultImage;
    }

    public void setClearDefaultImage(Drawable clearDefaultImage) {
        this.clearDefaultImage = clearDefaultImage;
    }

    public int getClearImageEndMargin() {
        return clearImageEndMargin;
    }

    public void setClearImageEndMargin(int clearImageEndMargin) {
        this.clearImageEndMargin = clearImageEndMargin;
    }

    public ViewGroup.LayoutParams getClearLayoutParams(AppCompatImageView clearImage) {
        ConstraintLayout.LayoutParams clearLayoutParams = (ConstraintLayout.LayoutParams) clearImage.getLayoutParams();
        if (clearLayoutParams == null) {
            clearLayoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        clearLayoutParams.width = clearImageWidth;
        clearLayoutParams.height = clearImageHeight;
        clearLayoutParams.topMargin = clearImageTopMargin;
        clearLayoutParams.setMarginEnd(clearImageEndMargin);
        return clearLayoutParams;
    }

}

