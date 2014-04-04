package org.systemsbiology.gaggle.boss;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Created by Ning Jiang on 3/6/14.
 */

public class BossWebSocketServlet extends WebSocketServlet
{
    private BossWebSocketController myController = null;
    private BossWebSocketCreator mySocketCreator = null;

    public BossWebSocketServlet(BossWebSocketController controller)
    {
        this.myController = controller;
        mySocketCreator = new BossWebSocketCreator(controller);
    }

    @Override
    public void configure(WebSocketServletFactory factory)
    {
        //factory.register(BossWebSocket.class);
        factory.getPolicy().setMaxTextMessageSize(1000000);
        factory.setCreator(mySocketCreator);
    }
}

