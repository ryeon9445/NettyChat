package com.cbr.os.nettychat.service;

import com.cbr.os.nettychat.define.ServiceDefine;
import com.cbr.os.nettychat.model.Client;
import com.cbr.os.nettychat.model.Response;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

public class ResponseService
{
    public static String createErrorMessage(final int errorCode, final String errorMessage)
    {
        Response response = new Response();
        response.setCode(errorCode);
        response.getResponseData().put(ServiceDefine.PARAM_MESSAGE, errorMessage);

        return messageParse(response);
    }

    public static String createSendMessage(final int successCode, final Client client, final String message)
    {
        Response response = new Response();
        response.setCode(successCode);
        response.getResponseData().put(ServiceDefine.PARAM_USER_ID, client.getUserId());
        response.getResponseData().put(ServiceDefine.PARAM_ROOM_ID, client.getRoomId());
        response.getResponseData().put(ServiceDefine.PARAM_MESSAGE, message);
        response.getResponseData().put(ServiceDefine.PARAM_CURRENT_TIME, System.currentTimeMillis());

        return messageParse(response);
    }

    private static String messageParse(final Response response)
    {
        String jsonMessage = new JSONObject(response).toString();
        String base64Message = Base64.encodeBase64String(jsonMessage.toString().getBytes());
        return base64Message;
    }
}
