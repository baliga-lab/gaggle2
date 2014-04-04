package org.systemsbiology.gaggle.boss;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by Ning Jiang on 3/6/14.
 */
public class BossWebSocket extends WebSocketAdapter
{
    private BossWebSocketController myController;
    private Session mySession;
    private String mySocketID;
    private Logger Log = Logger.getLogger(this.getClass().getName());
    private SocketGoose myGoose;

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
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        //super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
    }

    private String generateJSONString(String id, String action, String data)
    {
        String jsonstring = "{";
        jsonstring += "'ID': " + id + ", ";
        jsonstring += "'Action': " + action + ", ";
        jsonstring += "'Data': " + data;
        jsonstring += "}";
        Log.info("Generated json string: " + jsonstring);
        return jsonstring;
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
                    String jsonstring = generateJSONString(mySocketID, "", "");
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
                            String jsonstring = generateJSONString(mySocketID, "GetGeese", namestring);
                            Log.info("Geese name string to be sent back: " + jsonstring);
                            mySession.getRemote().sendString(jsonstring);
                        }
                    }
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
