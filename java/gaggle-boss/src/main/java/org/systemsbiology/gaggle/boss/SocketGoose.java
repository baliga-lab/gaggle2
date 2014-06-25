package org.systemsbiology.gaggle.boss;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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
    private static Logger logger = Log.getLogger("Socket Goose");

    public SocketGoose(String gooseName, BossWebSocket socket)
    {
        logger.info("SocketGoose initialized " + gooseName);
        this.gooseName = gooseName;
        this.mySocket = socket;
        this.jsonWebSocketWriter = new JSONWebSocketWriter(socket);
    }

    public static Namelist fromJSONtoNamelist(JSONObject jsonObj)
    {
        logger.info("Convert json object to namelist...");
        if (jsonObj != null)
        {
            String name = jsonObj.getString("_name");
            String species = jsonObj.getString("_species");
            if (species == null) {
                JSONObject metadataJSONObj = jsonObj.getJSONObject("metadata");
                if (metadataJSONObj != null) {
                    species = metadataJSONObj.getString("species");
                }
            }
            int size = jsonObj.getInt("_size");
            JSONArray data = jsonObj.containsKey("_data") ? jsonObj.getJSONArray("_data") : jsonObj.getJSONArray("gaggle-data");
            String[] names = new String[data.size()];
            for (int i = 0; i < data.size(); i++) {
                logger.info("Data item: " + data.getString(i));
                names[i] = data.getString(i);
            }
            logger.info("Species: " + species + " Names: " + names);
            Namelist nl = new Namelist(name, species, names);
            return nl;
        }
        return null;
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
