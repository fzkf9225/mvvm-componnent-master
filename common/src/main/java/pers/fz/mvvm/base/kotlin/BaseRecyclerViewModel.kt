package pers.fz.mvvm.base.kotlin

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.bean.base.PageBean

/**
 * Created by fz on 2020/12/17 16:23
 * describe:
 */
abstract class BaseRecyclerViewModel<BV : BaseView, T>(application: Application) : BaseViewModel<BV>(application) {
   open var pageMutableStateFlow = MutableStateFlow<PageBean<T>?>(null)
}