package com.casic.otitan.media.compressor.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

import com.casic.otitan.media.utils.LogUtil;

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
    private void readAndWriteTrack(MediaExtractor extractor, MP4Writer mp4Writer, MediaCodec.BufferInfo info) throws Exception {
        // 获取并选择轨道
        int trackIndex = selectTrack(extractor,false);
        if (trackIndex < 0) {
            return;
        }

        extractor.selectTrack(trackIndex);
        MediaFormat trackFormat = extractor.getTrackFormat(trackIndex);
        int muxerTrackIndex = mp4Writer.addTrack(trackFormat);
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
                    if (mp4Writer.writeSampleData(muxerTrackIndex, buffer, info)) {
                        // 写入成功
                        LogUtil.show(TAG, "写入成功");
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
            LogUtil.show(TAG, "mime：" + mime);
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
    public void convertVideo(Context context, final Uri sourcePath, String destinationPath, int quality, CompressListener listener) throws Exception {
        VideoInfo videoInfo = VideoUtils.getVideoInfo(context, sourcePath);
        if (videoInfo == null || videoInfo.width() == 0 || videoInfo.height() == 0) {
            throw new IllegalArgumentException("源视频信息读取失败！");
        }

        LogUtil.show(TAG, "videoInfo：" + videoInfo.toString());
        //拷贝到缓存目录，这样拥有绝对的读写权利不会出权限的各种错误,也可以用sourcePath，转换为FileDescriptor使用
        File tempFile = VideoUtils.copyFileToCacheDir(context, sourcePath);
        if (tempFile == null) {
            throw new IllegalArgumentException("拷贝临时文件出错！");
        }

        LogUtil.show(TAG, "拷贝目标临时文件路径：" + tempFile.getAbsolutePath());
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
                    resultHeight = videoInfo.width();
                    resultWidth = videoInfo.height();
                    rotationValue = 0;
                } else if (rotationValue == 270) {
                    resultHeight = videoInfo.width();
                    resultWidth = videoInfo.height();
                    rotationValue = 0;
                } else {
                    rotationValue = 0;
                    resultWidth = videoInfo.width();
                    resultHeight = videoInfo.height();
                }
                yield resultWidth * resultHeight * 5;
            }
            default -> {
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
                yield resultWidth * resultHeight * 10;
            }
        };
        //在tempFile的同路径下生成一个compress目录存放只有视频没有声音的视频文件
        File cacheFile = VideoUtils.createCompressVideoFileByTempFile(tempFile);
        LogUtil.show(TAG, "视频输出临时目录:" + cacheFile.getAbsolutePath());
        MP4Writer mp4Writer = null;
        MediaExtractor extractor = null;
        long time = System.currentTimeMillis();
        try {
            //压缩后的文件配置相关信息
            mp4Writer = new MP4Writer().createMovie(new Mp4Movie(cacheFile, rotationValue, resultWidth, resultHeight, bitrate));
            extractor = new MediaExtractor();
            extractor.setDataSource(tempFile.getAbsolutePath());
            writeVideoFile(videoInfo, extractor, mp4Writer, listener);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("压缩视频出错，" + e);
        } finally {
            if (extractor != null) {
                extractor.release();
            }
            if (mp4Writer != null) {
                mp4Writer.finishMovie(false);
            }
            LogUtil.show(TAG, "压缩用时：" + (System.currentTimeMillis() - time));
        }

        List<com.googlecode.mp4parser.authoring.Track> audioTracks = new LinkedList<>();// 音频通道集合
        for (com.googlecode.mp4parser.authoring.Track inMovieTrack : MovieCreator.build(tempFile.getAbsolutePath()).getTracks()) {
            if ("soun".equals(inMovieTrack.getHandler())) {// 从Movie对象中取出音频通道
                audioTracks.add(inMovieTrack);
            }
        }
        //合并音视频，因为只是压缩，音频和视频的帧数是相同的，所以可以直接合并，并不需要额外处理
        //这里并没有压缩音频，只是把音频通道追加到视频通道后面，这样在合并的时候，音频和视频的帧数是相同的
        Movie resultMovie = new Movie();// 结果Movie对象[输出]
        if (!audioTracks.isEmpty()) {// 将所有音频通道追加合并
            resultMovie.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        List<Track> resultVideoTrack = MovieCreator.build(cacheFile.getAbsolutePath()).getTracks();
        for (Track videoTrack : resultVideoTrack) {
            resultMovie.addTrack(videoTrack);
        }
        Container outContainer = new DefaultMp4Builder().build(resultMovie);// 将结果Movie对象封装进容器
        FileChannel fileChannel = new RandomAccessFile(destinationPath, "rw").getChannel();
        outContainer.writeContainer(fileChannel);// 将容器内容写入磁盘
        fileChannel.close();
        videoConvertFirstWrite = false;
    }

    private void writeVideoFile(VideoInfo videoInfo, MediaExtractor extractor, MP4Writer mp4Writer, CompressListener listener) throws Exception {
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        if (mp4Writer.getMp4Movie().getWidth() != videoInfo.width() || mp4Writer.getMp4Movie().getHeight() != videoInfo.height()) {
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
                MediaFormat outputFormat = MediaFormat.createVideoFormat(MIME_TYPE, mp4Writer.getMp4Movie().getWidth(), mp4Writer.getMp4Movie().getHeight());
                //设置帧率
                outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
                outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, mp4Writer.getMp4Movie().getBitrate());
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
                            videoTrackIndex = mp4Writer.addTrack(encoder.getOutputFormat());
                        } else {
                            ByteBuffer encodedData;
                            encodedData = encoder.getOutputBuffer(encoderStatus);
                            if (encodedData == null) {
                                throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                            }
                            if (info.size > 1) {
                                if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                                    if (mp4Writer.writeSampleData(videoTrackIndex, encodedData, info)) {
                                        videoConvertFirstWrite = false;
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
            readAndWriteTrack(extractor, mp4Writer, info);
        }
    }

}