package pers.fz.mvvm.listener

import android.view.View
import androidx.fragment.app.Fragment
import pers.fz.mvvm.bean.HomeMenuBean

/**
 * created by fz on 2025/4/28 10:28
 * describe:
 */
interface OnMenuClickListener {
    fun onMenuClick(v :View?,fragment: Fragment?, menuBean: HomeMenuBean?)
    fun onMenuLongClick(v :View?,fragment: Fragment?, menuBean: HomeMenuBean?)
}