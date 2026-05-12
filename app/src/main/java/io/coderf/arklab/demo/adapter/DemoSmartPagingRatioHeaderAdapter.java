package io.coderf.arklab.demo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.coderf.arklab.demo.R;

/**
 * SmartPaging + 头布局示例：单条 16:9 区域展示图片，仅作演示，封装对任意 {@link RecyclerView.Adapter} 生效。
 */
public class DemoSmartPagingRatioHeaderAdapter extends RecyclerView.Adapter<DemoSmartPagingRatioHeaderAdapter.Holder> {

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.demo_smart_paging_header_banner, parent, false);
        return new Holder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        // 示例使用静态资源，无需绑定数据
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class Holder extends RecyclerView.ViewHolder {
        Holder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
