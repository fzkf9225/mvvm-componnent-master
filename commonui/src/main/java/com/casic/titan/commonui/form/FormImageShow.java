package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.casic.titan.commonui.R;

import java.util.List;

import pers.fz.mvvm.adapter.ImageShowAdapter;
import pers.fz.mvvm.wight.recyclerview.FullyGridLayoutManager;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormImageShow extends ConstraintLayout {
    protected String labelString;
    protected int bgColor;
    protected boolean required = false;
    protected boolean bottomBorder = true;
    protected TextView tvLabel, tvRequired;
    protected TextView tvSelection;
    protected RecyclerView mRecyclerViewImage;
    private ImageShowAdapter imageShowAdapter;

    public FormImageShow(Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public FormImageShow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FormImageShow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    protected void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormImage);
            labelString = typedArray.getString(R.styleable.FormImage_label);
            bgColor = typedArray.getColor(R.styleable.FormImage_bgColor, 0xFFF1F3F2);
            required = typedArray.getBoolean(R.styleable.FormImage_required, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormImage_bottomBorder, true);
            typedArray.recycle();
        }
    }

    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.form_image, this, true);
        setLayoutParams(new Constraints.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
        tvLabel = findViewById(R.id.tv_label);
        mRecyclerViewImage = findViewById(R.id.mRecyclerViewImage);
        tvRequired = findViewById(R.id.tv_required);
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        tvLabel.setText(labelString);
        if (bottomBorder) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
        imageShowAdapter = new ImageShowAdapter(getContext());
        mRecyclerViewImage.setLayoutManager(new FullyGridLayoutManager(getContext(), 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mRecyclerViewImage.setAdapter(imageShowAdapter);
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
    }

    public void setImages(List<String> images){
        imageShowAdapter.setList(images);
        imageShowAdapter.notifyDataSetChanged();
    }
    public CharSequence getText() {
        return tvSelection.getText();
    }

    public void setText(String text) {
        tvSelection.setText(text);
    }

    public void setLabel(String text) {
        tvLabel.setText(text);
    }


}
