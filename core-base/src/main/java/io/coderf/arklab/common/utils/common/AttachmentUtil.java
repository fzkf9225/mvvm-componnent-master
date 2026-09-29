package io.coderf.arklab.common.utils.common;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.core.content.FileProvider;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.coderf.arklab.common.activity.VideoPlayerActivity;
import io.coderf.arklab.common.api.AppManager;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.api.ConstantsHelper;
import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.enums.AttachmentTypeEnum;
import io.coderf.arklab.common.utils.download.DownloadManager;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.widget.dialog.MenuDialog;
import io.coderf.arklab.common.widget.gallery.PreviewPhotoDialog;

/**
 * AttachmentUtil 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/2/28 10:53
 */
public class AttachmentUtil {

    public static final String TAG = "AttachmentUtil";

    public static List<String> toStringList(List<AttachmentBean> attachmentList) {
        if (attachmentList == null) {
            return null;
        }
        return attachmentList.stream().map(AttachmentBean::getPath).collect(Collectors.toList());
    }

    public static List<Uri> toUriList(List<AttachmentBean> attachmentList) {
        if (attachmentList == null) {
            return null;
        }

        return attachmentList.stream().map(attachmentBean -> Uri.parse(attachmentBean.getPath())).collect(Collectors.toList());
    }

    public static List<String> toUriStringList(List<AttachmentBean> attachmentList) {
        if (attachmentList == null) {
            return null;
        }
        return attachmentList.stream().map(AttachmentBean::getPath).collect(Collectors.toList());
    }

    public static List<String> uriListToUriStringList(List<Uri> uriList) {
        if (uriList == null) {
            return null;
        }
        return uriList.stream().map(Uri::toString).collect(Collectors.toList());
    }

    public static List<Uri> uriStringListToUriList(List<String> uriStringList) {
        if (uriStringList == null) {
            return null;
        }
        return uriStringList.stream().map(Uri::parse).collect(Collectors.toList());
    }

    /**
     * 本地文件绝对地址转  List<AttachmentBean>
     *
     * @param stringList 本地绝对地址集合
     * @return
     */
    public static List<AttachmentBean> toAttachmentList(List<String> stringList) {
        return toAttachmentList(stringList, null, null);
    }

    /**
     * 本地文件绝对地址转  List<AttachmentBean>
     *
     * @param stringList 本地绝对地址集合
     * @param mainId     主键Id
     * @return
     */
    public static List<AttachmentBean> toAttachmentList(List<String> stringList, String mainId) {
        return toAttachmentList(stringList, mainId, null);
    }

    /**
     * 本地文件绝对地址转  List<AttachmentBean>
     *
     * @param stringList 本地绝对地址集合
     * @param mainId     主键Id
     * @param field      字段名称
     * @return
     */
    public static List<AttachmentBean> toAttachmentList(List<String> stringList, String mainId, String field) {
        if (stringList == null) {
            return null;
        }
        Context appContext = Config.getInstance().getApplication();
        return stringList.stream().map(str -> {
            AttachmentBean attachment = new AttachmentBean();
            attachment.setMainId(mainId);
            attachment.setPath(str);
            attachment.setFieldName(field);
            attachment.setFileName(FileUtil.getFileName(str));
            if (!TextUtils.isEmpty(str)) {
                File file = new File(str);
                if (file.isFile()) {
                    attachment.setFileSize(String.valueOf(file.length()));
                }
                AttachmentTypeEnum mediaType = getMediaType(appContext, null, str);
                if (mediaType != null) {
                    attachment.setFileType(mediaType.typeValue);
                }
            }
            return attachment;
        }).collect(Collectors.toList());
    }

    public static List<AttachmentBean> coverAttachmentList(List<AttachmentBean> attachmentBeanList, String mainId, @NotNull String field) {
        if (attachmentBeanList == null) {
            return null;
        }
        for (AttachmentBean attachmentBean : attachmentBeanList) {
            attachmentBean.setMainId(mainId);
            attachmentBean.setFieldName(field);
        }
        return attachmentBeanList;
    }

    @SuppressLint("Range")
    public static List<AttachmentBean> uriListToAttachmentList(Context context, List<Uri> uriList) {
        return uriListToAttachmentList(context, uriList, null, null);
    }

    @SuppressLint("Range")
    public static List<AttachmentBean> uriListToAttachmentList(List<Uri> uriList) {
        return uriListToAttachmentList(Config.getInstance().getApplication(), uriList, null, null);
    }


    @SuppressLint("Range")
    public static List<AttachmentBean> uriListToAttachmentList(Context context, List<Uri> uriList, String mainId) {
        return uriListToAttachmentList(context, uriList, mainId, null);
    }

    @SuppressLint("Range")
    public static List<AttachmentBean> uriListToAttachmentList(List<Uri> uriList, String mainId) {
        return uriListToAttachmentList(Config.getInstance().getApplication(), uriList, mainId, null);
    }

    @SuppressLint("Range")
    public static List<AttachmentBean> uriListToAttachmentList(List<Uri> uriList, String mainId, String field) {
        return uriListToAttachmentList(Config.getInstance().getApplication(), uriList, mainId, field);
    }

    @SuppressLint("Range")
    public static List<AttachmentBean> uriListToAttachmentList(Context context, List<Uri> uriList, String mainId, String field) {
        if (uriList == null) {
            return null;
        }
        if (context == null) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver == null) {
            return null;
        }

        return uriList.stream().map(uri -> {
            AttachmentBean attachment = new AttachmentBean();
            attachment.setMainId(mainId);
            attachment.setPath(uri.toString());
            attachment.setFieldName(field);
            try {
                attachment.setFileType(getAttachmentTypeByUri(context, uri).typeValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try (Cursor cursor = contentResolver.query(uri,
                    new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE},
                    null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                        attachment.setFileName(cursor.getString(nameIndex));
                    }
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        attachment.setFileSize(String.valueOf(cursor.getLong(sizeIndex)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return attachment;
        }).collect(Collectors.toList());
    }

    public static List<AttachmentBean> drawableResToAttachmentList(Context context, @DrawableRes List<Integer> drawableResList, String mainId, String field) {
        if (CollectionUtil.isEmpty(drawableResList)) {
            return null;
        }
        if (context == null) {
            return null;
        }
        return drawableResList.stream().map(resId -> {
            AttachmentBean attachment = new AttachmentBean();
            attachment.setMainId(mainId);
            attachment.setPath(DrawableUtil.resourceToBase64(context, resId));
            attachment.setFieldName(field);
            try {
                attachment.setFileName(context.getResources().getResourceEntryName(resId));
                String extension = FileUtil.getUrlFileExtensionName(attachment.getFileName());
                if (!TextUtils.isEmpty(extension) && ConstantsHelper.IMAGE_TYPE.contains(extension)) {
                    attachment.setFileType(AttachmentTypeEnum.IMAGE.typeValue);
                } else if ((!TextUtils.isEmpty(extension)) && ConstantsHelper.VIDEO_TYPE.contains(extension)) {
                    attachment.setFileType(AttachmentTypeEnum.VIDEO.typeValue);
                } else if ((!TextUtils.isEmpty(extension)) && ConstantsHelper.AUDIO_TYPE.contains(extension)) {
                    attachment.setFileType(AttachmentTypeEnum.AUDIO.typeValue);
                } else {
                    attachment.setFileType(AttachmentTypeEnum.FILE.typeValue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return attachment;
        }).collect(Collectors.toList());
    }

    public static List<AttachmentBean> drawableResToAttachmentList(@DrawableRes List<Integer> drawableResList, String mainId, String field) {
        return drawableResToAttachmentList(Config.getInstance().getApplication(), drawableResList, mainId, field);
    }

    /**
     * 根据uri获取文件类型
     *
     * @param context 上下文
     * @param uri     文件uri
     * @return 文件类型
     */
    public static AttachmentTypeEnum getAttachmentTypeByUri(Context context, Uri uri) {
        if (context == null) {
            return AttachmentTypeEnum.FILE;
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            return AttachmentTypeEnum.FILE;
        }
        // 获取文件MIME类型
        String mimeType = contentResolver.getType(uri);
        // 根据MIME类型判断文件类别
        if (isImageType(mimeType)) {
            return AttachmentTypeEnum.IMAGE;
        } else if (isVideoType(mimeType)) {
            return AttachmentTypeEnum.VIDEO;
        } else if (isAudioType(mimeType)) {
            return AttachmentTypeEnum.AUDIO;
        } else {
            return AttachmentTypeEnum.FILE;
        }
    }

    /**
     * 获取 Uri 持久化读权限。
     *
     * @return 是否授予成功。未携带 FLAG_GRANT_PERSISTABLE_URI_PERMISSION 的选择结果会失败
     */
    public static boolean takeUriPermission(Context context, Uri uri) {
        if (uri == null || context == null) {
            return false;
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            return false;
        }
        try {
            contentResolver.takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取 Uri 持久化读权限。
     *
     * @return 列表中的 Uri 是否全部授予成功
     */
    public static boolean takeUriPermission(Context context, List<Uri> uriList) {
        if (uriList == null || uriList.isEmpty() || context == null) {
            return false;
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            return false;
        }
        boolean allGranted = true;
        for (Uri uri : uriList) {
            if (uri == null) {
                allGranted = false;
                continue;
            }
            try {
                contentResolver.takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) {
                e.printStackTrace();
                allGranted = false;
            }
        }
        return allGranted;
    }

    public static void releaseUriPermission(Context context, List<Uri> uriList) {
        //释放Uri持久化权限
        if (uriList == null || uriList.isEmpty()) {
            return;
        }
        if (context == null) {
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            return;
        }
        try {
            List<UriPermission> uriPermissionList = contentResolver.getPersistedUriPermissions();
            if (uriPermissionList == null || uriPermissionList.isEmpty()) {
                return;
            }
            for (Uri uri : uriList) {
                for (UriPermission uriPermission : uriPermissionList) {
                    if (uri != null && uri.equals(uriPermission.getUri())) {
                        contentResolver.releasePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void releaseUriPermission(Context context, Uri uri) {
        //释放Uri持久化权限
        if (uri == null) {
            return;
        }
        if (context == null) {
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            return;
        }
        try {
            List<UriPermission> uriPermissionList = contentResolver.getPersistedUriPermissions();
            if (CollectionUtil.isEmpty(uriPermissionList)) {
                return;
            }
            for (UriPermission uriPermission : uriPermissionList) {
                if (uri.equals(uriPermission.getUri())) {
                    contentResolver.releasePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isImageType(String mineType) {
        if (TextUtils.isEmpty(mineType)) {
            return false;
        }
        return mineType.startsWith("image/") || mineType.startsWith("IMAGE/") || ConstantsHelper.IMAGE_TYPE.contains(mineType);
    }

    public static boolean isVideoType(String mineType) {
        if (TextUtils.isEmpty(mineType)) {
            return false;
        }
        return mineType.startsWith("video/") || mineType.startsWith("VIDEO/") || ConstantsHelper.VIDEO_TYPE.contains(mineType);
    }

    public static boolean isAudioType(String mineType) {
        if (TextUtils.isEmpty(mineType)) {
            return false;
        }
        return mineType.startsWith("audio/") || mineType.startsWith("AUDIO/") || ConstantsHelper.AUDIO_TYPE.contains(mineType);
    }

    public static AttachmentTypeEnum getMediaType(Context context, String path) {
        return getMediaType(context, null, path);
    }

    /**
     * 根据文件类型、文件地址获取文件类型
     *
     * @param context  上下文
     * @param fileType 文件类型 获取对应的枚举
     * @param path     文件地址，可能是网络地址，可能是uri可能是绝对地址
     * @return 文件类型枚举
     */
    public static AttachmentTypeEnum getMediaType(Context context, String fileType, String path) {
        if (!TextUtils.isEmpty(fileType)) {
            return AttachmentTypeEnum.getMediaType(fileType);
        }

        if (TextUtils.isEmpty(path)) {
            return null;
        }

        if (isHttp(path)) {
            return classifyByMimeOrExtension(getMimeType(path), path);
        } else if (isContentUri(path)) {
            if (context == null || context.getContentResolver() == null) {
                return null;
            }
            Uri uri;
            try {
                uri = Uri.parse(path);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            String type = context.getContentResolver().getType(uri);
            return classifyByMimeOrExtension(type, path);
        } else {
            return classifyByExtension(path);
        }
    }

    private static AttachmentTypeEnum classifyByMimeOrExtension(String mimeType, String path) {
        if (isImageType(mimeType)) {
            return AttachmentTypeEnum.IMAGE;
        } else if (isVideoType(mimeType)) {
            return AttachmentTypeEnum.VIDEO;
        } else if (isAudioType(mimeType)) {
            return AttachmentTypeEnum.AUDIO;
        }
        return classifyByExtension(path);
    }

    private static AttachmentTypeEnum classifyByExtension(String path) {
        String extension = FileUtil.getUrlFileExtensionName(path);
        if (TextUtils.isEmpty(extension)) {
            return AttachmentTypeEnum.FILE;
        }
        if (ConstantsHelper.IMAGE_TYPE.contains(extension)) {
            return AttachmentTypeEnum.IMAGE;
        } else if (ConstantsHelper.VIDEO_TYPE.contains(extension)) {
            return AttachmentTypeEnum.VIDEO;
        } else if (ConstantsHelper.AUDIO_TYPE.contains(extension)) {
            return AttachmentTypeEnum.AUDIO;
        }
        return AttachmentTypeEnum.FILE;
    }

    public static void viewFile(Context mContext, String path) {
        try {
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(mContext, "文件地址不存在或已删除！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isHttp(path)) {
                viewUrlFile(mContext, path);
            } else if (isContentUri(path)) {
                viewUriFile(mContext, path);
            } else {
                viewAbsoluteFile(mContext, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "此文件暂不支持预览！", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isHttp(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        return path.startsWith("http://") || path.startsWith("https://") || path.startsWith("HTTP://") || path.startsWith("HTTPS://");
    }

    public static boolean isContentUri(String uriString) {
        if (TextUtils.isEmpty(uriString)) {
            return false;
        }
        try {
            Uri uri = Uri.parse(uriString);
            return uri != null && "content".equalsIgnoreCase(uri.getScheme());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void viewUrlFile(Context mContext, String url) {
        try {
            AttachmentTypeEnum mediaType = getMediaType(mContext, null, url);
            if (mediaType == AttachmentTypeEnum.IMAGE) {
                new PreviewPhotoDialog(mContext)
                        .createImageInfo(url)
                        .currentPosition(0)
                        .show();
            } else if (mediaType == AttachmentTypeEnum.VIDEO) {
                Bundle bundleVideo = new Bundle();
                bundleVideo.putString(VideoPlayerActivity.VIDEO_TITLE, url);
                bundleVideo.putString(VideoPlayerActivity.VIDEO_PATH, url);
                VideoPlayerActivity.show(mContext, bundleVideo);
            } else if (mediaType == AttachmentTypeEnum.AUDIO) {
                openWithViewIntent(mContext, Uri.parse(url), getMimeType(url));
            } else {
                new MenuDialog<>(mContext)
                        .setData("下载", "下载并预览")
                        .setOnOptionBottomMenuClickListener((dialog, list, pos) -> {
                            dialog.dismiss();
                            Activity host = AppManager.getAppManager().currentActivity();
                            if (host == null || host.isFinishing() || host.isDestroyed()) {
                                return;
                            }
                            DownloadManager.getInstance().download(host, url)
                                    .subscribe(file -> {
                                        if (!isContextAlive(mContext)) {
                                            return;
                                        }
                                        if (pos == 1) {
                                            viewAbsoluteFile(mContext, file.getAbsolutePath());
                                        } else {
                                            Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show();
                                        }
                                    }, throwable -> {
                                        LogUtil.logger(TAG, "下载出现错误：" + throwable);
                                        if (isContextAlive(mContext)) {
                                            Toast.makeText(mContext, "文件预览出现错误！", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .builder()
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "未找到可以打开此类文件的应用", Toast.LENGTH_SHORT).show();
        }
    }

    private static void viewAbsoluteFile(Context mContext, String absolutePath) {
        String fileName = FileUtil.getFileNameByUrl(absolutePath);
        AttachmentTypeEnum mediaType = getMediaType(mContext, null, absolutePath);
        if (mediaType == AttachmentTypeEnum.IMAGE) {
            new PreviewPhotoDialog(mContext)
                    .createImageInfo(absolutePath)
                    .currentPosition(0)
                    .show();
        } else if (mediaType == AttachmentTypeEnum.VIDEO) {
            Bundle bundleVideo = new Bundle();
            bundleVideo.putString(VideoPlayerActivity.VIDEO_TITLE, fileName);
            bundleVideo.putString(VideoPlayerActivity.VIDEO_PATH, absolutePath);
            VideoPlayerActivity.show(mContext, bundleVideo);
        } else {
            try {
                File file = new File(absolutePath);
                if (!file.exists()) {
                    Toast.makeText(mContext, "文件不存在或已被删除！", Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri apkFileUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".FileProvider", file);
                openWithViewIntent(mContext, apkFileUri, getMimeType(absolutePath));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, "未找到可以打开此类文件的应用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static void viewUriFile(Context mContext, String uriPath) {
        try {
            Uri uri = Uri.parse(uriPath);
            AttachmentTypeEnum mediaType = getMediaType(mContext, null, uriPath);
            if (mediaType == AttachmentTypeEnum.IMAGE) {
                new PreviewPhotoDialog(mContext)
                        .createUriImageInfo(uri)
                        .currentPosition(0)
                        .show();
            } else if (mediaType == AttachmentTypeEnum.VIDEO) {
                Bundle bundleVideo = new Bundle();
                bundleVideo.putString(VideoPlayerActivity.VIDEO_TITLE, uriPath);
                bundleVideo.putString(VideoPlayerActivity.VIDEO_PATH, uriPath);
                VideoPlayerActivity.show(mContext, bundleVideo);
            } else {
                String type = mContext.getContentResolver().getType(uri);
                openWithViewIntent(mContext, uri, TextUtils.isEmpty(type) ? getMimeType(uriPath) : type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "未找到可以打开此类文件的应用", Toast.LENGTH_SHORT).show();
        }
    }

    private static void openWithViewIntent(Context context, Uri uri, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, TextUtils.isEmpty(mimeType) ? "*/*" : mimeType);
        context.startActivity(intent);
    }

    private static boolean isContextAlive(Context context) {
        if (!(context instanceof Activity)) {
            return context != null;
        }
        Activity activity = (Activity) context;
        return !activity.isFinishing() && !activity.isDestroyed();
    }

    private static String getMimeType(String url) {
        String extension = FileUtil.getUrlFileExtensionName(url);
        if (!TextUtils.isEmpty(extension)) {
            String mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension.toLowerCase(Locale.US));
            if (!TextUtils.isEmpty(mimeType)) {
                return mimeType;
            }
        }
        return "*/*";
    }
}
