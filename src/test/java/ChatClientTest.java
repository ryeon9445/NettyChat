import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class ChatClientTest extends WebSocketAdapter
{
    private static final String WS_URL = "ws://127.0.0.1:8086/websocket?request=";
    private final AtomicReference<Session> session = new AtomicReference<>();

    public static void main(String[] args) throws Exception
    {
        ChatClientTest chat = new ChatClientTest();
        chat.disableJettyLog();
        chat.connect(new URI(WS_URL + chat.initParameter()));
        chat.startChat();
    }

    @Override
    public void onWebSocketConnect(Session sess)
    {
        session.set(sess);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);
    }

    @Override
    public void onWebSocketText(String message)
    {
        super.onWebSocketText(message);
        System.out.println("[receive] : " + parseResponseData(message));
    }

    private String initParameter()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter your ID(Integer only)");
        int userId = scanner.nextByte();
        System.out.println("Please enter a room number(Integer only)");
        int roomId = scanner.nextInt();

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("userId", userId);
        requestData.put("roomId", roomId);

        JSONObject jsonRequestData = new JSONObject(requestData);
        return Base64.encodeBase64String(jsonRequestData.toString().getBytes());
    }

    private void connect(URI destServerURI) throws Exception
    {
        WebSocketClient client = new WebSocketClient();
        client.start();
        client.connect(this, destServerURI).get();
    }

    private void startChat()
    {
        while (session.get().isOpen())
        {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please enter a message to send");
            String sendMessage = scanner.nextLine();
            session.get().getRemote().sendStringByFuture(sendMessage);

            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private String parseResponseData(final String message)
    {
        String decodedMessage = new String(Base64.decodeBase64(message));
        JSONObject jsonMessage = new JSONObject(decodedMessage);
        return jsonMessage.toString();
    }


    private void disableJettyLog()
    {
        org.eclipse.jetty.util.log.Log.setLog(new Logger()
        {
            @Override
            public String getName()
            {
                return "no";
            }
            @Override
            public void warn(String msg, Object... args)
            {
            }
            @Override
            public void warn(Throwable thrown)
            {
            }
            @Override
            public void warn(String msg, Throwable thrown)
            {
            }
            @Override
            public void info(String msg, Object... args)
            {
            }
            @Override
            public void info(Throwable thrown)
            {
            }
            @Override
            public void info(String msg, Throwable thrown)
            {
            }
            @Override
            public boolean isDebugEnabled()
            {
                return false;
            }
            @Override
            public void setDebugEnabled(boolean enabled)
            {
            }
            @Override
            public void debug(String msg, Object... args)
            {
            }
            @Override
            public void debug(String msg, long value)
            {
            }
            @Override
            public void debug(Throwable thrown)
            {
            }
            @Override
            public void debug(String msg, Throwable thrown)
            {
            }
            @Override
            public Logger getLogger(String name)
            {
                return this;
            }
            @Override
            public void ignore(Throwable ignored)
            {
            }
        });
    }
}
