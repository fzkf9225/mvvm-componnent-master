package io.coderf.arklab.googlegps.logger;

import android.location.Location;

/**
 * 文件记录器接口
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/4/16 15:51
 */
public interface IFileLogger {
    void write(Location loc);
    void annotate(String description, Location loc);
    void close();
    String getFileName();
}
