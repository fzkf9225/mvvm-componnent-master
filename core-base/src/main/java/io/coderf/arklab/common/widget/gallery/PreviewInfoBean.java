package io.coderf.arklab.common.widget.gallery;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.enums.AttachmentTypeEnum;
import io.coderf.arklab.common.utils.common.DateUtil;
import io.coderf.arklab.common.utils.common.FileUtil;

/**
 * 信息大图预览条目：在 {@link AttachmentBean} 之上补充标题、时间、定位文案。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/11
 */
public class PreviewInfoBean {

    /**
     * 附件信息
     */
    @Nullable
    private AttachmentBean attachment;
    /**
     * 图片地址
     */
    @Nullable
    private String path;
    /**
     * 标题
     */
    @Nullable
    private String title;
    /**
     * 时间文本
     */
    @Nullable
    private String timeText;
    /**
     * 定位文本
     */
    @Nullable
    private String locationText;
    /**
     * 元数据可以存json之类的
     */
    @Nullable
    private String metaData;

    public PreviewInfoBean() {
    }

    public static PreviewInfoBean of(@NonNull AttachmentBean attachment) {
        PreviewInfoBean bean = new PreviewInfoBean();
        bean.attachment = attachment;
        return bean;
    }

    public static PreviewInfoBean of(@NonNull AttachmentBean attachment,
                                     @Nullable String title,
                                     @Nullable String timeText,
                                     @Nullable String locationText) {
        PreviewInfoBean bean = of(attachment);
        bean.title = title;
        bean.timeText = timeText;
        bean.locationText = locationText;
        return bean;
    }

    public static PreviewInfoBean of(@Nullable String path,
                                     @Nullable String title,
                                     @Nullable String timeText,
                                     @Nullable String locationText) {
        PreviewInfoBean bean = new PreviewInfoBean();
        bean.path = path;
        bean.title = title;
        bean.timeText = timeText;
        bean.locationText = locationText;
        return bean;
    }

    public static List<PreviewInfoBean> fromAttachments(@Nullable List<AttachmentBean> attachments) {
        List<PreviewInfoBean> list = new ArrayList<>();
        if (attachments == null) {
            return list;
        }
        for (AttachmentBean item : attachments) {
            if (item != null) {
                list.add(of(item));
            }
        }
        return list;
    }

    public static List<PreviewInfoBean> fromPaths(@Nullable List<String> paths) {
        List<PreviewInfoBean> list = new ArrayList<>();
        if (paths == null) {
            return list;
        }
        for (String item : paths) {
            PreviewInfoBean bean = new PreviewInfoBean();
            bean.path = item;
            bean.title = FileUtil.getFileName(item);
            list.add(bean);
        }
        return list;
    }

    @Nullable
    public AttachmentBean getAttachment() {
        return attachment;
    }

    public PreviewInfoBean setAttachment(@Nullable AttachmentBean attachment) {
        this.attachment = attachment;
        return this;
    }

    @Nullable
    public String getRawPath() {
        return path;
    }

    public PreviewInfoBean setPath(@Nullable String path) {
        this.path = path;
        return this;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public PreviewInfoBean setTitle(@Nullable String title) {
        this.title = title;
        return this;
    }

    @Nullable
    public String getTimeText() {
        return timeText;
    }

    public PreviewInfoBean setTimeText(@Nullable String timeText) {
        this.timeText = timeText;
        return this;
    }

    @Nullable
    public String getLocationText() {
        return locationText;
    }

    public PreviewInfoBean setLocationText(@Nullable String locationText) {
        this.locationText = locationText;
        return this;
    }

    /**
     * 实际加载路径：优先附件 path，其次自身 path。
     */
    @NonNull
    public String getPath() {
        if (attachment != null && !TextUtils.isEmpty(attachment.getPath())) {
            return attachment.getPath();
        }
        return path == null ? "" : path;
    }

    @NonNull
    public String getFileType() {
        if (attachment != null && !TextUtils.isEmpty(attachment.getFileType())) {
            return attachment.getFileType();
        }
        return AttachmentTypeEnum.IMAGE.typeValue;
    }

    @NonNull
    public String getFileName() {
        if (attachment != null && !TextUtils.isEmpty(attachment.getFileName())) {
            return attachment.getFileName();
        }
        return FileUtil.getFileName(getPath());
    }

    @Nullable
    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(@Nullable String metaData) {
        this.metaData = metaData;
    }

    /**
     * 第一行：图片名称。优先自定义 title，否则附件 fileName。
     */
    @NonNull
    public String resolveTitle() {
        if (!TextUtils.isEmpty(title)) {
            return title;
        }
        String fileName = getFileName();
        return fileName == null ? "" : fileName;
    }

    /**
     * 第二行：时间。优先自定义文案，否则格式化附件 createTime。
     */
    @NonNull
    public String resolveTimeText() {
        if (!TextUtils.isEmpty(timeText)) {
            return timeText;
        }
        if (attachment != null && attachment.getCreateTime() > 0) {
            String formatted = DateUtil.getDateTimeFromMillis(attachment.getCreateTime());
            return formatted == null ? "" : formatted;
        }
        return "";
    }

    /**
     * 第三行：定位位置。优先自定义文案，否则经纬度。
     */
    @NonNull
    public String resolveLocationText() {
        if (!TextUtils.isEmpty(locationText)) {
            return locationText;
        }
        if (attachment == null) {
            return "";
        }
        Double lat = attachment.getLatitude();
        Double lng = attachment.getLongitude();
        if (lat == null || lng == null) {
            return "";
        }
        if (lat == 0.0 && lng == 0.0) {
            return "";
        }
        return String.format(Locale.US, "%.6f, %.6f", lat, lng);
    }

    /**
     * 转成附件，供保存/播放等复用 {@link AttachmentBean} 的路径与类型。
     */
    @NonNull
    public AttachmentBean toAttachment() {
        if (attachment != null) {
            return attachment;
        }
        AttachmentBean bean = new AttachmentBean();
        bean.setPath(getPath());
        bean.setRelativePath(getPath());
        bean.setFileName(getFileName());
        bean.setFileType(getFileType());
        return bean;
    }
}
