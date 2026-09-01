package io.coderf.arklab.common.listener;

import android.view.View;

/**
 * 头部局点击事件
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/6/12 10:06
 */
public interface OnHeaderViewClickListener {

    void onHeaderViewClick(View view);

    void onHeaderViewLongClick(View view);
}
