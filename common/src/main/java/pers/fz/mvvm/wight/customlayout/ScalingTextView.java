package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import pers.fz.mvvm.R;

/**
 * Created by fz on 2017/5/8.
 * “收起全文”、“显示全文”收缩式textView
 */

public class ScalingTextView extends LinearLayout {
    TextView contentTextView;
    int initLines = 2;
    boolean PICK_UP = true;
    boolean SEE_ALL = false;
    TextView tipTextView;
    String contentText;
    Context context;
    int lineNum;
    View.OnClickListener contentTextViewOnClickListener;
    TipTextViewOnClickListener tipTextViewOnClickListener;
    boolean flag = SEE_ALL;

    public ScalingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.my_text_view, this);
        contentTextView = (TextView) view.findViewById(R.id.contentTextView);
        tipTextView = (TextView) view.findViewById(R.id.tipTextView);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.my_text_view);
        contentText = typedArray.getString(R.styleable.my_text_view_contentText);
        tipTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tipTextViewOnClick();
            }
        });
        initFlag();
    }
    public void setTipTextViewOnClickListener(TipTextViewOnClickListener tipTextViewOnClickListener){
        this.tipTextViewOnClickListener = tipTextViewOnClickListener;
    }
    public interface TipTextViewOnClickListener{
        void tipClick(boolean ifColl);
    }

    /**
     * 在此实现对contentTextView的显示控制
     */
    private void tipTextViewOnClick() {
        if (lineNum <= initLines) {
            return;
        }
        if (SEE_ALL == flag) {
            tipTextView.setText("收起全文");
            flag = PICK_UP;
            // 点击全文
            contentTextView.setMaxLines(lineNum);
        } else if (PICK_UP == flag) {
            tipTextView.setText("查看全文");
            flag = SEE_ALL;
            // 点击收起
            contentTextView.setMaxLines(initLines);
        }
        if(tipTextViewOnClickListener!=null){
            tipTextViewOnClickListener.tipClick(flag);
        }
    }

    /**
     * 初始化MyTextView
     */
    private void initMyTextView() {
        if (lineNum > initLines) {
            if (tipTextView.getVisibility() != VISIBLE) {
                tipTextView.setVisibility(VISIBLE);
            }
            contentTextView.setMaxLines(initLines);
        } else if (lineNum <= initLines) {
            if (tipTextView.getVisibility() != GONE) {
                tipTextView.setVisibility(GONE);
            }

        }
    }

    /**
     * 初始化flag
     */
    private void initFlag() {
        String content = tipTextView.getText().toString();
        if (!("查看全文".equals(content))) {
            tipTextView.setText("查看全文");
            flag = SEE_ALL;
        }
    }

    /**
     * 获取contentTextView的最大行数
     */
    private void getLineNum() {
        /** 新方法结束 */
        contentTextView.post(new Runnable() {
            @Override
            public void run() {
                lineNum = contentTextView.getLineCount();
                initMyTextView();
            }
        });
    }

    public void setText(String string) {
        contentTextView.setText(string);
        getLineNum();
    }

    public TextView getContentTextView() {
        return contentTextView;
    }

    public TextView getTipTextView() {
        return tipTextView;
    }

    public void setContentTextViewOnClickListener(View.OnClickListener contentTextViewOnClickListener) {
        contentTextView.setOnClickListener(contentTextViewOnClickListener);
    }

}
