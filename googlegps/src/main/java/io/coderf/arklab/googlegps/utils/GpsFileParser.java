package io.coderf.arklab.googlegps.utils;

import android.location.Location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.coderf.arklab.googlegps.logger.Files;
import io.coderf.arklab.googlegps.common.GpsSettingConfig;

/**
 * 日志文件解析工具类
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/4/16 17:22
 */
public class GpsFileParser {

    /**
     * 获取所有日志文件
     */
    public static List<File> getAllLogFiles() {
        File folder = Files.storageFolder(GpsSettingConfig.getInstance().getApplication());
        File[] files = folder.listFiles((dir, name) ->
                name.endsWith(".csv") || name.endsWith(".gpx"));
        return files != null ? java.util.Arrays.asList(files) : new ArrayList<>();
    }

    /**
     * 解析 CSV 文件为 Location 列表
     */
    public static List<Location> parseCsvFile(File csvFile) {
        List<Location> locations = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    Location loc = new Location("FILE");
                    try {
                        loc.setTime(parseCsvTime(parts[0]));
                        loc.setLatitude(Double.parseDouble(parts[1]));
                        loc.setLongitude(Double.parseDouble(parts[2]));
                        if (parts.length > 3 && !parts[3].isEmpty())
                            loc.setAltitude(Double.parseDouble(parts[3]));
                        if (parts.length > 4 && !parts[4].isEmpty())
                            loc.setAccuracy(Float.parseFloat(parts[4]));
                        if (parts.length > 5 && !parts[5].isEmpty())
                            loc.setSpeed(Float.parseFloat(parts[5]));
                        if (parts.length > 6 && !parts[6].isEmpty())
                            loc.setBearing(Float.parseFloat(parts[6]));
                        locations.add(loc);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ignored) {}
            }
        }
        return locations;
    }

    /**
     * 解析 GPX 文件为 Location 列表
     */
    public static List<Location> parseGpxFile(File gpxFile) {
        List<Location> locations = new ArrayList<>();
        // 简化实现，实际可用 XML 解析器如 XmlPullParser
        // 这里只做示意，完整实现需要 XML 解析
        return locations;
    }

    private static long parseCsvTime(String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            Date date = sdf.parse(timeStr);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 将 Location 列表转换为 CSV 格式字符串
     */
    public static String locationsToCsv(List<Location> locations) {
        StringBuilder sb = new StringBuilder();
        sb.append("Time,Latitude,Longitude,Altitude(m),Accuracy(m),Speed(m/s),Bearing(°),Satellites,Provider,Annotation\n");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        for (Location loc : locations) {
            sb.append(sdf.format(new Date(loc.getTime()))).append(",");
            sb.append(loc.getLatitude()).append(",");
            sb.append(loc.getLongitude()).append(",");
            sb.append(loc.hasAltitude() ? loc.getAltitude() : 0).append(",");
            sb.append(loc.hasAccuracy() ? loc.getAccuracy() : 0).append(",");
            sb.append(loc.hasSpeed() ? loc.getSpeed() : 0).append(",");
            sb.append(loc.hasBearing() ? loc.getBearing() : 0).append(",");
            sb.append("0,"); // satellites
            sb.append(loc.getProvider()).append(",");
            sb.append("\n");
        }
        return sb.toString();
    }
}
