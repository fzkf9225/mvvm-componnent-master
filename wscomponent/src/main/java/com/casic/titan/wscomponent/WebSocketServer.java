package com.casic.titan.wscomponent;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by fz on 2023/11/22 11:30
 * describe:长链接socket
 */
public class WebSocketServer {
    private volatile static WebSocketServer instance = null;
    private SocketChannel socketChannel;
    private final Handler handler;
    private String status = ResponseCode.SOCKET_CONNECT_FAILURE;
    private final Handler loginHandler;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    /**
     * 失败重试间隔
     */
    private final static long RETRY_PERIOD = 3000;
    private final static int HEART_TIME = 30;
    private URI websocketUri = null;
    private String userId;
    protected Callback<?> callback = null;
    /**
     * 回调
     */
    protected final MutableLiveData<WebSocketException> exceptionMutableLiveData = new MutableLiveData<>();

    public MutableLiveData<WebSocketException> getExceptionMutableLiveData() {
        return exceptionMutableLiveData;
    }

    private final List<Observer<WebSocketEvent<String>>> observers = new ArrayList<>();

    private static final String THREAD_NAME = "webSocketServerThread";

    private WebSocketServer() {
        HandlerThread thread = new HandlerThread(THREAD_NAME, 10);
        thread.start();
        handler = new Handler(thread.getLooper());
        loginHandler = new Handler(thread.getLooper());
        LogUtil.show("----------------GPSSocketServer-----------------");
    }
    public static WebSocketServer getInstance() {
        if (instance == null) {
            synchronized (WebSocketServer.class) {
                if (instance == null) {
                    instance = new WebSocketServer();
                }
            }
        }
        return instance;
    }
    public void addObserver(Observer<WebSocketEvent<String>> observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer<WebSocketEvent<String>> observer) {
        observers.remove(observer);
    }

    public void notifyObservers(WebSocketEvent<String> webSocketEvent) {
        if (observers.isEmpty()) {
            return;
        }
        for (Observer<WebSocketEvent<String>> observer : observers) {
            observer.onChanged(webSocketEvent);
        }
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
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

    public void onStartConnect(String socketUrl, String userIdStr, Callback<?> call) {
        LogUtil.show("-----------------开始连接WebSocket--------------");
        if (isConnected()) {
            if (call != null) {
                call.onEvent(ResponseCode.SOCKET_SUCCESS, "通道已连接,请勿重复连接！", null);
            }
            return;
        }
        if (TextUtils.isEmpty(socketUrl)) {
            if (call != null) {
                call.onEvent(ResponseCode.OTHER_FAILURE, "websocket地址为空", null);
            }
            return;
        }
        callback = call;
        userId = userIdStr;
        websocketUri = URI.create(socketUrl);
        loginHandler.removeCallbacks(runnable);
        loginHandler.post(runnable);
    }

    /**
     * 订阅
     * @param sn
     */
    public void subscribe(String sn, SendMessageListener sendMessageListener) {
        if(!isConnected()){
            if (sendMessageListener != null) {
                sendMessageListener.sendResult(ResponseCode.SEND_MESSAGE_FAILURE, "连接已断开");
            }
            return;
        }
        sendMsg(createSubscribeStr(sn, SubscribeEnum.getAppSubscribe()), sendMessageListener);
    }

    public void onStop() {
        try {
            LogUtil.show("-----------------断开连接--------------");
            loginHandler.removeCallbacks(runnable);
            if (socketChannel != null) {
                socketChannel.shutdown();
                socketChannel.close();
                socketChannel = null;
            }
            if (nioEventLoopGroup != null && !nioEventLoopGroup.isShutdown()) {
                nioEventLoopGroup.shutdownGracefully();
            }
            instance = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bootstrap bootstrap;
    private NioEventLoopGroup nioEventLoopGroup;

    /**
     * 连接socket
     */
    private void connect() {
        if (status.equals(ResponseCode.SOCKET_SUCCESS)) {
            return;
        }
        //避免重连时大量重新创建对象
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            //netty基本操作，启动类
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .group(nioEventLoopGroup = new NioEventLoopGroup())
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            HttpHeaders httpHeaders = new DefaultHttpHeaders();
                            //进行握手
                            WebSocketClientHandshaker handShaker = WebSocketClientHandshakerFactory.newHandshaker(websocketUri, WebSocketVersion.V13, (String) null,
                                    true, httpHeaders);
                            pipeline.addLast(new IdleStateHandler(2, 4, HEART_TIME, TimeUnit.SECONDS));
                            pipeline.addLast("http-codec", new HttpClientCodec());
                            // 支持异步发送大的码流(大的文件传输),但不占用过多的内存，防止java内存溢出
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new WebSocketClientProtocolHandler(handShaker));
                            // http 消息聚合器  512*1024为接收的最大content-length
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            WebSocketClientHandler webSocketClientHandler = new WebSocketClientHandler(WebSocketServer.this);
                            webSocketClientHandler.setHandshaker(handShaker);
                            pipeline.addLast(webSocketClientHandler);
                        }
                    });
        }
        bootstrap.connect(new InetSocketAddress(websocketUri.getHost(), websocketUri.getPort()))
                .addListener((ChannelFutureListener) future -> {
                    LogUtil.show("------------------addListener：" + future.isSuccess() + "------------------");
                    if (future.isSuccess()) {
                        socketChannel = (SocketChannel) future.channel();
                    } else {
                        //这里本应该关闭nioEventLoopGroup，但是我们又无限重试，所以不能关闭，因此放在了onDestroy生命周期中
                        close();
                        retryLogin();
                    }
                });
    }

    /**
     * 生成心跳包内容
     *
     * @return
     */
    protected String createPingStr() {
        WebSocketBean<HeaderBean, String> webSocketBean = new WebSocketBean<>();
        HeaderBean headerBean = new HeaderBean();
        headerBean.setUserId(userId);
        headerBean.setMsgId(MsgIdEnum.DRONE_DEVICE_WS_PP.getValue());
        webSocketBean.setHeader(headerBean);
        webSocketBean.setBody(PushEnum.PING.getKey());
        LogUtil.show("心跳包内容：" + new Gson().toJson(webSocketBean));
        return gson.toJson(webSocketBean);
    }

    /**
     * 生成心跳包内容
     *
     * @return
     */
    private String createSubscribeStr(String deviceSns, String topic) {
        WebSocketBean<HeaderBean, SubscribeBean> webSocketBean = new WebSocketBean<>();
        HeaderBean headerBean = new HeaderBean();
        headerBean.setUserId(userId);
        webSocketBean.setHeader(headerBean);
        webSocketBean.setBody(new SubscribeBean(false, deviceSns, topic));
        return gson.toJson(webSocketBean);
    }

    /**
     * 推送消息给服务端，处理服务端的推送消息回执给服务端
     *
     * @param msg                 消息内容
     * @param sendMessageListener 回调
     */
    public void sendMsg(String msg, SendMessageListener sendMessageListener) {
        if (!status.equals(ResponseCode.SOCKET_SUCCESS)) {
            sendMessageListener.sendResult(ResponseCode.SEND_MESSAGE_FAILURE, "WebSocket不在链接中，发送消息失败");
            return;
        }
        LogUtil.show("sendMsg发送消息内容：" + msg);
        socketChannel.writeAndFlush(new TextWebSocketFrame(msg))
                .addListener((ChannelFutureListener) future -> {
                    LogUtil.show("------------------sendMsg是否成功：" + future.isSuccess() + "------------------");
                    if (callback == null) {
                        return;
                    }
                    if (future.isSuccess()) {
                        handler.post(() -> sendMessageListener.sendResult(ResponseCode.SEND_MESSAGE_SUCCESS, "发送消息成功"));
                    } else {
                        handler.post(() -> sendMessageListener.sendResult(ResponseCode.SEND_MESSAGE_FAILURE, "发送消息失败"));
                    }
                });
    }

    protected void close() {
        if (socketChannel != null) {
            socketChannel.close();
            socketChannel = null;
        }
    }

    private final Runnable runnable = this::connect;

    /**
     * 重新连接
     */
    protected void retryLogin() {
        loginHandler.postDelayed(runnable, RETRY_PERIOD);
    }

    protected void updateStatus(String status) {
        if (!this.status.equals(status)) {
            LogUtil.show("原status：" + this.status + " 更新为：" + status);
            this.status = status;
        }
    }
}
