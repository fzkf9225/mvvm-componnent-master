package com.casic.otitan.common.listener

import android.view.View
import androidx.fragment.app.Fragment
import com.casic.otitan.common.bean.HomeMenuBean

/**
 * created by fz on 2025/4/28 10:28
 * describe:
 */
interface OnMenuClickListener {
    fun onMenuClick(v :View?,fragment: Fragment?, menuBean: HomeMenuBean?)
    fun onMenuLongClick(v :View?,fragment: Fragment?, menuBean: HomeMenuBean?)
}