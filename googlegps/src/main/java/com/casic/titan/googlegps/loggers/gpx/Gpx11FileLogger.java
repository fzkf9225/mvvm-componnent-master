package com.casic.titan.googlegps.loggers.gpx;

import android.location.Location;

import java.io.File;

/**
 * Extension of the Gpx10FileLogger that produces overrides key methods to produce a GPX 1.1 compliant output file
 */
public class Gpx11FileLogger extends Gpx10FileLogger {
    public Gpx11FileLogger(File gpxFile, boolean addNewTrackSegment) {
        super(gpxFile, addNewTrackSegment);
    }

    public Runnable getWriteHandler(String dateTimeString, File gpxFile, Location loc, boolean addNewTrackSegment)
    {
        return new Gpx11WriteHandler(dateTimeString, gpxFile, loc, addNewTrackSegment);
    }

}

class Gpx11WriteHandler extends com.casic.titan.googlegps.loggers.gpx.Gpx10WriteHandler {

    public Gpx11WriteHandler(String dateTimeString, File gpxFile, Location loc, boolean addNewTrackSegment) {
        super(dateTimeString, gpxFile, loc, addNewTrackSegment);
    }

    String getBeginningXml(String dateTimeString){
        // Use GPX 1.1 namespaces and put <time> inside a <metadata> element
        StringBuilder initialXml = new StringBuilder();
        initialXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        initialXml.append("<gpx version=\"1.1\" creator=\"GPSLogger " + " - http://gpslogger.mendhak.com/\" ");
        initialXml.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        initialXml.append("xmlns=\"http://www.topografix.com/GPX/1/1\" ");
        initialXml.append("xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v2\" ");
        initialXml.append("xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd ");
        initialXml.append("http://www.garmin.com/xmlschemas/TrackPointExtension/v2 https://www8.garmin.com/xmlschemas/TrackPointExtensionv2.xsd ");
        initialXml.append("\">");
        initialXml.append("<metadata><time>").append(dateTimeString).append("</time></metadata>");
        return initialXml.toString();
    }

    public void appendCourseAndSpeed(StringBuilder track, Location loc)
    {

        track.append("<extensions><gpxtpx:TrackPointExtension>");

        if (loc.hasBearing()) {
            track.append("<gpxtpx:bearing>").append(String.valueOf(loc.getBearing())).append("</gpxtpx:bearing>");
        }

        if (loc.hasSpeed()) {
            track.append("<gpxtpx:speed>").append(String.valueOf(loc.getSpeed())).append("</gpxtpx:speed>");
        }

        track.append("</gpxtpx:TrackPointExtension></extensions>");
    }
}
