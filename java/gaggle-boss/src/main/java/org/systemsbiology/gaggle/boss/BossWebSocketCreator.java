package org.systemsbiology.gaggle.boss;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

/**
 * Created by Ning Jiang on 3/7/14.
 */
public class BossWebSocketCreator implements WebSocketCreator {
    BossWebSocketController myController = null;

    public BossWebSocketCreator(BossWebSocketController controller)
    {
        this.myController = controller;
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        return new BossWebSocket(myController);
    }
}
