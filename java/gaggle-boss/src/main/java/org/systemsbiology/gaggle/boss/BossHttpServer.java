package org.systemsbiology.gaggle.boss;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.log.Log;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.util.TextFileReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;

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

    private HashMap<String, String> urlFileMap = new HashMap<String, String>();

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

    public void addFile(String id, String filepath)
    {
        if (id != null && filepath != null)
        {
            Log.info("Boss Http server saved file " + filepath + " with ID: " + id);
            urlFileMap.put(id, filepath);
        }
    }

    /**
     * Starts the HTTP service.
     * @throws Exception if server could not be started successfully
     */
    public void startListen() throws Exception {
        server.start();
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
         //req.getParameter("attachment").getBytes();
    }

    /** {@inheritDoc} */
    @Override public void doGet(final HttpServletRequest request,
                                final HttpServletResponse response)
        throws IOException {
        String requestURI =  request.getRequestURI();
        Log.info("HTTP Server received request " + requestURI);
        String id = requestURI.substring(requestURI.lastIndexOf("/") + 1);
        if (urlFileMap.containsKey(id))
        {
            // Fetch the file and return it
            String filepath =  urlFileMap.get(id);
            if (filepath.toLowerCase().startsWith("file:///"))
            {
                filepath = filepath.substring("file:///".length());
                filepath = filepath.replace("|", ":");
            }
            Log.info("Loading file " + filepath);
            if (filepath != null)
            {
                Log.info("Fetching file " + filepath);

                //FileInputStream fin = new FileInputStream(filepath);
                TextFileReader textFileReader = new TextFileReader(filepath);
                textFileReader.read();
                String filecontent = textFileReader.getText();
                //response.setHeader("Access-Control-Allow-Origin", "*");
                //response.setContentType("application/html");
                //response.setStatus(HttpServletResponse.SC_OK);
                Log.info(filecontent);
                response.getWriter().print(filecontent);
                //response.getWriter().flush();
            }
        }
        else if (request.isInitial()) {
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
            }
            else if ("doUrlFileMap".equals(command))  {
                // A goose has generated a file, and notify the Boss to associate the file with a url
                String url = request.getParameter("URL");
                String file = request.getParameter("FilePath");
                if (url!= null && file != null && !url.isEmpty() && !file.isEmpty())
                {
                    urlFileMap.put(url.toLowerCase(), file);
                }
            }
            else {
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
