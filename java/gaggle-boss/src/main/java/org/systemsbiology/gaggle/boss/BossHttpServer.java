package org.systemsbiology.gaggle.boss;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.mortbay.jetty.*;
import org.mortbay.jetty.handler.*;

class BossHttpServer extends AbstractHandler {

    private Server server;

    public BossHttpServer(int port) {
        server = new Server(port);
        server.setHandler(this);
    }

    public void startListen() throws Exception {
        System.out.println("startListen()");
        server.start();
        System.out.println("startListen() - end");
    }

    public void handle(String target, final HttpServletRequest request,
                       final HttpServletResponse response, int dispatch)
        throws IOException, ServletException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        System.out.println("REQUEST: " + request);
        String responseString = "Hello Boss";
        if (request.isInitial()) {
            System.out.println("Initial GET request !!");
            String command = request.getParameter("command");
            if ("register".equals(command)) {
                String gooseName = request.getParameter("name");
                System.out.println("register goose");
                responseString = gooseName;
                response.setContentType("text/plain");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(responseString);
                ((Request) request).setHandled(true);

            } else if ("waitBroadcast".equals(command)) {
                String gooseName = request.getParameter("name");
                System.out.printf("Goose '%s' is waiting for broadcast\n", gooseName);
                request.suspend();
                new Timer().schedule(new TimerTask() {
                        public void run() {
                            try {
                                request.resume();
                                response.setContentType("text/plain");
                                response.setStatus(HttpServletResponse.SC_OK);
                                response.getWriter().println("Ma cool broadcast");
                                ((Request) request).setHandled(true);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }, 3000);
                return;
            }
        }
        if (request.isTimeout()) {
        }
    }
}
