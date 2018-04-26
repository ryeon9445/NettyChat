package com.cbr.os.nettychat.model;

public class Client
{
    private long userId;
    private int roomId;

    public Client()
    {
        userId = 0L;
        roomId = 0;
    }

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public int getRoomId()
    {
        return roomId;
    }

    public void setRoomId(int roomId)
    {
        this.roomId = roomId;
    }
}
