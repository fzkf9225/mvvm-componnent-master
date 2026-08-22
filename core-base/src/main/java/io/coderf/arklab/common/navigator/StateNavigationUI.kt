package io.coderf.arklab.common.navigator

import androidx.core.view.get
import androidx.core.view.size
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import com.google.android.material.navigation.NavigationBarView

/**
 * 与 [StateNavigator] 配套的 BottomNavigation / NavigationRail 绑定。
 *
 * 不使用官方 [androidx.navigation.ui.NavigationUI] 的 saveState/restoreState，
 * 避免与 hide/show 保活策略冲突；改用 popUpTo(start) + singleTop 切换 tab。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/4 10:20
 */
object StateNavigationUI {

    /**
     * @return 是否成功绑定（始终为 true，便于 Java 调用方链式忽略返回值）
     */
    @JvmStatic
    fun setupWithNavController(
        navigationBarView: NavigationBarView,
        navController: NavController
    ) {
        navigationBarView.setOnItemSelectedListener { item ->
            if (item.itemId == navController.currentDestination?.id) {
                return@setOnItemSelectedListener true
            }
            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(navController.graph.findStartDestination().id, false)
                .build()
            try {
                navController.navigate(item.itemId, null, options)
                true
            } catch (_: IllegalArgumentException) {
                false
            }
        }
        // 重复点击当前 tab：不重建、不回顶
        navigationBarView.setOnItemReselectedListener { }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val menu = navigationBarView.menu
            for (i in 0 until menu.size) {
                val item = menu[i]
                if (matchDestination(destination, item.itemId)) {
                    item.isChecked = true
                }
            }
        }
    }

    private fun matchDestination(destination: NavDestination, destId: Int): Boolean {
        var current: NavDestination? = destination
        while (current != null) {
            if (current.id == destId) return true
            current = current.parent
        }
        return false
    }
}
