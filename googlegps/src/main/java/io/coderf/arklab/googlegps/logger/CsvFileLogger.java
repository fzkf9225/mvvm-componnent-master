package io.coderf.arklab.googlegps.logger;

import android.location.Location;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.coderf.arklab.googlegps.common.GpsSettingConfig;
import io.coderf.arklab.googlegps.service.GpsService;
import io.coderf.arklab.googlegps.utils.LogUtil;

/**
 * CSV 格式文件记录器
 * 格式: 时间,纬度,经度,海拔,精度,速度,方向,卫星数,提供者,注解
 *
 * 工作流程：
 * - startLogging() 时创建新文件
 * - 运行期间每次定位追加写入
 * - stopLogging() 时关闭文件
 * - 再次 startLogging() 时创建另一个新文件
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/4/16 15:52
 */
public class CsvFileLogger implements IFileLogger {
    private static final String FILE_EXTENSION = ".csv";
    private static final String DELIMITER = ",";

    private PrintWriter writer;
    private String currentFileName;
    private String currentFilePath;
    private final SimpleDateFormat dateFormat;
    private boolean isWriterReady = false;

    public CsvFileLogger() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
    }

    /**
     * 开始新的日志文件（每次 startLogging 调用时创建新文件）
     */
    public void startNewLog() {
        startNewLog(null);
    }

    /**
     * 开始新的日志文件，指定文件名文件名
     * @param customFileName 自定义文件名（含扩展名），为null时自动生成
     */
    public void startNewLog(String customFileName) {
        try {
            // 先关闭已有的 writer
            close();

            // 生成文件名 - 使用时间戳确保每次启动都是新文件
            String fileName;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            if (customFileName != null && !customFileName.isEmpty()) {
                fileName = customFileName;
            } else {
                fileName = "gps_track_" + sdf.format(new Date()) + FILE_EXTENSION;
            }

            File folder = Files.storageFolder(GpsSettingConfig.getInstance().getApplication());
            if (!folder.exists()) {
                boolean isCreated = folder.mkdirs();
            }

            File file = new File(folder, fileName);
            currentFilePath = file.getAbsolutePath();
            currentFileName = fileName;

            // 创建新文件（false = 覆盖/创建新文件，不是追加模式）
            // 因为每次 startLogging 都应该是一个全新的文件
            writer = new PrintWriter(new FileWriter(file, false), true);
            isWriterReady = true;

            // 写入 CSV 头
            writeHeader();

            LogUtil.logger(GpsService.TAG, "Created new log file: " + currentFilePath);
        } catch (IOException e) {
            LogUtil.logger(GpsService.TAG, "Failed to start log: " + e.getMessage());
            isWriterReady = false;
        }
    }

    /**
     * 写入 CSV 头
     */
    private void writeHeader() {
        if (writer == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Time").append(DELIMITER);
        sb.append("Latitude").append(DELIMITER);
        sb.append("Longitude").append(DELIMITER);
        sb.append("Altitude(m)").append(DELIMITER);
        sb.append("Accuracy(m)").append(DELIMITER);
        sb.append("Speed(m/s)").append(DELIMITER);
        sb.append("Bearing(°)").append(DELIMITER);
        sb.append("Satellites").append(DELIMITER);
        sb.append("Provider").append(DELIMITER);
        sb.append("Annotation");

        writer.println(sb.toString());
        writer.flush();
    }

    @Override
    public void write(Location loc) {
        if (writer == null || !isWriterReady) {
            LogUtil.logger(GpsService.TAG, "Writer not initialized, cannot write location");
            return;
        }

        String timeStr = dateFormat.format(new Date(loc.getTime()));

        StringBuilder sb = new StringBuilder();
        sb.append(timeStr).append(DELIMITER);
        sb.append(loc.getLatitude()).append(DELIMITER);
        sb.append(loc.getLongitude()).append(DELIMITER);
        sb.append(loc.hasAltitude() ? loc.getAltitude() : 0).append(DELIMITER);
        sb.append(loc.hasAccuracy() ? loc.getAccuracy() : 0).append(DELIMITER);
        sb.append(loc.hasSpeed() ? loc.getSpeed() : 0).append(DELIMITER);
        sb.append(loc.hasBearing() ? loc.getBearing() : 0).append(DELIMITER);

        // 卫星数（从 extras 获取）
        int satellites = 0;
        if (loc.getExtras() != null) {
            satellites = loc.getExtras().getInt("SATELLITES_FIX", 0);
        }
        sb.append(satellites).append(DELIMITER);

        sb.append(loc.getProvider() != null ? loc.getProvider() : "unknown").append(DELIMITER);
        sb.append(""); // 注解占位

        writer.println(sb.toString());
        writer.flush();

        LogUtil.logger(GpsService.TAG, "Written to CSV: " + timeStr + "," + loc.getLatitude() + "," + loc.getLongitude());
    }

    @Override
    public void annotate(String description, Location loc) {
        if (writer == null || !isWriterReady) {
            LogUtil.logger(GpsService.TAG, "Writer not initialized, cannot write annotation");
            return;
        }

        if (description == null || description.isEmpty()) return;

        String timeStr = dateFormat.format(new Date());
        writer.println("# " + timeStr + DELIMITER + "ANNOTATION" + DELIMITER + description);
        writer.flush();
        LogUtil.logger(GpsService.TAG, "Written annotation: " + description);
    }

    @Override
    public void close() {
        if (writer != null) {
            writer.flush();
            writer.close();
            writer = null;
            isWriterReady = false;
            LogUtil.logger(GpsService.TAG, "Closed log file: " + currentFilePath);
        }
    }

    @Override
    public String getFileName() {
        return currentFileName;
    }

    public String getFilePath() {
        return currentFilePath;
    }

    public boolean isReady() {
        return isWriterReady && writer != null;
    }
}