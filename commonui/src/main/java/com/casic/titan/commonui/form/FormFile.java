package com.casic.titan.commonui.form;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.adapter.FileAddAdapter;
import com.casic.titan.commonui.bean.AttachmentBean;
import com.casic.titan.commonui.databinding.FormFileBinding;
import com.casic.titan.commonui.utils.AttachmentUtil;

import java.util.ArrayList;
import java.util.List;

import pers.fz.media.LogUtil;
import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.dialog.OpenFileDialog;
import pers.fz.media.listener.MediaListener;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.base.BaseFragment;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.wight.recyclerview.FullyLinearLayoutManager;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormFile extends ConstraintLayout implements FileAddAdapter.FileClearListener {
    protected String labelString;
    protected int bgColor = 0xFFF1F3F2;
    protected boolean required = false;
    protected boolean bottomBorder = true;
    private FileAddAdapter fileAddAdapter;
    private MediaHelper mediaHelper;
    private int maxCount = MediaHelper.DEFAULT_ALBUM_MAX_COUNT;
    private float formLabelTextSize;
    private float formRequiredSize;
    private float formTextSize;
    private int rightTextColor;
    private int emptyTextColor;
    private int labelTextColor;
    private int fileAddSrc = R.mipmap.ic_add_theme_color;
    private float radius = 5;

    private FormFileBinding binding;

    public FormFile(Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public FormFile(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FormFile(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    protected void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormImage);
            labelString = typedArray.getString(R.styleable.FormImage_label);
            rightTextColor = typedArray.getColor(R.styleable.FormImage_rightTextColor, ContextCompat.getColor(getContext(), R.color.auto_color));
            labelTextColor = typedArray.getColor(R.styleable.FormImage_labelTextColor, ContextCompat.getColor(getContext(), R.color.auto_color));
            emptyTextColor = typedArray.getColor(R.styleable.FormImage_emptyTextColor, ContextCompat.getColor(getContext(), R.color.auto_color));
            bgColor = typedArray.getColor(R.styleable.FormImage_bgColor, 0xFFF1F3F2);
            required = typedArray.getBoolean(R.styleable.FormImage_required, false);
            fileAddSrc = typedArray.getResourceId(R.styleable.FormImage_file_add_src, R.mipmap.ic_add_theme_color);
            radius = typedArray.getDimension(R.styleable.FormImage_add_image_radius, DensityUtil.dp2px(getContext(), 4));
            bottomBorder = typedArray.getBoolean(R.styleable.FormImage_bottomBorder, true);
            maxCount = typedArray.getInt(R.styleable.FormImage_maxCount, MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
            formLabelTextSize = typedArray.getDimension(R.styleable.FormImage_formLabelTextSize, DensityUtil.sp2px(getContext(), 14));
            formRequiredSize = typedArray.getDimension(R.styleable.FormImage_formRequiredSize, DensityUtil.sp2px(getContext(), 14));
            formTextSize = typedArray.getDimension(R.styleable.FormImage_formTextSize, DensityUtil.sp2px(getContext(), 14));
            typedArray.recycle();
        } else {
            rightTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
            emptyTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
            labelTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
            radius = DensityUtil.dp2px(getContext(), 4);
            formLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            formRequiredSize = DensityUtil.sp2px(getContext(), 14);
            formTextSize = DensityUtil.sp2px(getContext(), 14);
        }
    }

    protected void init() {
        binding = FormFileBinding.inflate(LayoutInflater.from(getContext()), this, true);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setPadding(getPaddingStart(), DensityUtil.dp2px(getContext(), 12),
                getPaddingEnd(), DensityUtil.dp2px(getContext(), 12));
        binding.imageAdd.setImageResource(fileAddSrc);
        binding.tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        binding.tvLabel.setText(labelString);
        binding.tvEmpty.setTextColor(emptyTextColor);
        binding.tvLabel.setTextColor(labelTextColor);
        binding.tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, formLabelTextSize);
        binding.tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_PX, formTextSize);
        binding.tvRequired.setTextSize(TypedValue.COMPLEX_UNIT_PX, formRequiredSize);
        if (bottomBorder) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
        binding.tvEmpty.setVisibility(View.VISIBLE);
        binding.imageAdd.setOnClickListener(v -> {
            if (maxCount > 0 && fileAddAdapter.getList().size() >= maxCount) {
                Toast.makeText(getContext(), "最多只可选择" + maxCount + "个附件", Toast.LENGTH_SHORT).show();
                return;
            }
            mediaHelper.openFileDialog(v, "从文件管理器中选择", OpenFileDialog.FILE);
        });
        fileAddAdapter = new FileAddAdapter();
        fileAddAdapter.setRadius(radius);
        fileAddAdapter.setBgColor(bgColor);
        fileAddAdapter.setTextColor(rightTextColor);
        fileAddAdapter.setFileClearListener(this);
        ConstraintLayout.LayoutParams imageLayoutParams = (LayoutParams) binding.mRecyclerViewFile.getLayoutParams();
        imageLayoutParams.topMargin = DensityUtil.dp2px(getContext(), 12);
        binding.mRecyclerViewFile.setLayoutParams(imageLayoutParams);
        binding.mRecyclerViewFile.setLayoutManager(new FullyLinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.mRecyclerViewFile.setAdapter(fileAddAdapter);
        binding.mRecyclerViewFile.addItemDecoration(
                new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL, DensityUtil.dp2px(getContext(), 8),
                        0x00000000)
        );
        if (mediaHelper != null) {
            mediaHelper.getMediaBuilder().setFileMaxSelectedCount(maxCount == -1 ? Integer.MAX_VALUE : maxCount);
        }
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
        binding.tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
    }

    public List<AttachmentBean> getImages() {
        return fileAddAdapter.getList();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setImages(List<AttachmentBean> images) {
        if (images == null) {
            images = new ArrayList<>();
        }
        fileAddAdapter.setList(images);
        fileAddAdapter.notifyDataSetChanged();
        binding.tvEmpty.setVisibility((fileAddAdapter.getList() == null || fileAddAdapter.getList().isEmpty()) ? View.VISIBLE : View.GONE);
    }

    public void setLabel(String text) {
        binding.tvLabel.setText(text);
    }

    @Override
    public void fileClear(View view, int position) {
        fileAddAdapter.getList().remove(position);
        fileAddAdapter.notifyDataSetChanged();
        binding.tvEmpty.setVisibility((fileAddAdapter.getList() == null || fileAddAdapter.getList().isEmpty()) ? View.VISIBLE : View.GONE);
    }

    public MediaHelper getMediaHelper() {
        return mediaHelper;
    }
    public void bindLifecycle(LifecycleOwner lifecycleOwner){
        mediaHelper = new MediaBuilder(getContext())
                .bindLifeCycle(lifecycleOwner)
                .setFileMaxSelectedCount(maxCount == -1 ? Integer.MAX_VALUE : maxCount)
                .setChooseType(MediaHelper.PICK_TYPE)
                .setMediaListener(new MediaListener() {
                    @Override
                    public int onSelectedFileCount() {
                        return fileAddAdapter.getList().size();
                    }

                    @Override
                    public int onSelectedAudioCount() {
                        return fileAddAdapter.getList().size();
                    }

                    @Override
                    public int onSelectedImageCount() {
                        return fileAddAdapter.getList().size();
                    }

                    @Override
                    public int onSelectedVideoCount() {
                        return fileAddAdapter.getList().size();
                    }
                })
                .builder();
        mediaHelper.getMutableLiveData().observe(lifecycleOwner, mediaBean -> {
            AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
            fileAddAdapter.getList().addAll(AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList()));
            fileAddAdapter.notifyDataSetChanged();
            binding.tvEmpty.setVisibility((fileAddAdapter.getList() == null || fileAddAdapter.getList().isEmpty()) ? View.VISIBLE : View.GONE);
        });
    }

    public FormFileBinding getBinding() {
        return binding;
    }

}
