package pers.fz.mvvm.bean;

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
    /**
     * 文件id
     */
    @Bindable
    private String fileId;
    /**
     * 附件绝对路径
     */
    @Bindable
    private String path;
    /**
     * 附件相对路径
     */
    @Bindable
    private String relativePath;
    /**
     * 关联业务表id
     */
    @Bindable
    private String mainId;
    /**
     * 附件名称
     */
    @Bindable
    private String fileName;
    /**
     * 附件大小
     */
    @Bindable
    private String fileSize;
    /**
     * 附件类型，参考枚举值AttachmentTypeEnum，image-图片，video-视频，audio-音频，file-附件
     */
    @Bindable
    private String fileType;
    /**
     * 字段id，对应业务表的字段名，默认为空，也就是这个业务表只有一个类型的附件这个就不用赋值
     * 如果多个的话就赋值这个字段，区分各自的附件
     */
    @Bindable
    private String fieldName;
    /**
     * 插入数据的时间
     */
    @Bindable
    private long createTime = System.currentTimeMillis();
    /**
     * 插入数据的用户
     */
    private String createUser;

    public AttachmentBean() {
    }

    @NonNull
    public String getMobileId() {
        return mobileId;
    }

    public void setMobileId(@NonNull String mobileId) {
        this.mobileId = mobileId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getMainId() {
        return mainId;
    }

    public void setMainId(String mainId) {
        this.mainId = mainId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @NonNull
    @Override
    public String toString() {
        return "AttachmentBean{" +
                "mobileId='" + mobileId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", path='" + path + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", mainId='" + mainId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", createTime=" + createTime +
                ", createUser='" + createUser + '\'' +
                '}';
    }
}
