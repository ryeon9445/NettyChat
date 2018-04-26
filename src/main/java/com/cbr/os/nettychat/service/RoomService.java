package com.cbr.os.nettychat.service;

import com.cbr.os.nettychat.define.ServiceStatus;
import com.cbr.os.nettychat.model.Client;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Map;

public class RoomService
{
    public static void enterRoom(final Map<Integer, ChannelGroup> channelGroupMap, final ChannelHandlerContext ctx, final Client client)
    {
        if (!channelGroupMap.containsKey(client.getRoomId()))
        {
            channelGroupMap.put(client.getRoomId(), new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        }

        channelGroupMap.get(client.getRoomId()).add(ctx.channel());
    }

    public static void leaveRoom(final Map<Integer, ChannelGroup> channelGroupMap, final ChannelHandlerContext ctx, final Client client)
    {
        if (client != null && channelGroupMap.containsKey(client.getRoomId()))
        {
            channelGroupMap.get(client.getRoomId()).remove(ctx.channel());
        }
    }

    public static void broadcast(final Map<Integer, ChannelGroup> channelGroupMap, final ChannelHandlerContext ctx, final WebSocketFrame frame, final Client client)
    {
        if (client.getUserId() == 0)
        {
            String errorMessage = ResponseService.createErrorMessage(ServiceStatus.NOT_FOUND_USER, "Not found user");
            ctx.channel().write(new TextWebSocketFrame(errorMessage));
            return;
        }

        if (!channelGroupMap.containsKey(client.getRoomId()))
        {
            String errorMessage = ResponseService.createErrorMessage(ServiceStatus.NOT_FOUND_ROOM, "Not found room");
            ctx.channel().write(new TextWebSocketFrame(errorMessage));
            return;
        }

        String requestMessage = ((TextWebSocketFrame) frame).text();
        String sendMessage = ResponseService.createSendMessage(ServiceStatus.OK, client, requestMessage);
        System.out.println("Received message " + ctx.channel() + sendMessage);
        channelGroupMap.get(client.getRoomId()).writeAndFlush(new TextWebSocketFrame(sendMessage));
    }
}
