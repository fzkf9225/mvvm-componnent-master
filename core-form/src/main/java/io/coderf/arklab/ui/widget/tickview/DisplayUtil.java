package io.coderf.arklab.ui.widget.tickview;

import android.content.Context;

/**
 * DisplayUtil 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2019/10/22
 */
class DisplayUtil {
    static int dp2px(Context context, float dpValue) {
        if (context == null) {
            return (int) (dpValue * 1.5f + 0.5f);
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
