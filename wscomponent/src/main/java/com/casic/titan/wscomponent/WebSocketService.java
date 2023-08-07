package com.casic.titan.wscomponent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.gson.Gson;
import com.jeremyliao.liveeventbus.LiveEventBus;


import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

/**
 * Created by fangzhou on 2023/5/5 09:52
 * describe:长链接socket
 */
public class WebSocketService extends Service {
    private SocketChannel socketChannel;
    private Handler handler;
    private String status = ResponseCode.SOCKET_CONNECT_FAILURE;
    public WebSocketBinder mBinder = new WebSocketBinder();
    private Handler loginHandler;
    /**
     * 失败重试间隔
     */
    private final static long RETRY_PERIOD = 3000;
    private final static int HEART_TIME = 30;
    private URI websocketUri = null;
    private String userId;
    private Callback<?> callback = null;
    /**
     * 回调
     */
    private MutableLiveData<WebSocketException> exceptionMutableLiveData = new MutableLiveData<>();

    public MutableLiveData<WebSocketException> getExceptionMutableLiveData() {
        return exceptionMutableLiveData;
    }

    private final List<Observer<WebSocketEvent<String>>> observers = new ArrayList<>();


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


    public class WebSocketBinder extends Binder {
        public void onStartConnect(String socketUrl, String userIdStr, Callback call) {
            LogUtil.show("-----------------开始连接WebSocket--------------");
            if (TextUtils.isEmpty(socketUrl)) {
                throw new RuntimeException("websocket地址为空");
            }
            callback = call;
            userId = userIdStr;
            websocketUri = URI.create(socketUrl);
            loginHandler.removeCallbacks(runnable);
            loginHandler.post(runnable);
        }

        /**
         * 订阅
         *
         * @param sn
         */
        public void subscribe(String sn, SendMessageListener sendMessageListener) {
            sendMsg(createSubscribeStr(sn, SubscribeEnum.getAppSubscribe()), sendMessageListener);
        }

        public WebSocketService getService() {
            return WebSocketService.this;
        }

        public void onCloseConnect() {
            LogUtil.show("-----------------断开连接--------------");
            loginHandler.removeCallbacks(runnable);
            close();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.myLooper());
        loginHandler = new Handler(Looper.myLooper());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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
                            WebSocketClientHandler webSocketClientHandler = new WebSocketClientHandler();
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
    private String createPingStr() {
        WebSocketBean<HeaderBean, String> webSocketBean = new WebSocketBean<>();
        HeaderBean headerBean = new HeaderBean();
        headerBean.setUserId(userId);
        headerBean.setMsgId(MsgIdEnum.DRONE_DEVICE_WS_PP.getValue());
        webSocketBean.setHeader(headerBean);
        webSocketBean.setBody(PushEnum.PING.getKey());
        LogUtil.show("心跳包内容：" + new Gson().toJson(webSocketBean));
        return new Gson().toJson(webSocketBean);
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
        return new Gson().toJson(webSocketBean);
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
    private void close() {
        if (socketChannel != null) {
            socketChannel.close();
            socketChannel = null;
        }
    }

    /**
     * 处理消息
     */
    private class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
        //握手的状态信息
        private WebSocketClientHandshaker handshaker;
        //netty自带的异步处理
        private ChannelPromise handshakeFuture;
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            Channel ch = ctx.channel();
            FullHttpResponse response;
            //进行握手操作
            if (!this.handshaker.isHandshakeComplete()) {
                try {
                    response = (FullHttpResponse) msg;
                    //握手协议返回，设置结束握手
                    this.handshaker.finishHandshake(ch, response);
                    //设置成功
                    this.handshakeFuture.setSuccess();
                    LogUtil.show("握手成功");
                    updateStatus(ResponseCode.SOCKET_SUCCESS);
                    callback.onEvent(ResponseCode.SOCKET_SUCCESS, "success", null);
                } catch (WebSocketHandshakeException var7) {
                    FullHttpResponse res = (FullHttpResponse) msg;
                    String errorMsg = String.format("握手失败,status:%s,reason:%s", res.status(), res.content().toString(CharsetUtil.UTF_8));
                    LogUtil.show(errorMsg);
                    exceptionMutableLiveData.postValue(new WebSocketException(res.content().toString(), res.status().code()));
                    close();
                    callback.onEvent(res.status().code() + "", "WebSocket握手失败", null);
                    updateStatus(ResponseCode.SOCKET_SHAKE_HAND_FAILURE);
                    this.handshakeFuture.trySuccess();
//                    this.handshakeFuture.setFailure(new WebSocketException(res.content().toString(), res.status().code()));
                }
                return;
            }

            //接收服务端的消息
            WebSocketFrame frame = (WebSocketFrame) msg;
            //ping信息
            if (frame instanceof PongWebSocketFrame) {
                TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                LogUtil.show("ping信息返回:" + textFrame.text());
            }
            //文本信息
            if (frame instanceof TextWebSocketFrame) {
                TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                LogUtil.show("客户端接收的消息是:" + textFrame.text());
                if (PushEnum.PONG.getKey().equalsIgnoreCase(textFrame.text()) ||
                        PushEnum.PONG_SYMBOL.getKey().equalsIgnoreCase(textFrame.text())) {
                    return;
                }
                //观察者模式
                notifyObservers(new WebSocketEvent<String>(PushEnum.PUSH, textFrame.text()));
                //LiveEventBus推送消息模式，其实原理都差不多
                LiveEventBus.get(WebSocketService.class.getSimpleName()).post(new WebSocketEvent<String>(PushEnum.PUSH, textFrame.text()));
            }
            //二进制信息
            if (frame instanceof BinaryWebSocketFrame) {
                BinaryWebSocketFrame binFrame = (BinaryWebSocketFrame) frame;
                LogUtil.show("BinaryWebSocketFrame");
            }

            //关闭消息
            if (frame instanceof CloseWebSocketFrame) {
                LogUtil.show("receive close frame");
                ch.close();
            }
        }

        /**
         * Handler活跃状态，表示连接成功
         *
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            LogUtil.show("与服务端连接成功");
        }

        /**
         * 非活跃状态，没有连接远程主机的时候。
         *
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            LogUtil.show("--------连接断开了---------");
            close();
            updateStatus(ResponseCode.SOCKET_CONNECT_FAILURE);
            retryLogin();
        }

        /**
         * 利用写空闲发送心跳检测消息
         *
         * @param ctx
         * @param evt
         * @throws Exception
         */
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
            LogUtil.show("---------------自动发送心跳包,心跳时间:" + new Date() + "-------------");
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if (e.state() == IdleState.WRITER_IDLE) {
                    ctx.writeAndFlush(new TextWebSocketFrame(createPingStr())).addListener(future -> LogUtil.show(future.isSuccess() ? "心跳包发送成功！" : "心跳包发送失败！"));
                }
            }
        }

        /**
         * 异常处理
         *
         * @param ctx
         * @param cause
         * @throws Exception
         */
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            LogUtil.show("连接异常：" + cause.getMessage());
            if (cause instanceof WebSocketException) {
                exceptionMutableLiveData.postValue((WebSocketException) cause);
            } else {
                exceptionMutableLiveData.postValue(new WebSocketException(cause.getMessage(), cause, Integer.parseInt(ResponseCode.OTHER_FAILURE)));
            }
            close();
            updateStatus(ResponseCode.SOCKET_CONNECT_FAILURE);
            retryLogin();
//            sendMsg(createAppResponse(AppResponse.ResultFail(cause.getMessage())), receiveMsgCallback);
            super.exceptionCaught(ctx, cause);
        }

        public void handlerAdded(ChannelHandlerContext ctx) {
            this.handshakeFuture = ctx.newPromise();
        }

        public void setHandshaker(WebSocketClientHandshaker handshaker) {
            this.handshaker = handshaker;
        }

        public ChannelFuture handshakeFuture() {
            return this.handshakeFuture;
        }
    }

    private final Runnable runnable = this::connect;

    /**
     * 重新连接
     */
    private void retryLogin() {
        loginHandler.postDelayed(runnable, RETRY_PERIOD);
    }

    private void updateStatus(String status) {
        if (!this.status.equals(status)) {
            LogUtil.show("原status：" + this.status + " 更新为：" + status);
            this.status = status;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //设置service为前台服务，提高优先级
        String channelId = PropertiesUtil.getInstance().getProperties(getApplicationContext()).getPropertyValue(ConstantsHelper.WEB_SOCKET_CHANNEL_ID, ConstantsHelper.CHANNEL_ID);
        String channelName = PropertiesUtil.getInstance().getProperties(getApplicationContext()).getPropertyValue(ConstantsHelper.WEB_SOCKET_CHANNEL_NAME, ConstantsHelper.CHANNEL_NAME);
        String notifyId = PropertiesUtil.getInstance().getProperties(getApplicationContext()).getPropertyValue(ConstantsHelper.WEB_SOCKET_NOTIFY_ID, String.valueOf(ConstantsHelper.NOTIFY_ID));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Android8.0以上app启动后通知栏会出现一条"正在运行"的通知
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(getApplicationContext(), channelId).build();
                startForeground(Integer.parseInt(notifyId), notification);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (nioEventLoopGroup != null && !nioEventLoopGroup.isShutdown()) {
            nioEventLoopGroup.shutdownGracefully();
        }
        close();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH);
            stopSelf();
        }
    }
}
