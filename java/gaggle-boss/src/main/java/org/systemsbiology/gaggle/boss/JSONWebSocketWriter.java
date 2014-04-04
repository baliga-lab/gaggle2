package org.systemsbiology.gaggle.boss;

import net.sf.json.JSONObject;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.systemsbiology.gaggle.core.datatypes.JSONWriter;

/**
 * Created by Ning Jiang on 4/4/14.
 */
public class JSONWebSocketWriter extends JSONWriter {
    BossWebSocket mySocket;
    Logger logger = Log.getLogger(this.getClass().getName());

    public JSONWebSocketWriter(BossWebSocket socket)
    {
        mySocket = socket;
    }

    @Override
    protected void writeToWriter(JSONObject jsonGaggleData)
    {
         if (mySocket != null && jsonGaggleData != null) {
             try
             {
                mySocket.getRemote().sendString(jsonGaggleData.toString());
             }
             catch (Exception e) {
                 logger.warn("Failed to send json string to websocket " + e.getMessage());
                 e.printStackTrace();
             }
         }
    }
}
