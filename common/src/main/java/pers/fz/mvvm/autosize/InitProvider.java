/*
 * Copyright 2018 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pers.fz.mvvm.autosize;

import android.content.Context;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import pers.fz.mvvm.autosize.utils.AutoSizeUtils;
import pers.fz.mvvm.util.common.DensityUtil;


/**
 * ================================================
 * 通过声明 {@link ContentProvider} 自动完成初始化
 * Created by JessYan on 2018/8/19 11:55
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InitProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        Context application = getContext().getApplicationContext();
        if (application == null) {
            application = AutoSizeUtils.getApplicationByReflect();
        }
        AutoSizeConfig.getInstance()
                .setLog(true)
                .init((Application) application)
                .setUseDeviceSize(false)
                .setBaseOnWidth(!DensityUtil.isPad(application));
        return true;
    }
    /**
     * 屏幕适配尺寸，很多人把基准写在AndroidManifest中，但是我选择直接写BaseActivity中，是为了更好的支持各个Activity自愈更改
     *
     * @return 默认360dp
     */
    private float getDefaultWidth() {
        try {
            ApplicationInfo info =getContext().getPackageManager()
                    .getApplicationInfo(getContext().getPackageName(),
                            PackageManager.GET_META_DATA);
            return info.metaData.getInt("design_width_in_dp", 360);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 360;
    }

    /**
     * 屏幕适配尺寸，很多人把基准写在AndroidManifest中，但是我选择直接写BaseActivity中，是为了更好的支持各个Activity自愈更改
     *
     * @return 默认360dp
     */
    private float getDefaultHeight() {
        try {
            ApplicationInfo info = getContext().getPackageManager()
                    .getApplicationInfo(getContext().getPackageName(),
                            PackageManager.GET_META_DATA);
            return info.metaData.getInt("design_height_in_dp", 640);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 640;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
