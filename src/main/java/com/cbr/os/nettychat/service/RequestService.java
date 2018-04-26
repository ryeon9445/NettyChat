package com.cbr.os.nettychat.service;

import com.cbr.os.nettychat.define.ServiceDefine;
import com.cbr.os.nettychat.model.Client;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestService
{
    public static Client parameterParse(final String request)
    {
        String res = new String(Base64.decodeBase64(request));
        JSONObject json = new JSONObject(res);

        Client client = new Client();

        if (!json.has(ServiceDefine.PARAM_ROOM_ID) || !json.has(ServiceDefine.PARAM_USER_ID))
        {
            return client;
        }

        try
        {
            client.setRoomId(json.getInt(ServiceDefine.PARAM_ROOM_ID));
            client.setUserId(json.getInt(ServiceDefine.PARAM_USER_ID));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return client;
        }

        return client;
    }
}
