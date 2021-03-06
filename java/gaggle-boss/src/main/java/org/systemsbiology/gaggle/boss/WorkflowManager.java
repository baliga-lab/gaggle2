package org.systemsbiology.gaggle.boss;

import net.sf.json.JSONObject;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.Goose3;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.util.ClientHttpRequest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Ning Jiang
 * Date: 10/12/12
 * Time: 1:24 PM
 *
 * A boss can handle multiple workflows. Each workflow is executed in a thread.
 * This class manages the execution and resources of workflow threads.
 */
public class WorkflowManager {
    protected GooseManager gooseManager = null;
    protected BossImpl bossImpl = null;
    private long timerInterval = 1000L; //milliseconds
    private long timerTimeout = 600000L; // timer for verifying if a goose is started correctly
    private static Logger Log = Logger.getLogger("Boss");
    private Thread resourceManagementThread;
    private Map<UUID, WorkflowThread> threadMap;
    private ShutdownHookThread shutdownHook;
    private String tempFolderName = "WorkflowManager";
    private File myTempFolder;
    private String tempFileToken = UUID.randomUUID().toString();
    private ArrayList<Workflow> submittedWorkflows = new ArrayList<Workflow>();
    private Workflow topWorkflow = null;
    private Boolean gooseStarted = false;
    private Object topWorkflowSyncObj = new Object();

    public static int temporaryWorkflowID = -999;
    public static String ErrorMessage = "Error";
    public static String WarningMessage = "Warning";
    public static String InformationMessage = "Information";
    public static String SaveStateResponseMessage = "SaveStateResponse";
    public static String WorkflowInformation = "WorkflowInformation";
    public static String UploadResponse = "UploadResponse";

    public static String gaggleRInitFile = "gaggleInit.R";
    public static String gaggleRBatchFile64 = "gaggleR64.bat";
    public static String gaggleRBatchFile32 = "gaggleR32.bat";
    public static String gaggleRShellFile = "gaggleR.sh";

    /**
     * Grab the inputstream of a process started by Runtime.exec
     */
    class StreamGobbler extends Thread
    {
        InputStream is;
        String type;

        StreamGobbler(InputStream is, String type)
        {
            this.is = is;
            this.type = type;
        }

        public void run()
        {
            try
            {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                while ( (line = br.readLine()) != null)
                    Log.info(type + ">" + line);
            } catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }

    class ShutdownHookThread extends Thread
    {
        public void run()
        {
            Cleanup(true);
        }
    }

    /// This thread cleans up workflow threads that have finished
    class ResourceManagementThread extends Thread
    {
        public void run()
        {
            while (true)
            {
                try {
                    Thread.sleep(5000);
                    Cleanup(false);
                }
                catch (Exception e)
                {
                    Log.severe("Workflow resource management thread failed: " + e.getMessage());
                    Cleanup(true);
                }
            }
        }
    }

    private synchronized void Cleanup(boolean force)
    {
        for ( UUID key : threadMap.keySet())
        {
            WorkflowThread t = threadMap.get(key);
            if (force || t.cancel)
            {
                t.cancel = true;
                threadMap.remove(key);
                Log.info("Removed thread for workflow " + key);
            }
        }
    }

    public WorkflowManager(BossImpl bossImpl, GooseManager gm)
    {
        this.gooseManager = gm;
        this.bossImpl = bossImpl;
        this.threadMap = Collections.synchronizedMap(new HashMap<UUID, WorkflowThread>());
        this.shutdownHook = new ShutdownHookThread();
        Runtime.getRuntime().addShutdownHook(this.shutdownHook);
        this.resourceManagementThread = new ResourceManagementThread();
        this.resourceManagementThread.start();

        // Create the temp directory
        try
        {
            String tempDir = System.getProperty("java.io.tmpdir");
            Log.info("Temp dir: " + tempDir);
            if (tempDir.startsWith("/var/folders"))
                tempDir = "/tmp";
            Log.info("New temp dir: " + tempDir);
            tempDir += ("/Gaggle/" + tempFolderName);
            myTempFolder = new File(tempDir);
            if (!myTempFolder.exists())
            {
                Log.info("Make temp folder: " + myTempFolder.getAbsolutePath());
                myTempFolder.mkdirs();
            }
        }
        catch (Exception e)
        {
            Log.severe("Failed to create temp dir for workflowManager: " + e.getMessage());
        }
    }

    public File getMyTempFolder() { return myTempFolder; }

    /*public void addEdge(Goose source, Goose target, String data)
    {
        if (source != null && (source instanceof Goose3) && target != null && (target instanceof Goose3))
        {
            Log.info("Add edge from " + source + " " + target + " " + data);
            WorkflowComponent src = createNode((Goose3)source, data);
            WorkflowComponent trgt = (source != target) ? createNode((Goose3)target, null) : src;
            Log.info("Adding edge for " + src.getComponentWorkflowNodeID() + " " + trgt.getComponentWorkflowNodeID());
            topWorkflow.getWorkflow().get(src.getComponentWorkflowNodeID()).get(0).add(trgt);
        }
    }

    private WorkflowComponent createNode(Goose3 goose, String data)
    {
        try
        {
            GaggleGooseInfo gooseInfo = goose.getGooseInfo();
            String workflowNodeID = gooseInfo.getComponentWorkflowNodeID();
            Log.info("Goose workflow node ID");
            if (workflowNodeID == null || !topWorkflow.getWorkflow().containsKey(workflowNodeID))
            {
                String nodeID = "wfcid" + UUID.randomUUID().toString() + "_component_" + gooseInfo.getWorkflowComponentID();
                goose.setGooseInfo();
                HashMap<String, Object> params = new HashMap<String, Object>();
                if (data != null)
                {

                }
                WorkflowComponent c = new WorkflowComponent(nodeID, goose.getName(), goose.getName(), "", "", "", null);

            }
        }
        catch (Exception e)
        {
            Log.severe("Failed to create node " + e.getMessage());
        }

    }  */

    public void SubmitWorkflow(Goose3 goose, Workflow w)
    {
        if (w != null)
        {
            Log.info("Workflow submitted");

            // Add the workflow to the top level workflow.
            synchronized (topWorkflowSyncObj)
            {
                if (topWorkflow == null || w.getIsReset())
                {
                    topWorkflow = new Workflow();
                    temporaryWorkflowID--;
                    topWorkflow.setWorkflowID(Integer.toString(temporaryWorkflowID));
                    submittedWorkflows.clear();
                }

                if (!w.getIsReset())
                {
                    Log.info("Adding workflow to top level workflow");
                    // If the workflow has an existing ID (meaning it has been saved),
                    // we used its ID for the top workflow (i.e., it is no longer a "tempororay" workflow)
                    // And we can correlate the saved sessions using the workflow ID
                    if (w.getWorkflowID() != null && w.getWorkflowID().length() > 0)
                        topWorkflow.setWorkflowID(w.getWorkflowID());
                    topWorkflow.addWorkflow(w);
                    submittedWorkflows.add(w);

                    UUID sessionID = UUID.randomUUID();
                    WorkflowThread t = new WorkflowThread(goose, sessionID, w);
                    this.threadMap.put(sessionID, t);
                    t.start();
                    // We start reccording as soon as the user submits a workflow, recording
                    // will be on forever!! At each step, we broadcast the source and target
                    // goose back to the Proxy Goose
                    this.bossImpl.setRecording(true);
                    // Tell the proxy applet the top workflow ID
                    Report(WorkflowInformation, this.topWorkflow.getWorkflowID());
                }
            }
        }
    }

    /**
     * Handle workflow action received from children of a source goose
     * If it is a Response, we need to inform the source goose to
     * proceed for the next geese
     * @param action
     */
    public void HandleWorkflowAction(WorkflowAction action)
    {
        if (action != null)
        {
            if (action.getSource() != null)
                Log.info("Handle workflow action from: "
                        + action.getSource().getName() + " " + action.getSessionID());
            switch (action.getActionType())
            {
                case Response:
                    WorkflowThread t = threadMap.get(UUID.fromString(action.getSessionID()));
                    if (t != null)
                    {
                        if ((action.getOption() & WorkflowAction.Options.ErrorMessage.getValue()) > 0)
                        {
                            // we need to pass the error back to the proxy goose
                        }
                        else if ((action.getOption() & WorkflowAction.Options.SuccessMessage.getValue()) > 0)
                        {
                            // received acknowledge data from a goose
                            t.Acknowledge(action);
                        }
                    }
                    break;

                case Request:
                    Log.info("Workflow Action Request received with option " + action.getOption());
                    if ((action.getOption() & WorkflowAction.Options.WorkflowReportData.getValue()) > 0)
                    {
                        // This is report data, we need to send it to web server
                        reportData(action);
                    }
                    else if ((action.getOption() & WorkflowAction.Options.FileUploadRequest.getValue()) > 0)
                    {
                        // This is a request to upload a user data file captured from the gaggled web page to the server
                        uploadUserData(action);                    }
                    break;

                default:
                    Log.warning("Unexpected action type received by Workflow Manager.");
                    break;
            }
        }
    }

    private void uploadUserData(WorkflowAction action)
    {
        String jsonstring = (String)((WorkflowData)action.getData()[0]).getData();
        System.out.println("Upload data json string " + jsonstring);
        JSONObject jsonObject = JSONObject.fromObject(jsonstring);

        String[] propNames = new String[6];
        String[] propValues = new String[6];
        propNames[0] = "organismtype";
        propValues[0] = jsonObject.getString("organismtype");
        propNames[1] = "userid";
        propValues[1] = jsonObject.getString("userid");

        JSONObject filesobj = jsonObject.getJSONObject("data");
        int index = 0;
        do{
            Log.info("Retrieving file object with index " + Integer.toString(index));
            JSONObject fileobj = filesobj.getJSONObject(Integer.toString(index));
            if (fileobj != null)
            {
                try
                {
                    propNames[2] = "datatype";
                    propValues[2] = fileobj.getString("datatype");
                    propNames[3] = "description";
                    propValues[3] = fileobj.getString("description");
                    propNames[4] = "nodeindex";
                    propValues[4] = fileobj.getString("nodeindex");
                    propNames[5] = "text";
                    propValues[5] = fileobj.getString("text");

                    String fileurl = fileobj.getString("fileurl");
                    System.out.println("File url to upload " + fileurl);
                    if (fileurl.toLowerCase().startsWith("file://"))
                    {
                        int hostnameslash = fileurl.indexOf("/", 7);
                        if (hostnameslash >= 7)
                        {
                            String hostname = fileurl.substring(7, hostnameslash);
                            Log.info("File hostname " + hostname);
                            if (hostname.length() == 0 || hostname.toLowerCase().equals("localhost"))
                            {
                                if ((fileurl.charAt(hostnameslash + 2) == ':') || (fileurl.charAt(hostnameslash + 2) == '|'))
                                {
                                    // this is a windows file url
                                    fileurl = fileurl.substring(hostnameslash + 1);
                                }
                                else
                                {
                                    // This is a Unix file url
                                    fileurl = fileurl.substring(hostnameslash);
                                }
                            }
                        }
                    }

                    Log.info("File to be uploaded: " + fileurl);
                    URL serverurl = new URL(BossImpl.GAGGLE_SERVER + "/workflow/uploaddata");
                    ArrayList<File> files = new ArrayList<File>();
                    File f = new File(fileurl);
                    files.add(f);
                    HttpFileUploadHelper httpFileUploadHelper = new HttpFileUploadHelper(serverurl, propNames, propValues, files);
                    httpFileUploadHelper.startUpload();
                    ArrayList<String> jsonresponses = httpFileUploadHelper.getUploadResult();
                    for (String response : jsonresponses)
                    {
                        if (response != null && response.length() > 0)
                        {
                            bossImpl.addMessage(WorkflowManager.UploadResponse, response);
                        }
                    }
                }
                catch (Exception e)
                {
                    System.out.println("Failed to handle file object " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
                index++;
            }
            else
                break;
        }
        while (true);
    }

    /**
     * Send the report data to server to generate report html page
     * @param action
     */
    private void reportData(WorkflowAction action)
    {
        if (action.getData() != null)
        {
            Log.info("Handle report data for Workflow " + action.getWorkflowID() + " session " + action.getSessionID() + " component " + action.getComponentID());
            GaggleData data = action.getData()[0];
            if (data != null && data instanceof WorkflowData)
            {
                Tuple tpl = (Tuple)(((WorkflowData)data).getData());
                Log.info("====> Processing " + tpl.getSingleList().size() + " report parameters");
                String type = "";
                Object value = null;
                String componentname = "";
                for (int i = 0; i < tpl.getSingleList().size(); i++)
                {
                    Single s = tpl.getSingleList().get(i);
                    if (s != null)
                    {
                        if (s.getName().toLowerCase().equals("type"))
                        {
                            type = (String)s.getValue();
                            Log.info("Type: " + type);
                        }
                        else if (s.getName().toLowerCase().equals("file") || s.getName().toLowerCase().equals("url"))
                        {
                            value = s.getValue();
                            Log.info("Value: " + value);
                        }
                        else if (s.getName().toLowerCase().equals("component-name"))
                        {
                            componentname = (String)s.getValue();
                            Log.info("Component name: " + componentname);
                        }
                        else {
                            Log.warning("Encountered unknown property name: " + s.getName());
                        }
                    }
                    else
                        Log.warning("Encountered a null Single");
                }

                if (!type.isEmpty() && value != null)
                {
                    URL url = null;
                    try {
                        String server = BossImpl.GAGGLE_SERVER;
                        Log.info("Web server: " + server);
                        if (server == null || server.length() == 0)
                            server = "http://networks.systemsbiology.net";
                        url = new URL((server + "/workflow/savereport/"));
                        //url = new URL("http://localhost:8000/workflow/savereport/");
                    } catch (MalformedURLException ex) {
                        Log.warning("Malformed URL");
                    }

                    HttpURLConnection urlConn = null;
                    try {
                        // URL connection channel.
                        urlConn = (HttpURLConnection) url.openConnection();


                        // Let the run-time system (RTS) know that we want input.
                        urlConn.setDoInput (true);

                        // Let the RTS know that we want to do output.
                        urlConn.setDoOutput (true);

                        // No caching, we want the real thing.
                        urlConn.setUseCaches (false);
                    }
                    catch (IOException ex) {
                        Log.severe("Failed to create url Connection " + ex.getMessage());
                    }

                    try
                    {
                        ClientHttpRequest httpRequest = new ClientHttpRequest(urlConn);
                        InputStream responseStream = null;
                        if (type.equals("file"))
                        {
                            Log.info("Upload file path: " + (String)value + " of Component " + componentname + " component workflow node id " + action.getSource().getComponentWorkflowNodeID() + " for Workflow " + action.getWorkflowID());
                            if (value != null && !((String)value).isEmpty())
                            {
                                // send the component name
                                synchronized (topWorkflowSyncObj)
                                {
                                    httpRequest.setParameter("userid", topWorkflow.getUserID());
                                    httpRequest.setParameter("workflowid", topWorkflow.getWorkflowID());
                                }
                                httpRequest.setParameter("sessionid", action.getSessionID());
                                httpRequest.setParameter("component-name", componentname);
                                httpRequest.setParameter("componentid", action.getComponentID());
                                httpRequest.setParameter("componentworkflownodeid", action.getSource().getComponentWorkflowNodeID());
                                File f = new File((String)value);
                                //FileInputStream is = new FileInputStream((String)value);
                                httpRequest.setParameter("file", f);
                                responseStream = httpRequest.post();

                                // remove the temp file after uploading to the server
                                f.delete();
                            }
                        }
                        else if (type.equals("url"))
                        {
                            Log.info("Upload url: " + (String)value + " of Component " + componentname + " component workflow node id " + action.getSource().getComponentWorkflowNodeID() + " for Workflow " + action.getWorkflowID());
                            synchronized (topWorkflowSyncObj)
                            {
                                httpRequest.setParameter("userid", topWorkflow.getUserID());
                                httpRequest.setParameter("workflowid", topWorkflow.getWorkflowID());
                            }
                            httpRequest.setParameter("sessionid", action.getSessionID());
                            httpRequest.setParameter("component-name", componentname);
                            httpRequest.setParameter("componentid", action.getComponentID());
                            httpRequest.setParameter("componentworkflownodeid", action.getSource().getComponentWorkflowNodeID());

                            //String content =
                            //        "name=" + URLEncoder.encode((String)value, "utf-8");
                            httpRequest.setParameter("url", (String)value);
                            responseStream = httpRequest.post();
                        }

                        if (responseStream != null)
                        {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                            StringBuilder builder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null)
                            {
                                Log.info("Got line: " + line);
                                builder.append(line);
                            }
                            String workflowid = builder.toString();
                            Log.info("Save report to workflow " + workflowid);
                            if (workflowid != null && workflowid.length() > 0)
                            {
                                this.topWorkflow.setWorkflowID(workflowid);
                                Report(WorkflowInformation, this.topWorkflow.getWorkflowID());
                                Report(InformationMessage, ("Pass workflow ID " + this.topWorkflow.getWorkflowID() + " to Proxy Applet"));
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        Log.severe("Failed to post workflow data to server: " + e.getMessage());
                    }
                }

            }
        }
    }

    /**
     *  State machine
     */
    public enum ProcessingState
    {
        Initial,
        Pending,
        ParallelProcessed,
        ParallelAcknowledged,
        SequentialProcessed,
        SequentialAcknowledged,
        Finished,
        Error
    }

    class WorkflowNode
    {
        public ProcessingState state;
        public WorkflowComponent component;
        public Goose3 goose;
        // The number of sequential children that has acknowledged
        // to this node
        public int acknowledgedSequentials = 0;
        public int errorRetries = 0;
        public String gooseName;

        public WorkflowNode(WorkflowComponent c)
        {
            this.component = c;
            this.state = ProcessingState.Initial;
        }
    }

    public void downloadFileFromUrl(String filename, String urlString)
    {
        File f = new File(filename);
        if (f.exists())
            return;

        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try
        {
            in = new BufferedInputStream(new URL(urlString).openStream());
            fout = new FileOutputStream(filename);

            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1)
            {
                fout.write(data, 0, count);
            }
        }
        catch (Exception e)
        {
            Log.severe("Failed to save file from url " + urlString);
            for (StackTraceElement ste : e.getStackTrace())
            {
                Log.severe(ste.toString() + " " + ste.getClassName() + " " + ste.getFileName() + " "
                        + ste.getMethodName() + " " + ste.getLineNumber());
            }
        }
        finally
        {
            try {
                if (in != null)
                    in.close();
                if (fout != null)
                    fout.close();
            }
            catch (Exception e1)
            {
                Log.severe("Failed to close io streams: " + e1.getMessage());
            }
        }
    }



    class WaitForGooseStart extends TimerTask {
        long startTime = System.currentTimeMillis();
        String gooseName;
        boolean gooseStarted = false;
        Object syncObj;
        String[] snapshotBeforeStartingGoose;

        public WaitForGooseStart(String gooseName, Object syncObj)
        {
            super();
            this.gooseName = gooseName;
            this.syncObj = syncObj;
            // Take a snapshot
            snapshotBeforeStartingGoose = bossImpl.getListeningGooseNames();
        }

        public boolean IsGooseStarted() { return gooseStarted; }

        private boolean InSnapshot(String gooseName)
        {
            if (snapshotBeforeStartingGoose != null)
            {
                for (int i = 0; i < snapshotBeforeStartingGoose.length; i++)
                {
                    Log.info(snapshotBeforeStartingGoose[i] + " " + gooseName);
                    if (snapshotBeforeStartingGoose[i].equals(gooseName))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        public void run() {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > timerTimeout || this.syncObj == null) {
                Log.info("Didn't hear from the goose, timing out.");
                this.cancel();
            }

            try {
                // we get the listening geese, non-listening geese could be "phantom"
                // (e.g. Close Firefox and firegoose might still lingering there
                String[] gooseNames = bossImpl.getListeningGooseNames();
                for (int i = 0; i < gooseNames.length; i++) {
                    String currentGooseName = gooseNames[i];
                    //Log.info("Retrieve a goose: " + currentGooseName + " " + this.gooseName);
                    String[] gooseNameSplitted = currentGooseName.split(";");
                    if ((gooseNameSplitted.length > 1 && gooseNameSplitted[0].equals(gooseNameSplitted[1]))
                            || gooseNameSplitted.length == 1)
                    {
                        // We always append the name of the application for Cytoscape, we want to broadcast
                        // to the original goose
                        String finalgoosename = NameUniquifier.getOrginalGooseName(currentGooseName).toLowerCase();
                        Log.info("Wait for goose final name " + finalgoosename);
                        if (((this.gooseName.equals("R") && (finalgoosename.toLowerCase().equals("r")))
                                || (!this.gooseName.equals("R") && finalgoosename.contains(this.gooseName.toLowerCase())))
                                && !InSnapshot(currentGooseName))
                        {
                            // The goose has been started correctly!
                            Log.info("Our goose is started!");
                            gooseStarted = true;
                            this.gooseName = currentGooseName;
                            synchronized (syncObj) {
                                syncObj.notify();
                            }
                            this.cancel();
                        }
                    }
                }
            } catch (Exception ex) {
                Log.log(Level.WARNING, "unknown Exception in WaitForGooseStart: " + ex.getMessage());
                String message = "general exception trying to start goose: " + this.gooseName + " " + ex.getMessage();
                String messageType = "Error";
                System.out.println(message);
                ex.printStackTrace();
            }
        }
    }


    public Goose3 PrepareGoose(WorkflowComponent source, Object syncObj)
    {
        if (source != null)
        {
            // First clean up unregistered geese (Firegoose can be lingering around even if Firefox is closed)
            bossImpl.unregisterIdleGeeseAndUpdate();

            Goose goose = null;
            boolean  forceStart = ((source.getOptions() & WorkflowComponent.Options.OpenInNewWindow.getValue()) > 0) ? true : false;
            Log.info("Component " + source.getComponentID() + " force start " + forceStart);
            if (!forceStart)
            {

                if (source.getExistingGooseName() != null)
                {
                    Log.info("Find existing goose: " + source.getExistingGooseName());

                    String[] geeseNames = bossImpl.getListeningGooseNames();
                    for (int i = 0; i < geeseNames.length; i++)
                    {
                        if (geeseNames[i].equals(source.getExistingGooseName()))
                        {
                            Log.info("Found existing goose " + geeseNames[i]);
                            goose = bossImpl.getGoose(geeseNames[i]);
                            break;
                        }
                    }

                    if (goose == null)
                    {
                        // If we didn't find an exact match, we go for the same existing goose
                        // This is for loading state, where we don't have an exact match.
                        for (int i = 0; i < geeseNames.length; i++)
                        {
                            String finalgoosename = NameUniquifier.getOrginalGooseName(geeseNames[i]).toLowerCase();
                            if (finalgoosename.equals(source.getExistingGooseName().toLowerCase()))
                            {
                                Log.info("Found existing goose with unmatched name " + geeseNames[i]);
                                goose = bossImpl.getGoose(geeseNames[i]);
                                break;
                            }
                        }
                    }
                }
            }
            /*else
            {

                String[] geeseNames = bossImpl.getListeningGooseNames();
                boolean foundGoose = false;

                if (geeseNames != null)
                {
                    for (int i = 0; i < geeseNames.length; i++)
                    {
                        String currentGooseName = geeseNames[i];
                        Log.info("Current goose name: " + currentGooseName);
                        String[] gooseNameSplitted = currentGooseName.split(";");
                        Log.info("Splitted goose name: " + gooseNameSplitted.length);
                        if (gooseNameSplitted.length > 1)
                            Log.info("Splitted goose name: " + gooseNameSplitted[0] + " " + gooseNameSplitted[1]);
                        if ((gooseNameSplitted.length > 1 && gooseNameSplitted[0].equals(gooseNameSplitted[1]))
                                || gooseNameSplitted.length == 1)
                        {
                            // We always append the name of the application for Cytoscape, we want to broadcast
                            // to the original goose with the name similar to Cytoscape v2.8.3;Cytoscape v2.8.3
                            String finalgoosename = NameUniquifier.getOrginalGooseName(currentGooseName).toLowerCase();
                            Log.info("Final goose name to compare " + finalgoosename);
                            if (finalgoosename.equals(source.getGooseName().toLowerCase()))
                            {
                                goose = bossImpl.getGoose(geeseNames[i]);
                                if (goose instanceof Goose3)
                                {
                                    Log.info("Found existing goose " + geeseNames[i]);
                                    Report(InformationMessage, ("Found existing goose " + geeseNames[i]));
                                    return (Goose3)goose;
                                }
                            }
                        }
                    }
                }
            } */

            if (goose == null)
            {
                Log.info("No goose found for " + source.getGooseName());
                Log.info("Goose name " + source.getGooseName() + " os.name: " + System.getProperty("os.arch") + " " + bossImpl.getGoose("Firegoose"));
                if (source.getGooseName().equalsIgnoreCase("firegoose")
                        && System.getProperty("os.name").startsWith("Mac") && bossImpl.getGoose("Firegoose") != null)
                {
                    // On Mac, Firefox cannot start a new window if one is already openned, we
                    // have to reuse the existing window anyway
                    goose =  bossImpl.getGoose("Firegoose");
                }
                else
                {
                    goose = tryToStartGoose(source, syncObj);
                }
                if (goose != null && goose instanceof Goose3)
                    return (Goose3)goose;
            }
            else
            {
                Log.info("Goose " + source.getGooseName() + " Found! ");
                if (goose instanceof  Goose3)
                    return (Goose3)goose;
            }
            return null;
        }
        return null;
    }

    public Goose tryToStartGoose(WorkflowComponent goose, Object syncObj) {
        if (goose != null && syncObj != null)
        {
            //String command = System.getProperty("java.home");
            //command += File.separator +  "bin" + File.separator + "javaws " + GaggleConstants.BOSS_URL;
            gooseStarted = false;
            String[] gooseCmds = new String[2];
            gooseCmds[0] = bossImpl.getAppInfo(goose.getName());
            gooseCmds[1] = goose.getCommandUri(); // If goose is "Generic", command uri is the data uri
            Log.info("Goose commanduri: " + gooseCmds[1]);
            if (gooseCmds[1].toLowerCase().indexOf(".jnlp") >= 0)
            {
                // JNLP overrides execution path
                gooseCmds[0] = gooseCmds[1];
            }
            for (int i = 0; i < 2; i++)
            {
                try {
                    String cmdToRun = gooseCmds[i];
                    Log.info("Starting goose " + goose.getGooseName() + " using " + cmdToRun);
                    if (cmdToRun == null || cmdToRun.isEmpty())
                        continue;

                    Report(InformationMessage, ("Starting goose " + goose.getGooseName() + " using " + cmdToRun));
                    if (goose.getArguments() != null && goose.getArguments().length() > 0)
                        Log.info("Arguments: " + goose.getArguments());
                    if (cmdToRun != null && cmdToRun.trim().length() > 0)
                    {
                        String goosename = goose.getGooseName();
                        String subaction = (String)goose.getParams().get(WorkflowComponent.ParamNames.SubTarget.getValue());
                        gooseStarted = startGoose(goose.getGooseName(), cmdToRun,
                                subaction,
                                goose.getArguments(), tempFileToken);

                        if (!gooseStarted)
                        {
                            Timer timer = new Timer();
                            Log.info("Starting the WaitForGooseStart thread..." + goose.getGooseName());
                            WaitForGooseStart wfg = new WaitForGooseStart(goose.getGooseName(), syncObj); //.getName());
                            timer.schedule(wfg, 0, timerInterval);
                            synchronized (syncObj) {
                                syncObj.wait();
                            }
                            if (wfg.IsGooseStarted())
                                return gooseManager.getGoose(wfg.gooseName);
                        }
                        else
                        {
                            // In cases that we only start a command shell, we j
                            return null;
                        }
                    }
                    else
                        Log.warning("Empty command line for " + goose.getGooseName() + "encountered!");
                } catch (IOException e) {
                    String message = "Failed to start goose: " + goose.getGooseName() + " " + e.getMessage();
                    String messageType = "Error";
                    Log.severe(message);
                    Report(ErrorMessage, ("Failed to start goose: " + goose.getGooseName() + " " + e.getMessage()));
                    e.printStackTrace();
                }
                catch (InterruptedException e1) {
                    String message = "Failed to wait for goose: " + goose.getGooseName() + " " + e1.getMessage();
                    String messageType = "Error";
                    Log.severe(message);
                    Report(ErrorMessage, message);
                    e1.printStackTrace();
                }
                catch (Exception e2)
                {
                    Log.severe("Failed to start goose: " + e2.getMessage());
                    Report(ErrorMessage, ("Failed to start goose: " + goose.getGooseName() + " " + e2.getMessage()));
                }
            }
        }
        return null;
    }

    private boolean gooseIsListening(String gooseName)
    {
        String[] geeseNames = bossImpl.getListeningGooseNames();
        if (geeseNames != null)
        {
            for (int i = 0; i < geeseNames.length; i++)
            {
                String currentGooseName = geeseNames[i];
                Log.info("Current goose name: " + currentGooseName);
                String[] gooseNameSplitted = currentGooseName.split(";");
                Log.info("Splitted goose name: " + gooseNameSplitted.length);
                if (gooseNameSplitted.length > 1)
                    Log.info("Splitted goose name: " + gooseNameSplitted[0] + " " + gooseNameSplitted[1]);
                if ((gooseNameSplitted.length > 1 && gooseNameSplitted[0].equals(gooseNameSplitted[1]))
                        || gooseNameSplitted.length == 1)
                {
                    // We always append the name of the application for Cytoscape, we want to broadcast
                    // to the original goose with the name similar to Cytoscape v2.8.3;Cytoscape v2.8.3
                    if (currentGooseName.trim().toLowerCase().contains(gooseName.toLowerCase()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean startGoose(String gooseName, String cmdToRun, String subaction, String arguments, String tempFileToken) throws Exception
    {
        if (cmdToRun == null || cmdToRun.length() == 0)
            return false;

        Report(InformationMessage, "Start goose " + gooseName + " Command line: " + cmdToRun
                + " Subaction: " + subaction + " Arguments: " + arguments + " File token: " + tempFileToken);
        boolean gooseStarted = false;
        String os = System.getProperty("os.name");
        String cmdToRunTarget = cmdToRun.trim().toLowerCase();
        Log.info("---===>Lowercase command line: " + cmdToRunTarget);
        // Somehow JSON Stringify wrapped the command uri with "".
        if (cmdToRunTarget.toLowerCase().endsWith(".bat\"") || cmdToRunTarget.toLowerCase().endsWith("bat"))
        {
            Log.info("Command line is a batch file... " + cmdToRun);
            // if it is a batch file, we need to do something more...
            // Remove the quotations
            if (cmdToRunTarget.endsWith(".bat\""))
                cmdToRunTarget = cmdToRunTarget.substring(1, cmdToRun.length() - 1);

            File f = new File(cmdToRunTarget);
            String path = f.getParent();

            String[] cmdsToRun = new String[5];
            cmdsToRun[0] = "cmd.exe";
            cmdsToRun[1] = "/C";
            cmdsToRun[2] = "start";
            cmdsToRun[3] = cmdToRunTarget;
            cmdsToRun[4] = arguments;
            Runtime.getRuntime().exec(cmdsToRun, null, new File(path));
        }
        else if (cmdToRunTarget.toLowerCase().endsWith("r.exe") || cmdToRun.toLowerCase().endsWith("/r") || cmdToRunTarget.endsWith(".r"))
        {
            // Download the gaggle init.r file
            String server = BossImpl.GAGGLE_SERVER;
            String initfilename = this.getMyTempFolder().getAbsolutePath() + File.separator + gaggleRInitFile;
            downloadFileFromUrl(initfilename, (server + "/static/applications/" + gaggleRInitFile));
            Log.info("Downloading R init file " + gaggleRInitFile + " from " + server);

            String[] cmdsToRun = null;
            String fullscriptfilename = "";
            String scriptfilename = "";
            String arch = System.getProperty("os.arch");
            if (os.toLowerCase().indexOf("win") >= 0)
            {
                if (arch.startsWith("amd64") || arch.startsWith("x64"))
                {
                    fullscriptfilename = this.getMyTempFolder().getAbsolutePath() + File.separator + gaggleRBatchFile64;
                    scriptfilename = gaggleRBatchFile64;
                }
                else
                {
                    fullscriptfilename = this.getMyTempFolder().getAbsolutePath() + File.separator + gaggleRBatchFile32;
                    scriptfilename = gaggleRBatchFile32;
                }

                Log.info("Downloading script file " + fullscriptfilename + " Server " + server);
                downloadFileFromUrl(fullscriptfilename, (server + "/static/applications/" + scriptfilename));

                cmdsToRun = new String[6];
                cmdsToRun[0] = "cmd.exe";
                cmdsToRun[1] = "/K";
                cmdsToRun[2] = "start";
                cmdsToRun[3] = "cmd";
                cmdsToRun[4] = "/K";
                cmdsToRun[5] = fullscriptfilename;
                Process proc = Runtime.getRuntime().exec(cmdsToRun, null, this.getMyTempFolder());
            }
            else if (os.startsWith("Mac"))
            {
                fullscriptfilename = this.getMyTempFolder().getAbsolutePath() + File.separator + gaggleRShellFile;
                scriptfilename = gaggleRShellFile;
                Log.info("Downloading Mac script file " + fullscriptfilename + " Server " + server);
                downloadFileFromUrl(fullscriptfilename, (server + "/static/applications/" + scriptfilename));

                // First give permission to the script file
                //Process chmod = Runtime.getRuntime().exec("chmod u+x \"" + fullscriptfilename + "\"");
                Log.info("Setting execution permission for " + fullscriptfilename);
                File file = new File(fullscriptfilename);
                file.setReadable(true, true);
                file.setExecutable(true, false);

                cmdsToRun = new String[4];
                cmdsToRun[0] = "/usr/bin/open";
                cmdsToRun[1] = "-a";
                cmdsToRun[2] = "Terminal";
                cmdsToRun[3] = fullscriptfilename;
                Log.info("Execute /bin/sh " + scriptfilename);
                ProcessBuilder pb = new ProcessBuilder(cmdsToRun);
                pb.directory(this.getMyTempFolder());
                //pb.redirectErrorStream(true);
                Process proc = pb.start();
                //Process proc = Runtime.getRuntime().exec(("/bin/sh " + fullscriptfilename + " --save"), null, this.getMyTempFolder());
                StreamGobbler errorGobbler = new
                        StreamGobbler(proc.getErrorStream(), "ERROR");

                // any output?
                StreamGobbler outputGobbler = new
                        StreamGobbler(proc.getInputStream(), "OUTPUT");
                errorGobbler.start();
                outputGobbler.start();

                //BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                proc.waitFor();
                if(proc.exitValue()!=0)
                {
                    Log.info("Failed to start R script file " + proc.exitValue());
                }


            }
            else if (os.startsWith("Linux"))
            {
                fullscriptfilename = this.getMyTempFolder().getAbsolutePath() + File.separator + gaggleRShellFile;
                scriptfilename = gaggleRShellFile;
                Log.info("Downloading Linux script file " + fullscriptfilename + " Server " + server);
                downloadFileFromUrl(fullscriptfilename, (server + "/static/applications/" + scriptfilename));

                // First give permission to the script file
                //Process chmod = Runtime.getRuntime().exec("chmod u+x \"" + fullscriptfilename + "\"");
                Log.info("Setting execution permission for " + fullscriptfilename);
                File file = new File(fullscriptfilename);
                file.setReadable(true, true);
                file.setExecutable(true, false);

                cmdsToRun = new String[3];
                cmdsToRun[0] = "/usr/bin/xterm";
                cmdsToRun[1] = "-e";
                //cmdsToRun[2] = "Terminal";
                cmdsToRun[2] = fullscriptfilename;
                Log.info("Execute xterm " + scriptfilename);
                ProcessBuilder pb = new ProcessBuilder(cmdsToRun);
                pb.directory(this.getMyTempFolder());
                //pb.redirectErrorStream(true);
                Process proc = pb.start();
                //Process proc = Runtime.getRuntime().exec(("/bin/sh " + fullscriptfilename + " --save"), null, this.getMyTempFolder());
                StreamGobbler errorGobbler = new
                        StreamGobbler(proc.getErrorStream(), "ERROR");

                // any output?
                StreamGobbler outputGobbler = new
                        StreamGobbler(proc.getInputStream(), "OUTPUT");
                errorGobbler.start();
                outputGobbler.start();

                //BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                proc.waitFor();
                if(proc.exitValue()!=0)
                {
                    Log.info("Failed to start R script file " + proc.exitValue());
                }


            }
        }
        else if (cmdToRunTarget.toLowerCase().endsWith(".py;;"))
        {
            // This is a python script file, we first take a look at the subaction, which contains the
            // url of the application needs to be started first and the name of the goose to be started
            if (subaction != null && subaction.length() > 0)
            {
                String[] subactionsplitted = subaction.split(";;");
                if (subactionsplitted.length > 1)
                    // In this case, the goose name is the second string of the subaction string
                    gooseName = subactionsplitted[1];

                if (!gooseIsListening(gooseName))
                {
                    if (subaction != null)
                    {
                        if (subactionsplitted != null && subactionsplitted.length > 0)
                        Report(InformationMessage, "Starting goose " + gooseName + " before executing script " + cmdToRunTarget);
                        startGoose(gooseName, subactionsplitted[0], null, null, tempFileToken);
                        int wait = 0;
                        while (!gooseIsListening(gooseName) && wait < 40)
                        {
                            Thread.sleep(5000);
                            wait++;
                        }

                        if (gooseIsListening(gooseName))
                        {
                            // Make sure we wait a while before we pass data to the goose
                            // Cytoscape crashes if we run script right after it starts
                            Thread.sleep(5000);
                        }
                    }
                }
            }


            int argcnt = 0;
            String[] arglist = null;
            if (arguments != null)
            {
                arglist = arguments.split(" ");
                argcnt = arglist.length;
                Log.info("arguments " + arglist.length);
            }
            String[] commandsplitted = cmdToRun.split(";;");
            if (commandsplitted != null && commandsplitted.length > 0)
            {
                for (int i= 0; i < commandsplitted.length; i++)
                {
                    if (commandsplitted[i] != null && commandsplitted[i].length() > 0)
                    {
                        try {
                            String tempfilename = commandsplitted[i];
                            if (commandsplitted[i].contains("http://"))
                            {
                                // We need to download the script first
                                tempfilename = this.getMyTempFolder().getAbsolutePath() + File.separator + UUID.randomUUID().toString() + ".py";
                                downloadFileFromUrl(tempfilename, commandsplitted[i]);
                            }

                            Report(InformationMessage, "Now start script " + tempfilename);
                            try
                            {
                                File f = new File(tempfilename);
                                String path = f.getParent();
                                Log.info("Runtime Path " + path);

                                String[] cmdsToRun = new String[4 + argcnt];
                                cmdsToRun[0] = "cmd.exe";
                                cmdsToRun[1] = "/C";
                                //cmdsToRun[2] = "start";
                                cmdsToRun[2] = "python";
                                cmdsToRun[3] = tempfilename;
                                for (int j = 0; j < argcnt; j++)
                                {
                                    Log.info("Argument " + arglist[j]);
                                    cmdsToRun[4 + j] = arglist[j];
                                }
                                Runtime.getRuntime().exec(cmdsToRun, null, new File(path));
                            }
                            catch (Exception e1)
                            {
                                // FAiled, try Shell
                                File f = new File(tempfilename);
                                String path = f.getParent();

                                String[] cmdsToRun = new String[3 + argcnt];
                                cmdsToRun[0] = "python";
                                cmdsToRun[1] = tempfilename;
                                cmdsToRun[2] = arguments;
                                for (int j = 0; j < argcnt; j++)
                                {
                                    Log.info("Argument " + arglist[j]);
                                    cmdsToRun[3 + j] = arglist[j];
                                }
                                Runtime.getRuntime().exec(cmdsToRun, null, new File(path));
                            }
                        }
                        catch (Exception e)
                        {
                            Report(ErrorMessage, "Failed to start script " + commandsplitted[i] + " " + e.getMessage());
                        }
                    }
                }
            }
            gooseStarted = true;
        }
        else if (cmdToRunTarget.endsWith(".sh"))
        {
            Log.info("Shell script: " + cmdToRun);

            File f = new File(cmdToRun.trim());
            String path = f.getParent();

            String[] cmdsToRun = new String[3];
            cmdsToRun[0] = "sh";
            cmdsToRun[1] = cmdToRun.trim();
            cmdsToRun[2] = arguments;
            Runtime.getRuntime().exec(cmdsToRun, null, new File(path));
        }
        else if (cmdToRunTarget.endsWith(".jnlp"))
        {
            Log.info("jnlp " + cmdToRunTarget);
            // Save the file in temp dir
            String jnlpFileName = cmdToRun.substring(cmdToRunTarget.lastIndexOf("/") + 1);
            String jnlpFullPath = myTempFolder.getAbsolutePath() + File.separator + tempFileToken + "_" + jnlpFileName;
            Log.info("Temp file name: " + jnlpFullPath);
            downloadFileFromUrl(jnlpFullPath, cmdToRunTarget);
            //cmdToRunTarget = createJnlpForData((tempFileToken + "_" + jnlpFileName), datauri);
            Log.info("JNLP to run: " + cmdToRunTarget);
            String jwsdir = System.getProperty("java.home");
            jwsdir += File.separator + "bin";

            try {
                ProcessBuilder pb = new ProcessBuilder("javaws", cmdToRunTarget);
                pb.directory(new File(jwsdir));
                Process p = pb.start();
            } catch (Exception e) {
                Log.severe("Failed to start " + cmdToRunTarget + " " + e.getMessage());
                e.printStackTrace();
            }
        }
        else if (cmdToRunTarget.endsWith(".pl"))
        {
            Log.info("Perl script: " + cmdToRun);
            File f = new File(cmdToRunTarget);
            String path = f.getParent();
            if (os.toLowerCase().indexOf("win") >= 0)
            {
                Log.info("Starting windows console... " + path);
                String cmd = "cmd.exe /C start cmd.exe /K \"perl " + cmdToRun + "\"";
                Runtime.getRuntime().exec(cmd);
                //String[] cmdsToRun = new String[3];
                //cmdsToRun[0] = "cmd.exe";
                //cmdsToRun[1] = "/C";
                //cmdsToRun[2] = "cmd.exe /K \"perl " + cmdToRunTarget + "\"";
                //C:\\\\github\\\\baligalab\\\\projects\\\\gnome.pl\"";
                //Runtime.getRuntime().exec(cmdsToRun, null, new File(path));
                // This is a perl script (e.g., KBase), we consider the goose is started
                gooseStarted = true;
            }
            else
            {
                Log.info("Starting shell... " + path);
                String[] cmdsToRun = new String[3];
                cmdsToRun[0] = "sh";
                cmdsToRun[1] = "perl";
                cmdsToRun[2] = cmdToRun.trim();
                //cmdsToRun[3] = goose.getArguments();
                Runtime.getRuntime().exec(cmdsToRun, null, new File(path));
                gooseStarted = true;
            }
        }
        else
        {
            Log.info("Goose command line: " + cmdToRun.trim());
            String[] cmdsToRun = null;
            if (arguments == null || arguments == "")
            {
                cmdsToRun = new String[1];
                cmdsToRun[0] = cmdToRun.trim();
            }
            else
            {
                cmdsToRun = new String[2];
                cmdsToRun[0] = cmdToRun.trim();
                cmdsToRun[1] = arguments;
            }
            File f = new File(cmdToRun.trim());
            String path = f.getParent();
            Runtime.getRuntime().exec(cmdsToRun, null, new File(path)); // Goose will register itself with boss once it starts
        }
        return gooseStarted;
    }


    public void Report(String type, String msg)
    {
        Log.info(type + " " + msg);

        try{
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();

            bossImpl.addMessage(type, (dateFormat.format(date) + " " + msg));
            //proxyGoose.handleWorkflowInformation(type, (dateFormat.format(date) + " " + msg));
        }
        catch(Exception e)
        {
            Log.severe("Failed to call handleWorkflowInformation on Proxy Goose");
        }
    }

    class WorkflowThread extends Thread
    {
        String workflowID;
        UUID sessionID; // sessionID is needed for goose to communicate with the boss
        //int myIndex;
        //WorkflowSessionManager sessionManager;
        Workflow myWorkflow = null;
        HashMap<String, ArrayList<ArrayList<WorkflowComponent>>> workflowMap = null;

        // nodes that have been acknowledged by the processing component
        Map<String, String> acknowledgedParallelNodes = Collections.synchronizedMap(new HashMap<String, String>());
        Map<String, String> acknowledgedSequentialNodes = Collections.synchronizedMap(new HashMap<String, String>());

        // stores the data received from children component
        Map<String, GaggleData> stagingParallelData = Collections.synchronizedMap(new HashMap<String, GaggleData>());
        Map<String, GaggleData> stagingSequentialData = Collections.synchronizedMap(new HashMap<String, GaggleData>());

        // contains nodes currently being processed
        List<WorkflowNode> processingQueue = Collections.synchronizedList(new ArrayList<WorkflowNode>());

        // contains nodes that will be processed after all the nodes in the processingQueue are finished
        //ArrayList<WorkflowNode> pendingQueue;

        public boolean cancel = false;
        Goose3 proxyGoose = null;
        String messageType;
        String message;
        int MAX_ERROR_RETRIES = 1;
        int currIndex = 0;

        //Object threadSyncObj = new Object();
        //int stepsize = 1;

        public WorkflowThread(Goose3 proxyGoose, UUID sessionID, Workflow w)
        {
            Log.info("Initiating workflow thread " + sessionID.toString());
            //this.myIndex = indx;
            //this.sessionManager = sessionManager;
            this.workflowID = w.getWorkflowID();
            this.sessionID = sessionID;
            this.proxyGoose = proxyGoose;
            this.myWorkflow = w;
            this.workflowMap = w.getWorkflow();
            //this.processingQueue = new ArrayList<WorkflowNode>();
            //this.pendingQueue = new ArrayList<WorkflowNode>();
            //this.acknowledgedNodes = new HashedMap();
            //this.stagingData = new HashedMap<String, GaggleData>();
            //this.stepsize = stepsize;

            // add start nodes into the processingQueue
            /*ArrayList<String> startnodes = w.getStartNodeIDs();
            for (int i = 0; i < startnodes.size(); i++)
            {
                Log.info("Adding start node " + startnodes.get(i));
                try{
                    ArrayList parallelcomponents = this.workflowMap.get(startnodes.get(i)).get(0);
                    // Push the start node to the processing queue
                    System.out.println("Pushing start node " + startnodes.get(i));
                    WorkflowComponent c = (WorkflowComponent)parallelcomponents.get(0);
                    WorkflowNode node = new WorkflowNode(c);
                    this.processingQueue.add(node);
                }
                catch (Exception e)
                {
                    Log.severe(e.getMessage());
                }
            } */

            currIndex = 0;
            WorkflowComponent c = this.myWorkflow.getNode(currIndex++);
            if (c != null)
            {
                WorkflowNode node = new WorkflowNode(c);
                this.processingQueue.add(node);
                Report(InformationMessage, ("Workflow " + this.sessionID.toString() + " started with index " + currIndex));
            }
            else
                Report(WarningMessage, ("Workflow " + this.sessionID.toString() + " is empty"));
        }

        public void HandleError(WorkflowAction action)
        {
           try {
                this.proxyGoose.handleWorkflowAction(action);
           }
           catch (Exception e)
           {
               Log.info("Failed to pass error info to proxy goose");
           }
        }

        /**
         * Process acknowledgement from the source and children components
         * Store the data to the target geese
         * @param action
         */
        public void Acknowledge(WorkflowAction action)
        {
            Log.info("Acknowledging workflow thread: " + sessionID.toString());
            if ((action.getOption() & WorkflowAction.Options.Parallel.getValue()) > 0
                    && action.getTargets() != null)
            {
                GaggleData[] data = action.getData();
                WorkflowComponent[] targets = action.getTargets();

                Log.info("Storing parallel acknowledgement " + action.getSource().getComponentID() + " " + data.length);
                Report(InformationMessage, "Received parallel acknowledgement " + action.getSource().getGooseName());

                this.acknowledgedParallelNodes.put(action.getSource().getComponentID(), action.getSource().getComponentID());

                if (data != null && targets != null)
                {
                    for (int i = 0; i < data.length; i++)
                    {
                        this.stagingParallelData.put(targets[i].getComponentID(), data[i]);
                    }
                }
            }
            else if ((action.getOption() & WorkflowAction.Options.Sequential.getValue()) > 0
                    && action.getTargets() != null)
            {
                this.acknowledgedSequentialNodes.put(action.getSource().getComponentID(), action.getSource().getComponentID());
                GaggleData[] data = action.getData();
                WorkflowComponent[] targets = action.getTargets();
                Log.info("Storing sequential acknowledgement " + action.getSource().getComponentID() + " " + data.length);
                Report(InformationMessage, "Received sequential acknowledgement " + action.getSource().getGooseName());

                if (data != null && targets != null)
                {
                    for (int i = 0; i < data.length; i++)
                    {
                        this.stagingSequentialData.put(targets[i].getComponentID(), data[i]);
                    }
                }
            }
        }

        public void run()
        {
            //int readahead = stepsize;
            try {
                while (!cancel)
                {
                    boolean hasPendingNodes = false;
                    if (processingQueue.size() > 0)
                    {
                        //Log.info("New round of processing nodes");
                        for (int i = 0; i < processingQueue.size(); i++)
                        {
                            WorkflowNode c = processingQueue.get(i);
                            if (c.state != ProcessingState.Finished)
                            {
                                ProcessWorkflowNode(c);
                                hasPendingNodes = true;
                            }
                        }

                        if (!hasPendingNodes)
                        {
                            int i = 0;
                            while (true)
                            {
                                // check if there are still nodes not processed
                                WorkflowComponent c = myWorkflow.getNode(i);
                                if (c != null && !nodeProcessed(c))
                                {
                                    hasPendingNodes = true;
                                    WorkflowNode n = new WorkflowNode(c);
                                    processingQueue.add(n);
                                    break;
                                }
                                if (c == null)
                                    break;
                                i++;
                            }
                            if (!hasPendingNodes)
                                cancel = true;
                        }
                    }
                    /*else if (pendingQueue.size() > 0)
                    {
                        // fetch pending nodes to the processingQueue
                        readahead = (stepsize < 0 ? this.pendingQueue.size() : stepsize);
                        for (int i = readahead - 1; i >= 0; i--)
                        {
                            WorkflowComponent c = this.processingQueue.get(i);
                            processingQueue.add(c);
                            pendingQueue.remove(i);
                        }
                    } */
                    else
                    {
                        Log.info("Finished processing session " + this.sessionID.toString());
                        cancel = true;
                    }
                    Thread.sleep(2000);
                }

                // Inform the proxy goose that the workflow has finished execution
                Report(InformationMessage, ("Workflow " + this.sessionID.toString() + " Finished"));
            }
            catch (Exception e)
            {
                Log.severe("Error processing nodes for session " + this.sessionID.toString() + ": " + e.getMessage());
            }
            finally {


            }
        }

        /**
         *  Start a goose in a thread. If a goose takes long time to start, it won't block
         *  other geese.
         */
        class StartGooseThread extends Thread
        {
            private WorkflowNode workflowNode;
            private Workflow workflow;

            public StartGooseThread(WorkflowNode c, Workflow w)
            {
                this.workflowNode = c;
                this.workflow = w;
            }

            public void run()
            {
                Goose3 sourceGoose = workflowNode.goose;
                WorkflowComponent source = workflowNode.component;
                workflowNode.errorRetries++;
                Log.info("Handling workflow node " + source.getComponentID() + " state: initial" + " Cmd uri: " + source.getCommandUri());
                boolean sourceStarted = false;
                Object syncObj = new Object();

                // Find or create the goose corresponding to the source component
                if ((sourceGoose = PrepareGoose(source, syncObj)) != null)
                {
                    Log.info("Goose " + source.getGooseName() + " started.");
                    Report(InformationMessage, "Goose " + source.getGooseName() + " started.");

                    sourceStarted = true;
                    workflowNode.goose = sourceGoose;
                    try {
                        workflowNode.gooseName = sourceGoose.getName();
                    }
                    catch (Exception e)
                    {
                        Log.severe("Failed to get goose name...");
                        workflowNode.state = ProcessingState.Error;
                    }
                }
                else if (!gooseStarted)
                {
                    Report(ErrorMessage, ("Failed to start goose " + workflowNode.component.getGooseName()));

                    // we cannot start the goose, so let's mark this node as error
                    // we allow other errors so that the workflow won't be broken by one single node
                    Log.info("Node " + workflowNode.component.getComponentID() + " marked error state.");
                    workflowNode.state = ProcessingState.Error;
                }
                else
                {
                    // A command shell is started
                    workflowNode.state = ProcessingState.Finished;
                    Report(InformationMessage, "Successfully started " + workflowNode.gooseName);
                }

                if (sourceStarted)
                {
                    ArrayList<WorkflowComponent> parallelcomponents = workflowMap.get(source.getComponentID()).get(0);
                    Log.info("Component " + source.getComponentID() + " has " + parallelcomponents.size() + " parallel children");
                    WorkflowComponent[] targets = null;
                    //if (parallelcomponents != null && parallelcomponents.size() > 1)
                    {
                        // Pass the parallel children info to the source
                        targets = new WorkflowComponent[parallelcomponents.size()];
                        for (int i = 0; i < parallelcomponents.size(); i++)
                        {
                            targets[i] = parallelcomponents.get(i);
                            Log.info("Target component: " + targets[i].getGooseName());
                        }
                    }

                    // We need to do this even if there is no parallel child
                    // because the user might just want to show some data using
                    // the source component
                    try {
                        Log.info("Pass data to source for parallel children");
                        Log.info(source.getParams().get(WorkflowComponent.ParamNames.Data.getValue()).toString());
                        Log.info("Pass species info " + workflow.getSpecies());
                        source.addParam(JSONConstants.WORKFLOW_ORGANISMINFO, workflow.getSpecies());
                        Log.info("SessionID: " + sessionID.toString());
                        Log.info("ComponentID: " + source.getComponentID());
                        Report(InformationMessage, "Passing data to goose "
                                + workflowNode.component.getGooseName() + " Data "
                                + source.getParams().get(WorkflowComponent.ParamNames.Data.getValue()).toString());
                        WorkflowAction action = new WorkflowAction(
                                workflowID,
                                sessionID.toString(),
                                source.getComponentID(),
                                WorkflowAction.ActionType.Request,
                                source,
                                targets,
                                WorkflowAction.Options.Parallel.getValue(),
                                //WorkflowAction.DataType.WorkflowData,
                                null  // Data is contained in the source node's params property
                        );

                        //threadSyncObj = new Object();
                        sourceGoose.handleWorkflowAction(action);
                        // if there is no parallel child, we skip to the ParallelAcknowledged state
                        //workflowNode.state = (parallelcomponents != null && parallelcomponents.size() > 1) ?
                        //        ProcessingState.ParallelProcessed : ProcessingState.ParallelAcknowledged;
                        workflowNode.state = ProcessingState.ParallelProcessed;
                    }
                    catch (Exception e0)
                    {
                        Report(ErrorMessage, "Failed to process parallel action for node "
                                + workflowNode.component.getGooseName() + " " + e0.getMessage());

                        Log.severe("Failed to process parallel action for node " + workflowNode.component.getComponentID()
                                    + " " + e0.getMessage());
                        workflowNode.state = ProcessingState.Error;
                    }
                }
                else if (workflowNode.component.getCommandUri().toLowerCase().endsWith(".pl"))
                {
                    // TODO: this is hard coded now for KBase, we need to remove this once the Kgoose is done
                    workflowNode.state = ProcessingState.Finished;
                }
            }
        }

        /**
         * Check if a goose is in a particular state
         * @param c
         * @return
         */
        private Boolean checkState(WorkflowNode c, ProcessingState target)
        {
            if (c != null && c.component != null && c.component.getGooseName() != null)
            {
                for (int i = 0; i < processingQueue.size(); i++)
                {
                    WorkflowNode n = processingQueue.get(i);
                    if (n.state == target &&  n.component.getGooseName().equalsIgnoreCase(c.component.getGooseName()))
                    {
                       return true;
                    }

                }
            }
            return false;
        }

        private Boolean nodeProcessed(WorkflowComponent c)
        {
            for (int i = 0; i < this.processingQueue.size(); i++)
            {
                if (c.getComponentID().equals(this.processingQueue.get(i).component.getComponentID()))
                {
                    Log.info("Component " + c.getComponentID() + " " + c.getGooseName() + " already processed.");
                    return true;
                }
            }
            return false;
        }

        /**
         * Process a workflow. It's a state machine. Basically, for each component,
         * we first start its corresponding goose
         * and then process its parallel and sequential children in turn
         * @param c A workflowNode that needs to be processed according to its state
         */
        private void ProcessWorkflowNode(WorkflowNode c)
        {
            Goose3 sourceGoose = c.goose;
            WorkflowComponent source = c.component;
            if (c.state == ProcessingState.Initial) // && !checkState(c, ProcessingState.Pending))
            {
                StartGooseThread startGooseThread = new StartGooseThread(c, this.myWorkflow);
                c.state = ProcessingState.Pending;
                startGooseThread.start();
            }
            else if (c.state == ProcessingState.ParallelProcessed)
            {
                //Log.info("Handling workflow node " + source.getComponentID() + " state: ParallelProcessed");
                if (this.acknowledgedParallelNodes.containsKey(source.getComponentID()))
                {
                    // Add all the parallel components to processingQueue
                    Log.info("Found acknowledgement for " + source.getComponentID() + " " + source.getGooseName());

                    ArrayList<WorkflowComponent> parallelcomponents = workflowMap.get(source.getComponentID()).get(0);

                    for (int i = 0; i < parallelcomponents.size(); i++)
                    {
                        WorkflowComponent pc = parallelcomponents.get(i);
                        GaggleData dataForChild = this.stagingParallelData.get(pc.getComponentID());
                        if (dataForChild != null)
                        {
                            if (i == 0)
                            {
                                // Some nodes might generate output. We still need to handle
                                // the acknowledgement
                                if (dataForChild instanceof Namelist)
                                {
                                    Namelist nl = (Namelist)dataForChild;
                                    if (nl.getSpecies().equals("Workspace"))
                                    {
                                        // We need to pass the information to the workspace
                                        String outputfile = nl.getName();
                                        String server = System.getProperty("jnlp.server");
                                        String[] serverstrings = server.split(":");
                                        String host = serverstrings[0];
                                        if (serverstrings[0].equalsIgnoreCase("http"))
                                            host = serverstrings[1].substring(2); // skip the "//"
                                        Log.info("Host: " + host);
                                        String id = UUID.randomUUID().toString();
                                        String retrieveurl = "http://" + host + ":8082" + "/" + id;
                                        Log.info("Passing output file " + outputfile + "as " + retrieveurl + " to Workspace");
                                        bossImpl.sendInfoToHttpGoose(id, outputfile);
                                        Report(WorkflowInformation, ("WorkspaceData:::" + retrieveurl));

                                        /*Goose firegoose = bossImpl.getGoose("Firegoose");
                                        if (firegoose != null)
                                        {
                                            Log.info("Pass data to Workspace");
                                            try {
                                                firegoose.handleNameList("Boss", nl);
                                            }
                                            catch (Exception e)
                                            {
                                                Log.severe("Failed to pass data to Workspace " + e.getMessage());
                                                e.printStackTrace();
                                            }
                                        } */
                                    }
                                }
                            }
                            else {
                                WorkflowNode pn = new WorkflowNode(pc);
                                // If this component's order is less than the current workflow order,
                                // we should add it to the process queue
                                Log.info("Adding ack data " + dataForChild.getName() + " for node " + pc.getComponentID());
                                pc.addParam(WorkflowComponent.ParamNames.Data.getValue(), dataForChild);
                                this.processingQueue.add(pn);
                            }
                        }
                    }
                    // remove acknowledge data
                    this.acknowledgedParallelNodes.remove(source.getComponentID());
                    c.state = ProcessingState.ParallelAcknowledged;
                }
            }
            else if (c.state == ProcessingState.ParallelAcknowledged)
            {
                Log.info("Handling workflow node " + source.getComponentID() + " state: ParallelAcknowledged");
                // Now we process sequential nodes
                Log.info("Pass data to source for sequential children of " + source.getComponentID());

                ArrayList<WorkflowComponent> sequentialcomponents = workflowMap.get(source.getComponentID()).get(1);
                if (sequentialcomponents != null && sequentialcomponents.size() > 0)
                {
                    try {
                        WorkflowComponent[] targets = null;
                        targets = new WorkflowComponent[1];
                        targets[1] = sequentialcomponents.get(0);
                        WorkflowAction action = new WorkflowAction(
                                this.workflowID,
                                sessionID.toString(),
                                targets[1].getComponentID(),
                                WorkflowAction.ActionType.Request,
                                source,
                                targets,
                                WorkflowAction.Options.Sequential.getValue(),
                                //WorkflowAction.DataType.WorkflowData,
                                null
                        );
                        sourceGoose.handleWorkflowAction(action);
                        c.state = ProcessingState.SequentialProcessed;
                    }
                    catch (Exception e1)
                    {
                        Report(ErrorMessage, "Failed to process sequential action for node "
                                + c.component.getGooseName() + " " + e1.getMessage());
                        c.state = ProcessingState.Error;
                    }
                }
                else {
                    Log.info("No sequential child for " + source.getComponentID());
                    c.state = ProcessingState.Finished;
                }
            }
            else if (c.state == ProcessingState.SequentialProcessed)
            {
                Log.info("Handling workflow node " + source.getComponentID() + " state: SequentialProcessed");
                Log.info("Currently acknowledged: " + c.acknowledgedSequentials);
                ArrayList<WorkflowComponent> sequentialcomponents = workflowMap.get(source.getComponentID()).get(1);
                if (this.acknowledgedSequentialNodes.containsKey(source.getComponentID())) //, sequentialcomponents.get(c.acknowledgedSequentials)))
                {
                    WorkflowComponent sc = sequentialcomponents.get(c.acknowledgedSequentials);
                    GaggleData dataForChild = this.stagingSequentialData.get(sc.getComponentID());
                    if (dataForChild != null)
                    {
                        Log.info("Adding ack data for sequential child " + dataForChild.getName());
                        sc.addParam(WorkflowComponent.ParamNames.Data.getValue(), dataForChild);
                    }
                    // Now we add the child sequential node to the processing queue
                    this.processingQueue.add(new WorkflowNode(sc));
                    c.acknowledgedSequentials++;

                    if (c.acknowledgedSequentials >= sequentialcomponents.size())
                    {
                        // all sequential nodes are processed
                        c.state = ProcessingState.SequentialAcknowledged;
                        this.acknowledgedSequentialNodes.remove(source.getComponentID());
                    }
                    else
                    {
                        // Start an action for the next component
                        try {
                            WorkflowComponent[] targets = null;
                            targets = new WorkflowComponent[1];
                            targets[0] = sequentialcomponents.get(c.acknowledgedSequentials);
                            Log.info("Prepare for sequential node " + targets[0].getComponentID());
                            WorkflowAction action = new WorkflowAction(
                                    this.workflowID,
                                    sessionID.toString(),
                                    targets[0].getComponentID(),
                                    WorkflowAction.ActionType.Request,
                                    source,
                                    targets,
                                    //WorkflowAction.DataType.WorkflowData,
                                    WorkflowAction.Options.Sequential.getValue(),
                                    null
                            );

                            // Remove the node from the acknowledgement map because we are
                            // waiting for the ack for the current target
                            this.acknowledgedSequentialNodes.remove(source.getComponentID());

                            sourceGoose.handleWorkflowAction(action);
                        }
                        catch (Exception e1)
                        {
                            Report(ErrorMessage, "Failed to process sequential action for node "
                                    + source.getGooseName() + " " + e1.getMessage());
                            c.state = ProcessingState.Error;
                        }
                    }
                }
            }
            else if (c.state == ProcessingState.SequentialAcknowledged)
            {
                Log.info("Handling workflow node " + source.getComponentID() + " state: SequentialAcknowledged");

                // All parallel and sequential nodes are processed, we can remove c from processingQueue
                c.state = ProcessingState.Finished;
            }
            else if (c.state == ProcessingState.Error)
            {
                if (c.errorRetries <= MAX_ERROR_RETRIES)
                {
                    // We failed to connect to the goose, remove the goose from Boss and try again
                    try {
                        Thread.sleep(5000);
                        Log.warning("Trying to unregister the goose...");
                        bossImpl.unregister(c.gooseName);
                        c.state = ProcessingState.Initial;
                    }
                    catch (Exception e)
                    {
                        Log.info("RMI failed to unregister goose" + e.getMessage());
                        c.state = ProcessingState.Initial;
                    }
                }
                else
                {
                    Log.severe("All retries failed. Terminate the processing of node " + c.component.getGooseName());
                    c.state = ProcessingState.Finished;
                }
            }
        }


        private String createJnlpForData(String jnlpFileName, String datauri)
        {
            String replacement = (datauri == null || datauri.length() == 0) ? JSONConstants.WORKFLOW_EMPTY_ARGUMENT : datauri;
            try {
                String oldjnlpfilename = myTempFolder.getName() + File.separator + jnlpFileName;
                String newjnlpfilename = myTempFolder.getName() + File.separator + UUID.randomUUID().toString() + "_" + jnlpFileName;
                Log.info("New output jnlp file: " + newjnlpfilename);
                FileInputStream in = null;
                FileOutputStream fout = null;

                in = new FileInputStream(oldjnlpfilename);
                fout = new FileOutputStream(newjnlpfilename);
                String filecontent = "";
                byte data[] = new byte[1024];
                int count;
                while ((count = in.read(data, 0, 1024)) != -1)
                {
                    filecontent += new String(data);
                }
                Log.info("Old jnlp file content: " + filecontent);

                // Replace the %s in old jnlp with datauri
                String replacedcontent = filecontent.replace("%s", replacement);
                fout.write(replacedcontent.getBytes(), 0, replacedcontent.length());
                in.close();
                fout.close();
                return newjnlpfilename;
            }
            catch (Exception e)
            {
                Log.severe("Failed to write to new jnlp file: " + e.getMessage());
            }
            return null;
        }


    }


}
