package org.systemsbiology.gaggle.boss;

/**
 * Created by Ning Jiang on 3/6/14.
 */

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.HashMap;
import java.util.logging.Logger;


class WebSocketServerThread extends Thread
{
    private Server myServer;
    private Logger Log = Logger.getLogger(this.getClass().getName());

    public WebSocketServerThread(Server server)
    {
        myServer = server;
    }

    public void run()
    {
        try
        {
            Log.info("Starting websocket server...");
            myServer.start();
            //myServer.dump(System.err);
            myServer.join();
        }
        catch (Throwable t)
        {
            Log.warning("Failed to start websocket server " + t.getMessage());
            t.printStackTrace(System.err);
        }
    }
}

public class BossWebSocketController
{
    private BossImpl bossImpl = null;
    private BossWebSocketServlet servlet = null;
    private HashMap<String, SocketGoose> socketGooseHashMap = new HashMap<String, SocketGoose>();
    private Logger Log = Logger.getLogger(this.getClass().getName());

    public void saveSocketGoose(String key, SocketGoose socketGoose)
    {
        if (key != null && socketGoose != null)
        {
            try
            {
                Log.info("Saving websocket with ID " + key);
                socketGooseHashMap.put(key, socketGoose);
                this.bossImpl.register(socketGoose);
            }
            catch (Exception e)
            {
                Log.severe("Failed to save socket goose: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void removeSocketGoose(String key)
    {
        if (key != null)
        {
            if (socketGooseHashMap.containsKey(key)) {
                SocketGoose goose = socketGooseHashMap.get(key);
                if (goose != null) {
                    bossImpl.unregister(goose.getName());
                }
                socketGooseHashMap.remove(key);
            }
        }
    }

    public void sendMessage(String key, String message)
    {
        if (key != null && message != null)
        {
            Log.info("Sending message to " + key);
            try
            {
                if (socketGooseHashMap.containsKey(key)) {
                    SocketGoose socketGoose = socketGooseHashMap.get(key);
                    socketGoose.getMySocket().getRemote().sendString(message);
                }
            }
            catch (Exception e)
            {
                Log.warning("Failed to send message " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public String[] getGeese()
    {
        if (bossImpl != null)
        {
            return bossImpl.getGooseNames();
        }
        return null;
    }

    public BossWebSocketController(BossImpl bossImpl, int port)
    {
        this.bossImpl = bossImpl;

        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Add a websocket to a specific path spec
        servlet = new BossWebSocketServlet(this);
        ServletHolder holderEvents = new ServletHolder("BossWebSocket", servlet);// BossWebSocketServlet.class);
        context.addServlet(holderEvents, "/BossWebSocket/*");

        server.setHandler(context);

        WebSocketServerThread serverThread = new WebSocketServerThread(server);
        serverThread.start();
    }
}


