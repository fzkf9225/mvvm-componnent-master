package io.coderf.arklab.common.widget.feedback;

import com.google.android.material.badge.ExperimentalBadgeUtils;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;

import io.coderf.arklab.common.utils.theme.ThemeAttrs;

/**
 * Tab / 底栏角标。宿主需在 layout 完成后调用，并自行在 detach 时 {@link BadgeUtils#detachBadgeDrawable}。
 */
@ExperimentalBadgeUtils
public final class BadgeHelper {

    private BadgeHelper() {
    }

    @NonNull
    public static BadgeDrawable attach(@NonNull View anchor, int number) {
        BadgeDrawable badge = BadgeDrawable.create(anchor.getContext());
        badge.setBackgroundColor(ThemeAttrs.error(anchor.getContext()));
        badge.setBadgeTextColor(ThemeAttrs.onPrimary(anchor.getContext()));
        if (number > 0) {
            badge.setNumber(number);
            badge.setVisible(true);
        } else {
            badge.clearNumber();
            badge.setVisible(false);
        }
        anchor.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                BadgeUtils.attachBadgeDrawable(badge, v);
            }
        });
        if (anchor.isLaidOut()) {
            BadgeUtils.attachBadgeDrawable(badge, anchor);
        }
        return badge;
    }

    public static void update(@Nullable BadgeDrawable badge, int number) {
        if (badge == null) {
            return;
        }
        if (number > 0) {
            badge.setNumber(number);
            badge.setVisible(true);
        } else {
            badge.clearNumber();
            badge.setVisible(false);
        }
    }
}
