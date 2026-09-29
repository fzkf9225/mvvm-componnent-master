package io.coderf.arklab.common.widget.gallery;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.base.ToolbarConfig;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.widget.gallery.adapter.PreviewAlbumAdapter;
import io.coderf.arklab.common.widget.gallery.bean.PreviewInfoBean;
import io.coderf.arklab.common.widget.recyclerview.GridSpacingItemDecoration;

/**
 * 仿微信「查看全部」相册：当前预览列表的宫格，点击条目回到对应大图。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/11
 */
public class PreviewAlbumDialog extends Dialog {

    /**
     * 默认宫格列数
     */
    public static final int DEFAULT_SPAN_COUNT = 4;

    /**
     * 相册条目
     */
    private List<PreviewInfoBean> imageInfos = new ArrayList<>();
    /**
     * 从大图进入时的当前下标，用于高亮并滚动到对应格子
     */
    private int currentPosition;
    /**
     * 宫格列数
     */
    private int spanCount = DEFAULT_SPAN_COUNT;
    /**
     * 标题栏文案，为空时使用默认「图片和视频」
     */
    @Nullable
    private String title;
    /**
     * 缩略图占位图
     */
    @Nullable
    private Drawable placeholderImage;
    /**
     * 缩略图失败图
     */
    @Nullable
    private Drawable errorImage;
    /**
     * 点击格子后的回调
     */
    @Nullable
    private OnAlbumItemClickListener onItemClickListener;

    /**
     * 标题栏
     */
    private MaterialToolbar toolbar;
    /**
     * 宫格列表
     */
    private RecyclerView recyclerView;
    /**
     * 宫格适配器
     */
    private PreviewAlbumAdapter albumAdapter;

    /**
     * @param context 上下文
     */
    public PreviewAlbumDialog(@NonNull Context context) {
        super(context, R.style.PreviewPhotoDialog);
    }

    /**
     * @param context         上下文
     * @param imageInfos      相册条目
     * @param currentPosition 当前大图下标
     */
    public PreviewAlbumDialog(@NonNull Context context, List<PreviewInfoBean> imageInfos, int currentPosition) {
        this(context);
        setImages(imageInfos);
        this.currentPosition = currentPosition;
    }

    /**
     * @param imageInfos 相册条目，null 会当成空列表
     */
    public PreviewAlbumDialog setImages(@Nullable List<PreviewInfoBean> imageInfos) {
        this.imageInfos = imageInfos == null ? new ArrayList<>() : imageInfos;
        return this;
    }

    /**
     * @param currentPosition 需要高亮的下标
     */
    public PreviewAlbumDialog setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        return this;
    }

    /**
     * @param spanCount 列数，小于等于 0 时使用 {@link #DEFAULT_SPAN_COUNT}
     */
    public PreviewAlbumDialog setSpanCount(int spanCount) {
        this.spanCount = spanCount > 0 ? spanCount : DEFAULT_SPAN_COUNT;
        return this;
    }

    /**
     * @param title 标题栏文案
     */
    public PreviewAlbumDialog setAlbumTitle(@Nullable String title) {
        this.title = title;
        return this;
    }

    /**
     * @param placeholderImage 缩略图占位图
     */
    public PreviewAlbumDialog setPlaceholderImage(@Nullable Drawable placeholderImage) {
        this.placeholderImage = placeholderImage;
        return this;
    }

    /**
     * @param errorImage 缩略图失败图
     */
    public PreviewAlbumDialog setErrorImage(@Nullable Drawable errorImage) {
        this.errorImage = errorImage;
        return this;
    }

    /**
     * @param onItemClickListener 点击格子后的回调
     */
    public PreviewAlbumDialog setOnItemClickListener(@Nullable OnAlbumItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    @Nullable
    public OnAlbumItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    @Nullable
    public Drawable getPlaceholderImage() {
        return placeholderImage;
    }

    @Nullable
    public Drawable getErrorImage() {
        return errorImage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dialog_preview_album);
        Window window = getWindow();
        if (window != null) {
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            WindowCompat.setDecorFitsSystemWindows(window, false);
        }
        toolbar = findViewById(R.id.toolbar_preview_album);
        recyclerView = findViewById(R.id.rv_preview_album);
        toolbar.setTitle(TextUtils.isEmpty(title)
                ? getContext().getString(R.string.preview_album_title)
                : title);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        withTitleView(toolbar);
        albumAdapter = new PreviewAlbumAdapter(this);
        albumAdapter.setSelectedPosition(currentPosition);
        albumAdapter.setList(imageInfos);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(DensityUtil.dp2px(getContext(), 1.5f)));
        recyclerView.setAdapter(albumAdapter);
        applyWindowInsets();
        if (currentPosition >= 0 && currentPosition < imageInfos.size()) {
            recyclerView.post(() -> recyclerView.scrollToPosition(currentPosition));
        }
    }
    /**
     * 标题栏文字尺寸在布局完成后才能拿到 TextView，这里延迟设置
     *
     * @param toolbar 相册标题栏
     */
    private static void withTitleView(@NonNull MaterialToolbar toolbar) {
        toolbar.post(() -> {
            TextView delayed = ToolbarConfig.findToolbarTitleView(toolbar);
            if (delayed != null) {
                delayed.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
            }
        });
    }
    /**
     * 给标题栏和列表补上状态栏、导航栏间距
     */
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp != null) {
                lp.height = getContext().getResources().getDimensionPixelSize(R.dimen.toolbar_height) + bars.top;
                v.setLayoutParams(lp);
            }
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(toolbar);
        ViewCompat.requestApplyInsets(recyclerView);
    }

    /**
     * 宫格条目点击
     */
    public interface OnAlbumItemClickListener {
        /**
         * @param dialog   当前相册弹窗
         * @param item     被点击的条目
         * @param position 条目下标
         */
        void onAlbumItemClick(@NonNull PreviewAlbumDialog dialog, @NonNull PreviewInfoBean item, int position);
    }
}
