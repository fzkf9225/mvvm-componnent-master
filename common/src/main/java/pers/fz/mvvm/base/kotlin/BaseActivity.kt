package pers.fz.mvvm.base.kotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.gyf.immersionbar.ImmersionBar
import pers.fz.mvvm.R
import pers.fz.mvvm.api.AppManager
import pers.fz.mvvm.base.BaseModelEntity
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.bean.base.ToolbarConfig
import pers.fz.mvvm.databinding.BaseActivityBinding
import pers.fz.mvvm.inter.ErrorService
import pers.fz.mvvm.util.apiUtil.StringUtil
import pers.fz.mvvm.util.log.ToastUtils
import pers.fz.mvvm.util.permission.PermissionsChecker
import pers.fz.mvvm.util.theme.ThemeUtils
import pers.fz.mvvm.wight.dialog.CustomProgressDialog
import pers.fz.mvvm.wight.dialog.LoginDialog.OnLoginClickListener
import java.lang.reflect.ParameterizedType
import javax.inject.Inject

/**
 * Create by CherishTang on 2019/8/1
 * describe:BaseActivity封装
 */
abstract class BaseActivity<VM : BaseViewModel<V>?, VDB : ViewDataBinding?, V : BaseView> :
    AppCompatActivity(),
    BaseView, OnLoginClickListener {
    protected var TAG = this.javaClass.simpleName
    protected var mViewModel: VM? = null
    protected var binding: VDB? = null
    protected var toolbarBind: BaseActivityBinding? = null

    // 权限检测器
    private var mPermissionsChecker: PermissionsChecker? = null
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    protected var loginLauncher: ActivityResultLauncher<Intent>? = null

    @Inject
    lateinit  var errorService: ErrorService

    protected abstract val layoutId: Int
    override fun onCreate(savedInstanceState: Bundle?) {
        loginLauncher =
            registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    onLoginSuccessCallback(if (result.data == null) null else result.data!!.extras)
                } else {
                    onLoginFailCallback(
                        result.resultCode, if (result.data == null) null else result.data!!
                            .extras
                    )
                }
            }
        super.onCreate(savedInstanceState)
        AppManager.getAppManager().addActivity(this)
        if (hasToolBar()) {
            toolbarBind = DataBindingUtil.setContentView(this, R.layout.base_activity)
            toolbarBind?.setContext(this)
            toolbarBind?.setToolbarConfig(setToolbarStyle())
            toolbarBind?.setLifecycleOwner(this)
        } else {
            binding = DataBindingUtil.setContentView(this, layoutId)
            binding!!.lifecycleOwner = this
        }
        initImmersionBar()
        createViewModel()
        initView(savedInstanceState)
        initData(if (intent == null || intent.extras == null) Bundle() else intent.extras)
    }

    private val defaultWidth: Float
        /**
         * 屏幕适配尺寸，很多人把基准写在AndroidManifest中，但是我选择直接写BaseActivity中，是为了更好的支持各个Activity自愈更改
         *
         * @return 默认360dp
         */
        private get() {
            try {
                val info = packageManager
                    .getApplicationInfo(
                        packageName,
                        PackageManager.GET_META_DATA
                    )
                return info.metaData.getInt("design_width_in_dp", 360).toFloat()
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return 360f
        }
    private val defaultHeight: Float
        /**
         * 屏幕适配尺寸，很多人把基准写在AndroidManifest中，但是我选择直接写BaseActivity中，是为了更好的支持各个Activity自愈更改
         *
         * @return 默认360dp
         */
        private get() {
            try {
                val info = packageManager
                    .getApplicationInfo(
                        packageName,
                        PackageManager.GET_META_DATA
                    )
                return info.metaData.getInt("design_height_in_dp", 640).toFloat()
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return 640f
        }

    override fun setContentView(layoutResId: Int) {
        super.setContentView(layoutResId)
        if (hasToolBar()) {
            super.setContentView(R.layout.base_activity)
            val container = findViewById<FrameLayout>(R.id.container)
            binding = DataBindingUtil.inflate(LayoutInflater.from(this), layoutId, container, true)
            (findViewById<View>(R.id.main_bar) as Toolbar).setNavigationOnClickListener { v: View? -> finish() }
            binding!!.lifecycleOwner = this
        }
    }

    val toolbar: Toolbar
        get() = toolbarBind!!.mainBar

    /**
     * 设置toolbar默认样式
     *
     * @return toolbar配置
     */
    fun setToolbarStyle(): ToolbarConfig {
        return ToolbarConfig.Builder(this).setTitle(setTitleBar()).setBgColor(R.color.white).build()
    }

    /**
     * 初始化沉浸式
     * Init immersion bar.
     */
    protected fun initImmersionBar() {
        //设置共同沉浸式样式
        if (toolbarBind == null) {
            ImmersionBar.with(this).init()
        } else {
            ImmersionBar.with(this).titleBar(toolbarBind!!.appBar).init()
        }
    }

    /**
     * 创建viewModel
     */
    @Suppress("UNCHECKED_CAST")
    fun createViewModel() {
        if (mViewModel == null) {
            mViewModel = ViewModelProvider(this).get(getVmClazz(this))
            mViewModel?.baseView = this as V
        }
    }

    /**
     * 获取当前类绑定的泛型ViewModel-clazz
     */
    @Suppress("UNCHECKED_CAST")
    fun <VM> getVmClazz(obj: Any): VM {
        return (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as VM
    }

    /**
     * 给toolbar添加菜单
     *
     * @param menuRes
     * @param listener
     */
    protected fun addMenu(@MenuRes menuRes: Int, listener: Toolbar.OnMenuItemClickListener?) {
        if (toolbarBind == null) {
            return
        }
        toolbarBind!!.mainBar.inflateMenu(menuRes)
        toolbarBind!!.mainBar.setOnMenuItemClickListener(listener)
    }

    fun lacksPermissions(vararg permission: String?): Boolean {
        return permissionsChecker.lacksPermissions(*permission)
    }

    private val permissionsChecker: PermissionsChecker
        private get() {
            if (mPermissionsChecker == null) {
                mPermissionsChecker = PermissionsChecker(this)
            }
            return mPermissionsChecker!!
        }

    /**
     * 设置toolbar背景色和状态栏的颜色，兼容华为小米手机
     *
     * @param color
     */
    fun setThemeBarAndToolBarColor(@ColorRes color: Int) {
        try {
            if (toolbarBind == null) {
                return
            }
            if (toolbarBind!!.toolbarConfig == null) {
                return
            }
            toolbarBind!!.toolbarConfig?.builder?.setBgColor(color)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置toolbar背景色和状态栏的颜色，兼容华为小米手机
     *
     * @param color
     */
    fun setThemeBarAndToolBarColor(@ColorRes color: Int, @DrawableRes backRes: Int) {
        try {
            ThemeUtils.setStatusBarLightMode(this, ContextCompat.getColor(this, color))
            if (toolbarBind == null) {
                return
            }
            if (toolbarBind!!.toolbarConfig == null) {
                return
            }
            toolbarBind!!.toolbarConfig?.builder?.setBgColor(color)?.setBackIconRes(backRes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 权限请求
     *
     * @param permissions 权限
     */
    fun requestPermission(permissions: Array<String>) {
        // 缺少权限时, 进入权限配置页面
        permissionLauncher!!.launch(permissions)
    }

    /**
     * 注册权限请求监听
     */
    protected fun registerPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            run {
                val allPermissionsGranted = permissions.values.all { it }
                if (allPermissionsGranted) {
                    onPermissionGranted(permissions)
                } else {
                    onPermissionRefused(permissions)
                }
            }
        }
    }

    protected fun unregisterPermission() {
        if (permissionLauncher != null) {
            permissionLauncher!!.unregister()
        }
    }

    protected fun onLoginSuccessCallback(bundle: Bundle?) {}
    protected fun onLoginFailCallback(resultCode: Int, bundle: Bundle?) {}

    /**
     * 权限同意
     */
    protected fun onPermissionGranted(permissions: Map<String, Boolean>?) {}

    /**
     * 权限拒绝
     */
    protected fun onPermissionRefused(permissions: Map<String, Boolean>?) {
        showToast("拒绝权限可能会导致应用软件运行异常!")
    }

    var lastClick: Long = 0

    /**
     * [防止快速点击]
     *
     * @return false --> 快读点击
     */
    fun fastClick(intervalTime: Long): Boolean {
        if (System.currentTimeMillis() - lastClick <= intervalTime) {
            return true
        }
        lastClick = System.currentTimeMillis()
        return false
    }

    protected fun hasToolBar(): Boolean {
        return true
    }

    abstract fun setTitleBar(): String?

    /**
     * 初始化布局
     */
    abstract fun initView(savedInstanceState: Bundle?)

    /**
     * 设置数据
     */
    abstract fun initData(bundle: Bundle?)
    override fun onDestroy() {
        super.onDestroy()
        AppManager.getAppManager().finishActivity(this)
        if (loginLauncher != null) {
            loginLauncher!!.unregister()
        }
    }

    override fun onLoginClick(v: View, code: Int) {
        if (errorService == null) {
            return
        }
        errorService!!.toLogin(this, loginLauncher)
    }

    private fun closeLoadingDialog() {
        CustomProgressDialog.getInstance(this).hide()
    }

    private fun showLoadingDialog(dialogMessage: String, isCanCancel: Boolean) {
        CustomProgressDialog.getInstance(this)
            .setCanCancel(isCanCancel)
            .setMessage(dialogMessage)
            .builder()
            .show()
    }

    override fun showLoading(dialogMessage: String) {
        runOnUiThread { showLoadingDialog(dialogMessage, false) }
    }

    override fun refreshLoading(dialogMessage: String) {
        runOnUiThread {
            CustomProgressDialog.getInstance(this)
                .refreshMessage(dialogMessage)
        }
    }

    override fun hideLoading() {
        runOnUiThread { closeLoadingDialog() }
    }

    override fun showToast(msg: String) {
        runOnUiThread {
            if (StringUtil.isEmpty(msg)) {
                return@runOnUiThread
            }
            ToastUtils.showShort(this, msg)
        }
    }

    /**
     * 注意判断空，根据自己需求更改
     *
     * @param model 错误吗实体
     */
    override fun onErrorCode(model: BaseModelEntity<*>?) {
        if (errorService == null || model == null) {
            return
        }
        if (!errorService!!.isLogin(model.code)) {
            errorService!!.toLogin(this, loginLauncher)
            return
        }
        if (!errorService!!.hasPermission(model.code)) {
            errorService!!.toNoPermission(this)
        }
    }

    @JvmOverloads
    fun startActivity(toClx: Class<*>?, bundle: Bundle? = null) {
        val intent = Intent(this, toClx)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        startActivity(intent)
    }

    @JvmOverloads
    fun startForResult(
        activityResultLauncher: ActivityResultLauncher<Intent?>,
        toClx: Class<*>?,
        bundle: Bundle? = null
    ) {
        val intent = Intent(this, toClx)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        activityResultLauncher.launch(intent)
    }

    fun startForResult(activityResultLauncher: ActivityResultLauncher<Intent?>, intent: Intent?) {
        activityResultLauncher.launch(intent)
    }
}