package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.enums.LabelTextStyleEnum;

import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.wight.recyclerview.FullyGridLayoutManager;
import pers.fz.mvvm.wight.recyclerview.GridSpacingItemDecoration;

public abstract class FormMedia extends ConstraintLayout {
    public static final String TAG = "FormUi";
    /**
     * 主要用于适配器中图片item、视频item等背景颜色
     */
    protected int bgColor;
    /**
     * label文字内容
     */
    protected String labelString;
    /**
     * 是否必填
     */
    protected boolean required = false;
    /**
     * 是否展示底部边框
     */
    protected boolean bottomBorder = true;
    /**
     * label文字颜色
     */
    protected int labelTextColor;
    /**
     * label文字大小
     */
    protected float formLabelTextSize;
    /**
     * 是否必填文字大小
     */
    protected float formRequiredSize;
    /**
     * label文字控件
     */
    protected AppCompatTextView tvLabel;
    /**
     * 底部边框控件
     */
    protected View vBorderBottom;
    /**
     * 必填*号控件
     */
    protected AppCompatTextView tvRequired;
    /**
     * 图片、视频的列表
     */
    protected RecyclerView mediaRecyclerView;
    /**
     * 底部边框距离左侧margin
     */
    protected float borderBottomStartMargin;
    /**
     * 底部边框距离右侧margin
     */
    protected float borderBottomEndMargin;
    /**
     * label距离左侧margin，默认为16dp
     */
    protected float labelStartMargin;
    /**
     * label距离右侧margin，默认为0
     */
    protected float labelEndMargin;
    /**
     * 正文（这里是列表）距离左侧margin，默认为16dp
     */
    protected float textStartMargin;
    /**
     * 正文（这里是列表）距离右侧margin，默认为16dp
     */
    protected float textEndMargin;
    /**
     * 正文内容，列表上下的默认边距，防止与边界挤在一起，默认为12dp
     */
    protected float defaultTextMargin;
    /**
     * 网格RecyclerView的时候列数
     */
    protected int columnCount = 4;
    /**
     * 列间距
     */
    protected float columnMargin;
    /**
     * 底部边框颜色
     */
    protected int borderBottomColor;
    /**
     * 默认占位图
     */
    protected Drawable placeholderImage;
    /**
     * 加载错误时的占位图
     */
    protected Drawable errorImage;
    /**
     * 主要是适配器item的圆角
     */
    protected float radius = 8;
    /**
     * 文件类型
     */
    protected String[] fileType = null;
    /**
     * 是否显示协议dialog
     */
    protected boolean protocolDialog = true;
    /**
     * 是否调用uri的持久化权限takeUriPermission，默认为true
     */
    protected boolean requireUriPermission = true;
    /**
     * label文字样式，默认不加粗
     */
    protected int labelTextStyle = LabelTextStyleEnum.NORMAL.value;

    public FormMedia(@NonNull Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public FormMedia(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FormMedia(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    protected void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);

            bgColor = typedArray.getColor(R.styleable.FormUI_bgColor, 0xFFF1F3F2);
            labelString = typedArray.getString(R.styleable.FormUI_label);
            required = typedArray.getBoolean(R.styleable.FormUI_required, false);
            requireUriPermission = typedArray.getBoolean(R.styleable.FormUI_requireUriPermission, true);
            protocolDialog = typedArray.getBoolean(R.styleable.FormUI_protocolDialog, true);
            labelTextColor = typedArray.getColor(R.styleable.FormUI_labelTextColor, ContextCompat.getColor(getContext(), R.color.auto_color));
            bottomBorder = typedArray.getBoolean(R.styleable.FormUI_bottomBorder, true);
            formLabelTextSize = typedArray.getDimension(R.styleable.FormUI_formLabelTextSize, DensityUtil.sp2px(getContext(), 14));
            formRequiredSize = typedArray.getDimension(R.styleable.FormUI_formRequiredSize, DensityUtil.sp2px(getContext(), 14));

            radius = typedArray.getDimension(R.styleable.FormUI_mediaItemRadius, DensityUtil.dp2px(getContext(), 8));

            columnCount = typedArray.getInt(R.styleable.FormUI_columnCount, 4);
            borderBottomStartMargin = typedArray.getDimension(R.styleable.FormUI_borderBottomStartMargin, DensityUtil.dp2px(getContext(), 16f));
            borderBottomEndMargin = typedArray.getDimension(R.styleable.FormUI_borderBottomEndMargin, 0);

            labelStartMargin = typedArray.getDimension(R.styleable.FormUI_labelStartMargin, DensityUtil.dp2px(getContext(), 16f));
            labelEndMargin = typedArray.getDimension(R.styleable.FormUI_labelEndMargin, 0);

            textStartMargin = typedArray.getDimension(R.styleable.FormUI_textStartMargin, DensityUtil.dp2px(getContext(), 12f));
            textEndMargin = typedArray.getDimension(R.styleable.FormUI_textEndMargin, DensityUtil.dp2px(getContext(), 16f));

            defaultTextMargin = typedArray.getDimension(R.styleable.FormUI_defaultTextMargin, DensityUtil.dp2px(getContext(), 12f));

            columnMargin = typedArray.getDimension(R.styleable.FormUI_columnMargin, DensityUtil.dp2px(getContext(), 8f));

            labelTextStyle = typedArray.getInt(R.styleable.FormUI_labelTextStyle, LabelTextStyleEnum.NORMAL.value);

            borderBottomColor = typedArray.getColor(R.styleable.FormUI_borderBottomColor, ContextCompat.getColor(getContext(), R.color.line));

            placeholderImage = typedArray.getDrawable(R.styleable.FormUI_placeholderImage);
            if (placeholderImage == null) {
                placeholderImage = ContextCompat.getDrawable(getContext(), pers.fz.mvvm.R.mipmap.ic_default_image);
            }
            errorImage = typedArray.getDrawable(R.styleable.FormUI_errorImage);
            if (errorImage == null) {
                errorImage = ContextCompat.getDrawable(getContext(), pers.fz.mvvm.R.mipmap.ic_default_image);
            }

            String fileTypeStr = typedArray.getString(R.styleable.FormUI_fileType);
            if (!TextUtils.isEmpty(fileTypeStr)) {
                fileType = fileTypeStr.split(",");
            } else {
                fileType = defaultFileType();
            }
            typedArray.recycle();
        } else {
            bgColor = 0xFFF1F3F2;
            radius = DensityUtil.dp2px(getContext(), 8);
            fileType = defaultFileType();
            protocolDialog = true;
            requireUriPermission = true;
            labelTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
            formLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            formRequiredSize = DensityUtil.sp2px(getContext(), 14);

            borderBottomStartMargin = DensityUtil.dp2px(getContext(), 16f);
            borderBottomEndMargin = 0;
            labelStartMargin = DensityUtil.dp2px(getContext(), 16f);
            labelEndMargin = 0;
            textStartMargin = DensityUtil.dp2px(getContext(), 12f);
            textEndMargin = DensityUtil.dp2px(getContext(), 16f);
            defaultTextMargin = DensityUtil.dp2px(getContext(), 12f);
            labelTextStyle = LabelTextStyleEnum.NORMAL.value;
            columnCount = 4;
            borderBottomColor = ContextCompat.getColor(getContext(), R.color.line);
            columnMargin = DensityUtil.dp2px(getContext(), 8f);
            placeholderImage = ContextCompat.getDrawable(getContext(), pers.fz.mvvm.R.mipmap.ic_default_image);
            errorImage = ContextCompat.getDrawable(getContext(), pers.fz.mvvm.R.mipmap.ic_default_image);
        }
    }

    public abstract String[] defaultFileType();

    protected void init() {
        createLabel();
        createRequired();
        createRecyclerView();
        createBottomLine();
        layoutLabel();
        layoutRequired();
        layoutRecyclerView();
    }


    public AppCompatTextView getTvLabel() {
        return tvLabel;
    }

    public RecyclerView getMediaRecyclerView() {
        return mediaRecyclerView;
    }

    public AppCompatTextView getTvRequired() {
        return tvRequired;
    }

    public View getVBorderBottom() {
        return vBorderBottom;
    }

    public void createLabel() {
        tvLabel = new AppCompatTextView(getContext());
        tvLabel.setId(View.generateViewId());
        tvLabel.setLines(1);
        tvLabel.setTextColor(labelTextColor);
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, formLabelTextSize);
        tvLabel.setText(labelString);
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMarginStart((int) labelStartMargin);
        params.setMarginEnd((int) labelEndMargin);
        if (LabelTextStyleEnum.BOLD.value == labelTextStyle) {
            // 设置为加粗
            tvLabel.setTypeface(tvLabel.getTypeface(), Typeface.BOLD);
        } else {
            // 取消加粗（恢复正常）
            tvLabel.setTypeface(tvLabel.getTypeface(), Typeface.NORMAL);
        }
        addView(tvLabel, params);
    }

    public void createRequired() {
        tvRequired = new AppCompatTextView(getContext());
        tvRequired.setId(View.generateViewId());
        tvRequired.setLines(1);
        tvRequired.setText("*");
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        tvRequired.setGravity(Gravity.CENTER);
        tvRequired.setTextColor(ContextCompat.getColor(getContext(), R.color.theme_red));
        tvRequired.setTextSize(TypedValue.COMPLEX_UNIT_PX, formRequiredSize);

        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMarginStart(DensityUtil.dp2px(getContext(), 4f));

        addView(tvRequired, params);
    }

    public void createRecyclerView() {
        mediaRecyclerView = new RecyclerView(getContext());
        mediaRecyclerView.setId(View.generateViewId());
        mediaRecyclerView.addItemDecoration(new GridSpacingItemDecoration((int) columnMargin, 0x00000000));
        mediaRecyclerView.setLayoutManager(new FullyGridLayoutManager(getContext(), columnCount) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
        params.setMarginStart((int) textEndMargin);
        params.setMarginEnd((int) textEndMargin);
        //如果不是当行显示的话
        params.topMargin = (int) defaultTextMargin;
        params.bottomMargin = (int) defaultTextMargin;
        addView(mediaRecyclerView, params);
    }

    public void createBottomLine() {
        if (!bottomBorder) {
            //不展示底部边框的情况下
            return;
        }
        vBorderBottom = new View(getContext());
        vBorderBottom.setId(View.generateViewId());
        // 设置布局参数
        LayoutParams params = new LayoutParams(
                0, DensityUtil.dp2px(getContext(), 1f));
        params.setMarginStart((int) borderBottomStartMargin);
        params.setMarginEnd((int) borderBottomEndMargin);
        vBorderBottom.setBackgroundColor(borderBottomColor);
        addView(vBorderBottom, params);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(vBorderBottom.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.connect(vBorderBottom.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(vBorderBottom.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.applyTo(this);
    }

    public void layoutLabel() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.applyTo(this);
        LayoutParams params = (LayoutParams) tvLabel.getLayoutParams();
        params.topMargin = (int) defaultTextMargin;
    }

    public void layoutRequired() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(tvRequired.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
        constraintSet.connect(tvRequired.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(tvRequired.getId(), ConstraintSet.START, tvLabel.getId(), ConstraintSet.END);
        constraintSet.applyTo(this);
    }

    public void layoutRecyclerView() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(mediaRecyclerView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(mediaRecyclerView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.connect(mediaRecyclerView.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(mediaRecyclerView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.applyTo(this);
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
    }

}