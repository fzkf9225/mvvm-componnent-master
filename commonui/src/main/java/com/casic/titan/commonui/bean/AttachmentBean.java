package com.casic.titan.commonui.bean;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

/**
 * Created by fz on 2023/12/28 17:00
 * describe :附件表
 */
@Entity
public class AttachmentBean extends BaseObservable {
    /**
     * 移动端主键
     */
    @PrimaryKey
    @NonNull
    private String mobileId = UUID.randomUUID().toString().replace("-", "");

    private String fileId;

    private String path;

    private String url;

    private String mainId;

    private String fileName;

    private String fileSize;

    private String fieldName;

    private long createdTime = System.currentTimeMillis();

    public AttachmentBean() {
    }

    @NonNull
    public String getMobileId() {
        return mobileId;
    }

    public void setMobileId(@NonNull String mobileId) {
        this.mobileId = mobileId;
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

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
}
