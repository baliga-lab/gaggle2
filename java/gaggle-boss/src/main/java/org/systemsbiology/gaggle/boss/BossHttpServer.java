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

class BossHttpServer extends HttpServlet {

    private Server server;
    private BossImpl bossImpl;

    public BossHttpServer(BossImpl bossImpl, int port) {
        this.bossImpl = bossImpl;
        server = new Server(port);
        Context root = new Context(server, "/", Context.SESSIONS);
        ServletHandler handler = new ServletHandler();
        root.addServlet(new ServletHolder(this), "/*");
    }

    public void startListen() throws Exception {
        server.start();
    }

    @Override public void doGet(final HttpServletRequest request,
                                final HttpServletResponse response)
        throws IOException {
        if (request.isInitial()) {
            String command = request.getParameter("command");
            if ("register".equals(command)) {
                String gooseName = registerHttpGoose(request.getParameter("name"));
                setJSONResponse(response, jsonGooseName(gooseName));
            } else if ("waitBroadcast".equals(command)) {
                String gooseName = request.getParameter("name");
                HttpGoose httpGoose = getHttpGooseFor(gooseName);
                if (httpGoose != null) {
                    request.suspend();
                    httpGoose.waitForBroadcast(new HttpGoose.RequestHandler(request));
                } else {
                    System.out.println("HTTP Goose unavailable");
                }
                return;
            } else if ("doBroadcast".equals(command)) {
                String gooseName = request.getParameter("name");
                String jsonData = request.getParameter("data");
                bossImpl.broadcastJSON(gooseName, "boss", jsonData);
                setJSONResponse(response, "{\"status\":\"ok\"}");
            }
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
}
