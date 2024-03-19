package com.casic.titan.commonui.utils;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.casic.titan.commonui.bean.AttachmentBean;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    @SuppressLint("Range")
    public static List<AttachmentBean> uriListToAttachmentList(Context context, List<Uri> uriList) {
        if (uriList == null) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();

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
    public static List<AttachmentBean> uriListToAttachmentList(Context context,List<Uri> uriList, String mainId) {
        if (uriList == null) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();
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

    @SuppressLint("Range")
//    public static List<AttachmentBean> uriListToAttachmentList(List<Uri> uriList, String mainId, @NotNull AttachmentFieldEnum attachmentFieldEnum) {
//        if (uriList == null) {
//            return null;
//        }
//        ContentResolver contentResolver = null;
//
//        if (Config.getInstance() != null && Config.getInstance().getApplication() != null) {
//            contentResolver = Config.getInstance().getApplication().getContentResolver();
//        }
//
//        List<AttachmentBean> attachmentList = new ArrayList<>();
//
//        for (Uri uri : uriList) {
//            AttachmentBean attachment = new AttachmentBean();
//            attachment.setMainId(mainId);
//            attachment.setPath(uri.toString());
//            attachment.setFieldName(attachmentFieldEnum.field);
//
//            if (contentResolver == null) {
//                attachmentList.add(attachment);
//                continue;
//            }
//            //也有可能当前手机不需要Uri权限，因为我们尝试强行获取一下，但是要记得捕获异常
//            try {
//                Cursor cursor = contentResolver.query(uri, null, null, null, null);
//                if (cursor != null && cursor.moveToFirst()) {
//                    attachment.setFileName(cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
//                    attachment.setFileSize(cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE)) + "");
//                    cursor.close();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            attachmentList.add(attachment);
//        }
//        return attachmentList;
//    }

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

}
