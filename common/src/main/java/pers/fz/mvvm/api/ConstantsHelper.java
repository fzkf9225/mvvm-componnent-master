package pers.fz.mvvm.api;

import java.util.Arrays;
import java.util.List;

/**
 * Created by fz on 2018/1/10.
 * 静态变量类
 */

public class ConstantsHelper {
    /**
     * 请求是RetryWhen最大默认重试次数
     */
    public static final int RETRY_WHEN_MAX_COUNT = 3;
    /**
     * 跳转目标Activity页面，目前用于自动检测登录的作用
     */
    public static final String TARGET_ACTIVITY = "targetActivity";
    public static final String TAG = "casic";
    public static boolean isSuccessRequestUpdate = false;//更新请求是否成功了

    public static boolean hasNewAppVersion = false;//是否有新版本

    public static final String DOWNLOAD_CHANNEL_ID = Config.getInstance().getApplication().getPackageName() + "._app_download";
    public static final String DOWNLOAD_CHANNEL_NAME = "文件下载";

    public static final String NOTICE_CHANNEL_ID = Config.getInstance().getApplication().getPackageName() + "._app_info_notice";
    public static final String NOTICE_CHANNEL_NAME = "消息通知";

    public  final static List<String> IMAGE_TYPE = Arrays.asList(
            "png", "jpg", "jpeg", "bmp", "gif", "tif", "tiff", "pcx", "tga", "exif", "fpx", "svg", "svgz",
            "psd", "cdr", "pcd", "dxf", "ufo", "eps", "ai", "raw", "wmf", "webp", "avif", "apng", "jfif",
            "ico", "icns", "heic", "heif", "jp2", "j2k", "jpx", "jpm", "mj2", "dng", "cr2", "nef", "arw",
            "sr2", "raf", "orf", "rw2", "pef", "x3f", "bpg", "hdr", "exr", "pbm", "pgm", "ppm", "pam",
            "PNG", "JPG", "JPEG", "BMP", "GIF", "TIF", "TIFF", "PCX", "TGA", "EXIF", "FPX", "SVG", "SVGZ",
            "PSD", "CDR", "PCD", "DXF", "UFO", "EPS", "AI", "RAW", "WMF", "WEBP", "AVIF", "APNG", "JFIF",
            "ICO", "ICNS", "HEIC", "HEIF", "JP2", "J2K", "JPX", "JPM", "MJ2", "DNG", "CR2", "NEF", "ARW",
            "SR2", "RAF", "ORF", "RW2", "PEF", "X3F", "BPG", "HDR", "EXR", "PBM", "PGM", "PPM", "PAM"
    );

    public  final static List<String> VIDEO_TYPE = Arrays.asList(
            "mp4", "m2v", "mkv", "avi", "mpeg", "mpg", "wmv", "mov", "rm", "ram", "swf", "flv", "rmvb",
            "asf", "m4v", "3gp", "3g2", "dat", "vob", "asx", "ts", "mts", "m2ts", "divx", "f4v", "ogv",
            "webm", "mxf", "wtv", "dv", "mod", "tod", "m4p", "m4b", "ismv", "isma", "avchd", "hdv", "ogm",
            "bik", "smk", "nsv", "roq", "yuv", "m1v", "m2p", "m4v", "mpv", "qt", "amv", "drc", "flv", "f4p",
            "MP4", "M2V", "MKV", "AVI", "MPEG", "MPG", "WMV", "MOV", "RM", "RAM", "SWF", "FLV", "RMVB",
            "ASF", "M4V", "3GP", "3G2", "DAT", "VOB", "ASX", "TS", "MTS", "M2TS", "DIVX", "F4V", "OGV",
            "WEBM", "MXF", "WTV", "DV", "MOD", "TOD", "M4P", "M4B", "ISMV", "ISMA", "AVCHD", "HDV", "OGM",
            "BIK", "SMK", "NSV", "ROQ", "YUV", "M1V", "M2P", "M4V", "MPV", "QT", "AMV", "DRC", "FLV", "F4P"
    );

    public final static List<String> AUDIO_TYPE = Arrays.asList(
            // 常见无损格式
            "wav", "aiff", "aif", "aifc", "flac", "alac",
            // 有损压缩格式
            "mp3", "aac", "m4a", "ogg", "oga", "opus", "wma",
            // 专业音频格式
            "aup3", "cda", "mid", "midi", "kar", "rmi",
            // 音频工程格式
            "ape", "tta", "wv", "ofr", "ofs", "spx",
            // 模块音乐格式
            "mod", "xm", "it", "s3m", "mtm", "umx",
            // 游戏音频格式
            "vgm", "ay", "gbs", "hes", "kss", "nsf", "nsfe", "sap", "spc",
            // 流媒体和网络音频
            "m3u", "m3u8", "pls", "asx", "wax", "wvx",
            // 其他专业格式
            "ac3", "dts", "thd", "mlp", "amr", "awb", "mmf",
            "au", "snd", "voc", "8svx", "sb", "sf", "paf",
            "raw", "pcm", "adpcm", "gsm", "dss", "msv",
            // 新兴格式
            "mka", "weba", "caf", "tak", "ofr", "ofs",
            // 语音和通信格式
            "vox", "dvf", "imy", "m4p", "m4b", "mpc",
            // 元数据和播放列表
            "cue", "m3u", "pls", "xspf", "asx",
            // 常见无损格式
            "WAV", "AIFF", "AIF", "AIFC", "FLAC", "ALAC",
            // 有损压缩格式
            "MP3", "AAC", "M4A", "OGG", "OGA", "OPUS", "WMA",
            // 专业音频格式
            "AUP3", "CDA", "MID", "MIDI", "KAR", "RMI",
            // 音频工程格式
            "APE", "TTA", "WV", "OFR", "OFS", "SPX",
            // 模块音乐格式
            "MOD", "XM", "IT", "S3M", "MTM", "UMX",
            // 游戏音频格式
            "VGM", "AY", "GBS", "HES", "KSS", "NSF", "NSFE", "SAP", "SPC",
            // 流媒体和网络音频
            "M3U", "M3U8", "PLS", "ASX", "WAX", "WVX",
            // 其他专业格式
            "AC3", "DTS", "THD", "MLP", "AMR", "AWB", "MMF",
            "AU", "SND", "VOC", "8SVX", "SB", "SF", "PAF",
            "RAW", "PCM", "ADPCM", "GSM", "DSS", "MSV",
            // 新兴格式
            "MKA", "WEBA", "CAF", "TAK", "OFR", "OFS",
            // 语音和通信格式
            "VOX", "DVF", "IMY", "M4P", "M4B", "MPC",
            // 元数据和播放列表
            "CUE", "M3U", "PLS", "XSPF", "ASX"
    );
}
