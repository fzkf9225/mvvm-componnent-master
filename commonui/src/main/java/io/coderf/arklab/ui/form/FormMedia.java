package io.coderf.arklab.ui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.api.FileApiService;
import io.coderf.arklab.ui.api.MediaUploadConfig;
import io.coderf.arklab.ui.enums.LabelTextStyleEnum;

import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.widget.recyclerview.FullyGridLayoutManager;
import io.coderf.arklab.common.widget.recyclerview.GridSpacingItemDecoration;

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
    protected boolean required;
    /**
     * 是否展示底部边框
     */
    protected boolean bottomBorder;
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
     * 是否隐藏顶部layout，也就是顶部的标签栏和数量栏，默认为false，也就是展示
     */
    protected boolean hideTopLabelLayout;
    /**
     * 左侧文字的图标控件
     */
    protected AppCompatImageView ivLabelIcon;
    /**
     * 是否展示label左侧图标，默认false
     */
    protected boolean showLabelIcon;
    /**
     * 左侧文字的图标
     */
    protected Drawable labelIcon;
    /**
     * 左侧文字的图标宽高
     */
    protected float labelIconWidth;
    /**
     *左侧文字的图标宽高
     */
    protected float labelIconHeight;
    /**
     * 左侧文字的图标左侧margin
     */
    protected float labelIconStartMargin;
    /**
     * 左侧文字的图标右侧margin
     */
    protected float labelIconEndMargin;
    /**
     * required*号控件的左侧margin
     */
    protected float requiredStartMargin;
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
    protected int columnCount;
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
    protected float radius;
    /**
     * 文件类型
     */
    protected String[] fileType = null;
    /**
     * 是否显示协议dialog
     */
    protected boolean protocolDialog;
    /**
     * 是否调用uri的持久化权限takeUriPermission，默认为true
     */
    protected boolean requireUriPermission;
    /**
     * label文字样式，默认不加粗
     */
    protected int labelTextStyle;
    /**
     * 文件上传服务
     */
    protected FileApiService fileApiService = MediaUploadConfig.getInstance().getFileApiService();
    /**
     * 数量标签控件
     */
    protected String uploadUrl = MediaUploadConfig.getInstance().getUploadUrl();
    /**
     * 是否自动上传
     */
    protected boolean autoUpload = false;
    /**
     * 是否自动上传成功后的回调
     */
    protected Handler handler = null;
    /**
     * 回调
     */
    protected BaseView baseView;
    /**
     * 右上角占位按钮图片
     */
    protected Drawable clearImage;
    /**
     * 默认右上角占位按钮图片宽高
     */
    protected float clearImageWidth;
    /**
     * 默认右上角占位按钮图片宽高
     */
    protected float clearImageHeight;
    /**
     * 默认右上角占位按钮图片topMargin，这个理论上是个-值
     */
    protected float clearImageTopMargin;
    /**
     * 默认右上角占位按钮图片endMargin，这个理论上是个-值
     */
    protected float clearImageEndMargin;

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

            columnMargin = typedArray.getDimension(R.styleable.FormUI_columnMargin, DensityUtil.dp2px(getContext(), 16f));

            labelTextStyle = typedArray.getInt(R.styleable.FormUI_labelTextStyle, LabelTextStyleEnum.NORMAL.value);

            borderBottomColor = typedArray.getColor(R.styleable.FormUI_borderBottomColor, ContextCompat.getColor(getContext(), R.color.line));

            placeholderImage = typedArray.getDrawable(R.styleable.FormUI_placeholderImage);
            if (placeholderImage == null) {
                placeholderImage = ContextCompat.getDrawable(getContext(), io.coderf.arklab.common.R.mipmap.ic_default_image);
            }
            errorImage = typedArray.getDrawable(R.styleable.FormUI_errorImage);
            if (errorImage == null) {
                errorImage = ContextCompat.getDrawable(getContext(), io.coderf.arklab.common.R.mipmap.ic_default_image);
            }
            autoUpload = typedArray.getBoolean(R.styleable.FormUI_autoUpload, false);
            String fileTypeStr = typedArray.getString(R.styleable.FormUI_fileType);
            if (!TextUtils.isEmpty(fileTypeStr)) {
                fileType = fileTypeStr.split(",");
            } else {
                fileType = defaultFileType();
            }
            // 右上角占位按钮图片
            clearImage = typedArray.getDrawable(R.styleable.FormUI_clearImage);
            if (clearImage == null) {
                clearImage = ContextCompat.getDrawable(getContext(), io.coderf.arklab.common.R.drawable.ib_clear_image_selector);
            }
            // 默认右上角占位按钮图片宽度
            clearImageWidth = typedArray.getDimension(R.styleable.FormUI_clearImageWidth, DensityUtil.dp2px(getContext(), 24f));
            // 默认右上角占位按钮图片高度
            clearImageHeight = typedArray.getDimension(R.styleable.FormUI_clearImageHeight, DensityUtil.dp2px(getContext(), 24f));
            // 默认右上角占位按钮图片topMargin
            clearImageTopMargin = typedArray.getDimension(R.styleable.FormUI_clearImageTopMargin, DensityUtil.dp2px(getContext(), -8f));
            // 默认右上角占位按钮图片endMargin
            clearImageEndMargin = typedArray.getDimension(R.styleable.FormUI_clearImageEndMargin, DensityUtil.dp2px(getContext(), -8f));

            showLabelIcon = typedArray.getBoolean(R.styleable.FormUI_showLabelIcon, false);
            hideTopLabelLayout = typedArray.getBoolean(R.styleable.FormUI_hideTopLabelLayout, false);
            labelIcon = typedArray.getDrawable(R.styleable.FormUI_labelIcon);
            labelIconWidth = typedArray.getDimension(R.styleable.FormUI_labelIconWidth, 0);
            labelIconHeight = typedArray.getDimension(R.styleable.FormUI_labelIconHeight, 0);
            labelIconStartMargin = typedArray.getDimension(R.styleable.FormUI_labelIconStartMargin, 0);
            labelIconEndMargin = typedArray.getDimension(R.styleable.FormUI_labelIconEndMargin, 0);
            requiredStartMargin = typedArray.getDimension(R.styleable.FormUI_requiredStartMargin, DensityUtil.dp2px(getContext(), 4f));
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
            columnMargin = DensityUtil.dp2px(getContext(), 16f);
            placeholderImage = ContextCompat.getDrawable(getContext(), io.coderf.arklab.common.R.mipmap.ic_default_image);
            errorImage = ContextCompat.getDrawable(getContext(), io.coderf.arklab.common.R.mipmap.ic_default_image);
            autoUpload = false;
            // 右上角占位按钮图片
            clearImage = ContextCompat.getDrawable(getContext(), io.coderf.arklab.common.R.drawable.ib_clear_image_selector);
            // 默认右上角占位按钮图片宽度
            clearImageWidth = DensityUtil.dp2px(getContext(), 24f);
            // 默认右上角占位按钮图片高度
            clearImageHeight = DensityUtil.dp2px(getContext(), 24f);
            // 默认右上角占位按钮图片topMargin
            clearImageTopMargin = DensityUtil.dp2px(getContext(), -8f);
            // 默认右上角占位按钮图片endMargin
            clearImageEndMargin = DensityUtil.dp2px(getContext(), -8f);
            hideTopLabelLayout = false;
            showLabelIcon = false;
            labelIconWidth = DensityUtil.dp2px(getContext(), 0);
            labelIconHeight = DensityUtil.dp2px(getContext(), 0);
            labelIconStartMargin = 0;
            labelIconEndMargin = DensityUtil.dp2px(getContext(), 8);
            requiredStartMargin = DensityUtil.dp2px(getContext(), 4f);
        }
    }

    public abstract String[] defaultFileType();

    /**
     * 设置视频自动上传接口地址
     * @param uploadUrl 上传接口地址，相对地址
     */
    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    /**
     * 回调
     * @param baseView 回调
     */
    public void setBaseView(BaseView baseView) {
        this.baseView = baseView;
    }

    /**
     * 设置文件上传接口服务
     * @param fileApiService 代理
     */
    public void setFileApiService(FileApiService fileApiService) {
        this.fileApiService = fileApiService;
    }

    protected void init() {
        createLabelIcon();
        createLabel();
        createRequired();
        createRecyclerView();
        createBottomLine();
        layoutLabelIcon();
        layoutLabel();
        layoutRequired();
        layoutRecyclerView();
        applyShowTopLayout();
    }

    public void applyShowTopLayout() {
        if (hideTopLabelLayout) {
            if (tvLabel != null) {
                tvLabel.setVisibility(GONE);
            }
            if (ivLabelIcon != null) {
                ivLabelIcon.setVisibility(GONE);
            }
            if (tvRequired != null) {
                tvRequired.setVisibility(GONE);
            }
        } else {
            if (tvLabel != null) {
                tvLabel.setVisibility(VISIBLE);
            }
            if (ivLabelIcon != null) {
                ivLabelIcon.setVisibility(VISIBLE);
            }
            if (tvRequired != null) {
                tvRequired.setVisibility(VISIBLE);
            }
        }
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

    public AppCompatImageView getIvLabelIcon() {
        return ivLabelIcon;
    }

    public void createLabelIcon() {
        ivLabelIcon = new AppCompatImageView(getContext());
        ivLabelIcon.setId(View.generateViewId());
        LayoutParams params = new LayoutParams(
                labelIconWidth <= 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : (int) labelIconWidth,
                labelIconHeight <= 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : (int) labelIconHeight
        );
        ivLabelIcon.setImageDrawable(labelIcon);
        params.setMarginStart((int) labelIconStartMargin);
        params.setMarginEnd((int) labelIconEndMargin);//这样设置其实没用，因为右侧没有宽度限制
        addView(ivLabelIcon, params);
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
        params.setMarginEnd((int) labelEndMargin);//这样设置其实没用，因为右侧没有宽度限制
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
        params.setMarginStart((int) requiredStartMargin);

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

    public void layoutLabelIcon() {
        if (!showLabelIcon || labelIcon == null) {
            return;
        }
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
        constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
        constraintSet.applyTo(this);
    }

    public void layoutLabel() {
        if (!showLabelIcon || labelIcon == null) {
            //没有左侧图标，所以直接直连父级
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.applyTo(this);
            LayoutParams params = (LayoutParams) tvLabel.getLayoutParams();
            params.topMargin = (int) defaultTextMargin;
        } else {
            //有左侧图标，所以左侧是图标
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ivLabelIcon.getId(), ConstraintSet.END);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.applyTo(this);
            LayoutParams params = (LayoutParams) tvLabel.getLayoutParams();
            params.topMargin = (int) defaultTextMargin;
        }
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