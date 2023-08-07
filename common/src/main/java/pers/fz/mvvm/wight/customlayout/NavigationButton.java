package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import pers.fz.mvvm.R;

/**
 * Created by JuQiu
 * on 16/8/18.
 * 底部菜单按钮
 */
public class NavigationButton extends FrameLayout {
    private Fragment mFragment = null;
    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mDot;
    private Class<?> path;

    public NavigationButton(Context context) {
        super(context);
        init();
    }

    public NavigationButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NavigationButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NavigationButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.layout_nav_item, this, true);

        mIconView = findViewById(R.id.nav_iv_icon);
        mTitleView = findViewById(R.id.nav_tv_title);
        mDot = findViewById(R.id.nav_tv_dot);
    }

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        mIconView.setSelected(selected);
        mTitleView.setSelected(selected);
    }

    public void showRedDot(int count) {
        mDot.setVisibility(count > 0 ? VISIBLE : GONE);
        mDot.setText(String.valueOf(count));
    }

    public void setTextColor(int color) {
        mTitleView.setTextColor(ContextCompat.getColor(getContext(), color));
    }

    public void init(@DrawableRes int resId, @StringRes int strId, Class<?> path) {
        mIconView.setImageResource(resId);
        mTitleView.setVisibility(VISIBLE);
        mTitleView.setText(strId);
        this.path = path;
    }

    public void init(@DrawableRes int resId, Class<?> path) {
        mIconView.setImageResource(resId);
        mTitleView.setVisibility(GONE);
        this.path = path;
    }

    public Fragment getFragment() {
        return mFragment;
    }

    public Class<?> getPath() {
        return path;
    }

    public void setFragment(Fragment fragment) {
        this.mFragment = fragment;
    }

}
