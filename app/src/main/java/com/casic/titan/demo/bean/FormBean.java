package com.casic.titan.demo.bean;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

/**
 * created by fz on 2024/11/12 16:10
 * describe:
 */
public class FormBean extends BaseObservable {
    private String id;

    private String title;
    private String content;

    private String date;

    private String richText;
    public FormBean() {
    }

    public FormBean(String id, String title,String date, String content, String richText) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.richText = richText;
    }

    @Bindable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Bindable
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Bindable
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRichText() {
        return richText;
    }

    public void setRichText(String richText) {
        this.richText = richText;
    }
}

