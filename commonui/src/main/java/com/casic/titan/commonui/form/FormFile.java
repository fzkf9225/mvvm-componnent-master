package com.casic.titan.commonui.form;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
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
import androidx.recyclerview.widget.RecyclerView;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.adapter.FileAddAdapter;
import com.casic.titan.commonui.bean.AttachmentBean;
import com.casic.titan.commonui.utils.AttachmentUtil;

import java.util.ArrayList;
import java.util.List;

import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.MediaListener;
import pers.fz.mvvm.base.BaseModelEntity;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.wight.dialog.CustomProgressDialog;
import pers.fz.mvvm.wight.dialog.OpenFileDialog;
import pers.fz.mvvm.wight.dialog.OpenImageDialog;
import pers.fz.mvvm.wight.recyclerview.FullyLinearLayoutManager;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormFile extends FrameLayout implements FileAddAdapter.FileClearListener, DefaultLifecycleObserver,
        BaseView {
    protected String labelString;
    protected int bgColor;
    protected boolean required = false;
    protected boolean bottomBorder = true;
    protected TextView tvLabel, tvRequired;
    protected TextView tvSelection, tvEmpty;
    protected int labelTextColor = 0xFF999999;
    protected ImageView imageAdd;
    protected RecyclerView mRecyclerViewFile;
    private FileAddAdapter fileAddAdapter;
    private MediaHelper mediaHelper;
    private String fieldName;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int mediaType = OpenImageDialog.CAMERA_ALBUM;
    private int maxCount = MediaHelper.DEFAULT_ALBUM_MAX_COUNT;

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
            fieldName = typedArray.getString(R.styleable.FormImage_field);
            labelTextColor = typedArray.getColor(R.styleable.FormImage_labelTextColor, labelTextColor);
            bgColor = typedArray.getColor(R.styleable.FormImage_bgColor, 0xFFF1F3F2);
            required = typedArray.getBoolean(R.styleable.FormImage_required, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormImage_bottomBorder, true);
            mediaType = typedArray.getInt(R.styleable.FormImage_mediaType, OpenImageDialog.CAMERA_ALBUM);
            maxCount = typedArray.getInt(R.styleable.FormImage_maxCount, MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
            typedArray.recycle();
        }
    }

    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.form_file, this, true);
        tvLabel = findViewById(R.id.tv_label);
        mRecyclerViewFile = findViewById(R.id.mRecyclerViewFile);
        imageAdd = findViewById(R.id.image_add);
        tvEmpty = findViewById(R.id.tv_empty);
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
        tvRequired = findViewById(R.id.tv_required);
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        tvLabel.setText(labelString);
        tvLabel.setTextColor(labelTextColor);
        if (bottomBorder) {
            constraintLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
        tvEmpty.setVisibility(View.VISIBLE);
        imageAdd.setOnClickListener(v -> {
            if (maxCount > 0 && fileAddAdapter.getList().size() >= maxCount) {
                showToast("最多只可选择" + maxCount + "个附件");
                return;
            }
            mediaHelper.openFileDialog(v, "从文件管理器中选择", OpenFileDialog.FILE);
        });
        fileAddAdapter = new FileAddAdapter(getContext());
        fileAddAdapter.setFileClearListener(this);
        mRecyclerViewFile.setLayoutManager(new FullyLinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mRecyclerViewFile.setAdapter(fileAddAdapter);
        mRecyclerViewFile.addItemDecoration(
                new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL, DensityUtil.dp2px(getContext(), 8),
                        ContextCompat.getColor(getContext(), R.color.white))
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
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
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
        tvEmpty.setVisibility((fileAddAdapter.getList() == null || fileAddAdapter.getList().isEmpty()) ? View.VISIBLE : View.GONE);

    }

    public CharSequence getText() {
        return tvSelection.getText();
    }

    public void setText(String text) {
        tvSelection.setText(text);
    }

    public void setLabel(String text) {
        tvLabel.setText(text);
    }

    @Override
    public void fileClear(View view, int position) {
        fileAddAdapter.getList().remove(position);
        fileAddAdapter.notifyDataSetChanged();
        tvEmpty.setVisibility((fileAddAdapter.getList() == null || fileAddAdapter.getList().isEmpty()) ? View.VISIBLE : View.GONE);
    }

    public MediaHelper getMediaHelper() {
        return mediaHelper;
    }

    public int getMediaType() {
        return mediaType;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void initFragment(Fragment fragment) {
        mediaHelper = new MediaBuilder((ComponentActivity) getContext(), this)
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
        mediaHelper.getMutableLiveData().observe(fragment, mediaBean -> {
            AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
            fileAddAdapter.getList().addAll(AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList()));
            fileAddAdapter.notifyDataSetChanged();
            tvEmpty.setVisibility((fileAddAdapter.getList() == null || fileAddAdapter.getList().isEmpty()) ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
        if (getContext() instanceof ComponentActivity) {
            mediaHelper = new MediaBuilder((ComponentActivity) getContext(), this)
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
            mediaHelper.getMutableLiveData().observe(owner, mediaBean -> {
                AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
                fileAddAdapter.getList().addAll(AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList()));
                fileAddAdapter.notifyDataSetChanged();
                tvEmpty.setVisibility((fileAddAdapter.getList() == null || fileAddAdapter.getList().isEmpty()) ? View.VISIBLE : View.GONE);
            });
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
        if (mediaHelper != null) {
            mediaHelper.unregister();
        }
    }

    @Override
    public void showLoading(String dialogMessage) {
        handler.post(() -> CustomProgressDialog.getInstance(getContext())
                .setCanCancel(false)
                .setMessage(dialogMessage)
                .builder()
                .show());
    }

    @Override
    public void refreshLoading(String dialogMessage) {
        handler.post(() -> CustomProgressDialog.getInstance(getContext())
                .refreshMessage(dialogMessage));
    }

    @Override
    public void hideLoading() {
        handler.post(() -> CustomProgressDialog.getInstance(getContext()).dismiss());
    }

    @Override
    public void showToast(String s) {
        handler.post(() -> Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onErrorCode(BaseModelEntity model) {

    }

}
