package pers.fz.media.videocompressor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@SuppressLint("NewApi")
public class VideoController {
    static final int COMPRESS_QUALITY_HIGH = 1;
    static final int COMPRESS_QUALITY_MEDIUM = 2;
    static final int COMPRESS_QUALITY_LOW = 3;

    public static File cachedFile;
    public Uri path;

    public final static String MIME_TYPE = "video/avc";
    private final static int PROCESSOR_TYPE_OTHER = 0;
    private final static int PROCESSOR_TYPE_QCOM = 1;
    private final static int PROCESSOR_TYPE_INTEL = 2;
    private final static int PROCESSOR_TYPE_MTK = 3;
    private final static int PROCESSOR_TYPE_SEC = 4;
    private final static int PROCESSOR_TYPE_TI = 5;
    private static volatile VideoController Instance = null;
    private boolean videoConvertFirstWrite = true;

    interface CompressProgressListener {
        void onProgress(float percent);
    }

    public static VideoController getInstance() {
        VideoController localInstance = Instance;
        if (localInstance == null) {
            synchronized (VideoController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new VideoController();
                }
            }
        }
        return localInstance;
    }

    @SuppressLint("NewApi")
    public static int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        int lastColorFormat = 0;
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)) {
                lastColorFormat = colorFormat;
                if (!(codecInfo.getName().equals("OMX.SEC.AVC.Encoder") && colorFormat == 19)) {
                    return colorFormat;
                }
            }
        }
        return lastColorFormat;
    }

    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }

    public native static int convertVideoFrame(ByteBuffer src, ByteBuffer dest, int destFormat, int width, int height, int padding, int swap);

    private void didWriteData(final boolean last, final boolean error) {
        final boolean firstWrite = videoConvertFirstWrite;
        if (firstWrite) {
            videoConvertFirstWrite = false;
        }
    }

    public static class VideoConvertRunnable implements Runnable {

        private Uri videoPath;
        private String destPath;
        private Context context;
        private VideoConvertRunnable(Context context,Uri videoPath, String destPath) {
            this.videoPath = videoPath;
            this.destPath = destPath;
            this.context = context;
        }

        public static void runConversion(Context context,final Uri videoPath, final String destPath) {
           ThreadExecutor.getInstance().execute(()->{
               try {
                   VideoConvertRunnable wrapper = new VideoConvertRunnable(context,videoPath, destPath);
                   Thread th = new Thread(wrapper, "VideoConvertRunnable");
                   th.start();
                   th.join();
               } catch (Exception e) {
                   Log.e("tmessages", e.getMessage());
               }
           });
        }

        @Override
        public void run() {
            VideoController.getInstance().convertVideo(context,videoPath, destPath, 0, null);
        }
    }

    public static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo lastCodecInfo = null;
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mimeType)) {
                    lastCodecInfo = codecInfo;
                    if (!lastCodecInfo.getName().equals("OMX.SEC.avc.enc")) {
                        return lastCodecInfo;
                    } else if (lastCodecInfo.getName().equals("OMX.SEC.AVC.Encoder")) {
                        return lastCodecInfo;
                    }
                }
            }
        }
        return lastCodecInfo;
    }

    /**
     * Background conversion for queueing tasks
     *
     * @param path source file to compress
     * @param dest destination directory to put result
     */

    public void scheduleVideoConvert(Context context,Uri path, String dest) {
        startVideoConvertFromQueue(context,path, dest);
    }

    private void startVideoConvertFromQueue(Context context,Uri path, String dest) {
        VideoConvertRunnable.runConversion(context,path, dest);
    }

    @SuppressLint("WrongConstant")
    @TargetApi(16)
    private long readAndWriteTrack(MediaExtractor extractor, MP4Builder mediaMuxer, MediaCodec.BufferInfo info, long start, long end, File file, boolean isAudio) throws Exception {
        int trackIndex = selectTrack(extractor, isAudio);
        if (trackIndex >= 0) {
            extractor.selectTrack(trackIndex);
            MediaFormat trackFormat = extractor.getTrackFormat(trackIndex);
            int muxerTrackIndex = mediaMuxer.addTrack(trackFormat, isAudio);
            int maxBufferSize = trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            boolean inputDone = false;
            if (start > 0) {
                extractor.seekTo(start, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            } else {
                extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            }
            ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
            long startTime = -1;

            while (!inputDone) {

                boolean eof = false;
                int index = extractor.getSampleTrackIndex();
                if (index == trackIndex) {
                    info.size = extractor.readSampleData(buffer, 0);

                    if (info.size < 0) {
                        info.size = 0;
                        eof = true;
                    } else {
                        info.presentationTimeUs = extractor.getSampleTime();
                        if (start > 0 && startTime == -1) {
                            startTime = info.presentationTimeUs;
                        }
                        if (end < 0 || info.presentationTimeUs < end) {
                            info.offset = 0;
                            info.flags = extractor.getSampleFlags();
                            if (mediaMuxer.writeSampleData(muxerTrackIndex, buffer, info, isAudio)) {
                                // didWriteData(messageObject, file, false, false);
                            }
                            extractor.advance();
                        } else {
                            eof = true;
                        }
                    }
                } else if (index == -1) {
                    eof = true;
                }
                if (eof) {
                    inputDone = true;
                }
            }

            extractor.unselectTrack(trackIndex);
            return startTime;
        }
        return -1;
    }

    @TargetApi(16)
    private int selectTrack(MediaExtractor extractor, boolean audio) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (audio) {
                if (mime.startsWith("audio/")) {
                    return i;
                }
            } else {
                if (mime.startsWith("video/")) {
                    return i;
                }
            }
        }
        return -5;
    }

    /**
     * 执行视频压缩处理帧
     *
     * @param sourcePath      压缩前的文件
     * @param destinationPath 压缩后的视频文件
     * @return
     */
    public boolean convertVideo(Context context, final Uri sourcePath, String destinationPath, int quality, CompressProgressListener listener) {
        this.path = sourcePath;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        if (context == null || context.getApplicationContext().getContentResolver() == null) {
            return false;
        }
        retriever.setDataSource(context,sourcePath);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        long duration = Long.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;

        long startTime = -1;
        long endTime = -1;

        int rotationValue = Integer.valueOf(rotation);
        int originalWidth = Integer.valueOf(width);
        int originalHeight = Integer.valueOf(height);

        int resultWidth;
        int resultHeight;
        int bitrate;
        switch (quality) {
            default:
            case COMPRESS_QUALITY_HIGH:
                resultWidth = originalWidth * 2 / 3;
                resultHeight = originalHeight * 2 / 3;
                bitrate = resultWidth * resultHeight * 30;
                break;
            case COMPRESS_QUALITY_MEDIUM:

//              resultWidth = originalWidth / 2;
//              resultHeight = originalHeight / 2;

                resultWidth = originalWidth;
                resultHeight = originalHeight;
                bitrate = resultWidth * resultHeight * 5;
                Log.i("tangpeng", "bitrate=" + bitrate);

                break;
            case COMPRESS_QUALITY_LOW:
                resultWidth = originalWidth / 2;
                resultHeight = originalHeight / 2;
                bitrate = (resultWidth / 2) * (resultHeight / 2) * 10;
                break;
        }

        int rotateRender = 0;

        File cacheFile = new File(destinationPath);

        if (rotationValue == 90) {
            int temp = resultHeight;
            resultHeight = resultWidth;
            resultWidth = temp;
            rotationValue = 0;
            rotateRender = 270;
        } else if (rotationValue == 180) {
            rotateRender = 180;
            rotationValue = 0;
        } else if (rotationValue == 270) {
            int temp = resultHeight;
            resultHeight = resultWidth;
            resultWidth = temp;
            rotationValue = 0;
            rotateRender = 90;
        }

        FileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openFileDescriptor(path,"r").getFileDescriptor();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (!fileDescriptor.valid()) {
            didWriteData(true, true);
            return false;
        }

        videoConvertFirstWrite = true;
        boolean error = false;
        long videoStartTime = startTime;

        long time = System.currentTimeMillis();

        if (resultWidth != 0 && resultHeight != 0) {
            MP4Builder mediaMuxer = null;
            MediaExtractor extractor = null;
            try {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                //压缩后的文件配置相关信息
                Mp4Movie movie = new Mp4Movie();
                movie.setCacheFile(cacheFile);
                movie.setRotation(rotationValue);
                movie.setSize(resultWidth, resultHeight);
                mediaMuxer = new MP4Builder().createMovie(movie);
                extractor = new MediaExtractor();
                extractor.setDataSource(fileDescriptor);


                if (resultWidth != originalWidth || resultHeight != originalHeight) {
                    int videoIndex;
                    videoIndex = selectTrack(extractor, false);

                    if (videoIndex >= 0) {
                        MediaCodec decoder = null;
                        MediaCodec encoder = null;
                        InputSurface inputSurface = null;
                        OutputSurface outputSurface = null;

                        try {
                            long videoTime = -1;
                            boolean outputDone = false;
                            boolean inputDone = false;
                            boolean decoderDone = false;
                            int swapUV = 0;
                            int videoTrackIndex = -5;

                            int colorFormat;
                            int processorType = PROCESSOR_TYPE_OTHER;
                            String manufacturer = Build.MANUFACTURER.toLowerCase();
                            colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
                            Log.e("tmessages", "colorFormat = " + colorFormat);

                            int resultHeightAligned = resultHeight;
                            int padding = 0;
                            int bufferSize = resultWidth * resultHeight * 3 / 2;
                            if (resultHeight % 16 != 0) {
                                resultHeightAligned += (16 - (resultHeight % 16));
                                padding = resultWidth * (resultHeightAligned - resultHeight);
                                bufferSize += padding * 5 / 4;
                            }

                            extractor.selectTrack(videoIndex);
                            extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                            MediaFormat inputFormat = extractor.getTrackFormat(videoIndex);

                            //初始化解码器格式 预设宽高,设置相关参数
                            MediaFormat outputFormat = MediaFormat.createVideoFormat(MIME_TYPE, resultWidth, resultHeight);
                            //设置帧率
                            outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
                            outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
                            outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
                            outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);

                            // //通过多媒体格式名创建一个可用的解码器
                            encoder = MediaCodec.createEncoderByType(MIME_TYPE);
                            encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                            inputSurface = new InputSurface(encoder.createInputSurface());
                            inputSurface.makeCurrent();
                            encoder.start();

                            decoder = MediaCodec.createDecoderByType(inputFormat.getString(MediaFormat.KEY_MIME));
                            outputSurface = new OutputSurface();
                            //crypto:数据加密 flags:编码器/编码器
                            decoder.configure(inputFormat, outputSurface.getSurface(), null, 0);
                            decoder.start();

                            final int TIMEOUT_USEC = 2500;
                            ByteBuffer[] decoderInputBuffers = null;
                            ByteBuffer[] encoderOutputBuffers = null;
                            ByteBuffer[] encoderInputBuffers = null;

                            while (!outputDone) {
                                if (!inputDone) {
                                    boolean eof = false;
                                    int index = extractor.getSampleTrackIndex();
                                    if (index == videoIndex) {
                                        int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                                        if (inputBufIndex >= 0) {
                                            ByteBuffer inputBuf;
                                            inputBuf = decoder.getInputBuffer(inputBufIndex);
                                            int chunkSize = extractor.readSampleData(inputBuf, 0);
                                            if (chunkSize < 0) {
                                                decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                                inputDone = true;
                                            } else {
                                                decoder.queueInputBuffer(inputBufIndex, 0, chunkSize, extractor.getSampleTime(), 0);
                                                extractor.advance();
                                            }
                                        }
                                    } else if (index == -1) {
                                        eof = true;
                                    }
                                    if (eof) {
                                        int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                                        if (inputBufIndex >= 0) {
                                            decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                            inputDone = true;
                                        }
                                    }
                                }

                                boolean decoderOutputAvailable = !decoderDone;
                                boolean encoderOutputAvailable = true;
                                while (decoderOutputAvailable || encoderOutputAvailable) {
                                    int encoderStatus = encoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                                    if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                                        encoderOutputAvailable = false;
                                    } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                                    } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                        MediaFormat newFormat = encoder.getOutputFormat();
                                        if (videoTrackIndex == -5) {
                                            videoTrackIndex = mediaMuxer.addTrack(newFormat, false);
                                        }
                                    } else if (encoderStatus < 0) {
                                        throw new RuntimeException("unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                                    } else {
                                        ByteBuffer encodedData;
                                        encodedData = encoder.getOutputBuffer(encoderStatus);
                                        if (encodedData == null) {
                                            throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                                        }
                                        if (info.size > 1) {
                                            if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                                                if (mediaMuxer.writeSampleData(videoTrackIndex, encodedData, info, false)) {
                                                    didWriteData(false, false);
                                                }
                                            } else if (videoTrackIndex == -5) {
                                                byte[] csd = new byte[info.size];
                                                encodedData.limit(info.offset + info.size);
                                                encodedData.position(info.offset);
                                                encodedData.get(csd);
                                                ByteBuffer sps = null;
                                                ByteBuffer pps = null;
                                                for (int a = info.size - 1; a >= 0; a--) {
                                                    if (a > 3) {
                                                        if (csd[a] == 1 && csd[a - 1] == 0 && csd[a - 2] == 0 && csd[a - 3] == 0) {
                                                            sps = ByteBuffer.allocate(a - 3);
                                                            pps = ByteBuffer.allocate(info.size - (a - 3));
                                                            sps.put(csd, 0, a - 3).position(0);
                                                            pps.put(csd, a - 3, info.size - (a - 3)).position(0);
                                                            break;
                                                        }
                                                    } else {
                                                        break;
                                                    }
                                                }

                                                MediaFormat newFormat = MediaFormat.createVideoFormat(MIME_TYPE, resultWidth, resultHeight);
                                                if (sps != null && pps != null) {
                                                    newFormat.setByteBuffer("csd-0", sps);
                                                    newFormat.setByteBuffer("csd-1", pps);
                                                }
                                                videoTrackIndex = mediaMuxer.addTrack(newFormat, false);
                                            }
                                        }
                                        outputDone = (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                                        encoder.releaseOutputBuffer(encoderStatus, false);
                                    }
                                    if (encoderStatus != MediaCodec.INFO_TRY_AGAIN_LATER) {
                                        continue;
                                    }

                                    if (!decoderDone) {
                                        int decoderStatus = decoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                                        if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                                            decoderOutputAvailable = false;
                                        } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

                                        } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                            MediaFormat newFormat = decoder.getOutputFormat();
                                            Log.e("tmessages", "newFormat = " + newFormat);
                                        } else if (decoderStatus < 0) {
                                            throw new RuntimeException("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                                        } else {
                                            boolean doRender;
                                            doRender = info.size != 0;
                                            decoder.releaseOutputBuffer(decoderStatus, doRender);
                                            if (doRender) {
                                                boolean errorWait = false;
                                                try {
                                                    outputSurface.awaitNewImage();
                                                } catch (Exception e) {
                                                    errorWait = true;
                                                    Log.e("tmessages", e.getMessage());
                                                }
                                                if (!errorWait) {
                                                    outputSurface.drawImage(false);
                                                    inputSurface.setPresentationTime(info.presentationTimeUs * 1000);

                                                    if (listener != null) {
                                                        listener.onProgress((float) info.presentationTimeUs / (float) duration * 100);
                                                    }

                                                    inputSurface.swapBuffers();
                                                }
                                            }
                                            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                                decoderOutputAvailable = false;
                                                Log.e("tmessages", "decoder stream end");
                                                encoder.signalEndOfInputStream();
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("tmessages", e.getMessage());
                            error = true;
                        }

                        extractor.unselectTrack(videoIndex);

                        if (outputSurface != null) {
                            outputSurface.release();
                        }
                        if (inputSurface != null) {
                            inputSurface.release();
                        }
                        if (decoder != null) {
                            decoder.stop();
                            decoder.release();
                        }
                        if (encoder != null) {
                            encoder.stop();
                            encoder.release();
                        }
                    }
                } else {
                    long videoTime = readAndWriteTrack(extractor, mediaMuxer, info, startTime, endTime, cacheFile, false);
                    if (videoTime != -1) {
                        videoStartTime = videoTime;
                    }
                }
                if (!error) {
                    readAndWriteTrack(extractor, mediaMuxer, info, videoStartTime, endTime, cacheFile, true);
                }
            } catch (Exception e) {
                error = true;
                Log.e("tmessages", e.getMessage());
            } finally {
                if (extractor != null) {
                    extractor.release();
                }
                if (mediaMuxer != null) {
                    try {
                        mediaMuxer.finishMovie(false);
                    } catch (Exception e) {
                        Log.e("tmessages", e.getMessage());
                    }
                }
                Log.e("tmessages", "time = " + (System.currentTimeMillis() - time));
            }
        } else {
            didWriteData(true, true);
            return false;
        }
        didWriteData(true, error);

        cachedFile = cacheFile;
        Log.e("ViratPath", path + "");
        Log.e("ViratPath", cacheFile.getPath() + "");

        return true;
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(1, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }
}