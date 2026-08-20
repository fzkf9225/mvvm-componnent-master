package io.coderf.arklab.common.listener

import android.view.View
import androidx.fragment.app.Fragment
import io.coderf.arklab.common.bean.GridMenuBean

/**
 * created by fz on 2025/4/28 10:28
 * describe:
 */
interface OnMenuClickListener {
    fun onMenuClick(v: View?, fragment: Fragment?, menuBean: GridMenuBean?)
    fun onMenuLongClick(v: View?, fragment: Fragment?, menuBean: GridMenuBean?)
}
