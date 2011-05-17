package org.systemsbiology.gaggle.boss;

import java.io.*;
import java.rmi.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.mortbay.jetty.*;
import org.mortbay.jetty.handler.*;
import org.mortbay.jetty.servlet.*;

import org.systemsbiology.gaggle.core.*;

/**
 * Gaggle boss embeds Jetty Server in order to support Geese that connect
 * through HTTP. It requires Jetty &gt;= 7.0 to use the Java Servlet 3.0
 * compatible API for continuation-based Servlets.
 * 
 * This represents a platformneutral approach to connect to the Gaggle Boss.
 * In order to support AJAX applications, BossHttpServer employs the HTTP long
 * polling.
 */
class BossHttpServer extends HttpServlet {

    private static final String HTTP_CONTEXT = "/";
    private static final String SERVLET_PATTERN = "/*";
    private Server server;
    private BossImpl bossImpl;

    /**
     * Constructor.
     * @param bossImpl the main boss implementation
     * @param port the number of the port to run this service on
     */
    public BossHttpServer(BossImpl bossImpl, int port) {
        this.bossImpl = bossImpl;
        server = new Server(port);
        Context root = new Context(server, HTTP_CONTEXT, Context.SESSIONS);
        root.addServlet(new ServletHolder(this), SERVLET_PATTERN);
    }

    /**
     * Starts the HTTP service.
     * @throws Exception if server could not be started successfully
     */
    public void startListen() throws Exception {
        server.start();
    }

    /** {@inheritDoc} */
    @Override public void doGet(final HttpServletRequest request,
                                final HttpServletResponse response)
        throws IOException {
        if (request.isInitial()) {
            String command = request.getParameter("command");
            String gooseName = request.getParameter("name");
            if ("register".equals(command)) {
                String finalGooseName = registerHttpGoose(gooseName);
                setJSONResponse(response, jsonGooseName(finalGooseName));
            } else if ("unregister".equals(command)) {
                unregisterHttpGoose(gooseName);
            } else if ("waitBroadcast".equals(command)) {
                HttpGoose httpGoose = getHttpGooseFor(gooseName);
                if (httpGoose != null) {
                    request.suspend();
                    httpGoose.waitForBroadcast(new HttpGoose.RequestHandler(request));
                } else {
                    System.out.println("HTTP Goose unavailable");
                }
            } else if ("doBroadcast".equals(command)) {
                String jsonData = request.getParameter("data");
                bossImpl.broadcastJSON(gooseName, "boss", jsonData);
                setJSONResponse(response, "{\"status\":\"ok\"}");
            } else {
                // TODO: report unhandled command
            }
        } else {
            // TODO: report unhandled
        }
    }

    private HttpGoose getHttpGooseFor(String gooseName) {
        Goose goose = bossImpl.getGoose(gooseName);
        if (goose != null && goose instanceof JSONGooseAdapter) {
            return (HttpGoose) ((JSONGooseAdapter) goose).getWrappedGoose();
        }
        return null;
    }

    private String jsonGooseName(String gooseName) {
        return String.format("{ \"gooseName\": \"%s\" }", gooseName);
    }

    private void setJSONResponse(HttpServletResponse response,
                                 String json) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(json);
        response.getWriter().flush();
    }

    private String registerHttpGoose(String gooseName) throws RemoteException {
        return bossImpl.register(new HttpGoose(gooseName));
    }
    private void unregisterHttpGoose(String gooseName) throws RemoteException {
        bossImpl.unregister(gooseName);
    }
}
