/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of GPSLogger for Android.
 *
 * GPSLogger for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPSLogger for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.coderf.arklab.googlegps.common;


import android.location.Location;


/**
 * 会话管理类，存储运行时状态
 * 不再依赖 SharedPreferences，全部使用内存变量
 */
public class Session {

    private static Session instance = null;

    // ========== 运行时内存变量 ==========
    private Location previousLocationInfo;
    private Location currentLocationInfo;
    private Location temporaryLocationForBestAccuracy;

    // ========== 会话状态 ==========
    private boolean isSinglePointMode = false;
    private boolean isStarted = false;
    private boolean isUsingGps = false;
    private boolean isWaitingForLocation = false;
    private boolean isAnnotationMarked = false;
    private boolean isBoundToService = false;
    private boolean addNewTrackSegment = false;
    private boolean locationServiceUnavailable = false;
    private boolean towerEnabled = false;
    private boolean gpsEnabled = false;

    // ========== 时间戳 ==========
    private long startTimeStamp = 0;
    private long latestTimeStamp = 0;
    private long latestPassiveTimeStamp = 0;
    private long userStillSinceTimeStamp = 0;
    private long significantMotionSensorCreationTimeStamp = 0;
    private long firstRetryTimeStamp = 0;

    // ========== 数值 ==========
    private double totalTravelled = 0;
    private int numLegs = 0;
    private int visibleSatelliteCount = 0;
    private float autoSendDelay = 0;

    // ========== 字符串 ==========
    private String currentFileName = "";
    private String currentFormattedFileName = "";
    private String description = "";

    private Session() {
        // 初始化时间戳
        startTimeStamp = System.currentTimeMillis();
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    // ========== 状态重置方法 ==========

    /**
     * 重置所有会话数据（停止日志时调用）
     */
    public void reset() {
        isSinglePointMode = false;
        isStarted = false;
        isUsingGps = false;
        isWaitingForLocation = false;
        isAnnotationMarked = false;
        addNewTrackSegment = true;
        locationServiceUnavailable = false;

        totalTravelled = 0;
        numLegs = 0;
        visibleSatelliteCount = 0;

        latestTimeStamp = 0;
        latestPassiveTimeStamp = 0;
        userStillSinceTimeStamp = 0;
        significantMotionSensorCreationTimeStamp = 0;
        firstRetryTimeStamp = 0;

        currentFileName = "";
        currentFormattedFileName = "";
        description = "";

        previousLocationInfo = null;
        currentLocationInfo = null;
        temporaryLocationForBestAccuracy = null;
    }

    /**
     * 开始新会话时调用
     */
    public void startSession() {
        startTimeStamp = System.currentTimeMillis();
        totalTravelled = 0;
        numLegs = 0;
        addNewTrackSegment = true;
    }

    // ========== Getter / Setter ==========

    public boolean isSinglePointMode() {
        return isSinglePointMode;
    }

    public void setSinglePointMode(boolean singlePointMode) {
        this.isSinglePointMode = singlePointMode;
    }

    public boolean isTowerEnabled() {
        return towerEnabled;
    }

    public void setTowerEnabled(boolean towerEnabled) {
        this.towerEnabled = towerEnabled;
    }

    public boolean isGpsEnabled() {
        return gpsEnabled;
    }

    public void setGpsEnabled(boolean gpsEnabled) {
        this.gpsEnabled = gpsEnabled;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean isStarted) {
        this.isStarted = isStarted;
        if (isStarted) {
            startTimeStamp = System.currentTimeMillis();
        }
    }

    public boolean isLocationServiceUnavailable() {
        return locationServiceUnavailable;
    }

    public void setLocationServiceUnavailable(boolean unavailable) {
        this.locationServiceUnavailable = unavailable;
    }

    public boolean isUsingGps() {
        return isUsingGps;
    }

    public void setUsingGps(boolean isUsingGps) {
        this.isUsingGps = isUsingGps;
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    public void setCurrentFileName(String currentFileName) {
        this.currentFileName = currentFileName;
    }

    public int getVisibleSatelliteCount() {
        return visibleSatelliteCount;
    }

    public void setVisibleSatelliteCount(int satellites) {
        this.visibleSatelliteCount = satellites;
    }

    public double getCurrentLatitude() {
        if (getCurrentLocationInfo() != null) {
            return getCurrentLocationInfo().getLatitude();
        } else {
            return 0;
        }
    }

    public double getPreviousLatitude() {
        Location loc = getPreviousLocationInfo();
        return loc != null ? loc.getLatitude() : 0;
    }

    public double getPreviousLongitude() {
        Location loc = getPreviousLocationInfo();
        return loc != null ? loc.getLongitude() : 0;
    }

    public double getTotalTravelled() {
        return totalTravelled;
    }

    public int getNumLegs() {
        return numLegs;
    }

    public void setNumLegs(int numLegs) {
        this.numLegs = numLegs;
    }

    public void setTotalTravelled(double totalTravelled) {
        if (totalTravelled == 0) {
            setNumLegs(1);
        } else {
            setNumLegs(getNumLegs() + 1);
        }
        this.totalTravelled = totalTravelled;
    }

    public Location getPreviousLocationInfo() {
        return previousLocationInfo;
    }

    public void setPreviousLocationInfo(Location previousLocationInfo) {
        this.previousLocationInfo = previousLocationInfo;
    }

    public boolean hasValidLocation() {
        return (getCurrentLocationInfo() != null && getCurrentLatitude() != 0 && getCurrentLongitude() != 0);
    }

    public double getCurrentLongitude() {
        if (getCurrentLocationInfo() != null) {
            return getCurrentLocationInfo().getLongitude();
        } else {
            return 0;
        }
    }

    public long getLatestTimeStamp() {
        return latestTimeStamp;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setLatestTimeStamp(long latestTimeStamp) {
        this.latestTimeStamp = latestTimeStamp;
    }

    public long getLatestPassiveTimeStamp() {
        return latestPassiveTimeStamp;
    }

    public void setLatestPassiveTimeStamp(long latestPassiveTimeStamp) {
        this.latestPassiveTimeStamp = latestPassiveTimeStamp;
    }

    public boolean shouldAddNewTrackSegment() {
        return addNewTrackSegment;
    }

    public void setAddNewTrackSegment(boolean addNewTrackSegment) {
        this.addNewTrackSegment = addNewTrackSegment;
    }

    public float getAutoSendDelay() {
        return autoSendDelay;
    }

    public void setAutoSendDelay(float autoSendDelay) {
        this.autoSendDelay = autoSendDelay;
    }

    public void setCurrentLocationInfo(Location currentLocationInfo) {
        this.currentLocationInfo = currentLocationInfo;
    }

    public Location getCurrentLocationInfo() {
        return currentLocationInfo;
    }

    public boolean isBoundToService() {
        return isBoundToService;
    }

    public void setBoundToService(boolean isBound) {
        this.isBoundToService = isBound;
    }

    public boolean hasDescription() {
        return description != null && description.length() > 0;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void clearDescription() {
        this.description = "";
    }

    public void setDescription(String newDescription) {
        this.description = newDescription == null ? "" : newDescription;
    }

    public boolean isWaitingForLocation() {
        return isWaitingForLocation;
    }

    public void setWaitingForLocation(boolean waitingForLocation) {
        this.isWaitingForLocation = waitingForLocation;
    }

    public boolean isAnnotationMarked() {
        return isAnnotationMarked;
    }

    public void setAnnotationMarked(boolean annotationMarked) {
        this.isAnnotationMarked = annotationMarked;
    }

    public String getCurrentFormattedFileName() {
        return currentFormattedFileName;
    }

    public void setCurrentFormattedFileName(String currentFormattedFileName) {
        this.currentFormattedFileName = currentFormattedFileName;
    }

    public long getUserStillSinceTimeStamp() {
        return userStillSinceTimeStamp;
    }

    public void setUserStillSinceTimeStamp(long lastUserStillTimeStamp) {
        this.userStillSinceTimeStamp = lastUserStillTimeStamp;
    }

    public long getSignificantMotionSensorCreationTimeStamp() {
        return significantMotionSensorCreationTimeStamp;
    }

    public void setSignificantMotionSensorCreationTimeStamp(long significantMotionSensorCreationTimeStamp) {
        this.significantMotionSensorCreationTimeStamp = significantMotionSensorCreationTimeStamp;
    }

    public long getFirstRetryTimeStamp() {
        return firstRetryTimeStamp;
    }

    public void setFirstRetryTimeStamp(long firstRetryTimeStamp) {
        this.firstRetryTimeStamp = firstRetryTimeStamp;
    }

    public Location getTemporaryLocationForBestAccuracy() {
        return temporaryLocationForBestAccuracy;
    }

    public void setTemporaryLocationForBestAccuracy(Location loc) {
        this.temporaryLocationForBestAccuracy = loc;
    }
}