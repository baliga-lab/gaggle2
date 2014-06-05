package org.systemsbiology.gaggle.boss;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by Ning Jiang on 3/6/14.
 */

class PingThread extends Thread
{
    private BossWebSocket mySocket;
    private Logger Log = Logger.getLogger(this.getClass().getName());

    public PingThread(BossWebSocket socket)
    {
        mySocket = socket;
    }

    public void run()
    {
        if (mySocket != null)
        {
            Log.info("Starting websocket ping thread...");
            try
            {
                while (mySocket.isConnected())
                {
                    String data = "Ping";
                    Log.info("Send Ping...");
                    ByteBuffer payload = ByteBuffer.wrap(data.getBytes());
                    mySocket.getRemote().sendPing(payload);
                    Thread.sleep(60000);
                }
            }
            catch (Throwable t)
            {
                Log.warning("Failed to start websocket server " + t.getMessage());
                t.printStackTrace(System.err);
            }
        }
    }
}

public class BossWebSocket extends WebSocketAdapter
{
    private BossWebSocketController myController;
    private Session mySession;
    private String mySocketID;
    private Logger Log = Logger.getLogger(this.getClass().getName());
    private SocketGoose myGoose;
    private PingThread pingThread = new PingThread(this);

    public BossWebSocket() {
    }

    public BossWebSocket(BossWebSocketController controller)
    {
        this.myController = controller;
    }

    public void setGoose(SocketGoose goose)
    {
        this.myGoose = goose;
    }

    @Override
    public void onWebSocketConnect(Session sess)
    {
        try
        {
            super.onWebSocketConnect(sess);
            mySession = sess;
            mySocketID = UUID.randomUUID().toString();
            pingThread.start();
            Log.info("Websocket connection received " + mySocketID);
            //sess.getRemote().sendString("{'socketid': \'" + socketid + "\'}");

        }
        catch (Exception e)
        {
            Log.warning("Failed to handle websocket connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        Log.info("+++ ignoring binary message");
    }

    @Override
    public void onWebSocketText(String message)
    {
        try
        {
            super.onWebSocketText(message);
            Log.info("Received TEXT message: " + message);
            processTextMessage(message);
        }
        catch (Exception e)
        {
            Log.warning("Failed to handle websocket text: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        //super.onWebSocketClose(statusCode,reason);
        Log.info("Socket Closed: [" + statusCode + "] " + reason);
        this.myController.removeSocketGoose(mySocketID);
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        //super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
        this.myController.removeSocketGoose(mySocketID);
    }

    public void broadcastJSON(String jsonstring) throws IOException
    {
        if (jsonstring != null)
        {
            String data = generateJSONString(this.mySocketID, "Broadcast", jsonstring, true);
            Log.info("Broadcasting json data " + data);
            this.getRemote().sendString(data);
        }
    }

    private String generateJSONString(String id, String action, String data, boolean isjson)
    {
        Log.info("Generate JSON string from " + data);
        JSONObject jsonobj = new JSONObject();
        jsonobj = jsonobj.element("ID", id).element("Action", action);
        if (isjson)
        {
            JSONObject jsondata = JSONObject.fromObject(data.trim());
            jsonobj.put("Data", jsondata);
        }
        else
            jsonobj = jsonobj.element("Data", data);
        StringWriter stringWriter = new StringWriter();
        jsonobj.write(stringWriter);
        Log.info("Generated json string: " + stringWriter.toString());
        return stringWriter.toString();
    }

    private void processTextMessage(String message)
    {
        if (message != null && message.length() > 0)
        {
            try
            {
                JSONObject jsonObj = JSONObject.fromObject(message);
                String id = jsonObj.getString("ID");
                String action = jsonObj.getString("Action");
                Log.info("ID: " + id + " Action: " + action);
                if (action.equalsIgnoreCase("Register"))
                {
                    String goosename = jsonObj.getString("Data");
                    SocketGoose goose = new SocketGoose(goosename, this);

                    if (myController != null) {
                        myController.saveSocketGoose(mySocketID, goose);
                        Log.info("Session saved: " + mySocketID);
                    }
                    String jsonstring = generateJSONString(mySocketID, "", "", false);
                    mySession.getRemote().sendString(jsonstring);
                }
                else if (action.equalsIgnoreCase("GetGeese")) {
                    Log.info("Getting geese...");
                    if (myController != null)
                    {
                        String[] geese = myController.getGeese();
                        if (geese != null)
                        {
                            String namestring = StringUtils.join(geese, ";;;");
                            String jsonstring = generateJSONString(mySocketID, "GetGeese", namestring, false);
                            Log.info("Geese name string to be sent back: " + jsonstring);
                            mySession.getRemote().sendString(jsonstring);
                        }
                    }
                }
                else if (action.equalsIgnoreCase("Chrome")) {
                    String dataString = jsonObj.getString("Data");
                    Log.info("Selenium data string: " + dataString);
                    JSONObject jsonActionObject = JSONObject.fromObject(dataString);
                    this.myController.getSeleniumChromeHandler().handleAction(jsonActionObject);
                }
            }
            catch (Exception e)
            {
                Log.warning("Failed to process text message " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
