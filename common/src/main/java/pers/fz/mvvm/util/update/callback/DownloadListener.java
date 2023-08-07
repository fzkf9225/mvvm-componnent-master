package pers.fz.mvvm.util.update.callback;

import okhttp3.ResponseBody;

/**
 * Created by fz on 2020/6/19.
 * describe：下载监听
 */
public interface DownloadListener {
    void onStart(ResponseBody responseBody);
}
