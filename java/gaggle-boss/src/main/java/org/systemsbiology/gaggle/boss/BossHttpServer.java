package org.systemsbiology.gaggle.boss;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.eclipse.jetty.server.Server;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.Namelist;
import org.systemsbiology.gaggle.util.TextFileReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/*import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.log.Log;
*/

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
    private Logger Log = Logger.getLogger(this.getClass().getName());

    private HashMap<String, String> urlFileMap = new HashMap<String, String>();

    /**
     * Constructor.
     * @param bossImpl the main boss implementation
     * @param port the number of the port to run this service on
     */
    public BossHttpServer(BossImpl bossImpl, int port) {
        this.bossImpl = bossImpl;
        server = new Server(port);
        //ServletContextHandler.Context root = new ServletContextHandler.Context(server, HTTP_CONTEXT, ServletContextHandler.Context.SESSIONS);
        //root.addServlet("BossHttpServer", this); //, SERVLET_PATTERN);
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
        String requestURI =  req.getRequestURI();
        Log.info("HTTP Server received request " + requestURI);
        String command = req.getParameter("command");
        Log.info("HTTP Server received command " + command);

        if (command.equalsIgnoreCase("CreateNameList"))
        {
            StringBuffer databuilder = new StringBuffer();
            String line = null;
            try {
                BufferedReader reader = req.getReader();
                ArrayList<String> namelistdata = new ArrayList<String>();
                while ((line = reader.readLine()) != null)
                {
                    Log.info("Read line: " + line);
                    namelistdata.add(line);
                }

                Log.info("Namelist array size: " + namelistdata.size());
                String[] splitted = namelistdata.get(0).split(";;;");
                if (splitted.length > 2)
                {
                    String[] names = splitted[2].split(",");
                    Log.info("Name: " + splitted[0] + " Species: " + splitted[1] + " names: " + names.length);
                    Namelist nl = new Namelist(splitted[0], splitted[1], names);
                    byte[] serialized = serializeGaggleData(nl);
                    if (serialized != null)
                    {
                        //BufferedWriter writer = new BufferedWriter(resp.getWriter());
                        //ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        //bos.write(serialized);
                        //bos.flush();
                        OutputStream os = resp.getOutputStream();
                        os.write(serialized);
                        os.flush();
                    }
                }
            }
            catch (Exception e)
            {
                /*report an error*/
                Log.warning("Failed to read data from request " + e.getMessage());
            }
        }
        else if (command.equalsIgnoreCase("Broadcast")) {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                req.getInputStream()));

                ArrayList<String> dataarray = new ArrayList<String>();
                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    Log.info("Read line: " + line);
                    dataarray.add(line);
                }

                Log.info("Data array size: " + dataarray.size());
                JSONObject jsonObj = JSONObject.fromObject(dataarray.get(0));
                if (jsonObj != null)
                {
                    String source = jsonObj.getString("source");
                    String target = jsonObj.getString("target");
                    JSONObject dataobj = (JSONObject)jsonObj.get("data");
                    String datatype = dataobj.getString("_type");
                    String name = dataobj.getString("_name");
                    String species = dataobj.getString("_species");
                    JSONArray namejsonarray = (JSONArray)dataobj.get("_data");
                    Log.info("JSONARRAY size: " + namejsonarray.size());
                    List<String> list = new ArrayList<String>();
                    for(int i = 0; i < namejsonarray.size(); i++){
                        String value = namejsonarray.getString(i);
                        Log.info("Namelist value: " + value);
                        list.add(value);
                    }
                    String[] names = new String[list.size()];
                    names = list.toArray(names);
                    Log.info("Names: " + names);
                    if (datatype.equalsIgnoreCase("NameList")) {
                        Namelist nl = new Namelist(name, species, names);
                        bossImpl.broadcastNamelist(source, target, nl);
                    }
                }
            }
            catch (Exception e)
            {
                /*report an error*/
                Log.warning("Failed to read data from request " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private byte[] serializeGaggleData(GaggleData data)
    {
        if (data != null)
        {
            Log.info("Serializing gaggle data " + data.getName());
            ByteArrayOutputStream  bos = new ByteArrayOutputStream();
            ObjectOutputStream out = null;
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(data);
                byte[] result = bos.toByteArray();
                return result;
            }
            catch (Exception e)
            {
                Log.warning("Failed to serialize object " + e.getMessage());
            }
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ex) {
                    // ignore close exception
                }
                try {
                    bos.close();
                } catch (IOException ex) {
                    // ignore close exception
                }
            }
        }
        return null;
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
        else if (request != null) //if (request.isInitial())
        {
            String command = request.getParameter("command");
            String gooseName = request.getParameter("name");
            Log.info("Request command: " + command);
            if ("register".equals(command)) {
                String finalGooseName = registerHttpGoose(gooseName);
                setJSONResponse(response, jsonGooseName(finalGooseName));
            } else if ("unregister".equals(command)) {
                unregisterHttpGoose(gooseName);
            } else if ("waitBroadcast".equals(command)) {
                HttpGoose httpGoose = getHttpGooseFor(gooseName);
                if (httpGoose != null) {
                    // TODO: suspend the request
                    //request.suspend();
                    //httpGoose.waitForBroadcast(new HttpGoose.RequestHandler(request));
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
            else if (("getGeese").equals(command)) {
                Log.info("HTTP server received getGeese");
                String[] gooseNames = bossImpl.getGooseNames();
                String result = "";
                if (gooseNames.length > 0)
                {
                    for (String name : gooseNames)
                    {
                        if (name != null && name.length() > 0)
                            result += name + ";;;";
                    }
                }
                Log.info("Return " + result);
                setJSONResponse(response, "{\"result\": \"" + result + "\"}");
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
