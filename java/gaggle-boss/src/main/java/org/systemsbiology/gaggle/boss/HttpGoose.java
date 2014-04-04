package org.systemsbiology.gaggle.boss;

import org.systemsbiology.gaggle.core.JSONGoose;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.rmi.RemoteException;

//import org.mortbay.jetty.*;

/**
 * A representation of a Gaggle Goose connected through HTTP.
 * This class only deals with the receiving part of a Goose, because the
 * BossHttpServer class deals with the broadcasting part.
 */
public class HttpGoose implements JSONGoose {

    /**
     * A RequestHandler is the means to push a broadcast to an HTTP goose.
     * We assume that a connection is used in a long polling fashion.
     * When data is broadcasted as a JSON string, the request is resumed and
     * the broadcast data is set into the request.
     */
    public static class RequestHandler {
        private HttpServletRequest request;

        /**
         * Constructor.
         * @param request the HTTP request
         */
        public RequestHandler(HttpServletRequest request) {
            this.request = request;
        }

        /**
         * Sends the specified JSON string to the associated HTTP connection.
         * @param source the source goose
         * @param json the JSON data
         */
        public void handleJSON(String source, String json) throws IOException {
            System.out.printf("HttpGoose.handleJSON() [REQ: %s], source: '%s', json: '%s'\n",
                              request, source, json);
            /*HttpServletResponse response = (HttpServletResponse) request.getServletResponse();
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(json);
            request.resume(); */
        }
    }

    private String gooseName;
    private RequestHandler currentHandler;

    /**
     * Constructor.
     * @param originalName original goose name
     */
    public HttpGoose(String originalName) {
        this.gooseName = originalName;
    }

    /** {@inheritDoc} */
    public void setName(String newName) { this.gooseName = newName; }
    /** {@inheritDoc} */
    public String getName() { return gooseName; }
    /** {@inheritDoc} */
    public void update(String[] gooseNames) {
        // TODO: Broadcast to handler
    }
    /** {@inheritDoc} */
    public void handleJSON(String source,
                           String json) throws RemoteException {
        if (currentHandler == null) {
            System.out.println("NO HANDLER SET !!!");
        } else {
            try {
                currentHandler.handleJSON(source, json);
            } catch (IOException ex) {
                throw new RemoteException("Could not handle JSON", ex);
            }
        }
    }

    /**
     * Sets a handler to wait for an incoming broadcast request.
     * @param handler the broadcast request handler
     */
    public void waitForBroadcast(RequestHandler handler) {
        this.currentHandler = handler;
    }
}
