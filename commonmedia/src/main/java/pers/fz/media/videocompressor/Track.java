package pers.fz.media.videocompressor;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.coremedia.iso.boxes.AbstractMediaHeaderBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.VideoMediaHeaderBox;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.mp4parser.iso14496.part15.AvcConfigurationBox;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Track {
    private long trackId = 0;
    private ArrayList<Sample> samples = new ArrayList<Sample>();
    private long duration = 0;
    private String handler;
    private AbstractMediaHeaderBox headerBox = null;
    private SampleDescriptionBox sampleDescriptionBox = null;
    private LinkedList<Integer> syncSamples = null;
    private int timeScale;
    private Date creationTime = new Date();
    private int height;
    private int width;
    private float volume = 0;
    private ArrayList<Long> sampleDurations = new ArrayList<Long>();
    private static Map<Integer, Integer> samplingFrequencyIndexMap = new HashMap<Integer, Integer>();
    private long lastPresentationTimeUs = 0;
    private boolean first = true;

    static {
        samplingFrequencyIndexMap.put(96000, 0x0);
        samplingFrequencyIndexMap.put(88200, 0x1);
        samplingFrequencyIndexMap.put(64000, 0x2);
        samplingFrequencyIndexMap.put(48000, 0x3);
        samplingFrequencyIndexMap.put(44100, 0x4);
        samplingFrequencyIndexMap.put(32000, 0x5);
        samplingFrequencyIndexMap.put(24000, 0x6);
        samplingFrequencyIndexMap.put(22050, 0x7);
        samplingFrequencyIndexMap.put(16000, 0x8);
        samplingFrequencyIndexMap.put(12000, 0x9);
        samplingFrequencyIndexMap.put(11025, 0xa);
        samplingFrequencyIndexMap.put(8000, 0xb);
    }

    public Track(int id, MediaFormat format) throws Exception {
        trackId = id;
        sampleDurations.add(3015L);
        duration = 3015;
        width = format.getInteger(MediaFormat.KEY_WIDTH);
        height = format.getInteger(MediaFormat.KEY_HEIGHT);
        timeScale = 90000;
        syncSamples = new LinkedList<Integer>();
        handler = "vide";
        headerBox = new VideoMediaHeaderBox();
        sampleDescriptionBox = new SampleDescriptionBox();
        String mime = format.getString(MediaFormat.KEY_MIME);
        if ("video/avc".equals(mime)) {
            VisualSampleEntry visualSampleEntry = new VisualSampleEntry("avc1");
            visualSampleEntry.setDataReferenceIndex(1);
            visualSampleEntry.setDepth(24);
            visualSampleEntry.setFrameCount(1);
            visualSampleEntry.setHorizresolution(72);
            visualSampleEntry.setVertresolution(72);
            visualSampleEntry.setWidth(width);
            visualSampleEntry.setHeight(height);

            AvcConfigurationBox avcConfigurationBox = new AvcConfigurationBox();

            if (format.getByteBuffer("csd-0") != null) {
                ArrayList<byte[]> spsArray = new ArrayList<byte[]>();
                ByteBuffer spsBuff = format.getByteBuffer("csd-0");
                if (spsBuff != null) {
                    spsBuff.position(4);
                    byte[] spsBytes = new byte[spsBuff.remaining()];
                    spsBuff.get(spsBytes);
                    spsArray.add(spsBytes);
                }

                ArrayList<byte[]> ppsArray = new ArrayList<byte[]>();
                ByteBuffer ppsBuff = format.getByteBuffer("csd-1");
                if (ppsBuff != null) {
                    ppsBuff.position(4);
                    byte[] ppsBytes = new byte[ppsBuff.remaining()];
                    ppsBuff.get(ppsBytes);
                    ppsArray.add(ppsBytes);
                }
                avcConfigurationBox.setSequenceParameterSets(spsArray);
                avcConfigurationBox.setPictureParameterSets(ppsArray);
            }
            //ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(spsBytes);
            //SeqParameterSet seqParameterSet = SeqParameterSet.read(byteArrayInputStream);

            avcConfigurationBox.setAvcLevelIndication(13);
            avcConfigurationBox.setAvcProfileIndication(100);
            avcConfigurationBox.setBitDepthLumaMinus8(-1);
            avcConfigurationBox.setBitDepthChromaMinus8(-1);
            avcConfigurationBox.setChromaFormat(-1);
            avcConfigurationBox.setConfigurationVersion(1);
            avcConfigurationBox.setLengthSizeMinusOne(3);
            avcConfigurationBox.setProfileCompatibility(0);

            visualSampleEntry.addBox(avcConfigurationBox);
            sampleDescriptionBox.addBox(visualSampleEntry);
        } else if ("video/mp4v".equals(mime)) {
            VisualSampleEntry visualSampleEntry = new VisualSampleEntry("mp4v");
            visualSampleEntry.setDataReferenceIndex(1);
            visualSampleEntry.setDepth(24);
            visualSampleEntry.setFrameCount(1);
            visualSampleEntry.setHorizresolution(72);
            visualSampleEntry.setVertresolution(72);
            visualSampleEntry.setWidth(width);
            visualSampleEntry.setHeight(height);

            sampleDescriptionBox.addBox(visualSampleEntry);
        }
    }

    public long getTrackId() {
        return trackId;
    }

    public void addSample(long offset, MediaCodec.BufferInfo bufferInfo) {
        boolean isSyncFrame = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
        samples.add(new Sample(offset, bufferInfo.size));
        if (syncSamples != null && isSyncFrame) {
            syncSamples.add(samples.size());
        }

        long delta = bufferInfo.presentationTimeUs - lastPresentationTimeUs;
        lastPresentationTimeUs = bufferInfo.presentationTimeUs;
        delta = (delta * timeScale + 500000L) / 1000000L;
        if (!first) {
            sampleDurations.add(sampleDurations.size() - 1, delta);
            duration += delta;
        }
        first = false;
    }

    public ArrayList<Sample> getSamples() {
        return samples;
    }

    public long getDuration() {
        return duration;
    }

    public String getHandler() {
        return handler;
    }

    public AbstractMediaHeaderBox getMediaHeaderBox() {
        return headerBox;
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return sampleDescriptionBox;
    }

    public long[] getSyncSamples() {
        if (syncSamples == null || syncSamples.isEmpty()) {
            return null;
        }
        long[] returns = new long[syncSamples.size()];
        for (int i = 0; i < syncSamples.size(); i++) {
            returns[i] = syncSamples.get(i);
        }
        return returns;
    }

    public int getTimeScale() {
        return timeScale;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getVolume() {
        return volume;
    }

    public ArrayList<Long> getSampleDurations() {
        return sampleDurations;
    }
}
