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

package com.casic.titan.googlegps.loggers.kml;

import android.location.Location;

import com.casic.titan.googlegps.common.PreferenceHelper;
import com.casic.titan.googlegps.common.RejectionHandler;
import com.casic.titan.googlegps.common.Strings;
import com.casic.titan.googlegps.common.slf4j.Logs;
import com.casic.titan.googlegps.loggers.FileLogger;
import com.casic.titan.googlegps.loggers.Files;

import org.slf4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Kml22FileLogger implements FileLogger {
    protected final static Object lock = new Object();
    private final static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(10), new RejectionHandler());
    private final boolean addNewTrackSegment;
    private final File kmlFile;
    protected final String name = "KML";


    public Kml22FileLogger(File kmlFile, boolean addNewTrackSegment) {
        this.kmlFile = kmlFile;
        this.addNewTrackSegment = addNewTrackSegment;
    }


    public void write(Location loc) throws Exception {
        Kml22WriteHandler writeHandler = new Kml22WriteHandler(loc, kmlFile, addNewTrackSegment);
        EXECUTOR.execute(writeHandler);
    }

    public void annotate(String description, Location loc) throws Exception {
        
        description = Strings.cleanDescriptionForXml(description);
        
        Kml22AnnotateHandler annotateHandler = new Kml22AnnotateHandler(kmlFile, description, loc);
        EXECUTOR.execute(annotateHandler);
    }

    @Override
    public String getName() {
        return name;
    }
}

class Kml22AnnotateHandler implements Runnable {
    private static final Logger LOG = Logs.of(Kml22AnnotateHandler.class);
    File kmlFile;
    String description;
    Location loc;
    int kmlAnnotationOffset = 258;

    public Kml22AnnotateHandler(File kmlFile, String description, Location loc) {
        this.kmlFile = kmlFile;
        this.description = description;
        this.loc = loc;
    }


    @Override
    public void run() {
        if(!Files.reallyExists(kmlFile)){
            return;
        }

        try {
            synchronized (Kml22FileLogger.lock) {

                String descriptionNode = getPlacemarkXml(description, loc);


                RandomAccessFile r = new RandomAccessFile(kmlFile, "rw");
                File tmpFile = new File(kmlFile.getAbsolutePath() + "~");
                tmpFile.createNewFile();
                RandomAccessFile rtemp = new RandomAccessFile(tmpFile, "rw");
                long fileSize = r.length();
                FileChannel sourceChannel = r.getChannel();
                FileChannel targetChannel = rtemp.getChannel();
                sourceChannel.transferTo(kmlAnnotationOffset, (fileSize - kmlAnnotationOffset), targetChannel);
                sourceChannel.truncate(kmlAnnotationOffset);
                r.seek(kmlAnnotationOffset);
                r.write(descriptionNode.getBytes());
                long newOffset = r.getFilePointer();
                targetChannel.position(0L);
                sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - kmlAnnotationOffset));
                sourceChannel.close();
                targetChannel.close();
                tmpFile.delete();


            }
        } catch (Exception e) {
            LOG.error("Kml22FileLogger.annotate", e);
        }
    }

    String getPlacemarkXml(String description, Location loc) {
        StringBuilder descriptionNode = new StringBuilder();
        descriptionNode.append("\n<Placemark><name>");
        descriptionNode.append(description);
        descriptionNode.append("</name><Point><coordinates>");
        descriptionNode.append(String.valueOf(loc.getLongitude()));
        descriptionNode.append(",");
        descriptionNode.append(String.valueOf(loc.getLatitude()));
        descriptionNode.append(",");
        descriptionNode.append(String.valueOf(loc.getAltitude()));
        descriptionNode.append("</coordinates></Point></Placemark>\n");

        return descriptionNode.toString();
    }
}

class Kml22WriteHandler implements Runnable {

    private static final Logger LOG = Logs.of(Kml22WriteHandler.class);
    boolean addNewTrackSegment;
    File kmlFile;
    Location loc;


    public Kml22WriteHandler(Location loc, File kmlFile, boolean addNewTrackSegment) {

        this.loc = loc;
        this.kmlFile = kmlFile;
        this.addNewTrackSegment = addNewTrackSegment;
    }


    @Override
    public void run() {
        try {

            RandomAccessFile raf;

            String dateTimeString = Strings.getIsoDateTime(new Date(loc.getTime()));
            if(PreferenceHelper.getInstance().shouldWriteTimeWithOffset()){
                dateTimeString = Strings.getIsoDateTimeWithOffset(new Date(loc.getTime()));
            }
            String placemarkHead = "<Placemark>\n<gx:Track>\n";
            String placemarkTail = "</gx:Track>\n</Placemark></Document></kml>\n";

            synchronized (Kml22FileLogger.lock) {

                if(!Files.reallyExists(kmlFile)){
                    kmlFile.createNewFile();

                    FileOutputStream initialWriter = new FileOutputStream(kmlFile, true);
                    BufferedOutputStream initialOutput = new BufferedOutputStream(initialWriter);

                    StringBuilder initialXml = new StringBuilder();
                    initialXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    initialXml.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\" ");
                    initialXml.append("xmlns:gx=\"http://www.google.com/kml/ext/2.2\" ");
                    initialXml.append("xmlns:kml=\"http://www.opengis.net/kml/2.2\" ");
                    initialXml.append("xmlns:atom=\"http://www.w3.org/2005/Atom\">");
                    initialXml.append("<Document>");
                    initialXml.append("<name>").append(dateTimeString).append("</name>\n");

                    initialXml.append("</Document></kml>\n");
                    initialOutput.write(initialXml.toString().getBytes());
                    initialOutput.flush();
                    initialOutput.close();

                    //New file, so new track segment
                    addNewTrackSegment = true;
                }


                if (addNewTrackSegment) {
                    raf = new RandomAccessFile(kmlFile, "rw");
                    raf.seek(kmlFile.length() - 18);
                    raf.write((placemarkHead + placemarkTail).getBytes());
                    raf.close();

                }

                StringBuilder coords = new StringBuilder();
                coords.append("\n<when>");
                coords.append(dateTimeString);
                coords.append("</when>\n<gx:coord>");
                coords.append(String.valueOf(loc.getLongitude()));
                coords.append(" ");
                coords.append(String.valueOf(loc.getLatitude()));
                coords.append(" ");
                coords.append(String.valueOf(loc.getAltitude()));
                coords.append("</gx:coord>\n");
                coords.append(placemarkTail);

                raf = new RandomAccessFile(kmlFile, "rw");
                raf.seek(kmlFile.length() - 42);
                raf.write(coords.toString().getBytes());
                raf.close();
                Files.addToMediaDatabase(kmlFile, "text/xml");
                LOG.debug("Finished writing to KML22 File");
            }

        } catch (Exception e) {
            LOG.error("Kml22FileLogger.write", e);
        }
    }
}
