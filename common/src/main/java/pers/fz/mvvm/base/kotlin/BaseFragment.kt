package pers.fz.mvvm.base.kotlin

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import pers.fz.mvvm.base.BaseModelEntity
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.inter.ErrorService
import pers.fz.mvvm.util.log.ToastUtils
import pers.fz.mvvm.util.permission.PermissionsChecker
import pers.fz.mvvm.wight.dialog.CustomProgressDialog
import java.lang.reflect.ParameterizedType
import javax.inject.Inject

/**
 * Created by fz on 2017/11/22.
 * BaseFragment封装
 */
abstract class BaseFragment<VM : BaseViewModel<V>?, VDB : ViewDataBinding?, V : BaseView> :
    Fragment(), BaseView {
    protected var TAG = this.javaClass.simpleName

    /**
     * 权限检测器
     */
    private var mPermissionsChecker: PermissionsChecker? = null
    protected var mViewModel: VM? = null
    protected var binding: VDB? = null
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null

    @Inject
    lateinit var errorService: ErrorService
    protected var loginLauncher: ActivityResultLauncher<Intent>? = null
    fun requestPermission(permissions: Array<String>) {
        permissionLauncher!!.launch(permissions)
    }

    private val permissionsChecker: PermissionsChecker
        get() {
            if (mPermissionsChecker == null) {
                mPermissionsChecker = PermissionsChecker(requireActivity())
            }
            return mPermissionsChecker as PermissionsChecker
        }

    fun lacksPermissions(permissions: Array<String?>): Boolean {
        return permissionsChecker.lacksPermissions(*permissions)
    }

    /**
     * 注册权限请求监听
     */
    protected fun registerPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding!!.lifecycleOwner = this
        loginLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    onLoginSuccessCallback(if (result.data == null) null else result.data!!.extras)
                } else {
                    onLoginFailCallback(
                        result.resultCode, if (result.data == null) null else result.data!!
                            .extras
                    )
                }
            }
        createViewModel()
        initView(savedInstanceState)
        initData(if (arguments == null) Bundle() else arguments)
        return binding!!.root
    }

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

    protected abstract val layoutId: Int

    /**
     * 该抽象方法就是 初始化view
     */
    protected abstract fun initView(savedInstanceState: Bundle?)

    /**
     * 执行数据的加载
     */
    protected abstract fun initData(bundle: Bundle?)
    protected open fun onLoginSuccessCallback(bundle: Bundle?) {}
    protected open fun onLoginFailCallback(resultCode: Int, bundle: Bundle?) {}
    override fun onDestroy() {
        super.onDestroy()
        if (loginLauncher != null) {
            loginLauncher!!.unregister()
        }
    }

    /**
     * 显示加载弹框
     *
     * @param dialogMessage 弹框内容，如果内容为空则不展示文字部分
     */
    private fun showLoadingDialog(dialogMessage: String, isCanCancel: Boolean) {
        CustomProgressDialog.getInstance(context)
            .setCanCancel(isCanCancel)
            .setMessage(dialogMessage)
            .builder()
            .show()
    }

    override fun showLoading(dialogMessage: String) {
        requireActivity().runOnUiThread { showLoadingDialog(dialogMessage, false) }
    }

    override fun hideLoading() {
        requireActivity().runOnUiThread { CustomProgressDialog.getInstance(context).dismiss() }
    }

    override fun refreshLoading(dialogMessage: String) {
        requireActivity().runOnUiThread  {
            CustomProgressDialog.getInstance(requireActivity())
                .refreshMessage(dialogMessage)
        }
    }

    override fun showToast(msg: String) {
        requireActivity().runOnUiThread { ToastUtils.showShort(activity, msg) }
    }

    override fun onErrorCode(model: BaseModelEntity<*>?) {
        if (model == null) {
            return
        }
        if (!errorService.isLogin(model.code)) {
            errorService.toLogin(requireContext(), loginLauncher)
            return
        }
        if (!errorService.hasPermission(model.code)) {
            errorService.toNoPermission(requireContext())
        }
    }

    @JvmOverloads
    fun startActivity(toClx: Class<*>?, bundle: Bundle? = null) {
        val intent = Intent(requireContext(), toClx)
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
        val intent = Intent(requireContext(), toClx)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        activityResultLauncher.launch(intent)
    }

    fun startForResult(activityResultLauncher: ActivityResultLauncher<Intent?>, intent: Intent?) {
        activityResultLauncher.launch(intent)
    }

}