package com.cbr.os.nettychat;

import com.cbr.os.nettychat.websocket.WebSocketServer;

public class Main
{
    public static void main(String[] arg)
    {
        try
        {
            WebSocketServer webSocketServer = new WebSocketServer();
            webSocketServer.start();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
