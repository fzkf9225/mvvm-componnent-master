package io.coderf.arklab.common.listener

import android.view.View
import androidx.fragment.app.Fragment
import io.coderf.arklab.common.bean.GridMenuBean

/**
 * OnMenuClickListener 接口。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/4/28 10:28
 */
interface OnMenuClickListener {
    fun onMenuClick(v: View?, fragment: Fragment?, menuBean: GridMenuBean?)
    fun onMenuLongClick(v: View?, fragment: Fragment?, menuBean: GridMenuBean?)
}
