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
import androidx.annotation.StyleRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.core.request.RequestUi;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.widget.customview.CornerConstraintLayout;
import io.coderf.arklab.common.widget.recyclerview.FullyGridLayoutManager;
import io.coderf.arklab.common.widget.recyclerview.GridSpacingItemDecoration;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.api.FileApiService;
import io.coderf.arklab.ui.api.FormUiConfig;
import io.coderf.arklab.ui.api.MediaUploadConfig;
import io.coderf.arklab.ui.enums.LabelTextStyleEnum;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;

/**
 * FormMedia 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/17 formStyle / dimens / FormUiConfig 与 FormConstraintLayout 对齐
 */
public abstract class FormMedia extends CornerConstraintLayout {
    public static final String TAG = "FormUi";
    /**
     * 主要用于适配器中图片item、视频item等背景颜色
     */
    protected int itemBgColor;
    /**
     * label文字内容
     */
    protected String labelString;
    /**
     * 文件自定义保存的子目录名
     */
    protected String saveSubPath;
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
     * 必填标记颜色，默认 ThemeAttrs.error
     */
    protected int requiredTextColor;
    /**
     * 必填标记文案，默认 *
     */
    protected String requiredText = "*";
    /**
     * 底部边框高度，默认 1dp
     */
    protected float bottomBorderHeight;
    /**
     * 文本大小
     */
    protected float formTextSize;
    /**
     * 系统相机拍照完成后是否写入姿态/GPS 等到 EXIF（失败则跳过，不影响回调）
     */
    protected boolean writeCaptureExifMetadata;
    /**
     * label文字控件
     */
    protected MaterialTextView tvLabel;
    /**
     * 是否隐藏顶部layout，也就是顶部的标签栏和数量栏，默认为false，也就是展示
     */
    protected boolean hideTopLabelLayout;
    /**
     * 左侧文字的图标控件
     */
    protected ShapeableImageView ivLabelIcon;
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
     * 左侧文字的图标宽高
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
    protected MaterialTextView tvRequired;
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
     * 请求 UI（上传错误 Toast 等）
     */
    @Nullable
    protected RequestUi requestUi;
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

    /**
     * 统一读取 FormUI：控件 XML > 主题 formStyle > Widget.App.Form > dimens / ThemeAttrs。
     */
    protected TypedArray obtainFormUi(@Nullable AttributeSet attrs) {
        return getContext().obtainStyledAttributes(
                attrs, R.styleable.FormUI, R.attr.formStyle, R.style.Widget_App_Form);
    }

    protected void initAttr(@Nullable AttributeSet attrs) {
        TypedArray typedArray = obtainFormUi(attrs);
        try {
            applyFormUiTypedArray(typedArray);
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * 从 TypedArray 灌入字段。供 initAttr / setFormStyleOverlay 复用。
     */
    protected void applyFormUiTypedArray(@NonNull TypedArray typedArray) {
        Context ctx = getContext();
        float defLabelSize = ctx.getResources().getDimension(R.dimen.form_label_text_size);
        float defTextSize = ctx.getResources().getDimension(R.dimen.form_text_size);
        float defRequiredSize = ctx.getResources().getDimension(R.dimen.form_required_size);

        itemBgColor = typedArray.getColor(R.styleable.FormUI_itemBgColor, 0xFFF1F3F2);
        if (typedArray.hasValue(R.styleable.FormUI_label)) {
            labelString = typedArray.getString(R.styleable.FormUI_label);
        }
        if (typedArray.hasValue(R.styleable.FormUI_saveSubPath)) {
            saveSubPath = typedArray.getString(R.styleable.FormUI_saveSubPath);
        }
        required = typedArray.getBoolean(R.styleable.FormUI_required, false);
        writeCaptureExifMetadata = typedArray.getBoolean(R.styleable.FormUI_writeCaptureExifMetadata, true);
        requireUriPermission = typedArray.getBoolean(R.styleable.FormUI_requireUriPermission, true);
        protocolDialog = typedArray.getBoolean(R.styleable.FormUI_protocolDialog, true);
        labelTextColor = typedArray.getColor(R.styleable.FormUI_labelTextColor, ThemeAttrs.onSurface(ctx));
        bottomBorder = typedArray.getBoolean(R.styleable.FormUI_bottomBorder, true);
        formLabelTextSize = typedArray.getDimension(R.styleable.FormUI_formLabelTextSize, defLabelSize);
        formRequiredSize = typedArray.getDimension(R.styleable.FormUI_formRequiredSize, defRequiredSize);
        formTextSize = typedArray.getDimension(R.styleable.FormUI_formTextSize, defTextSize);
        requiredTextColor = typedArray.getColor(R.styleable.FormUI_requiredTextColor, ThemeAttrs.error(ctx));
        if (typedArray.hasValue(R.styleable.FormUI_requiredText)) {
            String rt = typedArray.getString(R.styleable.FormUI_requiredText);
            if (rt != null) {
                requiredText = rt;
            }
        }

        radius = typedArray.getDimension(R.styleable.FormUI_mediaItemRadius, DensityUtil.dp2px(ctx, 4));
        columnCount = typedArray.getInt(R.styleable.FormUI_columnCount, 4);
        columnMargin = typedArray.getDimension(R.styleable.FormUI_columnMargin, DensityUtil.dp2px(ctx, 16f));

        borderBottomStartMargin = typedArray.getDimension(R.styleable.FormUI_borderBottomStartMargin,
                ctx.getResources().getDimension(R.dimen.form_border_bottom_start_margin));
        borderBottomEndMargin = typedArray.getDimension(R.styleable.FormUI_borderBottomEndMargin,
                ctx.getResources().getDimension(R.dimen.form_border_bottom_end_margin));
        bottomBorderHeight = typedArray.getDimension(R.styleable.FormUI_bottomBorderHeight,
                ctx.getResources().getDimension(R.dimen.form_bottom_border_height));
        borderBottomColor = typedArray.getColor(R.styleable.FormUI_borderBottomColor, ThemeAttrs.outlineVariant(ctx));

        labelStartMargin = typedArray.getDimension(R.styleable.FormUI_labelStartMargin,
                ctx.getResources().getDimension(R.dimen.form_label_start_margin));
        labelEndMargin = typedArray.getDimension(R.styleable.FormUI_labelEndMargin,
                ctx.getResources().getDimension(R.dimen.form_label_end_margin));
        textStartMargin = typedArray.getDimension(R.styleable.FormUI_textStartMargin,
                ctx.getResources().getDimension(R.dimen.form_text_start_margin));
        textEndMargin = typedArray.getDimension(R.styleable.FormUI_textEndMargin,
                ctx.getResources().getDimension(R.dimen.form_text_end_margin));
        defaultTextMargin = typedArray.getDimension(R.styleable.FormUI_defaultTextMargin,
                ctx.getResources().getDimension(R.dimen.form_default_text_margin));
        requiredStartMargin = typedArray.getDimension(R.styleable.FormUI_requiredStartMargin,
                ctx.getResources().getDimension(R.dimen.form_required_start_margin));
        labelTextStyle = typedArray.getInt(R.styleable.FormUI_labelTextStyle, LabelTextStyleEnum.NORMAL.value);

        if (typedArray.hasValue(R.styleable.FormUI_placeholderImage)) {
            placeholderImage = typedArray.getDrawable(R.styleable.FormUI_placeholderImage);
        }
        if (placeholderImage == null) {
            placeholderImage = Config.getInstance().getDefaultPlaceholderDrawable(ctx);
        }
        if (typedArray.hasValue(R.styleable.FormUI_errorImage)) {
            errorImage = typedArray.getDrawable(R.styleable.FormUI_errorImage);
        }
        if (errorImage == null) {
            errorImage = Config.getInstance().getDefaultErrorImageDrawable(ctx);
        }
        autoUpload = typedArray.getBoolean(R.styleable.FormUI_autoUpload, false);
        if (typedArray.hasValue(R.styleable.FormUI_fileType)) {
            String fileTypeStr = typedArray.getString(R.styleable.FormUI_fileType);
            if (!TextUtils.isEmpty(fileTypeStr)) {
                fileType = fileTypeStr.split(",");
            }
        }
        if (fileType == null) {
            fileType = defaultFileType();
        }

        if (typedArray.hasValue(R.styleable.FormUI_clearImage)) {
            clearImage = typedArray.getDrawable(R.styleable.FormUI_clearImage);
        }
        if (clearImage == null) {
            clearImage = ContextCompat.getDrawable(ctx, io.coderf.arklab.common.R.drawable.ib_clear_image_selector);
        }
        clearImageWidth = typedArray.getDimension(R.styleable.FormUI_clearImageWidth, DensityUtil.dp2px(ctx, 24f));
        clearImageHeight = typedArray.getDimension(R.styleable.FormUI_clearImageHeight, DensityUtil.dp2px(ctx, 24f));
        clearImageTopMargin = typedArray.getDimension(R.styleable.FormUI_clearImageTopMargin, DensityUtil.dp2px(ctx, -8f));
        clearImageEndMargin = typedArray.getDimension(R.styleable.FormUI_clearImageEndMargin, DensityUtil.dp2px(ctx, -8f));

        showLabelIcon = typedArray.getBoolean(R.styleable.FormUI_showLabelIcon, false);
        hideTopLabelLayout = typedArray.getBoolean(R.styleable.FormUI_hideTopLabelLayout, false);
        if (typedArray.hasValue(R.styleable.FormUI_labelIcon)) {
            labelIcon = typedArray.getDrawable(R.styleable.FormUI_labelIcon);
        }
        labelIconWidth = typedArray.getDimension(R.styleable.FormUI_labelIconWidth, 0);
        labelIconHeight = typedArray.getDimension(R.styleable.FormUI_labelIconHeight, 0);
        labelIconStartMargin = typedArray.getDimension(R.styleable.FormUI_labelIconStartMargin,
                ctx.getResources().getDimension(R.dimen.form_label_icon_start_margin));
        labelIconEndMargin = typedArray.getDimension(R.styleable.FormUI_labelIconEndMargin,
                ctx.getResources().getDimension(R.dimen.form_label_icon_end_margin));
    }

    public abstract String[] defaultFileType();

    /**
     * 设置视频自动上传接口地址
     *
     * @param uploadUrl 上传接口地址，相对地址
     */
    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    /**
     * 设置请求 UI（上传失败 Toast / 错误码）。
     *
     * @param requestUi 应传 ViewModel 的 {@link io.coderf.arklab.common.base.NetworkRequestUiHost}（或其它 RequestUi 实现），勿直接传 Activity/Fragment
     */
    public void setRequestUi(@Nullable RequestUi requestUi) {
        this.requestUi = requestUi;
    }

    /**
     * 设置文件上传接口服务
     *
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
                ivLabelIcon.setVisibility(showLabelIcon && labelIcon != null ? VISIBLE : GONE);
            }
            if (tvRequired != null) {
                tvRequired.setVisibility(required ? VISIBLE : GONE);
            }
        }
    }

    public MaterialTextView getTvLabel() {
        return tvLabel;
    }

    public RecyclerView getMediaRecyclerView() {
        return mediaRecyclerView;
    }

    public MaterialTextView getTvRequired() {
        return tvRequired;
    }

    public View getVBorderBottom() {
        return vBorderBottom;
    }

    public ShapeableImageView getIvLabelIcon() {
        return ivLabelIcon;
    }

    public void createLabelIcon() {
        ivLabelIcon = new ShapeableImageView(getContext());
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
        tvLabel = new MaterialTextView(getContext());
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
        tvRequired = new MaterialTextView(getContext());
        tvRequired.setId(View.generateViewId());
        tvRequired.setLines(1);
        tvRequired.setText(requiredText != null ? requiredText : "*");
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        tvRequired.setGravity(Gravity.CENTER);
        tvRequired.setTextColor(requiredTextColor);
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
        params.setMarginStart((int) textStartMargin);
        params.setMarginEnd((int) textEndMargin);
        //如果不是当行显示的话
        params.topMargin = (int) defaultTextMargin;
        params.bottomMargin = (int) defaultTextMargin;
        addView(mediaRecyclerView, params);
    }

    public void createBottomLine() {
        if (!bottomBorder) {
            return;
        }
        vBorderBottom = new View(getContext());
        vBorderBottom.setId(View.generateViewId());
        int heightPx = Math.max(1, Math.round(bottomBorderHeight));
        LayoutParams params = new LayoutParams(0, heightPx);
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
        if (tvRequired != null) {
            tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 运行时套一层 FormUI style，立即刷新 label / required / 底线外观。
     */
    public void setFormStyleOverlay(@StyleRes int styleRes) {
        if (styleRes == 0) {
            return;
        }
        TypedArray typedArray = getContext().obtainStyledAttributes(styleRes, R.styleable.FormUI);
        try {
            applyFormUiTypedArray(typedArray);
        } finally {
            typedArray.recycle();
        }
        applyChromeFromFields();
    }

    /**
     * 批量应用与 FormConstraintLayout 共用的 FormUiConfig（媒体专属字段仍用 attr / setter）。
     */
    public void applyFormConfig(@NonNull FormUiConfig config) {
        if (config.styleOverlay != 0) {
            TypedArray typedArray = getContext().obtainStyledAttributes(config.styleOverlay, R.styleable.FormUI);
            try {
                applyFormUiTypedArray(typedArray);
            } finally {
                typedArray.recycle();
            }
        }
        if (config.labelTextColor != null) {
            labelTextColor = config.labelTextColor;
        }
        if (config.borderBottomColor != null) {
            borderBottomColor = config.borderBottomColor;
        }
        if (config.requiredTextColor != null) {
            requiredTextColor = config.requiredTextColor;
        }
        if (config.formLabelTextSize != null) {
            formLabelTextSize = config.formLabelTextSize;
        }
        if (config.formTextSize != null) {
            formTextSize = config.formTextSize;
        }
        if (config.formRequiredSize != null) {
            formRequiredSize = config.formRequiredSize;
        }
        if (config.bottomBorderHeight != null) {
            bottomBorderHeight = config.bottomBorderHeight;
        }
        if (config.borderBottomStartMargin != null) {
            borderBottomStartMargin = config.borderBottomStartMargin;
        }
        if (config.borderBottomEndMargin != null) {
            borderBottomEndMargin = config.borderBottomEndMargin;
        }
        if (config.labelStartMargin != null) {
            labelStartMargin = config.labelStartMargin;
        }
        if (config.labelEndMargin != null) {
            labelEndMargin = config.labelEndMargin;
        }
        if (config.textStartMargin != null) {
            textStartMargin = config.textStartMargin;
        }
        if (config.textEndMargin != null) {
            textEndMargin = config.textEndMargin;
        }
        if (config.defaultTextMargin != null) {
            defaultTextMargin = config.defaultTextMargin;
        }
        if (config.requiredStartMargin != null) {
            requiredStartMargin = config.requiredStartMargin;
        }
        if (config.bottomBorder != null) {
            bottomBorder = config.bottomBorder;
        }
        if (config.required != null) {
            required = config.required;
        }
        if (config.requiredText != null) {
            requiredText = config.requiredText;
        }
        if (config.label != null) {
            labelString = config.label;
        }
        if (config.labelTextStyle != null) {
            labelTextStyle = config.labelTextStyle;
        }
        if (config.showLabelIcon != null) {
            showLabelIcon = config.showLabelIcon;
        }
        if (config.labelIcon != null) {
            labelIcon = config.labelIcon;
        }
        if (config.labelIconWidth != null) {
            labelIconWidth = config.labelIconWidth;
        }
        if (config.labelIconHeight != null) {
            labelIconHeight = config.labelIconHeight;
        }
        if (config.labelIconStartMargin != null) {
            labelIconStartMargin = config.labelIconStartMargin;
        }
        if (config.labelIconEndMargin != null) {
            labelIconEndMargin = config.labelIconEndMargin;
        }
        applyChromeFromFields();
    }

    /**
     * 把当前字段刷到已创建的 label / required / 底线，不重建列表。
     */
    protected void applyChromeFromFields() {
        if (tvLabel != null) {
            tvLabel.setTextColor(labelTextColor);
            tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, formLabelTextSize);
            if (labelString != null) {
                tvLabel.setText(labelString);
            }
            if (LabelTextStyleEnum.BOLD.value == labelTextStyle) {
                tvLabel.setTypeface(tvLabel.getTypeface(), Typeface.BOLD);
            } else {
                tvLabel.setTypeface(tvLabel.getTypeface(), Typeface.NORMAL);
            }
        }
        if (tvRequired != null) {
            tvRequired.setText(requiredText != null ? requiredText : "*");
            tvRequired.setTextColor(requiredTextColor);
            tvRequired.setTextSize(TypedValue.COMPLEX_UNIT_PX, formRequiredSize);
            tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        }
        if (ivLabelIcon != null && labelIcon != null) {
            ivLabelIcon.setImageDrawable(labelIcon);
        }
        if (vBorderBottom != null) {
            vBorderBottom.setBackgroundColor(borderBottomColor);
            LayoutParams params = (LayoutParams) vBorderBottom.getLayoutParams();
            if (params != null) {
                params.height = Math.max(1, Math.round(bottomBorderHeight));
                params.setMarginStart((int) borderBottomStartMargin);
                params.setMarginEnd((int) borderBottomEndMargin);
                vBorderBottom.setLayoutParams(params);
            }
            vBorderBottom.setVisibility(bottomBorder ? View.VISIBLE : View.GONE);
        } else if (bottomBorder) {
            createBottomLine();
        }
        applyShowTopLayout();
    }

}
