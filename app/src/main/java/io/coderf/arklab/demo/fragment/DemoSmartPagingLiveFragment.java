package io.coderf.arklab.demo.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseSmartPagingFragment;
import io.coderf.arklab.common.widget.dialog.ConfirmDialog;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.adapter.PagingLocalChatAdapter;
import io.coderf.arklab.demo.bean.LocalChatMessage;
import io.coderf.arklab.demo.databinding.DemoSmartPagingReverseBinding;
import io.coderf.arklab.demo.viewmodel.DemoLocalChatPagingViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import kotlin.Unit;

/**
 * Room + Paging3 本地发消息演示：历史分页 + reverse 贴底；发送 = insert + refresh。
 * <p>
 * 布局：{@link R.layout#demo_smart_paging_reverse}。
 */
@AndroidEntryPoint
public class DemoSmartPagingLiveFragment
        extends BaseSmartPagingFragment<DemoLocalChatPagingViewModel, DemoSmartPagingReverseBinding, LocalChatMessage>
        implements OnLoadMoreListener {

    private boolean pendingScrollToNewest;

    @Override
    protected int getLayoutId() {
        return R.layout.demo_smart_paging_reverse;
    }

    @Override
    protected BasePagingAdapter<LocalChatMessage, ?> getRecyclerAdapter() {
        return new PagingLocalChatAdapter();
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        // 仅 reverse：数据 index0=最新 → 视觉底部
        lm.setReverseLayout(true);
        return lm;
    }

    @Override
    protected boolean hideRecycleViewDivider() {
        return true;
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
                if (pendingScrollToNewest) {
                    pendingScrollToNewest = false;
                    scrollToNewest();
                }
            } else if (states.getRefresh() instanceof LoadState.Error) {
                refreshLayout.finishLoadMore(false);
                pendingScrollToNewest = false;
            }
            return Unit.INSTANCE;
        });
    }

    @Override
    protected void initData(Bundle bundle) {
        super.initData(bundle);
        mViewModel.getItems().observe(this, observer);
        mViewModel.ensureSeedHistory(
                () -> {
                    mViewModel.refreshData();
                    adapter.refresh();
                },
                e -> showToast("初始化本地消息失败")
        );
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

    /**
     * 本地发送：Room insert → refreshData → adapter.refresh → 贴底。
     */
    public void sendLocalMessage(@NonNull String text) {
        mViewModel.sendMessage(text, () -> {
            pendingScrollToNewest = true;
            adapter.refresh();
        }, e -> showToast("发送失败"));
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        // Demo：底部上拉 = 从 Room 重新拉分页（本地数据不会丢）
        mViewModel.refreshData();
        adapter.refresh();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        onLoadMore(refreshLayout);
    }

    @Override
    public void onItemClick(View view, LocalChatMessage item, int position) {
        super.onItemClick(view, item, position);
        String side = item.isMine() ? "我" : "对方";
        showToast(side + "：" + item.getContent());
    }

    @Override
    public void onItemLongClick(View view, LocalChatMessage item, int position) {
        super.onItemLongClick(view, item, position);
        new ConfirmDialog(requireContext())
                .setPositiveText("删除")
                .setMessage("从本地 Room 删除这条消息？")
                .setOnPositiveClickListener(dialog -> {
                    Disposable ignored = mViewModel.getIRepository().delete(item, false)
                            .subscribe(() -> {
                                mViewModel.refreshData();
                                adapter.refresh();
                                showToast("已删除");
                            }, e -> showToast("删除失败"));
                })
                .builder()
                .show();
    }
}
