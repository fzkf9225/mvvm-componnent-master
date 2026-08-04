package io.coderf.arklab.common.navigator

import androidx.navigation.NavHostController
import androidx.navigation.fragment.NavHostFragment

/**
 * 在设置导航图之前注册 [StateNavigator]，供 XML 中 `<state_fragment>` 使用。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/4 10:20
 */
class StateNavHostFragment : NavHostFragment() {

    override fun onCreateNavHostController(navHostController: NavHostController) {
        super.onCreateNavHostController(navHostController)
        navHostController.navigatorProvider.addNavigator(
            StateNavigator(
                requireContext(),
                childFragmentManager,
                id
            )
        )
    }
}
