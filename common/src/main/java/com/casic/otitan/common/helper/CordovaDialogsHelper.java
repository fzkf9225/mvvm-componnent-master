package com.casic.otitan.common.helper;

import android.content.Context;
import android.view.KeyEvent;

import com.casic.otitan.common.widget.dialog.ConfirmDialog;
import com.casic.otitan.common.widget.dialog.EditAreaDialog;
import com.casic.otitan.common.widget.dialog.MessageDialog;

/**
 * Helper class for WebViews to implement prompt(), alert(), confirm() dialogs.
 */
public class CordovaDialogsHelper {
    private final Context context;
    private ConfirmDialog lastHandledConfirmDialog;
    private MessageDialog lastHandledMessageDialog;
    private EditAreaDialog lastHandledEditAreaDialog;

    public CordovaDialogsHelper(Context context) {
        this.context = context;
    }

    public void showAlert(String message, final Result result) {
        MessageDialog dialog = new MessageDialog(context)
                .setMessage(message)
                .setMessageType("提示")
                .setOnPositiveClickListener(dialog1 -> result.gotResult(true, null))
                .setCanOutSide(false)
                .builder();

        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                result.gotResult(true, null);
                dialog1.dismiss();
                return true;
            }
            return false;
        });

        lastHandledMessageDialog = dialog;
        dialog.show();
    }

    public void showConfirm(String message, final Result result) {
        ConfirmDialog dialog = new ConfirmDialog(context)
                .setMessage(message)
                .setOnPositiveClickListener(dialog1 -> result.gotResult(true, null))
                .setOnNegativeClickListener(dialog1 -> result.gotResult(false, null))
                .setCanOutSide(false)
                .builder();

        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                result.gotResult(false, null);
                dialog1.dismiss();
                return true;
            }
            return false;
        });

        lastHandledConfirmDialog = dialog;
        dialog.show();
    }

    /**
     * Tell the client to display a prompt dialog to the user.
     * If the client returns true, WebView will assume that the client will
     * handle the prompt dialog and call the appropriate JsPromptResult method.
     */
    public void showPrompt(String message, String defaultValue, final Result result) {
        EditAreaDialog dialog = new EditAreaDialog(context)
                .setTipsStr(message)
                .setHintStr("请输入内容")
                .setDefaultStr(defaultValue)
                .setOnPositiveClickListener((dialog1, inputText) -> {
                    result.gotResult(true, inputText);
                    dialog1.dismiss();
                })
                .setOnNegativeClickListener((dialog1, inputText) -> {
                    result.gotResult(false, null);
                    dialog1.dismiss();
                })
                .setCanOutSide(false)
                .builder();

        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                result.gotResult(false, null);
                dialog1.dismiss();
                return true;
            }
            return false;
        });

        lastHandledEditAreaDialog = dialog;
        dialog.show();
    }

    public void destroyLastDialog(){
        if (lastHandledConfirmDialog != null && lastHandledConfirmDialog.isShowing()){
            lastHandledConfirmDialog.dismiss();
        }
        if (lastHandledMessageDialog != null && lastHandledMessageDialog.isShowing()){
            lastHandledMessageDialog.dismiss();
        }
        if (lastHandledEditAreaDialog != null && lastHandledEditAreaDialog.isShowing()){
            lastHandledEditAreaDialog.dismiss();
        }
    }

    public interface Result {
        void gotResult(boolean success, String value);
    }
}