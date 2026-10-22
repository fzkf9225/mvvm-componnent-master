package io.coderf.arklab.demo.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.adapter.PagingLiveAdapter;
import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseSmartPagingFragment;
import io.coderf.arklab.common.widget.dialog.ConfirmDialog;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.adapter.PagingDemoAdapter;
import io.coderf.arklab.demo.adapter.PagingLiveDemoAdapter;
import io.coderf.arklab.demo.bean.NotificationMessageBean;
import io.coderf.arklab.demo.databinding.DemoSmartPagingReverseBinding;
import io.coderf.arklab.demo.viewmodel.DemoPagingViewModel;
import kotlin.Unit;

/**
 * 演示：最新在屏幕底部（仅 {@code reverseLayout}，不用 stackFromEnd）+ 底部上拉刷新。
 * <p>
 * 布局：app 模块 {@link R.layout#demo_smart_paging_reverse}。
 */
@AndroidEntryPoint
public class DemoSmartPagingLiveFragment
        extends BaseSmartPagingFragment<DemoPagingViewModel, DemoSmartPagingReverseBinding, NotificationMessageBean>
        implements OnLoadMoreListener {

    private PagingLiveDemoAdapter liveAdapter;
    private int appendSeq = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.demo_smart_paging_reverse;
    }

    @Override
    protected BasePagingAdapter<NotificationMessageBean, ?> getRecyclerAdapter() {
        PagingDemoAdapter pagingDemoAdapter = new PagingDemoAdapter();
        liveAdapter = new PagingLiveDemoAdapter();
        liveAdapter.setOnPagingAdapterListener(this);
        pagingDemoAdapter.setPagingLiveAdapter(liveAdapter);
        return pagingDemoAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        // 仅 reverse：数据 index0=最新 → 视觉底部；不要 setStackFromEnd，否则首屏要对齐异常
        lm.setReverseLayout(true);
        return lm;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setEnableAutoLoadMore(false);
        refreshLayout.setOnLoadMoreListener(this);

        adapter.addLoadStateListener(states -> {
            if (states.getRefresh() instanceof LoadState.NotLoading) {
                refreshLayout.finishLoadMore();
            } else if (states.getRefresh() instanceof LoadState.Error) {
                refreshLayout.finishLoadMore(false);
            }
            return Unit.INSTANCE;
        });
    }

    @Override
    protected void initData(Bundle bundle) {
        super.initData(bundle);
        mViewModel.getItems().observe(this, observer);
        mViewModel.refreshData();
        adapter.refresh();
    }

    /** 新消息后贴视觉底部（reverse 下 position 0）。 */
    private void scrollToNewest() {
        if (mRecyclerView == null) {
            return;
        }
        mRecyclerView.post(() -> {
            if (mRecyclerView.getAdapter() != null && mRecyclerView.getAdapter().getItemCount() > 0) {
                mRecyclerView.scrollToPosition(0);
            }
        });
    }

    public void appendLiveItem() {
        if (liveAdapter == null) {
            return;
        }
        appendSeq++;
        NotificationMessageBean bean = new NotificationMessageBean();
        bean.setId("live-" + UUID.randomUUID());
        bean.setTitle("[实时最新 #" + appendSeq + "] 模拟新消息");
        bean.setContent("数据 index 0；reverseLayout 后在屏幕底部。");
        bean.setPublishDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        liveAdapter.append(bean);
        scrollToNewest();
        showToast("已插入第 " + appendSeq + " 条（视觉底部）");
    }

    @Nullable
    public PagingLiveAdapter<?, ?> getLiveAdapter() {
        return liveAdapter;
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        if (liveAdapter != null) {
            liveAdapter.clearLive();
        }
        appendSeq = 0;
        mViewModel.refreshData();
        adapter.refresh();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        onLoadMore(refreshLayout);
    }

    @Override
    public void onItemClick(View view, NotificationMessageBean item, int position) {
        super.onItemClick(view, item, position);
        String tag = item.getId() != null && item.getId().startsWith("live-") ? "（实时最新）" : "（历史分页）";
        showToast(tag + " 第" + position + "行：" + item.getTitle());
    }

    @Override
    public void onItemLongClick(View view, NotificationMessageBean item, int position) {
        super.onItemLongClick(view, item, position);
        if (item.getId() != null && item.getId().startsWith("live-") && liveAdapter != null) {
            new ConfirmDialog(requireContext())
                    .setPositiveText("删除")
                    .setMessage("删除这条实时消息？")
                    .setOnPositiveClickListener(dialog -> {
                        liveAdapter.removeIf(t -> item.getId().equals(t.getId()));
                    })
                    .builder()
                    .show();
            return;
        }
        new ConfirmDialog(requireContext())
                .setPositiveText("确认")
                .setMessage("历史分页项请从数据源删除，这里仅演示点击。")
                .builder()
                .show();
    }
}
