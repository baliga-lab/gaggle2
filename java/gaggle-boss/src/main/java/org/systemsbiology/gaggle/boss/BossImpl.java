package org.systemsbiology.gaggle.boss;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.*;

import java.rmi.server.*;
import java.rmi.registry.*;
import java.rmi.Naming;
import java.rmi.RemoteException;

import net.sf.json.JSONObject;
import org.systemsbiology.gaggle.core.*;
import org.systemsbiology.gaggle.geese.DeafGoose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.util.*;

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

    private static Logger Log = Logger.getLogger("Boss");

    public BossImpl(BossUI ui, String nameHelperURI)
        throws Exception {
        this.ui = ui;
        this.gooseManager = new GooseManager(ui);
        this.workflowManager = new WorkflowManager(this, this.gooseManager);

        if (nameHelperURI != null && nameHelperURI.length() > 0) {
            nameHelper = new NewNameHelper(nameHelperURI);
        }
    }
    public NewNameHelper getNameHelper() { return nameHelper; }

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
    public void submitWorkflow(Goose3 proxyGoose, String jsonWorkflow)
    {
        if (jsonWorkflow != null && jsonWorkflow.length() > 0)
        {
            Log.info("JSON workflow string: " + jsonWorkflow);
            JSONReader jsonReader = new JSONReader();
            Workflow w = (Workflow)jsonReader.createFromJSONString(jsonWorkflow);

            // Now we hand over to the manager, which will spawn a thread to process the workflow
            workflowManager.SubmitWorkflow(proxyGoose, w);
        }
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
        if (isRecording)
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
