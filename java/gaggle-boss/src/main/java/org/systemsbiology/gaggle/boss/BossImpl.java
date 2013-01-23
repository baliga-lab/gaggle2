package org.systemsbiology.gaggle.boss;

import java.io.*;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.URL;
import java.util.*;

import java.rmi.server.*;
import java.rmi.registry.*;
import java.rmi.Naming;
import java.rmi.RemoteException;

import net.sf.json.JSONObject;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.ptql.ProcessFinder;
import org.systemsbiology.gaggle.core.*;
import org.systemsbiology.gaggle.geese.DeafGoose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.util.*;
import org.hyperic.sigar.ProcExe;

import java.util.logging.*;

public class BossImpl extends UnicastRemoteObject implements Boss3 {
    public static final String SERVICE_NAME = "gaggle";
    private NewNameHelper nameHelper;
    private BossUI ui;
    private GooseManager gooseManager;
    private boolean isRecording = false;
    private HashMap<String, String> savedNodes = new HashMap<String, String>();
    private HashMap<String, HashMap<String, String>> dictNodes = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, HashMap<String, String>> dictEdges = new HashMap<String, HashMap<String, String>>();
    private String startNode = null;
    private int edgeCount = 0;
    private int nodeCount = 0;
    private WorkflowManager workflowManager;
    private HashMap<String, String> applicationInfo = new HashMap<String, String>();
    private Sigar sigar;

    private static Logger Log = Logger.getLogger("Boss");

    public BossImpl(BossUI ui, String nameHelperURI)
        throws Exception {
        this.ui = ui;
        this.gooseManager = new GooseManager(ui);
        this.workflowManager = new WorkflowManager(this, this.gooseManager);

        if (nameHelperURI != null && nameHelperURI.length() > 0) {
            nameHelper = new NewNameHelper(nameHelperURI);
        }

        String os = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");
        String javalibpath = System.getProperty("java.library.path");
        Log.info("SSIIGGAARRRRRRR ===== loading " + os + " " + arch + " " + javalibpath + " SSIIGGAARRRRR...");

        boolean libloaded = false;
        //System.setProperty( "java.library.path", "." );
        if(os.startsWith("Windows"))
        {
            Log.info("Windows OS detected...");
            if (arch.startsWith("amd64"))
            {
                Log.info("Loading windows sigar-amd64-winnt.dll");
                libloaded = loadSigarLibrary("sigar-amd64-winnt");
            }
            else
            {
                Log.info("Loading windows sigar-x86-winnt.dll");
                libloaded = loadSigarLibrary("sigar-x86-winnt");
            }
        }
        if (os.startsWith("Mac"))
        {
            Log.info("Mac OS detected");
            if (arch.equals("x86_64"))
            {
                Log.info("Loading Mac libsigar-universal64-macosx.dylib");
                libloaded = loadSigarLibrary("libsigar-universal64-macosx.dylib");
            }
            else if (arch.startsWith("i386") || arch.startsWith("x86"))
            {
                Log.info("Loading Mac libsigar-universal-macosx.dylib");
                libloaded = loadSigarLibrary("libsigar-universal-macosx.dylib");
            }
        }
        else if (os.startsWith("Linux"))
        {
            Log.info("Linux OS detected");
            if (arch.startsWith("x86"))
            {
                Log.info("Loading linux libsigar-x86-linux.so");
                libloaded = loadSigarLibrary("libsigar-x86-linux");
            }
            else if (arch.startsWith("amd64"))
            {
                Log.info("Loading linux libsigar-x64-linux.so");
                libloaded = loadSigarLibrary("libsigar-x64-linux");
            }
        }
        if (libloaded) {
            try
            {
                Log.info("Now loading SIGAR...");
                System.setProperty("org.hyperic.sigar.path", "-");
                //System.setProperty( "java.library.path", "." );
                Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
                fieldSysPath.setAccessible( true );
                fieldSysPath.set( null, null );
                sigar = new Sigar();
            }
            catch (Exception e)
            {
                Log.severe("Failed to load SIGAR class: " + e.getMessage());
            }
        }
    }

    private void loadJarLib(InputStream in, String libName) throws IOException {
        //InputStream in = MyClass.class.getResourceAsStream(name);
        Log.info("Load " + libName + " from inputstream");
        if (in != null && libName != null)
        {
            byte[] buffer = new byte[1024];
            int read = -1;
            File temp = File.createTempFile(libName, "");
            Log.info("Created temp file: " + temp.getAbsolutePath());

            FileOutputStream fos = new FileOutputStream(temp);
            while((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.close();
            in.close();

            Log.info("Loading library...");
            System.load(temp.getAbsolutePath());
        }
    }

    private boolean loadSigarLibrary(String libName)
    {
        if (libName != null)
        {
            try
            {
                System.loadLibrary(libName);
                return true;
            }
            catch (UnsatisfiedLinkError ule)
            {
                Log.severe("Cannot find library: " + ule.getMessage());
                try
                {
                    Log.info("Trying to load " + libName + "using getContextClassLoader().loadClass");
                    Thread.currentThread().getContextClassLoader().loadClass(libName);
                    return true;
                }
                catch (Exception e0)
                {
                    Log.severe("Failed to load " + libName + " using getContextClassLoader().loadClass " + e0.getMessage());
                }

                try
                {
                    Log.info("Trying to load " + libName + "using getClass().getClassLoader.getResourceAsStream");
                    InputStream ins0 = this.getClass().getClassLoader().getResourceAsStream(libName);
                    if (ins0 != null)
                    {
                        Log.info("Successfully loaded " + libName);
                        loadJarLib(ins0, libName);
                        return true;
                    }
                    else
                    {
                        Log.info("Trying to load " + libName + "using getClass().getResourceAsStream");
                        InputStream ins1 = this.getClass().getResourceAsStream(libName);
                        if (ins1 != null)
                        {
                            Log.info("Successfully loaded " + libName);

                            return true;
                        }
                        else
                            Log.warning("Failed to load " + libName);
                    }
                }
                catch (Exception ex)
                {
                    Log.severe("Failed to load " + libName + " using getResourceAsStream " + ex.getMessage());
                }
            }
            catch (Exception e)
            {
                Log.severe("Failed to load " + e.getMessage());
            }
        }
        return false;
    }

    public NewNameHelper getNameHelper() { return nameHelper; }

    public String getAppInfo(String appName)
    {
        for (String key : applicationInfo.keySet())
        {
            if (key.toLowerCase().equals(appName.toLowerCase()))
                return applicationInfo.get(key);
        }

        for (String key : applicationInfo.keySet())
        {
            if (key.toLowerCase().indexOf(appName.toLowerCase()) >= 0)
                return applicationInfo.get(key);
        }
        return null;
    }

    public void bind() throws Exception {
        //if (System.getSecurityManager() == null) {
        //    System.setSecurityManager(new SecurityManager());
        //}

        LocateRegistry.createRegistry(1099);
        Naming.rebind(SERVICE_NAME, this);
        Log.info("Boss Service bound");
    }
    public void unbind() throws Exception {
        Naming.unbind(SERVICE_NAME);
        Log.info("Boss Service unboudnd");
    }

    // ***** Goose Management *****
    public Goose getGoose(String name) { return gooseManager.getGoose(name); }
    public String[] getGooseNames() { return gooseManager.getGooseNames(); }
    public String[] getListeningGooseNames() { return ui.getListeningGeese(); }
    public String renameGoose(String oldName, String proposedName) {
        return gooseManager.renameGoose(oldName, proposedName);
    }
    public String renameGooseDirectly(String oldName,
                                      String proposedName) throws RemoteException {
        return gooseManager.renameGooseDirectly(oldName, proposedName);
    }
    public String register(Goose goose) throws RemoteException {
        Log.info("BossImpl.register(Goose)");
        return gooseManager.register(goose);
    }
    public String register(JSONGoose goose) throws RemoteException {
        Log.info("BossImpl.register(JSONGoose)");
        return gooseManager.register(goose);
    }
    public String register(DeafGoose deafGoose) { return ""; }
    public void unregister(String gooseName) { gooseManager.unregister(gooseName); }
    public void unregisterIdleGeeseAndUpdate() {
        gooseManager.unregisterIdleGeeseAndUpdate();
    }

    // ***** Broadcasting *****
    public void broadcastNamelist(String sourceGoose, String targetGoose,
                                  Namelist nameList) {
        ui.broadcastToPlugins(nameList.getNames());

        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
            targetGoose.equalsIgnoreCase("all")) {
            gooseNames = ui.getListeningGeese();
        } else {
            gooseNames = new String[]{targetGoose};
        }

        for (int i = 0; i < gooseNames.length; i++) {
            String gooseName = gooseNames[i];
            if (gooseName.equals(sourceGoose)) continue;
            Goose goose = getGoose(gooseName);
            if (goose == null) continue;

            try {
                if (isRecording)
                    recordAction(sourceGoose, gooseName, nameList, -1, null, null, null);
                goose.handleNameList(sourceGoose, nameList);
            } catch (Exception ex0) {
                Log.severe("error in select request to " + gooseName + ": " +
                           ex0.getMessage());
                ex0.printStackTrace();
            }
        }
    }

    public void broadcastMatrix(String sourceGoose, String targetGoose,
                                DataMatrix matrix) {
        ui.broadcastToPlugins(matrix.getRowTitles());

        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
            targetGoose.equalsIgnoreCase("all")) {
            gooseNames = ui.getListeningGeese();
        } else {
            gooseNames = new String[]{targetGoose};
        }

        for (int i = 0; i < gooseNames.length; i++) {
            String gooseName = gooseNames[i];
            if (gooseName.equals(sourceGoose)) continue;
            Goose goose = getGoose(gooseName);
            if (goose == null) continue;
            try {
                if (isRecording)
                    recordAction(sourceGoose, gooseName, matrix, -1, null, null, null);
                goose.handleMatrix(sourceGoose, matrix);
            } catch (Exception ex0) {
                Log.severe("error in handleMatrix request to " + gooseName + ": " +
                           ex0.getMessage());
                ex0.printStackTrace();
            }
        }
    }

    public void broadcastTuple(String sourceGoose, String targetGoose,
                               GaggleTuple gaggleTuple) {

        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
            targetGoose.equalsIgnoreCase("all")) {
            gooseNames = ui.getListeningGeese();
        } else {
            gooseNames = new String[]{targetGoose};
        }
        for (int i = 0; i < gooseNames.length; i++) {
            String gooseName = gooseNames[i];
            if (gooseName.equals(sourceGoose)) continue;
            Goose goose = getGoose(gooseName);
            if (goose == null) continue;
            try {
                System.out.println("broadcastTuple to " + gooseName);
                if (isRecording)
                    recordAction(sourceGoose, gooseName, gaggleTuple, -1, null, null, null);
                goose.handleTuple(sourceGoose, gaggleTuple);
            } catch (Exception ex0) {
                Log.severe("error in broadcastTuple to " + gooseName + ": " +
                           ex0.getMessage());
                ex0.printStackTrace();
            }
        }
    }

    public void broadcastCluster(String sourceGoose, String targetGoose,
                                 Cluster cluster) {
        ui.broadcastToPlugins(cluster.getRowNames());

        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
            targetGoose.equalsIgnoreCase("all")) {
            gooseNames = ui.getListeningGeese();
        } else {
            gooseNames = new String[]{targetGoose};
        }
        for (int i = 0; i < gooseNames.length; i++) {
            String gooseName = gooseNames[i];
            if (gooseName.equals(sourceGoose)) continue;
            if (!ui.isListening(gooseName)) continue;
            Goose goose = getGoose(gooseName);
            if (goose == null) continue;
            try {
                if (isRecording)
                {
                    // Record the action
                    recordAction(sourceGoose, gooseName, cluster, -1, null, null, null);
                }
                goose.handleCluster(sourceGoose, cluster);
            } catch (Exception ex0) {
                Log.severe("error in broadcastCluster () to " + gooseName + ": " +
                           ex0.getMessage());
                ex0.printStackTrace();
            }
        }
    }

    public void broadcastNetwork(String sourceGoose, String targetGoose,
                                 Network network) {
        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
            targetGoose.equalsIgnoreCase("all")) {
            gooseNames = ui.getListeningGeese();
        } else {
            gooseNames = new String[]{targetGoose};
        }

        for (int i = 0; i < gooseNames.length; i++) {
            String gooseName = gooseNames[i];
            if (gooseName.equals(sourceGoose)) continue;
            Goose goose = getGoose(gooseName);
            if (goose == null) continue;
            try {
                if (isRecording)
                {
                    // Record the action
                    recordAction(sourceGoose, gooseName, network, -1, null, null, null);
                }
                goose.handleNetwork(sourceGoose, network);
            } catch (Exception ex0) {
                System.err.println("error in broadcastNetwork () to " + gooseName + ": " +
                                   ex0.getMessage());
                ex0.printStackTrace();
            }
        }
    }

    /**
     * Accepts a workflow (usually from a Proxy Applet, but we do not assume so)
     * @param proxyGoose: A goose that communicates workflow related info with the boss.
     *               Typically it is the ProxyApplet goose.
     * @param jsonWorkflow: a workflow in JSON format submitted by goose
     */
    public String submitWorkflow(Goose3 proxyGoose, String jsonWorkflow)
    {
        if (jsonWorkflow != null && jsonWorkflow.length() > 0)
        {
            Log.info("JSON workflow string: " + jsonWorkflow);
            JSONReader jsonReader = new JSONReader();
            Workflow w = (Workflow)jsonReader.createFromJSONString(jsonWorkflow);

            // Now we hand over to the manager, which will spawn a thread to process the workflow
            workflowManager.SubmitWorkflow(proxyGoose, w);

            HashMap<String, String> nodeInfoMap = w.getNodeInfoMap();
            for (String key : nodeInfoMap.keySet())
            {
                // Key is the ID of the component
                String goosename = nodeInfoMap.get(key);
                String exepath = this.getAppInfo(key);
                Log.info("Path for " + goosename + ": " + exepath);
                if (exepath != null && !exepath.isEmpty())
                {
                    nodeInfoMap.put(key, exepath);
                }
                else
                    nodeInfoMap.remove(key);
            }
            JSONObject json = new JSONObject();
            json.putAll(nodeInfoMap);
            Log.info("Workflow goose json string: " + json.toString());
            return json.toString();
        }
        return null;
    }


    /**
     * Handles workflow action received from components
     * @param action A workflow action needs to be carried out.
     */
    public void handleWorkflowAction(WorkflowAction action)
    {
        System.out.println("Processing workflow action: " + action.getSource().getName());
        this.workflowManager.HandleWorkflowAction(action);
    }


    private String generateRecordedWorkflow()
    {
        JSONObject jsonObj = new JSONObject();
        HashMap<String, Object> finalObj = new HashMap<String, Object>();
        finalObj.put("nodes", this.dictNodes);
        finalObj.put("edges", this.dictEdges);
        finalObj.put("id", "");
        finalObj.put("name", "");
        finalObj.put("desc", "");
        finalObj.put("startNode", this.startNode);
        jsonObj.putAll(finalObj);
        Log.info("Generated workflow json string: " + jsonObj.toString());
        return jsonObj.toString();
    }

    // Some goose's name is appName + data name (e.g., cygoose), we extract the appName
    private String processGooseName(String gooseName)
    {
        if (gooseName != null)
        {
            String[] splitted = gooseName.split(";");
            if (splitted != null)
                return splitted[0];
        }
        return null;
    }

    /**
     * Start recording a workflow
     * @return
     */
    public UUID startRecordingWorkflow()
    {
        if (isRecording)
            return null;
        isRecording = true;

        // Create an array for recording the actions
        UUID rid = UUID.randomUUID();
        this.dictNodes.clear();
        this.dictEdges.clear();
        this.savedNodes.clear();
        this.edgeCount = 0;
        this.nodeCount = 0;
        // Inform all the goose
        return rid;
    }

    /**
     * Stop recording workflow
     * @return
     */
    public String terminateRecordingWorkflow(UUID rid)
    {
        isRecording = false;
        return generateRecordedWorkflow();
    }

    public String pauseRecordingWorkflow(UUID rid)
    {
        this.isRecording = false;
        return generateRecordedWorkflow();
    }

    public void resumeRecordingWorkflow(UUID rid)
    {
        this.isRecording = true;
    }

    /**
     * Record a broadcast action. The API can be used to update source goose and target goose parameters
     * as well as edge parameters
     * @param sourceGoose
     * @param targetGoose
     * @param data
     * @param edgeIndex: The index of the edge to be updated, -1 if it's a new edge
     * @param sourceParams
     * @param targetParams
     * @param edgeParams
     */
    public void recordAction(String sourceGoose, String targetGoose, Object data,
                             int edgeIndex,
                             HashMap<String, String> sourceParams,
                             HashMap<String, String> targetParams,
                             HashMap<String, String> edgeParams
                             )
    {
        Log.info("***** Recording action from " + sourceGoose + "********");
        if (sourceGoose != null && targetGoose == null && data != null)
        {
            // Application information report
            try
            {
                Log.info("Goose " + sourceGoose + " query: " + (String)data);
                ProcessFinder procFinder = new ProcessFinder(sigar);
                long[] pids = procFinder.find((String)data);
                if (pids != null && pids.length > 0)
                {
                    Log.info("Getting info for process " + pids[0]);
                    ProcExe procExe = new ProcExe();
                    procExe.gather(sigar, pids[0]);
                    String workdir = procExe.getCwd();
                    String exename = procExe.getName();
                    Log.info("Work dir: " + workdir + " Executable: " + exename);
                    this.applicationInfo.put(sourceGoose, exename);
                }
            }
            catch (Exception e0)
            {
                Log.severe("Failed to get path of the process for " + sourceGoose + " " + e0.getMessage());
            }

        }
        else if (isRecording)
        {
            Log.info("Recording source: " + sourceGoose + " target:" + targetGoose + " data: " + data.toString());
            String sourceGooseName = processGooseName(sourceGoose);
            String targetGooseName = processGooseName(targetGoose);

            // Add nodes to the nodes dict
            if (this.dictNodes.isEmpty())
            {
                // This is the starting node
                if (sourceGoose != null)
                this.startNode = sourceGooseName;
            }

            String sourceid = "";
            String targetid = "";
            if (sourceGooseName != null)
            {
                HashMap<String, String> node = null;
                if (!this.savedNodes.containsKey(sourceGooseName))
                {
                    node = new HashMap<String, String>();
                    node.put(JSONConstants.WORKFLOW_ID, String.valueOf(nodeCount));
                    node.put(JSONConstants.WORKFLOW_NAME, sourceGooseName);
                    this.dictNodes.put(String.valueOf(this.nodeCount), node);
                    this.savedNodes.put(sourceGooseName, String.valueOf(nodeCount));
                    sourceid = String.valueOf(nodeCount);
                    nodeCount++;
                }
                else {
                    sourceid = savedNodes.get(sourceGooseName);
                    node = this.dictNodes.get(sourceid);
                }
                if (sourceParams != null && node != null)
                {
                    for (String key: sourceParams.keySet())
                    {
                        String value = sourceParams.get(key);
                        if (value != null && value.length() > 0)
                        {
                            String nodevalue = "";
                            if (node.containsKey(key))
                                // If the parameter already exists, we concatenate it
                                // with the new value. This is especially important for
                                // some parameters such as subactions ( A node can accept multiple
                                // subactions). For example, users might want to pass the data to
                                // Firegoose and trigger multiple webhandlers. Concatenating
                                // subactions achieve this purpose.
                                nodevalue = (node.get(key) + ";");
                            nodevalue += value;
                            node.put(key, nodevalue);
                        }
                    }
                }
            }

            if (targetGoose != null)
            {
                HashMap<String, String> node = null;
                if (!this.savedNodes.containsKey(targetGooseName))
                {
                    node = new HashMap<String, String>();
                    node.put(JSONConstants.WORKFLOW_ID, String.valueOf(nodeCount));
                    node.put(JSONConstants.WORKFLOW_NAME, targetGooseName);
                    this.dictNodes.put(String.valueOf(this.nodeCount), node);
                    this.savedNodes.put(targetGooseName, String.valueOf(nodeCount));
                    targetid = String.valueOf(nodeCount);
                    nodeCount++;
                }
                else {
                    targetid = savedNodes.get(targetGooseName);
                    node = this.dictNodes.get(targetid);
                }
                if (targetParams != null && node != null)
                {
                    for (String key: targetParams.keySet())
                    {
                        node.put(key, targetParams.get(key));
                    }
                }
            }


            // Add to the edge dict
            if (sourceGoose != null && targetGoose != null)
            {
                Log.info("Source node id: " + sourceid + " target node id: " + targetid);
                HashMap<String, String> edge = null;
                if (edgeIndex >= 0)
                    edge = this.dictEdges.get(String.valueOf(edgeIndex));
                else
                    edge = new HashMap<String, String>();
                if (edge != null)
                {
                    edge.put(JSONConstants.WORKFLOW_EDGE_SOURCEID, sourceid);
                    edge.put(JSONConstants.WORKFLOW_EDGE_TARGETID, targetid);

                    String dataType = "Data";
                    if (data instanceof DataMatrix)
                        dataType = "Matrix";
                    else if (data instanceof GaggleTuple)
                        dataType = "Tuple";
                    else if (data instanceof Network)
                        dataType = "Network";
                    else if (data instanceof Cluster)
                        dataType = "Cluster";
                    edge.put(JSONConstants.WORKFLOW_EDGE_DATATYPE, dataType);
                    Log.info("Data type: " + dataType);
                    edge.put(JSONConstants.WORKFLOW_EDGE_PARALLELTYPE, "true");

                    if (edgeParams != null)
                    {
                        Log.info("Adding edge params...");
                        for (String key: edgeParams.keySet())
                        {
                            edge.put(key, edgeParams.get(key));
                        }
                    }
                    if (edgeIndex >= 0)
                    {
                        this.dictEdges.put(String.valueOf(edgeIndex), edge);
                    }
                    else
                    {
                        this.dictEdges.put(String.valueOf(this.edgeCount), edge);
                        this.edgeCount++;
                    }
                }
            }
        }
    }


    public void hide(String targetGoose) {
        String[] gooseNames;
        if (targetGoose == null) {
            gooseNames = ui.getListeningGeese();
        } else if (targetGoose.equalsIgnoreCase("boss")) {
            ui.hide();
            return;
        } else gooseNames = new String[]{targetGoose};

        for (int i = 0; i < gooseNames.length; i++) {
            Goose goose = getGoose(gooseNames[i]);
            if (goose == null) continue;
            try {
                goose.doHide();
            } catch (Exception ex0) {
                Log.severe("error in hide request to " + targetGoose + ": " +
                                   ex0.getMessage());
                ex0.printStackTrace();
            }
        }
    }

    public void show(String gooseName) {
        if (gooseName.equalsIgnoreCase("boss")) ui.show();
        else {
            final Goose goose = getGoose(gooseName);
            if (goose == null) return;

            try {
                goose.doShow();
            } catch (Exception ex0) {
                Log.severe("error in show request to " + gooseName + ": " +
                           ex0.getMessage());
                ex0.printStackTrace();
            }
        }
    }

    public void terminate(String gooseName) {
        Goose goose = getGoose(gooseName);
        if (goose == null) return;
        try {
            goose.doExit();
        } catch (java.rmi.UnmarshalException ignore0) {
            ignore0.printStackTrace();
        } catch (Exception ex1) {
            Log.severe("error in terminate request to " + gooseName + ": " + ex1.getMessage());
        }
    }

    public void broadcastJSON(String source, String target, String json) {
        String[] gooseNames;
        if (target == null || target.equalsIgnoreCase("boss") ||
            target.equalsIgnoreCase("all")) {
            gooseNames = ui.getListeningGeese();
        } else {
            gooseNames = new String[]{target};
        }

        for (int i = 0; i < gooseNames.length; i++) {
            String gooseName = gooseNames[i];
            if (gooseName.equals(source)) continue;
            SuperGoose goose = gooseManager.getGoose(gooseName);
            if (goose == null) continue;
            try {
                goose.handleJSON(source, json);
            } catch (Exception ex0) {
                System.err.println("error in broadcastJSON() to " + gooseName + ": " +
                                   ex0.getMessage());
                ex0.printStackTrace();
            }
        }
    }

    public void broadcastTable(String source, String target, Table table) {
        throw new UnsupportedOperationException("TODO");
    }
}
