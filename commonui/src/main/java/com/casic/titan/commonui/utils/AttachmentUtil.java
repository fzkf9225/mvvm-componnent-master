package com.casic.titan.commonui.utils;

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
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.casic.titan.commonui.api.ConstantsHelper;
import com.casic.titan.commonui.bean.AttachmentBean;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pers.fz.mvvm.activity.VideoPlayerActivity;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.util.common.FileUtil;
import pers.fz.mvvm.wight.gallery.PreviewPhotoDialog;

/**
 * Created by fz on 2024/2/28 10:53
 * describe :
 */
public class AttachmentUtil {
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
            if (uriPermissionList == null || uriPermissionList.isEmpty()) {
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

    public static void viewFile(Context mContext, String path) {
        try {
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(mContext, "文件地址不存在或已删除！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (path.startsWith("http") || path.startsWith("HTTP")) {
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

    private static boolean isContentUri(String uriString) {
        String regex = "^content://.*$";
        if (uriString.matches(regex)) {
            return true;
        }
        // 进一步使用 Uri 类验证
        Uri uri = Uri.parse(uriString);
        return uri != null && Objects.equals(uri.getScheme(), "content");
    }

    private static void viewUrlFile(Context mContext, String url) {
        try {
            String type = getMimeType(url);
            if (!TextUtils.isEmpty(type) && (type.startsWith("image") || type.startsWith("IMAGE"))) {
                new PreviewPhotoDialog(mContext, PreviewPhotoDialog.createImageInfo(url), 0).show();
            } else if ((!TextUtils.isEmpty(type)) && (type.startsWith("video") || type.startsWith("VIDEO"))) {
                Bundle bundleVideo = new Bundle();
                bundleVideo.putString("videoName", url);
                bundleVideo.putString("videoPath", url);
                VideoPlayerActivity.show(mContext, bundleVideo);
            } else {
                // 进一步使用 Uri 类验证
                Uri uri = Uri.parse(url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
                i.setDataAndType(uri, type);
                mContext.startActivity(i);
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
//            Toast.makeText(mContext, "暂不支持视频播放！", Toast.LENGTH_SHORT).show();
            Bundle bundleVideo = new Bundle();
            bundleVideo.putString("videoName", fileName);
            bundleVideo.putString("videoPath", absolutePath);
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
                Uri apkFileUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileProvider", file);
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
            if (!TextUtils.isEmpty(type) && (type.startsWith("image") || type.startsWith("IMAGE"))) {
                new PreviewPhotoDialog(mContext, PreviewPhotoDialog.createImageInfo(uriPath), 0).show();
            } else if ((!TextUtils.isEmpty(type)) && (type.startsWith("video") || type.startsWith("VIDEO"))) {
                Bundle bundleVideo = new Bundle();
                bundleVideo.putString("videoName", uriPath);
                bundleVideo.putString("videoPath", uriPath);
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
