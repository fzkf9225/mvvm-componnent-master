package io.coderf.arklab.demo.adapter;

import androidx.annotation.Nullable;

import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ToolbarDemoItemBinding;

/**
 * Toolbar 演示目录。
 */
public class ToolbarDemoAdapter extends BaseRecyclerViewAdapter<ToolbarDemoAdapter.Entry, ToolbarDemoItemBinding> {

    @Override
    public void onBindHolder(BaseViewHolder<ToolbarDemoItemBinding> holder, int pos) {
        Entry item = mList.get(pos);
        holder.getBinding().tvName.setText(item.name);
        holder.getBinding().tvDescribe.setText(item.describe);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.toolbar_demo_item;
    }

    public static final class Entry {
        public final String name;
        public final String describe;
        public final Class<?> target;
        @Nullable
        public final String style;

        public Entry(String name, String describe, Class<?> target, @Nullable String style) {
            this.name = name;
            this.describe = describe;
            this.target = target;
            this.style = style;
        }
    }
}
