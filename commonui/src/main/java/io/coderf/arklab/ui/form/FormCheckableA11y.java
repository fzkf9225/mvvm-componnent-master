package io.coderf.arklab.ui.form;

import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import java.util.function.BooleanSupplier;

/**
 * 自绘开关/勾选/单选补 TalkBack 角色与选中态。
 */
final class FormCheckableA11y {

    private FormCheckableA11y() {
    }

    static void asSwitch(@NonNull View host, @Nullable CharSequence label, @NonNull BooleanSupplier checked) {
        install(host, label, Switch.class.getName(), checked);
    }

    static void asCheckbox(@NonNull View host, @Nullable CharSequence label, @NonNull BooleanSupplier checked) {
        install(host, label, CheckBox.class.getName(), checked);
    }

    static void asRadio(@NonNull View host, @Nullable CharSequence label, @NonNull BooleanSupplier checked) {
        install(host, label, RadioButton.class.getName(), checked);
    }

    private static void install(@NonNull View host,
                                @Nullable CharSequence label,
                                @NonNull String className,
                                @NonNull BooleanSupplier checked) {
        if (label != null && host.getContentDescription() == null) {
            host.setContentDescription(label);
        }
        ViewCompat.setAccessibilityDelegate(host, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(@NonNull View v,
                                                          @NonNull AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(v, info);
                info.setClassName(className);
                info.setCheckable(true);
                info.setChecked(checked.getAsBoolean());
                info.setClickable(true);
                info.setEnabled(v.isEnabled());
            }
        });
    }
}
