package pers.fz.mvvm.util.common;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import android.annotation.SuppressLint;
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
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import pers.fz.mvvm.api.ConstantsHelper;
import pers.fz.mvvm.bean.AttachmentBean;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.activity.VideoPlayerActivity;
import pers.fz.mvvm.api.AppManager;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.enums.AttachmentTypeEnum;
import pers.fz.mvvm.util.download.DownloadManger;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.wight.dialog.MenuDialog;
import pers.fz.mvvm.wight.gallery.PreviewPhotoDialog;

/**
 * Created by fz on 2024/2/28 10:53
 * describe :
 */
public class AttachmentUtil {

    public static final String TAG = "AttachmentUtil";

    public static List<String> toStringList(List<AttachmentBean> attachmentList) {
        if (attachmentList == null) {
            return null;
        }
        List<String> stringList = new ArrayList<>();
        for (AttachmentBean attachmentBean : attachmentList) {
            stringList.add(attachmentBean.getPath());
        }
        return stringList;
    }

    public static List<Uri> toUriList(List<AttachmentBean> attachmentList) {
        if (attachmentList == null) {
            return null;
        }
        List<Uri> stringList = new ArrayList<>();
        for (AttachmentBean attachmentBean : attachmentList) {
            stringList.add(Uri.parse(attachmentBean.getPath()));
        }
        return stringList;
    }

    public static List<String> toUriStringList(List<AttachmentBean> attachmentList) {
        if (attachmentList == null) {
            return null;
        }
        List<String> stringList = new ArrayList<>();
        for (AttachmentBean attachmentBean : attachmentList) {
            stringList.add(attachmentBean.getPath());
        }
        return stringList;
    }

    public static List<String> uriListToUriStringList(List<Uri> uriList) {
        if (uriList == null) {
            return null;
        }
        List<String> stringList = new ArrayList<>();
        for (Uri uri : uriList) {
            stringList.add(uri.toString());
        }
        return stringList;
    }

    public static List<Uri> uriStringListToUriList(List<String> uriStringList) {
        if (uriStringList == null) {
            return null;
        }
        List<Uri> uriList = new ArrayList<>();
        for (String uri : uriStringList) {
            uriList.add(Uri.parse(uri));
        }
        return uriList;
    }

    public static List<AttachmentBean> toAttachmentList(List<String> stringList, String mainId) {
        if (stringList == null) {
            return null;
        }
        List<AttachmentBean> attachmentList = new ArrayList<>();
        for (String str : stringList) {
            AttachmentBean attachment = new AttachmentBean();
            attachment.setMainId(mainId);
            attachment.setPath(str);
            attachment.setFileName(FileUtil.getFileNameByUrl(str));
            attachmentList.add(attachment);
        }
        return attachmentList;
    }

    @SuppressLint("Range")
    public static List<AttachmentBean> uriListToAttachmentList(List<Uri> uriList) {
        if (uriList == null) {
            return null;
        }
        ContentResolver contentResolver = null;

        if (Config.getInstance() != null && Config.getInstance().getApplication() != null) {
            contentResolver = Config.getInstance().getApplication().getContentResolver();
        }

        List<AttachmentBean> attachmentList = new ArrayList<>();
        for (Uri uri : uriList) {
            AttachmentBean attachment = new AttachmentBean();
            attachment.setPath(uri.toString());
            if (contentResolver == null) {
                attachmentList.add(attachment);
                continue;
            }
            //也有可能当前手机不需要Uri权限，因为我们尝试强行获取一下，但是要记得捕获异常
            try {
                attachment.setFileType(getAttachmentTypeByUri(Config.getInstance().getApplication(), uri).typeValue);
                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    attachment.setFileName(cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
                    attachment.setFileSize(cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE)) + "");
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            attachmentList.add(attachment);
        }
        return attachmentList;
    }

    @SuppressLint("Range")
    public static List<AttachmentBean> uriListToAttachmentList(List<Uri> uriList, String mainId) {
        if (uriList == null) {
            return null;
        }
        ContentResolver contentResolver = null;

        if (Config.getInstance() != null && Config.getInstance().getApplication() != null) {
            contentResolver = Config.getInstance().getApplication().getContentResolver();
        }
        List<AttachmentBean> attachmentList = new ArrayList<>();
        for (Uri uri : uriList) {
            AttachmentBean attachment = new AttachmentBean();
            attachment.setMainId(mainId);
            attachment.setPath(uri.toString());

            if (contentResolver == null) {
                attachmentList.add(attachment);
                continue;
            }
            //也有可能当前手机不需要Uri权限，因为我们尝试强行获取一下，但是要记得捕获异常
            try {
                attachment.setFileType(getAttachmentTypeByUri(Config.getInstance().getApplication(), uri).typeValue);
                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    attachment.setFileName(cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
                    attachment.setFileSize(cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE)) + "");
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            attachmentList.add(attachment);
        }
        return attachmentList;
    }

    /**
     * 本地文件绝对地址转  List<AttachmentBean>
     *
     * @param stringList 本地绝对地址集合
     * @param mainId     主键Id
     * @param field      字段名称
     * @return
     */
    public static List<AttachmentBean> toAttachmentList(List<String> stringList, String mainId, @NotNull String field) {
        if (stringList == null) {
            return null;
        }
        List<AttachmentBean> attachmentList = new ArrayList<>();
        for (String str : stringList) {
            AttachmentBean attachment = new AttachmentBean();
            attachment.setMainId(mainId);
            attachment.setPath(str);
            attachment.setFieldName(field);
            attachment.setFileName(FileUtil.getFileName(str));
            attachmentList.add(attachment);
        }
        return attachmentList;
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
    public static List<AttachmentBean> uriListToAttachmentList(List<Uri> uriList, String mainId, @NotNull String field) {
        if (uriList == null) {
            return null;
        }
        ContentResolver contentResolver = null;

        if (Config.getInstance() != null && Config.getInstance().getApplication() != null) {
            contentResolver = Config.getInstance().getApplication().getContentResolver();
        }

        List<AttachmentBean> attachmentList = new ArrayList<>();

        for (Uri uri : uriList) {
            AttachmentBean attachment = new AttachmentBean();
            attachment.setMainId(mainId);
            attachment.setPath(uri.toString());
            attachment.setFieldName(field);

            if (contentResolver == null) {
                attachmentList.add(attachment);
                continue;
            }
            //也有可能当前手机不需要Uri权限，因为我们尝试强行获取一下，但是要记得捕获异常
            try {
                attachment.setFileType(getAttachmentTypeByUri(Config.getInstance().getApplication(), uri).typeValue);
                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    attachment.setFileName(cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
                    attachment.setFileSize(cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE)) + "");
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            attachmentList.add(attachment);
        }
        return attachmentList;
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
     * 获取Uri持久化权限
     */
    public static void takeUriPermission(Context context, Uri uri) {
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
            //授权Uri持久化权限
            contentResolver.takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Uri持久化权限
     */
    public static void takeUriPermission(Context context, List<Uri> uriList) {
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
        for (Uri uri : uriList) {
            try {
                //授权Uri持久化权限
                contentResolver.takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                    if (uriPermission.getUri() == uri) {
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
                if (uriPermission.getUri() == uri) {
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
        return mineType.startsWith("image/") || mineType.startsWith("IMAGE/");
    }

    public static boolean isVideoType(String mineType) {
        if (TextUtils.isEmpty(mineType)) {
            return false;
        }
        return mineType.startsWith("video/") || mineType.startsWith("VIDEO/");
    }

    public static boolean isAudioType(String mineType) {
        if (TextUtils.isEmpty(mineType)) {
            return false;
        }
        return mineType.startsWith("audio/") || mineType.startsWith("AUDIO/");
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
            String type = getMimeType(path);
            if (isImageType(type)) {
                return AttachmentTypeEnum.IMAGE;
            } else if (isVideoType(type)) {
                return AttachmentTypeEnum.VIDEO;
            } else {
                return AttachmentTypeEnum.FILE;
            }
        } else if (isContentUri(path)) {
            if (context == null || context.getContentResolver() == null) {
                return null;
            }
            Uri uri = null;
            try {
                uri = Uri.parse(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (uri == null) {
                return null;
            }
            String type = context.getContentResolver().getType(uri);
            if (isImageType(type)) {
                return AttachmentTypeEnum.IMAGE;
            } else if (isVideoType(type)) {
                return AttachmentTypeEnum.VIDEO;
            } else {
                return AttachmentTypeEnum.FILE;
            }
        } else {
            String extension = FileUtil.getUrlFileExtensionName(path);
            if (!TextUtils.isEmpty(extension) && ConstantsHelper.IMAGE_TYPE.contains(extension)) {
                return AttachmentTypeEnum.IMAGE;
            } else if ((!TextUtils.isEmpty(extension)) && ConstantsHelper.VIDEO_TYPE.contains(extension)) {
                return AttachmentTypeEnum.VIDEO;
            } else {
                return AttachmentTypeEnum.FILE;
            }
        }
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
        String regex = "^content://.*$";
        if (uriString.matches(regex)) {
            return true;
        }
        try {
            // 进一步使用 Uri 类验证
            Uri uri = Uri.parse(uriString);
            return uri != null && Objects.equals(uri.getScheme(), "content");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void viewUrlFile(Context mContext, String url) {
        try {
            String type = getMimeType(url);
            if (isImageType(type)) {
                new PreviewPhotoDialog(mContext, PreviewPhotoDialog.createImageInfo(url), 0).show();
            } else if (isVideoType(type)) {
                Bundle bundleVideo = new Bundle();
                bundleVideo.putString(VideoPlayerActivity.VIDEO_TITLE, url);
                bundleVideo.putString(VideoPlayerActivity.VIDEO_PATH, url);
                VideoPlayerActivity.show(mContext, bundleVideo);
            } else {
                new MenuDialog<>(mContext)
                        .setData("下载", "下载并预览")
                        .setOnOptionBottomMenuClickListener((dialog, list, pos) -> {
                            dialog.dismiss();
                            if (AppManager.getAppManager().currentActivity() == null || AppManager.getAppManager().currentActivity().isFinishing()) {
                                return;
                            }
                            Disposable disposable = DownloadManger.getInstance().download(AppManager.getAppManager().currentActivity(), url)
                                    .subscribe(file -> {
                                        if (pos == 1) {
                                            viewAbsoluteFile(mContext, file.getAbsolutePath());
                                        }
                                    }, throwable -> {
                                        LogUtil.show(TAG, "下载出现错误：" + throwable);
                                        Toast.makeText(mContext, "文件预览出现错误！", Toast.LENGTH_SHORT).show();
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
        String extension = FileUtil.getUrlFileExtensionName(absolutePath);
        String fileName = FileUtil.getFileNameByUrl(absolutePath);
        if (!TextUtils.isEmpty(extension) && ConstantsHelper.IMAGE_TYPE.contains(extension)) {
            new PreviewPhotoDialog(mContext, PreviewPhotoDialog.createImageInfo(absolutePath), 0).show();
        } else if ((!TextUtils.isEmpty(extension)) && ConstantsHelper.VIDEO_TYPE.contains(extension)) {
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
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
                Uri apkFileUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".FileProvider", file);
                i.setDataAndType(apkFileUri, getMimeType(absolutePath));
                mContext.startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, "未找到可以打开此类文件的应用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static void viewUriFile(Context mContext, String uriPath) {
        try {
            Uri uri = Uri.parse(uriPath);
            String type = mContext.getContentResolver().getType(uri);
            if (isImageType(type)) {
                new PreviewPhotoDialog(mContext, PreviewPhotoDialog.createImageInfo(uriPath), 0).show();
            } else if (isVideoType(type)) {
                Bundle bundleVideo = new Bundle();
                bundleVideo.putString(VideoPlayerActivity.VIDEO_TITLE, uriPath);
                bundleVideo.putString(VideoPlayerActivity.VIDEO_PATH, uriPath);
                VideoPlayerActivity.show(mContext, bundleVideo);
            } else {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
                i.setDataAndType(uri, TextUtils.isEmpty(type) ? "*/*" : type);
                mContext.startActivity(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "未找到可以打开此类文件的应用", Toast.LENGTH_SHORT).show();
        }
    }

    private static String getMimeType(String url) {
        String mimeType = "*/*";
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return mimeType;
    }
}
