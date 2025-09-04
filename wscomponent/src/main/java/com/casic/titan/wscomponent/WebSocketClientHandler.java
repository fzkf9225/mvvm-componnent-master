package com.casic.titan.wscomponent;


import java.util.Date;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

/**
 * Created by fz on 2023/11/22 11:38
 * describe :
 */
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private final WebSocketServer webSocketServer;
    /**
     * 握手的状态信息
     */
    private WebSocketClientHandshaker handshaker;
    /**
     * netty自带的异步处理
     */
    private ChannelPromise handshakeFuture;

    public WebSocketClientHandler(WebSocketServer webSocketServer) {
        this.webSocketServer = webSocketServer;
    }

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
                webSocketServer.updateStatus(ResponseCode.SOCKET_SUCCESS);
                webSocketServer.callback.onEvent(ResponseCode.SOCKET_SUCCESS, "success", null);
            } catch (WebSocketHandshakeException var7) {
                FullHttpResponse res = (FullHttpResponse) msg;
                String errorMsg = String.format("握手失败,status:%s,reason:%s", res.status(), res.content().toString(CharsetUtil.UTF_8));
                LogUtil.show(errorMsg);
                webSocketServer.exceptionMutableLiveData.postValue(new WebSocketException(res.content().toString(), res.status().code()));
                webSocketServer.close();
                webSocketServer.callback.onEvent(res.status().code() + "", "WebSocket握手失败", null);
                webSocketServer.updateStatus(ResponseCode.SOCKET_SHAKE_HAND_FAILURE);
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
            webSocketServer.notifyObservers(new WebSocketEvent<String>(PushEnum.PUSH, textFrame.text()));
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
        webSocketServer.close();
        webSocketServer.updateStatus(ResponseCode.SOCKET_CONNECT_FAILURE);
        webSocketServer.retryLogin();
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
                ctx.writeAndFlush(new TextWebSocketFrame(webSocketServer.createPingStr())).addListener(future -> LogUtil.show(future.isSuccess() ? "心跳包发送成功！" : "心跳包发送失败！"));
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
            webSocketServer.exceptionMutableLiveData.postValue((WebSocketException) cause);
        } else {
            webSocketServer.exceptionMutableLiveData.postValue(new WebSocketException(cause.getMessage(), cause, Integer.parseInt(ResponseCode.OTHER_FAILURE)));
        }
        webSocketServer.close();
        webSocketServer.updateStatus(ResponseCode.SOCKET_CONNECT_FAILURE);
        webSocketServer.retryLogin();
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
