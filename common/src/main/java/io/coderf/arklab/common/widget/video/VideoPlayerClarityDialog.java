package io.coderf.arklab.common.widget.video;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;

import io.coderf.arklab.common.R;

/**
 * 清晰度选择弹窗。
 */
public class VideoPlayerClarityDialog extends Dialog {

    public VideoPlayerClarityDialog(@NonNull Context context,
                                    @NonNull List<VideoPlayerClarityOption> options,
                                    int selectedIndex,
                                    @NonNull OnClaritySelectedListener listener) {
        super(context);
        setContentView(R.layout.dialog_video_clarity);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);
            window.setLayout(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.82f),
                WindowManager.LayoutParams.WRAP_CONTENT);
        }
        buildOptions(options, selectedIndex, listener);
    }

    private void buildOptions(@NonNull List<VideoPlayerClarityOption> options,
                              int selectedIndex,
                              @NonNull OnClaritySelectedListener listener) {
        LinearLayout container = findViewById(R.id.clarity_option_container);
        if (container == null) {
            return;
        }
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < options.size(); i++) {
            VideoPlayerClarityOption option = options.get(i);
            View item = inflater.inflate(R.layout.item_video_clarity_option, container, false);
            TextView nameView = item.findViewById(R.id.clarity_name);
            TextView descView = item.findViewById(R.id.clarity_desc);
            View indicator = item.findViewById(R.id.clarity_indicator);
            nameView.setText(option.name());
            if (option.description().isEmpty()) {
                descView.setVisibility(View.GONE);
            } else {
                descView.setVisibility(View.VISIBLE);
                descView.setText(option.description());
            }
            boolean selected = i == selectedIndex;
            item.setBackgroundResource(selected
                ? R.drawable.bg_video_clarity_item_selected
                : R.drawable.bg_video_clarity_item);
            nameView.setTextColor(ContextCompat.getColor(getContext(),
                selected ? android.R.color.white : android.R.color.white));
            if (indicator != null) {
                indicator.setVisibility(selected ? View.VISIBLE : View.GONE);
            }
            int index = i;
            item.setOnClickListener(v -> {
                listener.onClaritySelected(index, option);
                dismiss();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                lp.topMargin = dp(8);
            }
            container.addView(item, lp);
        }
    }

    private int dp(float value) {
        return (int) (value * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    public interface OnClaritySelectedListener {
        void onClaritySelected(int index, @NonNull VideoPlayerClarityOption option);
    }
}
