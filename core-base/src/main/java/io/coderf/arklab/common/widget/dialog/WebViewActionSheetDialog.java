package io.coderf.arklab.common.widget.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.webview.WebViewMenuAction;

/**
 * WebView 底部操作面板（微信风格：来源提示 + 横向图标菜单 + 取消）。
 */
public class WebViewActionSheetDialog extends com.google.android.material.bottomsheet.BottomSheetDialog {

    public interface OnActionClickListener {
        void onActionClick(@NonNull WebViewMenuAction action);
    }

    public WebViewActionSheetDialog(
            @NonNull Context context,
            @NonNull String providerHint,
            @NonNull List<WebViewMenuAction> actions,
            @NonNull OnActionClickListener listener
    ) {
        super(context);
        init(providerHint, actions, listener);
    }

    private void init(
            @NonNull String providerHint,
            @NonNull List<WebViewMenuAction> actions,
            @NonNull OnActionClickListener listener
    ) {
        View content = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_webview_action_sheet, null, false);
        View tvProviderHint = content.findViewById(R.id.tvProviderHint);
        RecyclerView recyclerView = content.findViewById(R.id.rvWebViewActions);
        View btnCancel = content.findViewById(R.id.btnCancel);

        if (tvProviderHint instanceof android.widget.TextView) {
            ((android.widget.TextView) tvProviderHint).setText(providerHint);
        }

        WebViewActionSheetAdapter adapter = new WebViewActionSheetAdapter(actions, action -> {
            listener.onActionClick(action);
            dismiss();
        });
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerView.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dismiss());

        setContentView(content);
        setCanceledOnTouchOutside(true);
        setCancelable(true);

        setOnShowListener(dialog -> {
            View bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet == null) {
                return;
            }
            bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
        });

        if (getWindow() != null) {
            getWindow().setDimAmount(0.45f);
        }
    }

    private static final class WebViewActionSheetAdapter extends RecyclerView.Adapter<WebViewActionSheetAdapter.Holder> {

        private final List<WebViewMenuAction> actions;
        private final OnActionClickListener listener;

        WebViewActionSheetAdapter(
                @NonNull List<WebViewMenuAction> actions,
                @NonNull OnActionClickListener listener
        ) {
            this.actions = actions;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_webview_action_sheet, parent, false);
            return new Holder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            WebViewMenuAction action = actions.get(position);
            holder.iconView.setImageResource(action.getIconRes());
            holder.titleView.setText(action.getTitle());
            holder.itemView.setOnClickListener(v -> listener.onActionClick(action));
        }

        @Override
        public int getItemCount() {
            return actions.size();
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final android.widget.ImageView iconView;
            final android.widget.TextView titleView;

            Holder(@NonNull View itemView) {
                super(itemView);
                iconView = itemView.findViewById(R.id.ivActionIcon);
                titleView = itemView.findViewById(R.id.tvActionTitle);
            }
        }
    }
}
