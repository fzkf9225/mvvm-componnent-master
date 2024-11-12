package com.casic.titan.commonui.bean;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

/**
 * Created by fz on 2023/12/28 17:00
 * describe :附件表
 */
public class AttachmentBean extends BaseObservable {
    private String fileId;

    private String path;

    private String url;

    private String mainId;

    private String fileName;

    private String fileSize;

    private String fieldName;

    public AttachmentBean() {
    }

    @Bindable
    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Bindable
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Bindable
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Bindable
    public String getMainId() {
        return mainId;
    }

    public void setMainId(String mainId) {
        this.mainId = mainId;
    }

    @Bindable
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Bindable
    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    @Bindable
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

}
