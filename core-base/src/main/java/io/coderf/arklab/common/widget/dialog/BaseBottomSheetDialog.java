package io.coderf.arklab.common.widget.dialog;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * Material {@link BottomSheetDialog} 基类：提供与 {@link BaseDialog} 一致的横屏密度对齐 inflate，
 * 以及 {@link #show()} 前的 Activity 存活校验。
 * <p>
 * 业务侧底部菜单请继续用 {@link io.coderf.arklab.common.widget.dialog.BottomSheetDialog}；
 * 其它直接继承 Material BottomSheet 的弹窗可改为继承本类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/23
 */
public abstract class BaseBottomSheetDialog extends BottomSheetDialog {

    public BaseBottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    public BaseBottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
        super(context, theme);
    }

    /**
     * 横屏 inflate 前同步宿主 CustomAdapt 密度；竖屏不改密度。
     */
    @NonNull
    protected final <T> T inflateWithHostAdapt(
            @NonNull DialogHostAdaptHelper.InflateAction<T> inflate) {
        return DialogHostAdaptHelper.inflateWithHostAdapt(getContext(), inflate);
    }

    @Override
    public void show() {
        if (!BaseDialog.canShow(getContext())) {
            return;
        }
        super.show();
    }
}
