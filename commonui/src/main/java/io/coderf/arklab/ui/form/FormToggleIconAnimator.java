package io.coderf.arklab.ui.form;

import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * CheckBox / Radio 图标选中切换动画。
 */
final class FormToggleIconAnimator {

    private static final long CHECKED_DURATION_MS = 180L;
    private static final long UNCHECKED_DURATION_MS = 120L;

    private FormToggleIconAnimator() {
    }

    static void applyIcon(@NonNull AppCompatImageView iconView, @NonNull Drawable drawable) {
        iconView.animate().cancel();
        iconView.setScaleX(1f);
        iconView.setScaleY(1f);
        iconView.setAlpha(1f);
        iconView.setImageDrawable(drawable);
    }

    static void animateToggle(@NonNull AppCompatImageView iconView,
                              @NonNull Drawable drawable,
                              boolean selected) {
        iconView.animate().cancel();
        iconView.setImageDrawable(drawable);
        if (selected) {
            iconView.setScaleX(0.72f);
            iconView.setScaleY(0.72f);
            iconView.setAlpha(0.55f);
            iconView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(CHECKED_DURATION_MS)
                    .setInterpolator(new OvershootInterpolator(1.6f))
                    .start();
        } else {
            iconView.setScaleX(1.08f);
            iconView.setScaleY(1.08f);
            iconView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(UNCHECKED_DURATION_MS)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }
}
