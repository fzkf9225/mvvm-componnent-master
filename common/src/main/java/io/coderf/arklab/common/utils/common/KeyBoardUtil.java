package io.coderf.arklab.common.utils.common;


import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

 /**
 * Created by fz on 2018/1/11.
  * 打开或关闭软键盘
 */

public class KeyBoardUtil {
    /**
     * 打卡软键盘
     *
     * @param mEditText
     *            输入框
     * @param mContext
     *            上下文
     */
    public static void openKeyboard(EditText mEditText, Context mContext)
    {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 关闭软键盘
     *
     * @param mEditText
     *            输入框
     * @param mContext
     *            上下文
     */
    public static void closeKeyboard(EditText mEditText, Context mContext)
    {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

     public static void setListener(Activity activity, SoftKeyBoardListener.OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
         SoftKeyBoardListener softKeyBoardListener = new SoftKeyBoardListener(activity);
         softKeyBoardListener.setOnSoftKeyBoardChangeListener(onSoftKeyBoardChangeListener);
     }

     /**
      * 在 Activity 的 dispatchTouchEvent 中调用
      * @param activity 当前 Activity
      * @param ev 触摸事件
      * @return 是否消费了事件（通常返回 false 以保持原有事件传递机制）
      */
     public static boolean handleDispatchTouchEvent(Activity activity, MotionEvent ev) {
         // 1. 如果是手指抬起动作（避免重复触发）
         if (ev.getAction() == MotionEvent.ACTION_UP) {
             View currentFocusView = activity.getCurrentFocus();

             // 2. 如果当前焦点在 EditText 上才处理
             if (currentFocusView != null && currentFocusView instanceof EditText) {
                 // 3. 获取点击坐标
                 int rawX = (int) ev.getRawX();
                 int rawY = (int) ev.getRawY();

                 // 4. 构造点击位置在焦点View中的坐标数组
                 int[] location = new int[2];
                 currentFocusView.getLocationOnScreen(location);
                 int viewLeft = location[0];
                 int viewTop = location[1];
                 int viewRight = viewLeft + currentFocusView.getWidth();
                 int viewBottom = viewTop + currentFocusView.getHeight();

                 // 5. 判断点击坐标是否在 EditText 的范围之外
                 boolean isTouchOutside = rawX < viewLeft || rawX > viewRight
                         || rawY < viewTop || rawY > viewBottom;

                 if (isTouchOutside) {
                     // 6. 收起键盘并清除焦点
                     hideSoftInput(activity);
                     currentFocusView.clearFocus();
                 }
             }
         }
         // 返回 false，不消费事件，让事件继续传递给子 View
         return false;
     }

     /**
      * 隐藏软键盘（基于当前焦点的 View）
      */
     public static void hideSoftInput(Activity activity) {
         View view = activity.getCurrentFocus();
         if (view != null) {
             hideSoftInput(activity, view);
         }
     }

     /**
      * 隐藏软键盘（指定 View）
      */
     public static void hideSoftInput(Context context, View view) {
         InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
         if (imm != null) {
             imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
         }
     }

     /**
      * 显示软键盘
      */
     public static void showSoftInput(Context context, View view) {
         InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
         if (imm != null) {
             view.requestFocus();
             imm.showSoftInput(view, 0);
         }
     }

     /**
      * 切换软键盘状态
      */
     public static void toggleSoftInput(Context context) {
         InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
         if (imm != null) {
             imm.toggleSoftInput(0, 0);
         }
     }

     /**
      * 判断软键盘是否显示
      * 注意：这个方法在不同设备上可能不准确，仅供参考
      */
     public static boolean isSoftInputActive(Activity activity) {
         View view = activity.getCurrentFocus();
         if (view != null) {
             InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
             return imm != null && imm.isActive(view);
         }
         return false;
     }
}
