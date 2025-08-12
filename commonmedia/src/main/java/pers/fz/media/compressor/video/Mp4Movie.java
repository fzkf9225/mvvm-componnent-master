package pers.fz.media.compressor.video;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.googlecode.mp4parser.util.Matrix;

import java.io.File;
import java.util.ArrayList;

public class Mp4Movie {
    private Matrix matrix = Matrix.ROTATE_0;
    private final ArrayList<Track> tracks = new ArrayList<Track>();
    private File cacheFile;
    private int width;
    private int height;
    private int bitrate;
    public Mp4Movie(File cacheFile, int angle, int width, int height,int bitrate) {
        this.cacheFile = cacheFile;
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
        setRotation(angle);
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setCacheFile(File file) {
        cacheFile = file;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setRotation(int angle) {
        if (angle == 0) {
            matrix = Matrix.ROTATE_0;
        } else if (angle == 90) {
            matrix = Matrix.ROTATE_90;
        } else if (angle == 180) {
            matrix = Matrix.ROTATE_180;
        } else if (angle == 270) {
            matrix = Matrix.ROTATE_270;
        }
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public void addSample(int trackIndex, long offset, MediaCodec.BufferInfo bufferInfo) throws Exception {
        if (trackIndex < 0 || trackIndex >= tracks.size()) {
            return;
        }
        Track track = tracks.get(trackIndex);
        track.addSample(offset, bufferInfo);
    }

    public int addTrack(MediaFormat mediaFormat) throws Exception {
        tracks.add(new Track(tracks.size(), mediaFormat));
        return tracks.size() - 1;
    }
}
