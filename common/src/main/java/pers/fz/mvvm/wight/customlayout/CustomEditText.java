package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.apiUtil.DensityUtil;
import pers.fz.mvvm.util.apiUtil.KeyBoardUtils;
import pers.fz.mvvm.util.apiUtil.StringUtil;
import pers.fz.mvvm.util.log.LogUtil;


/**
 * Create by CherishTang on 2019/12/25 0025
 * describe:自定义输入框
 */
public class CustomEditText extends AppCompatEditText implements AppCompatEditText.OnEditorActionListener, TextWatcher {
    private final String TAG = this.getClass().getSimpleName();

    private Drawable drawableSearch = null;
    private Drawable drawableClear = null;
    private Context mContext;
    /**
     * 1-左，2-上，3-右，4-下
     */
    private int drawablePosition = 1;

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        try {
            TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.CustomEditText, 0, 0);
            for (int i = 0; i < ta.getIndexCount(); i++) {
                int attr = ta.getIndex(i);
                if (attr == R.styleable.CustomEditText_drawable_position) {
                    drawablePosition = ta.getInt(attr, 3);
                } else if (attr == R.styleable.CustomEditText_searchIcon) {
                    drawableSearch = ta.getDrawable(attr);
                } else if (attr == R.styleable.CustomEditText_clearIcon) {
                    drawableClear = ta.getDrawable(attr);
                }
            }
            ta.recycle();
            setSingleLine();
            setLines(1);
            setPadding(DensityUtil.dp2px(getContext(),8),0,DensityUtil.dp2px(getContext(),8),0);
            setImeOptions(EditorInfo.IME_ACTION_SEARCH);
            setTextSize(12);
            setTextColor(ContextCompat.getColor(mContext, R.color.search_view_background));
            if (drawableSearch == null) {
                drawableSearch = ContextCompat.getDrawable(mContext, R.mipmap.ic_search_app_left);
            }
            if (drawableClear == null) {
                drawableClear = ContextCompat.getDrawable(mContext, R.mipmap.icon_clear);
            }
            if (drawablePosition == 1) {
                setCompoundDrawablesWithIntrinsicBounds(drawableSearch,
                        null, null, null);
            } else if (drawablePosition == 2) {
                setCompoundDrawablesWithIntrinsicBounds(null,
                        drawableSearch, null, null);
            } else if (drawablePosition == 4) {
                setCompoundDrawablesWithIntrinsicBounds(null,
                        null, null, drawableSearch);
            } else {
                setCompoundDrawablesWithIntrinsicBounds(null,
                        null, drawableSearch, null);
            }
            setCompoundDrawablePadding(DensityUtil.dp2px(mContext, 8));
            setOnEditorActionListener(this);
            addTextChangedListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show(TAG, "init:" + e);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            if (drawablePosition == 1) {
                setCompoundDrawablesWithIntrinsicBounds(drawableSearch,
                        null, (s == null || StringUtil.isEmpty(s.toString())) ? null : drawableClear, null);

            } else if (drawablePosition == 2) {
                setCompoundDrawablesWithIntrinsicBounds(null,
                        drawableSearch, (s == null || StringUtil.isEmpty(s.toString())) ? null : drawableClear, null);

            } else if (drawablePosition == 4) {
                setCompoundDrawablesWithIntrinsicBounds(null,
                        null, (s == null || StringUtil.isEmpty(s.toString())) ? null : drawableClear, drawableSearch);
            } else {
                setCompoundDrawablesWithIntrinsicBounds(null,
                        null, (s == null || StringUtil.isEmpty(s.toString())) ? drawableSearch : drawableClear, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show(TAG, "onTextChanged:" + e);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (!"".equals(getText().toString())) {
                int xDown = (int) event.getX();
                if (event.getAction() == MotionEvent.ACTION_DOWN && xDown >= (getWidth() - getCompoundPaddingRight() * 2) && xDown < getWidth()) {
                    // 清除按钮的点击范围 按钮自身大小 +-padding
                    setText("");
                    if (onInputSubmitListener != null) {
                        onInputSubmitListener.onInputClear();
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show(TAG, "onTouchEvent:" + e);
        }
        super.onTouchEvent(event);
        return true;
    }

    private OnInputSubmitListener onInputSubmitListener;

    public void setOnInputSubmitListener(OnInputSubmitListener onInputSubmitListener) {
        this.onInputSubmitListener = onInputSubmitListener;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        try {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = v.getText().toString().trim();
                KeyBoardUtils.closeKeybord(this, mContext);
                if (onInputSubmitListener != null) {
                    onInputSubmitListener.onInputSubmit(keyword);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show(TAG, "onEditorAction:" + e);
        }
        return false;
    }

    public interface OnInputSubmitListener {
        void onInputSubmit(String query);

        void onInputClear();
    }
}
