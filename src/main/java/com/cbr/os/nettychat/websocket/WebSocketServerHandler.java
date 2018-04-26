package com.cbr.os.nettychat.websocket;

import com.cbr.os.nettychat.define.ServiceDefine;
import com.cbr.os.nettychat.model.Client;
import com.cbr.os.nettychat.service.RequestService;
import com.cbr.os.nettychat.service.RoomService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object>
{
    private static Map<Integer, ChannelGroup> channelGroupMap = new ConcurrentHashMap<>();
    private Client client;
    private WebSocketServerHandshaker webSocketServerHandshaker;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception
    {
        Channel incoming = ctx.channel();
        System.out.println("Handshake client from : " + incoming.remoteAddress());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception
    {
        RoomService.leaveRoom(channelGroupMap, ctx, client);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (msg instanceof FullHttpRequest)
        {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        }
        else if (msg instanceof WebSocketFrame)
        {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
    {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        cause.printStackTrace();
        ctx.close();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req)
    {
        if (!req.decoderResult().isSuccess())
        {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        if (req.method() != HttpMethod.GET)
        {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
            return;
        }

        if ("/favicon.ico".equals(req.uri()) || ("/".equals(req.uri())))
        {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
            return;
        }

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
        Map<String, List<String>> parameters = queryStringDecoder.parameters();

        if (parameters.size() == 0 || !parameters.containsKey(ServiceDefine.HTTP_REQUEST_STRING))
        {
            System.err.println("Invalid request");
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
            return;
        }

        client = RequestService.parameterParse(parameters.get(ServiceDefine.HTTP_REQUEST_STRING).get(0));
        if (client.getRoomId() == 0 || client.getUserId() == 0)
        {
            System.err.println("Invalid parameter Parse");
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
            return;
        }

        RoomService.enterRoom(channelGroupMap, ctx, client);

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true);
        webSocketServerHandshaker = wsFactory.newHandshaker(req);
        if (webSocketServerHandshaker == null)
        {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }
        else
        {
            webSocketServerHandshaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame)
    {
        if (frame instanceof CloseWebSocketFrame)
        {
            webSocketServerHandshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame)
        {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame))
        {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }

        RoomService.broadcast(channelGroupMap, ctx, frame, client);
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res)
    {
        if (res.status().code() != 200)
        {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        ChannelFuture future = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200)
        {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private String getWebSocketLocation(FullHttpRequest req)
    {
        String location = req.headers().get(HttpHeaderNames.HOST) + ServiceDefine.WEB_SOCKET_PATH;
        return "ws://" + location;
    }
}

