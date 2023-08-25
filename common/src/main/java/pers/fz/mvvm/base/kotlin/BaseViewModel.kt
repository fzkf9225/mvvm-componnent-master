package pers.fz.mvvm.base.kotlin

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonParseException
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONException
import pers.fz.mvvm.api.ApiRetrofit
import pers.fz.mvvm.api.ConstantsHelper
import pers.fz.mvvm.api.RetryWhenNetworkException
import pers.fz.mvvm.base.BaseException
import pers.fz.mvvm.base.BaseModelEntity
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.inter.RetryService
import pers.fz.mvvm.util.log.LogUtil
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.text.ParseException
import javax.inject.Inject

/**
 * Created by fz on 2023/8/25 13:40
 * describe :
 */
abstract class BaseViewModel<V : BaseView?>(application: Application) : AndroidViewModel(application) {
    protected val TAG = this.javaClass.simpleName

    //离开页面，是否取消网络
    private var compositeDisposable: CompositeDisposable? = null

    /**
     * 如果开启，同一url还在请求网络时，不会
     */
    var onNetTags: ArrayList<String>? = null
    private var dialogMessage = "正在加载，请稍后..."
    @JvmField
    public var baseView: V? = null

    @JvmField
    @Inject
    var retryService: RetryService? = null

    fun setBaseView(baseView :V){
        this.baseView = baseView
    }

    override fun onCleared() {
        super.onCleared()
        removeDisposable()
        baseView = null
    }

    fun setDialogMessage(dialogMessage: String) {
        this.dialogMessage = dialogMessage
    }

    private fun addDisposable(disposable: Disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable!!.add(disposable)
    }

    private fun removeDisposable() {
        if (compositeDisposable != null && compositeDisposable!!.size() > 0) {
            compositeDisposable!!.dispose()
        }
    }

    //把统一操作全部放在这，不会重连
    protected fun <T : Any> observe(
        observable: Observable<T>,
        isShowDialog: Boolean,
        liveData: MutableLiveData<T>
    ): Disposable {
        return observe(observable, isShowDialog, true, null, null, liveData)
    }

    //把统一操作全部放在这，不会重连
    protected fun <T : Any> observe(
        observable: Observable<T>,
        liveData: MutableLiveData<T>
    ): Disposable {
        return observe(observable, true, true, null, null, liveData)
    }

    //把统一操作全部放在这，不会重连
    protected fun <T : Any> observe(
        observable: Observable<T>,
        requestParams: Any?,
        liveData: MutableLiveData<T>
    ): Disposable {
        return observe(observable, true, true, null, requestParams, liveData)
    }

    //把统一操作全部放在这，不会重连
    protected fun <T : Any> observe(
        observable: Observable<T>,
        requestParams: Any?,
        consumer: Consumer<T>
    ): Disposable {
        return observe(observable, true, true, null, requestParams, consumer)
    }

    //把统一操作全部放在这，不会重连
    protected fun <T : Any> observe(
        observable: Observable<T>,
        liveData: MutableLiveData<T>,
        toastMsg: String?
    ): Disposable {
        return observe(observable, true, true, toastMsg, null, liveData)
    }

    protected fun <T : Any> observe(
        observable: Observable<T>,
        isShowDialog: Boolean,
        isShowToast: Boolean,
        toastMsg: String?,
        requestParams: Any?,
        liveData: MutableLiveData<T>
    ): Disposable {
        return observe(
            observable,
            isShowDialog,
            isShowToast,
            toastMsg,
            requestParams,
            liveData,
            null
        )
    }

    protected fun <T : Any> observe(
        observable: Observable<T>,
        isShowDialog: Boolean,
        isShowToast: Boolean,
        toastMsg: String?,
        requestParams: Any?,
        consumer: Consumer<T>
    ): Disposable {
        return observe(
            observable,
            isShowDialog,
            isShowToast,
            toastMsg,
            requestParams,
            null,
            consumer
        )
    }

    /**
     * 请求
     *
     * @param observable    请求被观察者
     * @param isShowDialog  是否展示请求dialog
     * @param isShowToast   是否展示错误文字提示
     * @param toastMsg      错误文字提示内容
     * @param requestParams 请求参数或请求的tag标记等，为了再错误回调的时候根据这个参数判断他的请求数据或那一条请求
     * @param liveData      返回成功数据
     * @param <T>           返回成功数据泛型
     * @return Disposable
    </T> */
    protected fun <T : Any> observe(
        observable: Observable<T>,
        isShowDialog: Boolean,
        isShowToast: Boolean,
        toastMsg: String?,
        requestParams: Any?,
        liveData: MutableLiveData<T>?,
        consumer: Consumer<T>?
    ): Disposable {
        return observable.subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable: Disposable ->
                addDisposable(disposable)
                if (baseView != null && isShowDialog) {
                    baseView!!.showLoading(dialogMessage)
                }
            }
            .doFinally {
                if (baseView != null && isShowDialog) {
                    baseView!!.hideLoading()
                }
            }
            .retryWhen(retryWhen)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                consumer ?: Consumer { value: T -> liveData!!.setValue(value) }) { e: Throwable? ->
                LogUtil.show(ApiRetrofit.TAG, "BaseViewModel|系统异常: $e")
                if (baseView != null && isShowDialog) {
                    baseView!!.hideLoading()
                }
                var be: BaseException? = null
                if (e != null) {
                    if (e is BaseException) {
                        be = e
                        //回调到view层 处理 或者根据项目情况处理
                        if (baseView != null) {
                            if (isShowToast) {
                                if (!TextUtils.isEmpty(toastMsg)) {
                                    baseView!!.showToast(toastMsg)
                                } else {
                                    baseView!!.showToast(be!!.errorMsg)
                                }
                            }
                            baseView!!.onErrorCode(
                                BaseModelEntity(
                                    be!!.errorCode,
                                    be.errorMsg,
                                    requestParams
                                )
                            )
                            return@subscribe
                        }
                    } else {
                        be = if (e is HttpException) {
                            //   HTTP错误
                            BaseException(e.message, e, BaseException.BAD_NETWORK)
                        } else if (e is ConnectException
                            || e is UnknownHostException
                        ) {
                            //   连接错误
                            BaseException(
                                BaseException.CONNECT_ERROR_MSG,
                                e,
                                BaseException.CONNECT_ERROR
                            )
                        } else if (e is InterruptedIOException) {
                            //  连接超时
                            BaseException(
                                BaseException.CONNECT_TIMEOUT_MSG,
                                e,
                                BaseException.CONNECT_TIMEOUT
                            )
                        } else if (e is JsonParseException
                            || e is JSONException
                            || e is ParseException
                        ) {
                            //  解析错误
                            BaseException(
                                BaseException.PARSE_ERROR_MSG,
                                e,
                                BaseException.PARSE_ERROR
                            )
                        } else {
                            BaseException(e.message, e, BaseException.OTHER)
                        }
                    }
                } else {
                    be = BaseException(BaseException.OTHER_MSG, BaseException.OTHER)
                }
                LogUtil.show(ApiRetrofit.TAG, "BaseViewModel|异常消息: " + be!!.errorMsg)
                if (baseView != null) {
                    baseView!!.onErrorCode(
                        BaseModelEntity(
                            be.errorCode,
                            be.errorMsg,
                            requestParams
                        )
                    )
                    if (isShowToast) {
                        if (!TextUtils.isEmpty(toastMsg)) {
                            baseView!!.showToast(toastMsg)
                        } else {
                            baseView!!.showToast(be.errorMsg)
                        }
                    }
                }
            }
    }

    /**
     * 请求
     *
     * @param observable    请求被观察者
     * @param isShowDialog  是否展示请求dialog
     * @param isShowToast   是否展示错误文字提示
     * @param toastMsg      错误文字提示内容
     * @param requestParams 请求参数或请求的tag标记等，为了再错误回调的时候根据这个参数判断他的请求数据或那一条请求
     * @param liveData      返回成功数据
     * @param <T>           返回成功数据泛型
     * @return Disposable
    </T> */
    @OptIn(DelicateCoroutinesApi::class)
    protected fun <T : Any> observeFlow(
        observable: Observable<T>,
        isShowDialog: Boolean = true,
        isShowToast: Boolean = true,
        toastMsg: String? = null,
        requestParams: Any? = null,
        mutableStateFlow: MutableStateFlow<T?>?,
        consumer: Consumer<T>? = null
    ): Disposable {
        return observable.subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable: Disposable ->
                addDisposable(disposable)
                if (baseView != null && isShowDialog) {
                    baseView!!.showLoading(dialogMessage)
                }
            }
            .doFinally {
                if (baseView != null && isShowDialog) {
                    baseView!!.hideLoading()
                }
            }
            .retryWhen(retryWhen)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                consumer ?: Consumer { value: T ->
                    run {
                        GlobalScope.launch(Dispatchers.Main) { mutableStateFlow?.value = value }
                    }
                }) { e: Throwable? ->
                LogUtil.show(ApiRetrofit.TAG, "BaseViewModel|系统异常: $e")
                if (baseView != null && isShowDialog) {
                    baseView!!.hideLoading()
                }
                var be: BaseException? = null
                if (e != null) {
                    if (e is BaseException) {
                        be = e
                        //回调到view层 处理 或者根据项目情况处理
                        if (baseView != null) {
                            if (isShowToast) {
                                if (!TextUtils.isEmpty(toastMsg)) {
                                    baseView!!.showToast(toastMsg)
                                } else {
                                    baseView!!.showToast(be!!.errorMsg)
                                }
                            }
                            baseView!!.onErrorCode(
                                BaseModelEntity(
                                    be!!.errorCode,
                                    be.errorMsg,
                                    requestParams
                                )
                            )
                            return@subscribe
                        }
                    } else {
                        be = if (e is HttpException) {
                            //   HTTP错误
                            BaseException(e.message, e, BaseException.BAD_NETWORK)
                        } else if (e is ConnectException
                            || e is UnknownHostException
                        ) {
                            //   连接错误
                            BaseException(
                                BaseException.CONNECT_ERROR_MSG,
                                e,
                                BaseException.CONNECT_ERROR
                            )
                        } else if (e is InterruptedIOException) {
                            //  连接超时
                            BaseException(
                                BaseException.CONNECT_TIMEOUT_MSG,
                                e,
                                BaseException.CONNECT_TIMEOUT
                            )
                        } else if (e is JsonParseException
                            || e is JSONException
                            || e is ParseException
                        ) {
                            //  解析错误
                            BaseException(
                                BaseException.PARSE_ERROR_MSG,
                                e,
                                BaseException.PARSE_ERROR
                            )
                        } else {
                            BaseException(e.message, e, BaseException.OTHER)
                        }
                    }
                } else {
                    be = BaseException(BaseException.OTHER_MSG, BaseException.OTHER)
                }
                LogUtil.show(ApiRetrofit.TAG, "BaseViewModel|异常消息: " + be!!.errorMsg)
                if (baseView != null) {
                    baseView!!.onErrorCode(
                        BaseModelEntity(
                            be.errorCode,
                            be.errorMsg,
                            requestParams
                        )
                    )
                    if (isShowToast) {
                        if (!TextUtils.isEmpty(toastMsg)) {
                            baseView!!.showToast(toastMsg)
                        } else {
                            baseView!!.showToast(be.errorMsg)
                        }
                    }
                }
            }
    }

    val retryWhen: Function<Observable<out Throwable>, Observable<*>>
        get() = if (retryService == null) RetryWhenNetworkException(ConstantsHelper.RETRY_WHEN_MAX_COUNT) else retryService!!

    @JvmOverloads
    fun startActivity(context: Context?, toClx: Class<*>?, bundle: Bundle? = null) {
        val intent = Intent(context, toClx)
        ContextCompat.startActivity(context!!, intent, bundle)
    }
}