package io.coderf.arklab.common.bean;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.coderf.arklab.common.enums.AttachmentTypeEnum;
import io.coderf.arklab.common.enums.UploadStatusEnum;

/**
 * 附件表
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @created 2023/12/28 17:00
 * @updated 2026/4/9 0:03
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
    private String fileId = "";
    /**
     * 附件绝对路径
     */
    private String path = "";
    /**
     * 附件相对路径
     */
    private String relativePath = "";
    /**
     * 缩略图
     */
    private String thumbnailPath = "";
    /**
     * 关联业务表id
     */
    private String mainId = "";
    /**
     * 附件名称
     */
    private String fileName = "";
    /**
     * 附件大小
     */
    private String fileSize = "0";
    /**
     * 附件类型，参考枚举值AttachmentTypeEnum，image-图片，video-视频，audio-音频，file-附件
     */
    private String fileType = AttachmentTypeEnum.IMAGE.typeValue;
    /**
     * 字段id，对应业务表的字段名，默认为空，也就是这个业务表只有一个类型的附件这个就不用赋值
     * 如果多个的话就赋值这个字段，区分各自的附件
     */
    private String fieldName = "";
    /**
     * 插入数据的时间
     */
    private long createTime = System.currentTimeMillis();
    /**
     * 插入数据的用户
     */
    private String createUser = "";

    /**
     * 其他参数，存json
     */
    private String otherInfo = "";

    /**
     * 俯仰角
     */
    private Double pitch = 0.0;

    /**
     * 偏航角
     */
    private Double yaw = 0.0;

    /**
     * 翻滚角
     */
    private Double roll = 0.0;

    /**
     * 拍照时所在经度
     */
    private Double longitude = 0.0;

    /**
     * 拍照时所在纬度
     */
    private Double latitude = 0.0;

    /**
     * 拍照时所在海拔高程
     */
    private Double height = 0.0;
    /**
     * 是否正在上传
     */
    @Ignore
    private Integer uploading = UploadStatusEnum.DEFAULT.typeValue;

    /**
     * 已上传的附件百分比
     */
    @Ignore
    private String uploadingPercent = "0";

    @Ignore
    private Map<String,Object> uploadInfo = new HashMap<>();

    public AttachmentBean() {
    }

    @NonNull
    @Bindable
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
        this.fileId = fileId == null ? "" : fileId;
    }

    @Bindable
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? "" : path;
    }

    @Bindable
    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath == null ? "" : relativePath;
    }

    @Bindable
    public String getMainId() {
        return mainId;
    }

    public void setMainId(String mainId) {
        this.mainId = mainId == null ? "" : mainId;
    }

    @Bindable
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? "" : fileName;
    }

    @Bindable
    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize == null ? "0" : fileSize;
    }

    @Bindable
    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath == null ? "" : thumbnailPath;
    }

    @Bindable
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName == null ? "" : fieldName;
    }

    @Bindable
    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime == 0 ? System.currentTimeMillis() : createTime;
    }

    @Bindable
    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? "" : createUser;
    }

    @Bindable
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType == null ? AttachmentTypeEnum.IMAGE.typeValue : fileType;
    }

    @Bindable
    public Integer getUploading() {
        return uploading;
    }

    public void setUploading(Integer uploading) {
        this.uploading = uploading == null ? UploadStatusEnum.DEFAULT.typeValue : uploading;
    }

    @Bindable
    public String getUploadingPercent() {
        return uploadingPercent;
    }

    public void setUploadingPercent(String uploadingPercent) {
        this.uploadingPercent = uploadingPercent == null ? "0" : uploadingPercent;
    }

    @Bindable
    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo == null? "" : otherInfo;
    }

    public Map<String,Object> getUploadInfo() {
        return uploadInfo;
    }

    public void setUploadInfo(Map<String,Object> uploadInfo) {
        this.uploadInfo = uploadInfo == null ? new HashMap<>() : uploadInfo;
    }

    @Bindable
    public Double getPitch() {
        return pitch;
    }

    public void setPitch(Double pitch) {
        this.pitch = pitch;
    }

    @Bindable
    public Double getYaw() {
        return yaw;
    }

    public void setYaw(Double yaw) {
        this.yaw = yaw;
    }

    @Bindable
    public Double getRoll() {
        return roll;
    }

    public void setRoll(Double roll) {
        this.roll = roll;
    }

    @Bindable
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Bindable
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Bindable
    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }
    // ========== 新增字段的getter/setter结束 ==========

    @NonNull
    @Override
    public String toString() {
        return "AttachmentBean{" +
                "mobileId='" + mobileId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", path='" + path + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", thumbnailPath='" + thumbnailPath + '\'' +
                ", mainId='" + mainId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", createTime=" + createTime +
                ", createUser='" + createUser + '\'' +
                ", otherInfo='" + otherInfo + '\'' +
                ", uploading=" + uploading +
                ", uploadingPercent='" + uploadingPercent + '\'' +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                ", roll=" + roll +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", height=" + height +
                '}';
    }
}