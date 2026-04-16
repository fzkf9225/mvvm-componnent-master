package io.coderf.arklab.demo.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.common.utils.log.LogUtil
import io.coderf.arklab.common.utils.permission.PermissionManager
import io.coderf.arklab.common.viewmodel.EmptyViewModel
import io.coderf.arklab.common.widget.dialog.ProtectionGuidelinesDialog
import io.coderf.arklab.demo.R
import io.coderf.arklab.demo.databinding.ActivityLaunchBinding
import io.coderf.arklab.usercomponent.api.UserAccountHelper
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.java
import kotlin.text.indexOf

/**
 * created by fz on 2026/2/25 10:35
 * describe:启动页
 */
@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class LaunchActivity : BaseActivity<EmptyViewModel, ActivityLaunchBinding>() {
    private var disposable: Disposable? = null

    private val countDown = 2

    private var permissionManager: PermissionManager? = null

    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private var splashScreen: SplashScreen? = null

    override fun enableImmersionBar(): Boolean {
        return true
    }

    /**
     * 控制是否保持启动页面的变量，初始值为true，保持SplashScreen显示直到我们的内容准备好
     */
    private val keepOnScreenCondition = AtomicBoolean(true)

    override fun setTitleBar(): String? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须在super.onCreate之前安装SplashScreen
        splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 设置保持条件，当keepOnScreenCondition为true时保持SplashScreen显示
        splashScreen?.setKeepOnScreenCondition {
            keepOnScreenCondition.get()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_launch
    }

    override fun hasToolBar(): Boolean {
        return false
    }

    override fun initView(savedInstanceState: Bundle?) {
        // 加载背景图
        Glide.with(this)
            .load(R.mipmap.launcher_image)
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .into(object : com.bumptech.glide.request.target.CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    binding.clLauncher.background = resource

                    // 背景图加载完成后，检查是否可以继续
                    checkAndProceed()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    binding.clLauncher.background = placeholder

                    // 即使加载失败也继续
                    checkAndProceed()
                }
            })

        permissionManager = PermissionManager(this)
        permissionManager?.setOnDeniedCallback { _: MutableMap<String?, Boolean?>? ->
            showToast("拒绝权限可能会导致应用软件运行异常")
            checkAndProceed()
        }

        permissionManager?.setOnGrantedCallback { _: MutableMap<String?, Boolean?>? ->
            checkAndProceed()
        }
    }

    override fun initData(bundle: Bundle?) {
        // 检查用户协议同意状态
        if (UserAccountHelper.isAgree()) {
            checkPermissionsAndProceed()
        } else {
            showUserAgreementDialog()
        }
    }

    /**
     * 检查权限并决定是否继续
     */
    private fun checkPermissionsAndProceed() {
        if (permissionManager?.lacksPermissions(permissions) == true) {
            // 缺少权限，请求权限
            permissionManager?.request(permissions)
        } else {
            // 已有权限，继续
            checkAndProceed()
        }
    }

    /**
     * 检查是否可以继续进入应用
     */
    private fun checkAndProceed() {
        // 这里可以添加其他检查条件
        // 如果所有条件都满足，则允许继续
        if (allConditionsMet()) {
            // 允许SplashScreen消失，显示我们的自定义布局
            keepOnScreenCondition.set(false)
            // 开始倒计时
            startCountDown()
        } else {
            keepOnScreenCondition.set(false)
            // 检查用户协议同意状态
            if (UserAccountHelper.isAgree()) {
                checkPermissionsAndProceed()
            } else {
                showUserAgreementDialog()
            }
        }
    }

    /**
     * 检查所有条件是否满足
     */
    private fun allConditionsMet(): Boolean {
        return UserAccountHelper.isAgree() && permissionManager?.lacksPermissions(permissions) == false
    }

    private fun showUserAgreementDialog() {
        ProtectionGuidelinesDialog(this)
            .setCanOutSide(false)
            .setPositiveBackgroundColor(
                ContextCompat.getColor(
                    this,
                    io.coderf.arklab.common.R.color.themeColor
                )
            )
            .setSpannableContent(getSpannableContent())
            .setOnNegativeClickListener { _: Dialog? ->
                showToast("拒绝可能会导致部分功能使用异常")
                checkPermissionsAndProceed()
            }
            .setOnPositiveClickListener { _: Dialog? ->
                UserAccountHelper.setAgreement(true)
                checkPermissionsAndProceed()
            }
            .builder()
            .show()
    }

    private fun startCountDown() {
        if (disposable != null) {
            disposable?.dispose()
        }

        disposable = Observable.interval(1, TimeUnit.SECONDS)
            .map<Long> { aLong: Long -> countDown - aLong }
            .take((countDown + 1).toLong())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { aLong: Long ->
                    if (aLong == 0L) {
                        navigateToNextActivity()
                    }
                },
                { throwable: Throwable ->
                    LogUtil.show(TAG, "倒计时异常：$throwable")
                    navigateToNextActivity()
                }
            )
    }

    /**
     * 跳转到下一个Activity
     */
    private fun navigateToNextActivity() {
        startActivity(MainActivity::class.java)
        finish()
    }

    private fun getSpannableContent(): SpannableString {
        val content =
            "为了保护您的个人信息权益，我们将遵循合法、正当、必要和诚信原则，按照《用户信息保护指引》和《隐私保护协议》收集、使用您的信息。我们将收集相机权限、设备信息、位置权限、存储权限等，用于提供个性化内容推荐、本地文件存储、拍照、录音、录像、推送等功能。您可以在【系统设置】中管理、修改权限，拒绝提供仅会影响相关功能，相关功能可能会无法正常使用，但不会影响其他功能的使用。请仔细阅读相关协议内容了解详细信息，您也可以在【我的】右上角齿轮【设置】中找到相关协议详细信息。如您同意，请点击\"同意并继续\"开始使用我们的服务。"
        val spannableString = SpannableString(content)

        // 找到关键文本的位置
        val userAgreementStart = content.indexOf("《用户信息保护指引》")
        val userAgreementEnd = userAgreementStart + "《用户信息保护指引》".length

        val privacyPolicyStart = content.indexOf("《隐私保护协议》")
        val privacyPolicyEnd = privacyPolicyStart + "《隐私保护协议》".length

        // 设置用户协议点击
        val userAgreementSpan: ClickableSpan = object : android.text.style.ClickableSpan() {
            override fun onClick(widget: View) {
                showToast("用户协议")
                // 这里可以跳转到用户协议页面
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.setColor(
                    ContextCompat.getColor(
                        this@LaunchActivity,
                        io.coderf.arklab.common.R.color.themeColor
                    )
                )
                ds.isUnderlineText = false // 无下划线
            }
        }

        // 设置隐私保护政策点击
        val privacyPolicySpan: ClickableSpan = object : android.text.style.ClickableSpan() {
            override fun onClick(widget: View) {
                showToast("隐私保护协议")
                // 这里可以跳转到隐私协议页面
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.setColor(
                    ContextCompat.getColor(
                        this@LaunchActivity,
                        io.coderf.arklab.common.R.color.themeColor
                    )
                )
                ds.isUnderlineText = false // 无下划线
            }
        }

        // 应用点击范围
        spannableString.setSpan(
            userAgreementSpan,
            userAgreementStart,
            userAgreementEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            privacyPolicySpan,
            privacyPolicyStart,
            privacyPolicyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
    }
}