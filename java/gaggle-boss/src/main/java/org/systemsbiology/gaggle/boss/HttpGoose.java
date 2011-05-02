package org.systemsbiology.gaggle.boss;

import java.io.*;
import java.rmi.*;

import javax.servlet.http.*;

import org.mortbay.jetty.*;
import org.systemsbiology.gaggle.core.*;
import org.systemsbiology.gaggle.core.datatypes.*;

public class HttpGoose implements JSONGoose {

    public static class RequestHandler {
        private HttpServletRequest request;

        public RequestHandler(HttpServletRequest request) {
            this.request = request;
        }
        public void handleJSON(String source, String json) throws IOException {
            System.out.printf("HttpGoose.handleJSON() [REQ: %s], source: '%s', json: '%s'\n",
                              request, source, json);
            HttpServletResponse response = (HttpServletResponse) request.getServletResponse();
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(json);
            request.resume();
        }
    }

    private String originalName;
    private String gooseName;
    private String[] knownGeese = new String[0];
    private RequestHandler currentHandler;

    public HttpGoose(String originalName) {
        this.originalName = originalName;
        this.gooseName = originalName;
    }
    public void doExit() { }
    public void doShow() { }
    public void doHide() { }
    public void doBroadcastList() { }
    public void setName(String newName) {
        this.gooseName = newName;
    }
    public String getName() {
        return gooseName;
    }
    public void update(String[] gooseNames) {
        this.knownGeese = gooseNames;
    }

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

    public void waitForBroadcast(RequestHandler handler) {
        this.currentHandler = handler;
    }
}
