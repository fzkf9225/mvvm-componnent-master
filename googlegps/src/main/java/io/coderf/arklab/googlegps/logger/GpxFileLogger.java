package io.coderf.arklab.googlegps.logger;

import android.location.Location;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.coderf.arklab.googlegps.common.GpsSettingConfig;
import io.coderf.arklab.googlegps.utils.LogUtil;

/**
 * GPX 格式文件记录器（标准轨迹格式）
 *
 * 工作流程：
 * - startLogging() 时创建新文件
 * - 运行期间每次定位追加写入
 * - stopLogging() 时关闭文件并写入结束标签
 * - 再次 startLogging() 时创建另一个新文件
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/4/16 15:53
 */
public class GpxFileLogger implements IFileLogger {
    private static final String TAG = "GpxFileLogger";
    private static final String FILE_EXTENSION = ".gpx";

    private PrintWriter writer;
    private String currentFileName;
    private String currentFilePath;
    private SimpleDateFormat gpxDateFormat;
    private boolean hasWrittenFirstPoint = false;
    private boolean isWriterReady = false;

    public GpxFileLogger() {
        gpxDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        gpxDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * 开始新的日志文件（每次 startLogging 调用时创建新文件）
     */
    public void startNewLog() {
        startNewLog(null);
    }

    /**
     * 开始新的日志文件，指定文件名前缀
     * @param customFileName 自定义文件名前缀（不含扩展名），为null时自动生成
     */
    public void startNewLog(String customFileName) {
        try {
            // 先关闭已有的 writer
            close();

            String fileName;
            if (customFileName != null && !customFileName.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                fileName = customFileName + "_" + sdf.format(new Date()) + FILE_EXTENSION;
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                fileName = "gps_track_" + sdf.format(new Date()) + FILE_EXTENSION;
            }

            File folder = Files.storageFolder(GpsSettingConfig.getInstance().getApplication());
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, fileName);
            currentFilePath = file.getAbsolutePath();
            currentFileName = fileName;

            // 创建新文件（false = 覆盖/创建新文件）
            writer = new PrintWriter(new FileWriter(file, false), true);
            isWriterReady = true;

            // 写入 GPX 头
            writeHeader();
            hasWrittenFirstPoint = false;

            LogUtil.loggerI(TAG, "Created new GPX file: " + currentFilePath);
        } catch (IOException e) {
            LogUtil.loggerI(TAG, "Failed to start GPX log: " + e.getMessage());
            isWriterReady = false;
        }
    }

    private void writeHeader() {
        if (writer == null) return;

        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<gpx version=\"1.1\" creator=\"GPSLogger for Android\"");
        writer.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        writer.println("  xmlns=\"http://www.topografix.com/GPX/1/1\"");
        writer.println("  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">");
        writer.println("  <metadata>");
        writer.println("    <time>" + gpxDateFormat.format(new Date()) + "</time>");
        writer.println("  </metadata>");
        writer.println("  <trk>");
        writer.println("    <name>GPS Track</name>");
        writer.println("    <trkseg>");
        writer.flush();
    }

    private void writeFooter() {
        if (writer == null) return;
        writer.println("    </trkseg>");
        writer.println("  </trk>");
        writer.println("</gpx>");
        writer.flush();
    }

    @Override
    public void write(Location loc) {
        if (writer == null || !isWriterReady) {
            LogUtil.loggerI(TAG, "Writer not initialized, cannot write location");
            return;
        }

        String timeStr = gpxDateFormat.format(new Date(loc.getTime()));

        StringBuilder sb = new StringBuilder();
        sb.append("      <trkpt lat=\"").append(loc.getLatitude())
                .append("\" lon=\"").append(loc.getLongitude()).append("\">\n");
        sb.append("        <ele>").append(loc.hasAltitude() ? loc.getAltitude() : 0).append("</ele>\n");
        sb.append("        <time>").append(timeStr).append("</time>\n");

        if (loc.hasAccuracy()) {
            sb.append("        <fix>").append(loc.getAccuracy() < 10 ? "3d" : "2d").append("</fix>\n");
            sb.append("        <hdop>").append(loc.getAccuracy()).append("</hdop>\n");
        }

        if (loc.hasSpeed()) {
            sb.append("        <speed>").append(loc.getSpeed()).append("</speed>\n");
        }

        if (loc.hasBearing()) {
            sb.append("        <course>").append(loc.getBearing()).append("</course>\n");
        }

        sb.append("      </trkpt>");

        writer.println(sb.toString());
        writer.flush();
        hasWrittenFirstPoint = true;

        LogUtil.loggerI(TAG, "Written to GPX: " + loc.getLatitude() + "," + loc.getLongitude());
    }

    @Override
    public void annotate(String description, Location loc) {
        if (writer == null || !isWriterReady) {
            LogUtil.loggerI(TAG, "Writer not initialized, cannot write annotation");
            return;
        }

        if (description == null || description.isEmpty()) return;

        // GPX 注解作为 waypoint
        String timeStr = gpxDateFormat.format(new Date());
        writer.println("  <wpt lat=\"" + loc.getLatitude() + "\" lon=\"" + loc.getLongitude() + "\">");
        writer.println("    <time>" + timeStr + "</time>");
        writer.println("    <name>" + escapeXml(description) + "</name>");
        writer.println("  </wpt>");
        writer.flush();
        LogUtil.loggerI(TAG, "Written annotation to GPX: " + description);
    }

    private String escapeXml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    @Override
    public void close() {
        if (writer != null) {
            writeFooter();
            writer.flush();
            writer.close();
            writer = null;
            isWriterReady = false;
            LogUtil.loggerI(TAG, "Closed GPX file: " + currentFilePath);
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