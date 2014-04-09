package org.systemsbiology.gaggle.boss;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.systemsbiology.gaggle.core.JSONGoose;
import org.systemsbiology.gaggle.core.datatypes.*;

import java.rmi.RemoteException;

/**
 * Created by Ning Jiang on 4/4/14.
 */
public class SocketGoose implements JSONGoose {
    private String gooseName;
    private BossWebSocket mySocket;
    private JSONReader jsonReader = new JSONReader();
    private JSONWebSocketWriter jsonWebSocketWriter;
    private Logger logger = Log.getLogger(this.getName());

    public SocketGoose(String gooseName, BossWebSocket socket)
    {
        logger.info("SocketGoose initialized " + gooseName);
        this.gooseName = gooseName;
        this.mySocket = socket;
        this.jsonWebSocketWriter = new JSONWebSocketWriter(socket);
    }

    public void setName(String newName)
    {
        logger.info("Socket goose set name: " + newName);
        this.gooseName = newName;
    }
    /** {@inheritDoc} */
    public String getName() { return gooseName; }
    /** {@inheritDoc} */
    public void update(String[] gooseNames) {
        // TODO: Broadcast to handler
    }
    public BossWebSocket getMySocket() { return mySocket; }

    public void handleJSON(String source,
                           String json) throws RemoteException {
        if (json != null)
        {
            try
            {
                logger.info("Sending " + json + " to " + gooseName);

                this.mySocket.broadcastJSON(json);
            }
            catch (Exception e)
            {
                logger.warn("Failed to send json to remote endpoint: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void broadcastNameList(Namelist namelist)
    {
        if (namelist != null && jsonWebSocketWriter != null)
        {
            jsonWebSocketWriter.write(namelist);
        }
    }

    public void broadcastCluster(Cluster c)
    {
        if (c != null && jsonWebSocketWriter != null)
        {
            jsonWebSocketWriter.write(c);
        }
    }

    public void broadcastNetwork(Network network)
    {
        if (network != null && jsonWebSocketWriter != null)
        {
            jsonWebSocketWriter.write(network);
        }
    }

    public void broadcastMatrix(DataMatrix dataMatrix)
    {
        if (dataMatrix != null && jsonWebSocketWriter != null)
        {
            jsonWebSocketWriter.write(dataMatrix);
        }
    }
}
