package com.casic.titan.commonui.form;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.adapter.FileAddAdapter;

import java.util.ArrayList;
import java.util.List;

import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.dialog.OpenFileDialog;
import pers.fz.media.enums.MediaPickerTypeEnum;
import pers.fz.media.listener.MediaListener;
import pers.fz.mvvm.bean.AttachmentBean;
import pers.fz.mvvm.utils.common.AttachmentUtil;
import pers.fz.mvvm.utils.common.DensityUtil;
import pers.fz.mvvm.widget.recyclerview.FullyLinearLayoutManager;
import pers.fz.mvvm.widget.recyclerview.RecycleViewDivider;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormFile extends FormMedia implements FileAddAdapter.FileClearListener {
    /**
     * 列表适配器
     */
    protected FileAddAdapter fileAddAdapter;
    /**
     * 媒体管理器
     */
    protected MediaHelper mediaHelper;
    /**
     * 最大可选数量
     */
    protected int maxCount = MediaHelper.DEFAULT_ALBUM_MAX_COUNT;
    /**
     * 文本大小
     */
    protected float formTextSize;
    /**
     * 空白、暂无数据文字颜色
     */
    protected int emptyTextColor;
    /**
     * 添加附件图标资源id
     */
    protected int fileAddSrc = R.mipmap.ic_add_theme_color;
    /**
     * 空白、暂无数据控件
     */
    protected AppCompatTextView tvEmpty;
    /**
     * 附件添加控件
     */
    protected AppCompatImageView imageAdd;

    /**
     * 空白文字大小
     */
    protected float emptyTextSize;
    /**
     * 空白文字内容
     */
    protected String emptyText = "暂无附件，请点击右上角添加附件";
    /**
     * 文件限定格式，英文逗号分割
     */
    protected String[] fileType = null;

    /**
     * 列表中item文字颜色
     */
    protected int itemTextColor;

    public FormFile(@NonNull Context context) {
        super(context);
    }

    public FormFile(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormFile(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            fileAddSrc = typedArray.getResourceId(R.styleable.FormUI_fileAddSrc, R.mipmap.ic_add_theme_color);
            maxCount = typedArray.getInt(R.styleable.FormUI_maxCount, MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
            formTextSize = typedArray.getDimension(R.styleable.FormUI_formTextSize, DensityUtil.sp2px(getContext(), 14));
            emptyTextColor = typedArray.getColor(R.styleable.FormUI_emptyTextColor, ContextCompat.getColor(getContext(), R.color.dark_color));
            emptyTextSize = typedArray.getDimension(R.styleable.FormUI_emptyTextSize, DensityUtil.sp2px(getContext(), 14));
            emptyText = typedArray.getString(R.styleable.FormUI_emptyText);
            if (TextUtils.isEmpty(emptyText)) {
                emptyText = "暂无附件，请点击右上角添加附件";
            }

            String fileTypeStr = typedArray.getString(R.styleable.FormUI_fileType);
            if (!TextUtils.isEmpty(fileTypeStr)) {
                fileType = fileTypeStr.split(",");
            }
            itemTextColor = typedArray.getColor(R.styleable.FormUI_itemTextColor, ContextCompat.getColor(getContext(), R.color.auto_color));
            typedArray.recycle();
        } else {
            formTextSize = DensityUtil.sp2px(getContext(), 14);
            itemTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
            emptyTextColor = ContextCompat.getColor(getContext(), R.color.dark_color);
            emptyTextSize = DensityUtil.sp2px(getContext(), 14);
            emptyText = "暂无附件，请点击右上角添加附件";
            fileType = null;
        }
    }


    @Override
    protected void init() {
        super.init();
        createImageAdd();
        layoutImageAdd();
        createEmptyLayout();
        layoutEmptyLayout();
        tvEmpty.setVisibility(View.VISIBLE);
        mediaRecyclerView.setVisibility(View.GONE);
        imageAdd.setOnClickListener(v -> {
            if (maxCount > 0 && fileAddAdapter.getList().size() >= maxCount) {
                Toast.makeText(getContext(), "最多只可选择" + maxCount + "个附件", Toast.LENGTH_SHORT).show();
                return;
            }
            mediaHelper.openFileDialog(v, "从文件管理器中选择", OpenFileDialog.FILE);
        });
        fileAddAdapter = new FileAddAdapter();
        fileAddAdapter.setRadius(radius);
        fileAddAdapter.setBgColor(bgColor);
        fileAddAdapter.setTextColor(itemTextColor);
        fileAddAdapter.setFileClearListener(this);
        mediaRecyclerView.setLayoutManager(new FullyLinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mediaRecyclerView.setAdapter(fileAddAdapter);
        mediaRecyclerView.addItemDecoration(
                new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL, (int) columnMargin,
                        0x00000000)
        );
        if (mediaHelper != null) {
            mediaHelper.getMediaBuilder().setFileMaxSelectedCount(maxCount == -1 ? Integer.MAX_VALUE : maxCount);
        }
    }

    @Override
    public String[] defaultFileType() {
        return new String[]{"*/*"};
    }

    public AppCompatImageView getImageAdd() {
        return imageAdd;
    }

    public void createImageAdd() {
        imageAdd = new AppCompatImageView(getContext());
        imageAdd.setId(View.generateViewId());
        imageAdd.setImageResource(fileAddSrc);
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMarginStart((int) labelStartMargin);
        params.setMarginEnd((int) labelStartMargin);
        addView(imageAdd, params);
    }

    public void layoutImageAdd() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(imageAdd.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.connect(imageAdd.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
        constraintSet.connect(imageAdd.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
        constraintSet.applyTo(this);
    }

    public void createEmptyLayout() {
        tvEmpty = new AppCompatTextView(getContext());
        tvEmpty.setText(emptyText);
        tvEmpty.setId(View.generateViewId());
        tvEmpty.setTextColor(emptyTextColor);
        tvEmpty.setGravity(Gravity.CENTER);
        tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_PX, emptyTextSize);
        tvEmpty.setText(emptyText);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(0, DensityUtil.dp2px(getContext(), 60f));
        params.setMarginStart((int) textEndMargin);
        params.setMarginEnd((int) textEndMargin);
        params.topMargin = (int) defaultTextMargin;
        params.bottomMargin = (int) defaultTextMargin;
        addView(tvEmpty, params);
    }

    public void layoutEmptyLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(tvEmpty.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(tvEmpty.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.connect(tvEmpty.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(tvEmpty.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.applyTo(this);
    }

    public void refreshCount() {
        boolean isEmpty = fileAddAdapter.getList() == null || fileAddAdapter.getList().isEmpty();
        mediaRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    public List<AttachmentBean> getFiles() {
        return fileAddAdapter.getList();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFiles(List<AttachmentBean> files) {
        if (files == null) {
            files = new ArrayList<>();
        }
        fileAddAdapter.setList(files);
        fileAddAdapter.notifyDataSetChanged();
        refreshCount();
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void fileClear(View view, int position) {
        fileAddAdapter.getList().remove(position);
        fileAddAdapter.notifyDataSetChanged();
        refreshCount();
    }

    public MediaHelper getMediaHelper() {
        return mediaHelper;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void bindLifecycle(LifecycleOwner lifecycleOwner) {
        mediaHelper = new MediaBuilder(getContext())
                .bindLifeCycle(lifecycleOwner)
                .setFileMaxSelectedCount(maxCount == -1 ? Integer.MAX_VALUE : maxCount)
                .setChooseType(MediaPickerTypeEnum.PICK)
                .setShowPermissionDialog(protocolDialog)
                .setFileType(fileType)
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

                    @Override
                    public int onSelectedMediaCount() {
                        return fileAddAdapter.getList().size();
                    }
                })
                .builder();
        mediaHelper.getMutableLiveData().observe(lifecycleOwner, mediaBean -> {
            if(requireUriPermission){
                AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
            }
            fileAddAdapter.getList().addAll(AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList()));
            fileAddAdapter.notifyDataSetChanged();
            refreshCount();
        });
    }

    /**
     * 设置文件格式类型
     *
     * @param fileType 数组格式：image/* video/* audio/*
     */
    public void setFileType(String[] fileType) {
        this.fileType = fileType;
        if (mediaHelper == null) {
            Toast.makeText(getContext(), "请先调用bindLifecycle方法", Toast.LENGTH_SHORT).show();
            return;
        }
        mediaHelper.getMediaBuilder().setFileType(fileType);
    }
}
