package com.casic.otitan.googlegps.socket;

import java.util.Date;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by fz on 2023/10/31 10:54
 * describe :
 */
@ChannelHandler.Sharable
public class InboundClientHandler extends ChannelInboundHandlerAdapter {
    private final GPSSocketServer server;

    public InboundClientHandler(GPSSocketServer server) {
        this.server = server;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        //接收服务端的消息
        WebSocketFrame frame = (WebSocketFrame) msg;
        //ping信息
        if (frame instanceof PongWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            LogUtil.show(GPSSocketServer.TAG, "ping信息返回:" + textFrame.text());
        }
        //文本信息
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            LogUtil.show(GPSSocketServer.TAG, "客户端接收的消息是:" + textFrame.text());

        }
        //二进制信息
        if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binFrame = (BinaryWebSocketFrame) frame;
            LogUtil.show(GPSSocketServer.TAG, "BinaryWebSocketFrame");
        }
        //关闭消息
        if (frame instanceof CloseWebSocketFrame) {
            LogUtil.show(GPSSocketServer.TAG, "receive close frame");
            ch.close();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.show(GPSSocketServer.TAG, "与服务端连接成功");
        if (server.getOnConnectionChangedListener() != null) {
            server.getOnConnectionChangedListener().onConnected();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.show(GPSSocketServer.TAG, "--------连接断开了---------");
        if (server.getOnConnectionChangedListener() != null) {
            server.getOnConnectionChangedListener().onDisconnection();
        }
        server.connect(Integer.MAX_VALUE, (isConnect, errorMsg) -> {
            if (isConnect) {
                LogUtil.show(GPSSocketServer.TAG, "--------重新连接成功---------");
                return;
            }
            LogUtil.show(GPSSocketServer.TAG, "重新连接失败：" + errorMsg);
        });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        LogUtil.show(GPSSocketServer.TAG, "---------------自动发送心跳包,心跳时间:" + new Date() + "-------------");
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE) {
//                ctx.writeAndFlush(new TextWebSocketFrame(createPingStr())).addListener(future -> LogUtil.show(future.isSuccess() ? "心跳包发送成功！" : "心跳包发送失败！"));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtil.show(GPSSocketServer.TAG, "连接异常：" + cause.getMessage());
        super.exceptionCaught(ctx, cause);
        if (server.getOnConnectionChangedListener() != null) {
            server.getOnConnectionChangedListener().onException(new SocketException(cause.getMessage(),cause,Integer.parseInt(ResponseCode.OTHER_FAILURE)));
        }

    }
}
