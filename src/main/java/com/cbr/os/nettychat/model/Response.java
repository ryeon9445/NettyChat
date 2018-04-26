package com.cbr.os.nettychat.model;

import java.util.HashMap;
import java.util.Map;

public class Response
{
    private int code;
    private Map<String, Object> responseData;

    public Response()
    {
        responseData = new HashMap<>();
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public Map<String, Object> getResponseData()
    {
        return responseData;
    }

    public void setResponseData(Map<String, Object> responseData)
    {
        this.responseData = responseData;
    }
}
