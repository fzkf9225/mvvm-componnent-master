package io.coderf.arklab.common.navigator

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import io.coderf.arklab.common.utils.log.LogUtil

/**
 * 基于 hide/show 的 Fragment Navigator，用于 BottomNavigationView 切换时保留 Fragment 实例与 View 状态。
 *
 * 导航图中需使用 `<state_fragment>`，并通过 [StateNavHostFragment] 注册。
 * BottomNav 请使用 [StateNavigationUI.setupWithNavController]，避免官方 saveState/restoreState 与本实现冲突。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/4 10:21
 */
@Navigator.Name("state_fragment")
class StateNavigator(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) : FragmentNavigator(context, fragmentManager, containerId) {

    override fun navigate(
        entries: List<NavBackStackEntry>,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ) {
        if (fragmentManager.isStateSaved) {
            LogUtil.logger(TAG, "Ignoring navigate(): FragmentManager has already saved its state")
            return
        }
        for (entry in entries) {
            navigate(entry, navOptions)
        }
    }

    private fun navigate(entry: NavBackStackEntry, navOptions: NavOptions?) {
        val destination = entry.destination as Destination
        val topEntry = state.backStack.value.lastOrNull()

        // singleTop：已在目标页则不重复 push / 事务
        if (navOptions?.shouldLaunchSingleTop() == true
            && topEntry?.destination?.id == destination.id
        ) {
            state.onLaunchSingleTop(entry)
            return
        }

        val tag = destination.id.toString()
        val transaction = fragmentManager.beginTransaction()
        attachAnimations(transaction, navOptions)

        val currentFragment = fragmentManager.primaryNavigationFragment
        if (currentFragment != null) {
            transaction.hide(currentFragment)
            transaction.setMaxLifecycle(currentFragment, Lifecycle.State.STARTED)
        }

        var fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = fragmentManager.fragmentFactory.instantiate(
                context.classLoader,
                destination.className
            )
            fragment.arguments = entry.arguments
            transaction.add(containerId, fragment, tag)
        } else {
            transaction.show(fragment)
        }
        transaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        transaction.setPrimaryNavigationFragment(fragment)
        transaction.setReorderingAllowed(true)
        transaction.commitNow()

        state.push(entry)
    }

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        if (fragmentManager.isStateSaved) {
            Log.i(TAG, "Ignoring popBackStack(): FragmentManager has already saved its state")
            return
        }

        val transaction = fragmentManager.beginTransaction()
        val currentFragment = fragmentManager.primaryNavigationFragment
        val targetFragment = fragmentManager.findFragmentByTag(popUpTo.destination.id.toString())

        // 被 pop 的 tab Fragment 只隐藏不销毁，再次切回时可复用
        if (currentFragment != null && currentFragment != targetFragment) {
            transaction.hide(currentFragment)
            transaction.setMaxLifecycle(currentFragment, Lifecycle.State.STARTED)
        }
        if (targetFragment != null) {
            transaction.show(targetFragment)
            transaction.setMaxLifecycle(targetFragment, Lifecycle.State.RESUMED)
            transaction.setPrimaryNavigationFragment(targetFragment)
        }
        transaction.setReorderingAllowed(true)
        transaction.commitNow()

        // 本 Navigator 自行保活 Fragment，不走 FragmentManager.saveBackStack
        state.pop(popUpTo, false)
    }

    private fun attachAnimations(transaction: FragmentTransaction, navOptions: NavOptions?) {
        if (navOptions == null) return
        val enter = navOptions.enterAnim
        val exit = navOptions.exitAnim
        val popEnter = navOptions.popEnterAnim
        val popExit = navOptions.popExitAnim
        if (enter != -1 || exit != -1 || popEnter != -1 || popExit != -1) {
            transaction.setCustomAnimations(
                if (enter != -1) enter else 0,
                if (exit != -1) exit else 0,
                if (popEnter != -1) popEnter else 0,
                if (popExit != -1) popExit else 0
            )
        }
    }

    private companion object {
        const val TAG = "StateNavigator"
    }
}
