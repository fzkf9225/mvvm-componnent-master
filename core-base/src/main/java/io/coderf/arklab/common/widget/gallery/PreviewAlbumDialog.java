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

    public static final int DEFAULT_SPAN_COUNT = 4;

    private List<PreviewInfoBean> imageInfos = new ArrayList<>();
    private int currentPosition;
    private int spanCount = DEFAULT_SPAN_COUNT;
    @Nullable
    private String title;
    @Nullable
    private Drawable placeholderImage;
    @Nullable
    private Drawable errorImage;
    @Nullable
    private OnAlbumItemClickListener onItemClickListener;

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private PreviewAlbumAdapter albumAdapter;

    public PreviewAlbumDialog(@NonNull Context context) {
        super(context, R.style.PreviewPhotoDialog);
    }

    public PreviewAlbumDialog(@NonNull Context context, List<PreviewInfoBean> imageInfos, int currentPosition) {
        this(context);
        setImages(imageInfos);
        this.currentPosition = currentPosition;
    }

    public PreviewAlbumDialog setImages(@Nullable List<PreviewInfoBean> imageInfos) {
        this.imageInfos = imageInfos == null ? new ArrayList<>() : imageInfos;
        return this;
    }

    public PreviewAlbumDialog setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        return this;
    }

    public PreviewAlbumDialog setSpanCount(int spanCount) {
        this.spanCount = spanCount > 0 ? spanCount : DEFAULT_SPAN_COUNT;
        return this;
    }

    public PreviewAlbumDialog setAlbumTitle(@Nullable String title) {
        this.title = title;
        return this;
    }

    public PreviewAlbumDialog setPlaceholderImage(@Nullable Drawable placeholderImage) {
        this.placeholderImage = placeholderImage;
        return this;
    }

    public PreviewAlbumDialog setErrorImage(@Nullable Drawable errorImage) {
        this.errorImage = errorImage;
        return this;
    }

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
    private static void withTitleView(@NonNull MaterialToolbar toolbar) {
        toolbar.post(() -> {
            TextView delayed = ToolbarConfig.findToolbarTitleView(toolbar);
            if (delayed != null) {
                delayed.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
            }
        });
    }
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

    public interface OnAlbumItemClickListener {
        void onAlbumItemClick(@NonNull PreviewAlbumDialog dialog, @NonNull PreviewInfoBean item, int position);
    }
}
