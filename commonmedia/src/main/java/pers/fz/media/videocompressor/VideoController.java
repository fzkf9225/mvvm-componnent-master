package pers.fz.media.videocompressor;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

import pers.fz.media.MediaUtil;

public class VideoController {
    private static final String TAG = "VideoController";
    static final int COMPRESS_QUALITY_HIGH = 1;
    static final int COMPRESS_QUALITY_MEDIUM = 2;
    static final int COMPRESS_QUALITY_LOW = 3;

    public final static String MIME_TYPE = "video/avc";
    private static volatile VideoController instance = null;
    private boolean videoConvertFirstWrite = true;


    public static VideoController getInstance() {
        if (instance == null) {
            synchronized (VideoController.class) {
                if (instance == null) {
                    instance = new VideoController();
                }
            }
        }
        return instance;
    }

    private void didWriteData(final boolean last, final boolean error) {
        final boolean firstWrite = videoConvertFirstWrite;
        if (firstWrite) {
            videoConvertFirstWrite = false;
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

    @SuppressLint("WrongConstant")
    private void readAndWriteTrack(MediaExtractor extractor, MP4Writer mp4Writer, MediaCodec.BufferInfo info, boolean isAudio) throws Exception {
        // 获取并选择轨道
        int trackIndex = selectTrack(extractor, isAudio);
        if (trackIndex < 0) {
            return;
        }

        extractor.selectTrack(trackIndex);
        MediaFormat trackFormat = extractor.getTrackFormat(trackIndex);
        int muxerTrackIndex = mp4Writer.addTrack(trackFormat, isAudio);
        int maxBufferSize = trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);

        // Seek to the correct position
        extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
        boolean inputDone = false;

        // 读取和写入数据
        while (!inputDone) {
            int index = extractor.getSampleTrackIndex();
            if (index == trackIndex) {
                info.size = extractor.readSampleData(buffer, 0);

                // 判断是否为数据末尾
                if (info.size < 0) {
                    info.size = 0;
                    inputDone = true;  // 到达数据末尾
                } else {
                    info.presentationTimeUs = extractor.getSampleTime();
                    // 如果时间在有效范围内，则写入数据
                    info.offset = 0;
                    info.flags = extractor.getSampleFlags();

                    // 写入数据
                    if (mp4Writer.writeSampleData(muxerTrackIndex, buffer, info, isAudio)) {
                        // 写入成功
                        Log.d(TAG, "写入成功");
                    } else {
                        Log.d(TAG, "写入失败");
                    }
                    extractor.advance();
                }
            } else if (index == -1) {
                inputDone = true; // 如果没有数据
            }
        }

        // 取消选择轨道并返回开始时间
        extractor.unselectTrack(trackIndex);
    }

    private int selectTrack(MediaExtractor extractor, boolean audio) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            Log.d(TAG, "mime：" + mime);
            if (mime == null) {
                continue;
            }
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
    @SuppressLint("Range")
    public boolean convertVideo(Context context, final Uri sourcePath, String destinationPath, int quality, CompressListener listener) throws Exception {
        VideoInfo videoInfo = VideoUtils.getVideoInfo(context, sourcePath);
        if (videoInfo == null || videoInfo.width() == 0 || videoInfo.height() == 0) {
            return false;
        }

        File tempFile = VideoUtils.copyFileToCacheDir(context, sourcePath);
        Log.d(TAG, "videoInfo：" + videoInfo.toString());
        int rotationValue = videoInfo.rotation();

        int resultWidth;
        int resultHeight;
        int bitrate = switch (quality) {
            case COMPRESS_QUALITY_HIGH -> {
                if (rotationValue == 90) {
                    resultHeight = videoInfo.width() * 2 / 3;
                    resultWidth = videoInfo.height() * 2 / 3;
                    rotationValue = 0;
                } else if (rotationValue == 270) {
                    resultHeight = videoInfo.width() * 2 / 3;
                    resultWidth = videoInfo.height() * 2 / 3;
                    rotationValue = 0;
                } else {
                    rotationValue = 0;
                    resultWidth = videoInfo.width() * 2 / 3;
                    resultHeight = videoInfo.height() * 2 / 3;
                }
                yield resultWidth * resultHeight * 30;
            }
            case COMPRESS_QUALITY_MEDIUM -> {
                if (rotationValue == 90) {
                    resultHeight = videoInfo.width() / 2;
                    resultWidth = videoInfo.height() / 2;
                    rotationValue = 0;
                } else if (rotationValue == 270) {
                    resultHeight = videoInfo.width() / 2;
                    resultWidth = videoInfo.height() / 2;
                    rotationValue = 0;
                } else {
                    rotationValue = 0;
                    resultWidth = videoInfo.width() / 2;
                    resultHeight = videoInfo.height() / 2;
                }
                yield resultWidth * resultHeight * 20;
            }
            default -> {
                if (rotationValue == 90) {
                    resultHeight = videoInfo.width() / 3;
                    resultWidth = videoInfo.height() / 3;
                    rotationValue = 0;
                } else if (rotationValue == 270) {
                    resultHeight = videoInfo.width() / 3;
                    resultWidth = videoInfo.height() / 3;
                    rotationValue = 0;
                } else {
                    rotationValue = 0;
                    resultWidth = videoInfo.width() / 3;
                    resultHeight = videoInfo.height() / 3;
                }
                yield resultWidth * resultHeight * 10;
            }
        };
//        String fileName = MediaUtil.getNoRepeatFileName(tempFile.getParentFile().getName(), "VIDEO_", ".mp4");
//        File cacheFile = new File(tempFile.getParentFile(), fileName + ".mp4");
        File cacheFile = new File(destinationPath);
        Log.d(TAG, "cacheFile:" + cacheFile.getAbsolutePath());
        boolean error = false;

        long time = System.currentTimeMillis();

        MP4Writer mediaMuxer = null;
        MediaExtractor extractor = null;
        try {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            //压缩后的文件配置相关信息
            mediaMuxer = new MP4Writer().createMovie(new Mp4Movie(cacheFile, rotationValue, resultWidth, resultHeight),destinationPath);
            extractor = new MediaExtractor();
            extractor.setDataSource(tempFile.getAbsolutePath());

            if (resultWidth != videoInfo.width() || resultHeight != videoInfo.height()) {
                int videoIndex;
                videoIndex = selectTrack(extractor, false);

                if (videoIndex >= 0) {
                    MediaCodec decoder;
                    MediaCodec encoder;
                    InputSurface inputSurface;
                    OutputSurface outputSurface;

                    boolean outputDone = false;
                    boolean inputDone = false;
                    boolean decoderDone = false;
                    int videoTrackIndex = -5;

                    int colorFormat;
                    colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

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
                            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                videoTrackIndex = mediaMuxer.addTrack(encoder.getOutputFormat(), false);
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
                                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                    MediaFormat newFormat = decoder.getOutputFormat();
                                    Log.e(TAG, "newFormat = " + newFormat);
                                } else {
                                    boolean doRender;
                                    doRender = info.size != 0;
                                    decoder.releaseOutputBuffer(decoderStatus, doRender);
                                    if (doRender) {
                                        outputSurface.awaitNewImage();
                                        outputSurface.drawImage(false);
                                        inputSurface.setPresentationTime(info.presentationTimeUs * 1000);

                                        if (listener != null) {
                                            listener.onProgress((float) info.presentationTimeUs / (float) videoInfo.duration() * 100);
                                        }
                                        inputSurface.swapBuffers();
                                    }
                                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                        decoderOutputAvailable = false;
                                        Log.e(TAG, "decoder stream end");
                                        encoder.signalEndOfInputStream();
                                    }
                                }
                            }
                        }
                    }

                    extractor.unselectTrack(videoIndex);
                    outputSurface.release();
                    inputSurface.release();
                    decoder.stop();
                    decoder.release();
                    encoder.stop();
                    encoder.release();
                }
            } else {
                readAndWriteTrack(extractor, mediaMuxer, info, false);
            }
            readAndWriteTrack(extractor, mediaMuxer, info, true);
        } catch (Exception e) {
            error = true;
        } finally {
            if (extractor != null) {
                extractor.release();
            }
            if (mediaMuxer != null) {
                mediaMuxer.finishMovie(false);
            }
            Log.e(TAG, "time = " + (System.currentTimeMillis() - time));
        }
//        List<com.googlecode.mp4parser.authoring.Track> audioTracks = new LinkedList<>();// 音频通道集合
//        for (com.googlecode.mp4parser.authoring.Track inMovieTrack : MovieCreator.build(tempFile.getAbsolutePath()).getTracks()) {
//            if ("soun".equals(inMovieTrack.getHandler())) {// 从Movie对象中取出音频通道
//                audioTracks.add(inMovieTrack);
//            }
//        }
//
//        Movie resultMovie = new Movie();// 结果Movie对象[输出]
//        if (!audioTracks.isEmpty()) {// 将所有音频通道追加合并
//            resultMovie.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
//        }
//        List<Track> resultVideoTrack = MovieCreator.build(cacheFile.getAbsolutePath()).getTracks();
//        for(Track videoTrack:resultVideoTrack){
//            resultMovie.addTrack(videoTrack);
//        }
//        Container outContainer = new DefaultMp4Builder().build(resultMovie);// 将结果Movie对象封装进容器
//        FileChannel fileChannel = new RandomAccessFile(destinationPath, "rw").getChannel();
//        outContainer.writeContainer(fileChannel);// 将容器内容写入磁盘
//        fileChannel.close();
        didWriteData(true, error);
        return true;
    }

}