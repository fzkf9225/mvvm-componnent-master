package pers.fz.mvvm.viewmodel;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import pers.fz.mvvm.activity.WebViewActivity;
import pers.fz.mvvm.api.RegexUtils;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.util.common.StringUtil;

/**
 * Create by CherishTang on 2020/3/19 0019
 * describe:
 */
public class MainViewModel extends BaseViewModel<BaseView> {

    public final static String ARG_PAGE = "page";

    private Fragment mCurrentFragment;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * 处理二维码扫码结果
     *
     * @param context 视图
     * @param result 结果
     */
    public void decodeQRCodeResult(Context context, Object result) {
        if (StringUtil.isEmpty(result)) {
            return;
        }
        if (RegexUtils.isUrl(result.toString())) {
            WebViewActivity.show(context, result.toString(), "");
        } else {
            baseView.showToast("扫码结果为：" + result);
        }
    }

    public void setFragment(FragmentManager mFragmentManager, Fragment fragment, @IdRes int id) {
        if (fragment == null) {
            return;
        }
        if (mCurrentFragment != fragment) {
            if (fragment.isAdded()) {
                mFragmentManager.beginTransaction().hide(mCurrentFragment).show(fragment).commit();
            } else {
                mFragmentManager.beginTransaction().replace(id, fragment).commit();
            }
            mCurrentFragment = fragment;
        }
    }

    /**
     * 创建bundle
     *
     * @param page 页数
     * @param bd bundle数据
     * @return 创建新的bundle
     */
    public Bundle createBundle(int page, Bundle bd) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PAGE, page);
        if (bd != null) {
            bundle.putAll(bd);
        }
        return bundle;
    }

}
