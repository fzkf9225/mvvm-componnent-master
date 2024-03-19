package com.casic.titan.commonui.tickview;

import android.content.Context;

/**
 * Created by fz on 2019/10/22.
 * describe:
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
