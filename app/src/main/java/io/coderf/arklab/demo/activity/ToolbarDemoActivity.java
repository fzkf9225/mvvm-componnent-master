package io.coderf.arklab.demo.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.adapter.ToolbarDemoAdapter;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityToolbarDemoBinding;
import io.coderf.arklab.demo.enums.ToolbarDemoStyle;

/**
 * Toolbar 配置演示目录：ToolbarConfig 预设、页内 TitleBar、搜索标题栏。
 */
@AndroidEntryPoint
public class ToolbarDemoActivity extends BaseActivity<EmptyViewModel, ActivityToolbarDemoBinding>
        implements BaseRecyclerViewAdapter.OnItemClickListener {

    private ToolbarDemoAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_toolbar_demo;
    }

    @Override
    public String setTitleBar() {
        return "Toolbar 配置";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        adapter = new ToolbarDemoAdapter();
        adapter.setList(buildEntries());
        adapter.setOnItemClickListener(this);
        binding.demoList.setAdapter(adapter);
    }

    @Override
    public void initData(Bundle bundle) {
        if (bundle == null || toolbarBind == null) {
            return;
        }
        UseCase useCase = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? bundle.getParcelable("args", UseCase.class)
                : bundle.getParcelable("args");
        if (useCase != null) {
            toolbarBind.getToolbarConfig().setTitle(useCase.getName());
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        ToolbarDemoAdapter.Entry entry = adapter.getItem(position);
        Intent intent = new Intent(this, entry.target);
        if (entry.style != null) {
            intent.putExtra(ToolbarPreviewActivity.EXTRA_STYLE, entry.style);
        }
        startActivity(intent);
    }

    private static List<ToolbarDemoAdapter.Entry> buildEntries() {
        List<ToolbarDemoAdapter.Entry> entries = new ArrayList<>();
        for (ToolbarDemoStyle style : ToolbarDemoStyle.values()) {
            entries.add(new ToolbarDemoAdapter.Entry(
                    style.listName,
                    style.summary,
                    ToolbarPreviewActivity.class,
                    style.name()));
        }
        entries.add(new ToolbarDemoAdapter.Entry(
                "页内 TitleBar",
                "XML 属性与 setTitle / setBackIcon / setRightText / bind",
                ToolbarTitleBarDemoActivity.class,
                null));
        entries.add(new ToolbarDemoAdapter.Entry(
                "搜索标题栏",
                "BaseSearchTitleActivity：hint、返回图标、搜索按钮",
                ToolbarSearchDemoActivity.class,
                null));
        return entries;
    }
}
