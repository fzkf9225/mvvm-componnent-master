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

package com.casic.titan.googlegps.common;


import android.content.Context;
import android.os.Build;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {
    /**
     * Converts seconds into friendly, understandable description of the duration.
     *
     * @param numberOfSeconds
     * @return
     */
    public static String getDescriptiveDurationString(long numberOfSeconds) {

        String descriptive;
        long hours;
        long minutes;
        long seconds;

        long remainingSeconds;

        // Special cases
        if(numberOfSeconds==0){
            return "";
        }

        if (numberOfSeconds == 1) {
            return "1 秒";
        }

        if (numberOfSeconds == 30) {
            return "½ 分钟";
        }

        if (numberOfSeconds == 60) {
            return "1 分钟";
        }

        if (numberOfSeconds == 900) {
            return "15 分钟";
        }

        if (numberOfSeconds == 1800) {
            return "½ 小时";
        }

        if (numberOfSeconds == 3600) {
            return "1 小时";
        }

        if (numberOfSeconds == 4800) {
            return "1½ 小时";
        }

        if (numberOfSeconds == 9000) {
            return "2½ 小时";
        }

        // For all other cases, calculate

        hours = numberOfSeconds / 3600;
        remainingSeconds = numberOfSeconds % 3600;
        minutes = remainingSeconds / 60;
        seconds = remainingSeconds % 60;

        // Every 5 hours and 2 minutes
        // XYZ-5*2*20*

        descriptive = String.format("%s小时 %s分钟 %s秒",
                String.valueOf(hours), String.valueOf(minutes), String.valueOf(seconds));

        return descriptive;

    }
    /**
     * Makes string safe for writing to XML file. Removes lt and gt. Best used
     * when writing to file.
     *
     * @param desc
     * @return
     */
    public static String cleanDescriptionForXml(String desc) {
        desc = desc.replace("<", "");
        desc = desc.replace(">", "");
        desc = desc.replace("&", "&amp;");
        desc = desc.replace("\"", "&quot;");

        return desc;
    }

    public static String cleanDescriptionForJson(String desc){
        desc = desc.replace("\"", "");
        desc = desc.replace("\\","");
        return desc;
    }

    public static String getIsoDateTimeWithOffset(Date dateToFormat){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat ISO_8601_DATE_TIME = new SimpleDateFormat(getIsoDateTimeWithOffsetFormat(), Locale.US);
        ISO_8601_DATE_TIME.setTimeZone(calendar.getTimeZone());
        String dateTimeStringWithOffset = ISO_8601_DATE_TIME.format(dateToFormat);
        //Fix for older Android where ZZZZZ does not include colon
        if (!dateTimeStringWithOffset.substring(dateTimeStringWithOffset.length() - 3).startsWith(":")) {
            dateTimeStringWithOffset = dateTimeStringWithOffset.substring(0, dateTimeStringWithOffset.length() - 2) + ":" + dateTimeStringWithOffset.substring(dateTimeStringWithOffset.length() - 2);
        }
        return dateTimeStringWithOffset;
    }

    /**
     * Given a Date object, returns an ISO 8601 date time string in UTC.
     * Example: 2010-03-23T05:17:22Z but not 2010-03-23T05:17:22+04:00
     *
     * @param dateToFormat The Date object to format.
     * @return The ISO 8601 formatted string.
     */
    public static String getIsoDateTime(Date dateToFormat) {
        /**
         * This function is used in gpslogger.loggers.* and for most of them the
         * default locale should be fine, but in the case of HttpUrlLogger we
         * want machine-readable output, thus  Locale.US.
         *
         * Be wary of the default locale
         * http://developer.android.com/reference/java/util/Locale.html#default_locale
         */

        // GPX specs say that time given should be in UTC, no local time.
        SimpleDateFormat sdf = new SimpleDateFormat(getIsoDateTimeFormat(),
                Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return sdf.format(dateToFormat);
    }

    public static String getIsoDateTimeFormat() {
        return "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    }

    public static String getIsoDateTimeWithOffsetFormat() {
        return "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ";
    }


    /**
     * Given a Date object, returns an ISO 8601 calendar date string.
     * Example: 2010-03-23
     *
     * @param dateToFormat The Date object to format.
     * @return The ISO 8601 formatted string.
     */
    public static String getIsoCalendarDate(Date dateToFormat) {
        /**
         * This function is used in CustomUrlLogger.
         */

        // GPX specs say that time given should be in UTC, no local time.
        SimpleDateFormat sdf = new SimpleDateFormat(getIsoCalendarDateFormat(),
                Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return sdf.format(dateToFormat);
    }

    public static String getIsoCalendarDateFormat() {
        return "yyyy-MM-dd";
    }

    public static String getReadableDateTime(Date dateToFormat) {
        /**
         * Similar to getIsoDateTime(), this function is used in
         * AutoEmailManager, and we want machine-readable output.
         */
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm",
                Locale.US);
        return sdf.format(dateToFormat);
    }

    /**
     * Checks if a string is null or empty
     *
     * @param text
     * @return
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null ||  text.trim().length() == 0;
    }

    public static String htmlDecode(String text) {
        if (isNullOrEmpty(text)) {
            return text;
        }

        return text.replace("&amp;", "&").replace("&quot;", "\"");
    }

    public static String getBuildSerial() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return Build.SERIAL;
            }
            else {
                return Systems.getAndroidId();
            }
        } catch (Throwable t) {
            return "";
        }
    }

    public static String getFormattedFileName(){
        return getFormattedFileName(Session.getInstance(), PreferenceHelper.getInstance());
    }

    public static String getFormattedFileName(Session session, PreferenceHelper ph) {
        String currentFileName = session.getCurrentFileName();
        if (ph.shouldCreateCustomFile() && !Strings.isNullOrEmpty(currentFileName)) {
            return getFormattedCustomFileName(currentFileName, GregorianCalendar.getInstance(), ph);
        } else {
            if (!Strings.isNullOrEmpty(currentFileName) && ph.shouldPrefixSerialToFileName() && !currentFileName.contains(String.valueOf(getBuildSerial()))) {
                currentFileName = String.valueOf(getBuildSerial()) + "_" + currentFileName;
            }
        }
        return currentFileName;
    }

    public static String getFormattedCustomFileName(String baseName, Calendar calendar, PreferenceHelper ph){

        String finalFileName = baseName;
        finalFileName = finalFileName.replaceAll("(?i)%ser", String.valueOf(getBuildSerial()));
        finalFileName = finalFileName.replaceAll("(?i)%hour", String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)));
        finalFileName = finalFileName.replaceAll("(?i)%min", String.format("%02d", calendar.get(Calendar.MINUTE)));
        finalFileName = finalFileName.replaceAll("(?i)%year",  String.valueOf(calendar.get(Calendar.YEAR)));
        finalFileName = finalFileName.replaceAll("(?i)%monthname", new SimpleDateFormat("MMM", Locale.ENGLISH).format(calendar.getTime()).toLowerCase());
        finalFileName = finalFileName.replaceAll("(?i)%month", String.format("%02d", calendar.get(Calendar.MONTH) +1));
        finalFileName = finalFileName.replaceAll("(?i)%dayname", new SimpleDateFormat("EE", Locale.ENGLISH).format(calendar.getTime()).toLowerCase());
        finalFileName = finalFileName.replaceAll("(?i)%day", String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH) ));
        finalFileName = finalFileName.replaceAll("(?i)%profile", String.valueOf(ph.getCurrentProfileName()));
        return finalFileName;
    }

    public static int toInt(String number, int defaultVal) {
        try {
            return Integer.parseInt(number);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static String getDistanceDisplay(Context context, double meters, boolean imperial, boolean autoscale) {
        DecimalFormat df = new DecimalFormat("#.###");
        String result = df.format(meters) + "&#160;米";

        if(imperial){
            if (!autoscale || (meters <= 804)){
                result = df.format(meters * 3.2808399) + "&#160;英尺/秒";
            }
            else {
                result = df.format(meters/1609.344) + "&#160;英哩";
            }
        }
        else if(autoscale && (meters >= 1000)) {
            result = df.format(meters/1000) +"&#160;公里";
        }

        return result;
    }

    public static String getSanitizedMarkdownForFaqView(String md){
        if(Strings.isNullOrEmpty(md)){ return "";}

        //         \[[^\]]+\]\(((?!http)[^\)]+)\)
        //         Matches any Markdown link that isn't starting with http...
        String output = md;

        Matcher imgMatcher = Pattern.compile("(!\\[[^\\]]+\\]\\((?!http)[^\\)]+\\))", Pattern.MULTILINE).matcher(md);
        while(imgMatcher.find()){
            String group = imgMatcher.group(1);
            output = output.replace(group,"");
        }

        Matcher linkMatcher = Pattern.compile("\\[[^\\]]+\\]\\(((?!http)[^\\)]+)\\)", Pattern.MULTILINE).matcher(md);
        while(linkMatcher.find()){
            String group = linkMatcher.group(1);
            output = output.replace(group,"https://gpslogger.app/"+group);
        }

        return output;
    }

    public static String getDegreesMinutesSeconds(double decimaldegrees, boolean isLatitude) {
        String cardinality = (decimaldegrees<0) ? "S":"N";

        if(!isLatitude){
            cardinality = (decimaldegrees<0) ? "W":"E";
        }

        //Remove negative sign
        decimaldegrees = Math.abs(decimaldegrees);

        int deg =  (int) Math.floor(decimaldegrees);
        double minfloat = (decimaldegrees-deg)*60;
        int min = (int) Math.floor(minfloat);
        double secfloat = (minfloat-min)*60;
        double sec = Math.round(secfloat * 10000.0)/10000.0;

        // After rounding, the seconds might become 60. These two
        // if-tests are not necessary if no rounding is done.
        if (sec==60) {
            min++;
            sec=0;
        }
        if (min==60) {
            deg++;
            min=0;
        }


        return ("" + deg + "° " + min + "' " + sec + "\" " + cardinality);
    }

    public static String getDegreesDecimalMinutes(double decimaldegrees, boolean isLatitude) {
        String cardinality = (decimaldegrees<0) ? "S":"N";

        if(!isLatitude){
            cardinality = (decimaldegrees<0) ? "W":"E";
        }

        //Remove negative sign
        decimaldegrees = Math.abs(decimaldegrees);

        int deg =  (int) Math.floor(decimaldegrees);
        double minfloat = (decimaldegrees-deg)*60;
        double min = Math.round(minfloat*10000.0)/10000.0;

        return ("" + deg + "° " + min + "' " + cardinality);
    }

    public static String getDecimalDegrees(double decimaldegrees) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(6);
        return nf.format(decimaldegrees);
    }

    public static String getFormattedLatitude(double decimaldegrees){
        return getFormattedDegrees(decimaldegrees, true, PreferenceHelper.getInstance());
    }

    public static String getFormattedLongitude(double decimaldegrees){
        return getFormattedDegrees(decimaldegrees, false, PreferenceHelper.getInstance());
    }

    static String getFormattedDegrees(double decimaldegrees, boolean isLatitude, PreferenceHelper ph){
        switch(ph.getDisplayLatLongFormat()){

            case DEGREES_MINUTES_SECONDS:
                return getDegreesMinutesSeconds(decimaldegrees, isLatitude);

            case DEGREES_DECIMAL_MINUTES:
                return getDegreesDecimalMinutes(decimaldegrees, isLatitude);

            case DECIMAL_DEGREES:
            default:
                return getDecimalDegrees(decimaldegrees);

        }
    }

    public static String getUrlEncodedString(String message){
        try {
            return URLEncoder.encode(message, "UTF-8").replace("%3A", ":");
        } catch (Exception e) {
            return message;
        }
    }

    public static String getHexColorCodeFromInt(int color){
        return String.format("#%06X", (0xFFFFFF & color));
    }

    /**
     * F-Droid requires API keys be kept in source control
     * Here is a little obscurity.
     */
    public static String GetDropBox() {
        return (new Object() {
            int t;

            public String toString() {
                byte[] buf = new byte[15];
                t = 406508688;
                buf[0] = (byte) (t >>> 9);
                t = -2115861152;
                buf[1] = (byte) (t >>> 18);
                t = 2064462109;
                buf[2] = (byte) (t >>> 13);
                t = 1957901922;
                buf[3] = (byte) (t >>> 4);
                t = 443804309;
                buf[4] = (byte) (t >>> 22);
                t = 1381098959;
                buf[5] = (byte) (t >>> 10);
                t = 1791215717;
                buf[6] = (byte) (t >>> 17);
                t = -333759886;
                buf[7] = (byte) (t >>> 14);
                t = 1700473980;
                buf[8] = (byte) (t >>> 14);
                t = -2146362421;
                buf[9] = (byte) (t >>> 7);
                t = -1132636394;
                buf[10] = (byte) (t >>> 4);
                t = -1171088381;
                buf[11] = (byte) (t >>> 7);
                t = 1889063667;
                buf[12] = (byte) (t >>> 24);
                t = -443656636;
                buf[13] = (byte) (t >>> 13);
                t = 648401020;
                buf[14] = (byte) (t >>> 10);
                return new String(buf);
            }
        }.toString());

    }

    /**
     * F-Droid requires API keys be kept in source control
     * Here is a little obscurity.
     */
    public static String GetOSM(){
        return (new Object() {
            int t;

            public String toString() {
                byte[] buf = new byte[42];
                t = 1630577506;
                buf[0] = (byte) (t >>> 3);
                t = 1300449509;
                buf[1] = (byte) (t >>> 11);
                t = 1378988002;
                buf[2] = (byte) (t >>> 22);
                t = -1202854278;
                buf[3] = (byte) (t >>> 13);
                t = 797718293;
                buf[4] = (byte) (t >>> 7);
                t = 984242259;
                buf[5] = (byte) (t >>> 15);
                t = -1005885689;
                buf[6] = (byte) (t >>> 8);
                t = 433764492;
                buf[7] = (byte) (t >>> 18);
                t = -260681935;
                buf[8] = (byte) (t >>> 12);
                t = 1779455648;
                buf[9] = (byte) (t >>> 24);
                t = 1935554145;
                buf[10] = (byte) (t >>> 14);
                t = 1456973065;
                buf[11] = (byte) (t >>> 4);
                t = 204895709;
                buf[12] = (byte) (t >>> 12);
                t = -880012697;
                buf[13] = (byte) (t >>> 14);
                t = 619961294;
                buf[14] = (byte) (t >>> 23);
                t = 1207979712;
                buf[15] = (byte) (t >>> 8);
                t = 1293518812;
                buf[16] = (byte) (t >>> 18);
                t = -2102434169;
                buf[17] = (byte) (t >>> 17);
                t = -773650315;
                buf[18] = (byte) (t >>> 5);
                t = -48054848;
                buf[19] = (byte) (t >>> 11);
                t = 1318983186;
                buf[20] = (byte) (t >>> 21);
                t = -1528174941;
                buf[21] = (byte) (t >>> 10);
                t = 1103163740;
                buf[22] = (byte) (t >>> 9);
                t = 193516488;
                buf[23] = (byte) (t >>> 3);
                t = 1762581203;
                buf[24] = (byte) (t >>> 3);
                t = -1131074653;
                buf[25] = (byte) (t >>> 14);
                t = 1451518128;
                buf[26] = (byte) (t >>> 9);
                t = -188717159;
                buf[27] = (byte) (t >>> 18);
                t = 946974836;
                buf[28] = (byte) (t >>> 24);
                t = 2000001073;
                buf[29] = (byte) (t >>> 12);
                t = -52017343;
                buf[30] = (byte) (t >>> 4);
                t = -1645904154;
                buf[31] = (byte) (t >>> 12);
                t = -704373344;
                buf[32] = (byte) (t >>> 6);
                t = -529636483;
                buf[33] = (byte) (t >>> 4);
                t = 187868194;
                buf[34] = (byte) (t >>> 19);
                t = -913821365;
                buf[35] = (byte) (t >>> 13);
                t = 57752068;
                buf[36] = (byte) (t >>> 20);
                t = -1975699846;
                buf[37] = (byte) (t >>> 15);
                t = -1750489267;
                buf[38] = (byte) (t >>> 15);
                t = -1305551221;
                buf[39] = (byte) (t >>> 13);
                t = -1234360133;
                buf[40] = (byte) (t >>> 10);
                t = -2113284386;
                buf[41] = (byte) (t >>> 19);
                return new String(buf);
            }
        }.toString());
    }



}
