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
     * @param goose: A goose that communicates workflow related info with the boss.
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
