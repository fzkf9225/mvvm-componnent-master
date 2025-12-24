package com.casic.otitan.googlegps.socket;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.zip.CRC32;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by fz on 2023/10/19 13:31
 * describe :基于 Netty 实现的 TCP Socket 客户端，主要用于向指定服务器发送 GPS 定位数据，并支持断线重连、心跳维持、数据加密和连接状态监听等功能
 */
public class GPSSocketServer {
    public final static String TAG = GPSSocketServer.class.getSimpleName();
    private final static String TCP_IP = "192.168.1.221";
    private final static int TCP_PORT = 60000;
    /**
     * 最多重试次数
     */
    private final static int MAX_CONNECT_TIME = 3;
    /**
     * 重试时间间隔
     */
    private final static int RETRY_INTERVAL = 1000;
    /**
     * 连接超时时间
     */
    private final static int TIME_OUT = 15000;
    /**
     * 当前连接错误次数
     */
    private int errorCount = 0;
    private final Handler handler;
    private OnConnectionChangedListener onConnectionChangedListener;
    private SocketChannel socketChannel;
    private static final String THREAD_NAME = "socketServiceThread";
    private ConnectionRunnable connectionRunnable = null;

    private GPSSocketServer() {
        HandlerThread thread = new HandlerThread(THREAD_NAME, 6);
        thread.start();
        handler = new Handler(thread.getLooper());
        LogUtil.show(TAG, "----------------GPSSocketServer-----------------");
    }

    private static final class GpsServerHolder {
        static final GPSSocketServer GPS_SERVER = new GPSSocketServer();
    }

    public static GPSSocketServer getInstance() {
        return GpsServerHolder.GPS_SERVER;
    }

    private Bootstrap bootstrap;
    private NioEventLoopGroup nioEventLoopGroup;

    public void setOnConnectionChangedListener(OnConnectionChangedListener onConnectionChangedListener) {
        this.onConnectionChangedListener = onConnectionChangedListener;
    }

    public OnConnectionChangedListener getOnConnectionChangedListener() {
        return onConnectionChangedListener;
    }

    public boolean isConnected() {
        if (bootstrap == null) {
            return false;
        }
        if (socketChannel == null) {
            return false;
        }
        return socketChannel.isActive();
    }

    /**
     * 连接socket
     */
    public void connect(@NotNull SocketCallbackListener socketCallbackListener) {
        //重置错误次数
        errorCount = 0;
        if (connectionRunnable != null) {
            handler.removeCallbacks(connectionRunnable);
        }
        handler.post(connectionRunnable = new ConnectionRunnable(MAX_CONNECT_TIME, socketCallbackListener));
    }

    /**
     * 连接socket
     */
    public void connect(int retryTimes, @NotNull SocketCallbackListener socketCallbackListener) {
        //重置错误次数
        errorCount = 0;
        if (connectionRunnable != null) {
            handler.removeCallbacks(connectionRunnable);
        }
        handler.post(connectionRunnable = new ConnectionRunnable(retryTimes, socketCallbackListener));
    }

    private void connectServer(int retryTimes, @NotNull SocketCallbackListener socketCallbackListener) {
        LogUtil.show(TAG, "开始尝试连接，第" + (errorCount + 1) + "次");
        //避免重连时大量重新创建对象
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIME_OUT)
                    .group(nioEventLoopGroup = new NioEventLoopGroup())
                    .channel(NioSocketChannel.class)
                    .handler(new InboundClientHandler(GPSSocketServer.this));
        }
        try {
            bootstrap.connect(TCP_IP, TCP_PORT).sync()
                    .addListener((ChannelFutureListener) future -> {
                        LogUtil.show(TAG, "------------------addListener：" + future.isSuccess() + "------------------");
                        if (future.isSuccess()) {
                            socketChannel = (SocketChannel) future.channel();
                            connectionRunnable = null;
                            socketCallbackListener.callBack(true, "连接成功");
                            if (onConnectionChangedListener != null) {
                                onConnectionChangedListener.onConnected();
                            }
                        } else {
                            LogUtil.show(TAG, "连接失败");
                            errorCount++;
                            if (errorCount >= retryTimes) {
                                connectionRunnable = null;
                                socketCallbackListener.callBack(false, "连接失败已达最大次数，将不会再重试！");
                                LogUtil.show(TAG, "连接失败已达最大次数，将不会再重试！");
                                return;
                            }
                            LogUtil.show(TAG, "第" + (errorCount - 1) + "次连接失败," + (RETRY_INTERVAL / 1000) + "秒后将重试第" + (errorCount + 1) + "次");
                            handler.postDelayed(connectionRunnable = new ConnectionRunnable(retryTimes, socketCallbackListener), RETRY_INTERVAL);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            errorCount++;
            if (errorCount >= retryTimes) {
                connectionRunnable = null;
                socketCallbackListener.callBack(false, "连接失败已达最大次数，将不会再重试！");
                LogUtil.show(TAG, "连接失败已达最大次数，将不会再重试！");
                return;
            }
            LogUtil.show(TAG, "第" + (errorCount) + "次连接异常," + e);
            LogUtil.show(TAG, (RETRY_INTERVAL / 1000) + "秒后将重试第" + (errorCount + 1) + "次");
            handler.postDelayed(connectionRunnable = new ConnectionRunnable(retryTimes, socketCallbackListener), RETRY_INTERVAL);
        }
    }

    /**
     * 连接tcp的线程
     */
    public final class ConnectionRunnable implements Runnable {
        private final int retryTimes;
        private final @NotNull SocketCallbackListener socketCallbackListener;

        public ConnectionRunnable(int retryTimes, @NotNull SocketCallbackListener socketCallbackListener) {
            this.retryTimes = retryTimes;
            this.socketCallbackListener = socketCallbackListener;
        }

        @Override
        public void run() {
            connectServer(retryTimes, socketCallbackListener);
        }
    }


    /**
     * 推送消息给服务端，处理服务端的推送消息回执给服务端
     *
     * @param msg                    消息内容
     * @param socketCallbackListener 回调
     */
    public void sendMsg(String msg, @NotNull SocketCallbackListener socketCallbackListener) {
        SendMessageThreadExecutor.getInstance().execute(() -> {
            if (socketChannel == null || !socketChannel.isOpen()) {
                socketCallbackListener.callBack(true, "WebSocket不在链接中，发送消息失败");
                return;
            }
            LogUtil.show(TAG, "sendMsg发送消息内容：" + msg);
            //转换字节发送，
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeBytes(msg.getBytes());
            //直接发送文本TextWebSocketFrame
            socketChannel.writeAndFlush(buffer)
                    .addListener((ChannelFutureListener) future -> {
                        LogUtil.show(TAG, "------------------sendMsg是否成功：" + future.isSuccess() + "------------------");
                        if (future.isSuccess()) {
                            handler.post(() -> socketCallbackListener.callBack(true, "发送消息成功"));
                        } else {
                            handler.post(() -> socketCallbackListener.callBack(true, "发送消息失败"));
                        }
                    });
        });
    }

    /**
     * 发送信息异常
     *
     * @param longitude              经度
     * @param latitude               纬度
     * @param socketCallbackListener 发送结果
     */
    public void sendMsg(Context context, double longitude, double latitude, SocketCallbackListener socketCallbackListener) {
        SendMessageThreadExecutor.getInstance().execute(() -> {
            if (socketChannel == null || !socketChannel.isOpen()) {
                socketCallbackListener.callBack(true, "WebSocket不在链接中，发送消息失败");
                return;
            }
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeBytes(encode(context, longitude, latitude).getBytes());
            socketChannel.writeAndFlush(buffer)
                    .addListener((ChannelFutureListener) future -> {
                        LogUtil.show(TAG, "------------------sendMsg是否成功：" + future.isSuccess() + "------------------");
                        if (future.isSuccess()) {
                            handler.post(() -> socketCallbackListener.callBack(true, "发送消息成功"));
                        } else {
                            handler.post(() -> socketCallbackListener.callBack(true, "发送消息失败"));
                        }
                    });
        });
    }

    /**
     * 1字节=2个16进制位
     * 消息名称	 字节数	              说明	                                            明文/密文
     * 帧头标志      1               固定数值，0xFF                                        明文
     * <p>
     * 版本         1               固定数值，0x01                                        密文
     * 帧序号	    2	            0~65535	                                            密文
     * 设备ID	    8	            唯一序列号	                                    密文
     * 时间戳	    8	        从协调世界时（UTC）1970年1月1日00:00:00起至现在的秒数	        密文
     * 经度	        8	            双精度浮点型	                                        密文
     * 纬度	        8	            双精度浮点型	                                        密文
     * 速度	        4	            单精度浮点型	                                        密文
     * 高程	        4	            单精度浮点型，单位:米	                                密文
     * <p>
     * CRC	        2	            CRC校验，校验范围从"版本"开始,"高程"结束 	                明文
     * 帧尾标识	    1	            固定数值，0xEE	                                    明文
     * <p>
     * 长度 = 命令字 + 参数 + 校验和 ，不包括帧头和长度字节；
     * 校验和 = 帧头 + 长度 + 命令字 + 参数的字节累加和。
     */
    public String encode(Context context, Double longitude, Double latitude) {

        String data = StringUtils.leftPad(Integer.toHexString(1), 2, '0')
                + StringUtils.leftPad(Integer.toHexString(new Random().nextInt(65535)), 4, '0')
                + StringUtils.leftPad(PhoneUtils.getUniqueCode(context), 16, '0')
                + StringUtils.leftPad(Long.toHexString(System.currentTimeMillis()), 16, '0')
                + StringUtils.leftPad(Long.toHexString(Double.doubleToLongBits(longitude)), 16, '0')
                + StringUtils.leftPad(Long.toHexString(Double.doubleToLongBits(latitude)), 16, '0')
                + StringUtils.leftPad(Long.toHexString(Double.doubleToLongBits(0)), 8, '0')
                + StringUtils.leftPad(Long.toHexString(Double.doubleToLongBits(0)), 8, '0');
        CRC32 crc32 = new CRC32();
        crc32.update(data.getBytes());
        String s = "FF" + data + StringUtils.leftPad(Long.toHexString(crc32.getValue()), 8, '0') + "EE";
        LogUtil.show(TAG, "密文:" + s);
        return s;
    }

    public void disconnect(@NotNull SocketCallbackListener socketCallbackListener) {
        try {
            if (socketChannel == null) {
                socketCallbackListener.callBack(false, "socket为空");
                return;
            }
            if (!socketChannel.isActive() || socketChannel.isShutdown()) {
                socketCallbackListener.callBack(true, "socket连接已断开，无需重复断开");
                return;
            }
            if (!socketChannel.isOutputShutdown()) {
                socketChannel.shutdownOutput();
            }
            if (!socketChannel.isInputShutdown()) {
                socketChannel.shutdownInput();
            }
            if (!socketChannel.isActive()) {
                socketChannel.shutdown();
                socketChannel = null;
            }
            if (nioEventLoopGroup != null && !nioEventLoopGroup.isShutdown()) {
                nioEventLoopGroup.shutdownGracefully();
            }
            if (onConnectionChangedListener != null) {
                onConnectionChangedListener.onClosed();
            }
            socketCallbackListener.callBack(true, "断开连接成功");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show(TAG, "断开连接异常：" + e);
            socketCallbackListener.callBack(false, "断开连接异常：" + e);
        }
    }

}
