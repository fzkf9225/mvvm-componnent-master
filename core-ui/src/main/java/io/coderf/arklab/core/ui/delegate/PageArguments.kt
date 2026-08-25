package io.coderf.arklab.core.ui.delegate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * 解析传给页面 [initData] 的参数来源。
 *
 * Activity 默认取 Intent extras；Fragment 默认取 [Fragment.getArguments]。
 * 深链 / 路由场景可自定义实现。
 */
fun interface PageArgumentsResolver {
    fun resolve(): Bundle
}

object PageArguments {

    /** Activity：Intent.extras，无则空 Bundle。 */
    @JvmStatic
    fun fromActivity(activity: Activity): PageArgumentsResolver = PageArgumentsResolver {
        val extras: Bundle? = activity.intent?.extras
        extras ?: Bundle()
    }

    /** 指定 Intent（如 onNewIntent 后）。 */
    @JvmStatic
    fun fromIntent(intent: Intent?): PageArgumentsResolver = PageArgumentsResolver {
        intent?.extras ?: Bundle()
    }

    /** Fragment：getArguments()，无则空 Bundle。 */
    @JvmStatic
    fun fromFragment(fragment: Fragment): PageArgumentsResolver = PageArgumentsResolver {
        fragment.arguments ?: Bundle()
    }

    /** 固定参数（测试或预构造）。 */
    @JvmStatic
    fun of(bundle: Bundle): PageArgumentsResolver = PageArgumentsResolver { Bundle(bundle) }

    /** 合并多个来源：后者覆盖前者同名 key。 */
    @JvmStatic
    fun merge(vararg resolvers: PageArgumentsResolver): PageArgumentsResolver =
        PageArgumentsResolver {
            val out = Bundle()
            for (r in resolvers) {
                out.putAll(r.resolve())
            }
            out
        }
}
