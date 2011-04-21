package org.systemsbiology.gaggle.boss;

import java.util.*;

import java.rmi.server.*;
import java.rmi.registry.*;
import java.rmi.Naming;
import java.rmi.RemoteException;

import org.systemsbiology.gaggle.core.*;
import org.systemsbiology.gaggle.geese.DeafGoose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.util.*;

public class BossImpl extends UnicastRemoteObject implements Boss2 {
    public static final String SERVICE_NAME = "gaggle";
    private Map<String, Goose> gooseMap = new HashMap<String, Goose>();
    private Map gooseListeningMap = new HashMap();

    private List<DeafGoose> deafGeese = new ArrayList<DeafGoose>();
    private NewNameHelper nameHelper;
    private BossUI ui;

    public BossImpl(BossUI ui, String nameHelperURI)
        throws Exception {
        this.ui = ui;
        if (nameHelperURI != null && nameHelperURI.length() > 0) {
            nameHelper = new NewNameHelper(nameHelperURI);
        }
    }
    public NewNameHelper getNameHelper() { return nameHelper; }

    public void bind() throws Exception {
        LocateRegistry.createRegistry(1099);
        Naming.rebind(SERVICE_NAME, this);
    }
    public void unbind() throws Exception {
        Naming.unbind(SERVICE_NAME);
    }

    public Goose getGoose(String name) {
        return gooseMap.get(name);
    }
    public void addNewGoose(String name, Goose goose) {
        gooseMap.put(name, goose);
        ui.gooseAdded(name);
    }
    public String[] getGooseNames() {
        return gooseMap.keySet().toArray(new String[0]);
    }

    public Map<String, Goose> getGooseMap() { return gooseMap; }
    
    /**
     * Renames the goose, bypassing user insterface considerations.
     */
    public String renameGooseDirectly(String oldName,
                                      String proposedName)
        throws RemoteException {
        String uniqueName =
            NameUniquifier.makeUnique(proposedName,
                                      gooseMap.keySet().toArray(new String[0]));

        if (gooseMap.containsKey(oldName)) {
            Goose goose = (Goose) gooseMap.get(oldName);
            gooseMap.remove(oldName);
            gooseMap.put(uniqueName, goose);
            goose.setName(uniqueName);
            unregisterIdleGeeseAndUpdate();
            return uniqueName;
        }
        return null;
    }

    public String renameGoose(String oldName, String proposedName) {
        String uniqueName = null;
        try {
           uniqueName = renameGooseDirectly(oldName, proposedName);
        } catch (RemoteException ex) {
            String msg = "Failed to contact goose to rename: " + oldName + " -> " +
                proposedName;
            ui.displayErrorMessage(msg);
            return null;
        }
        ui.gooseRenamed(oldName, uniqueName);
        return uniqueName;
    }

    /**
     * Check to see if we can communicate with all currently registered geese
     * and unregister any that do not respond, then update all geese with the
     * newly derived list of active geese. This is currently triggered by
     * the refresh button, and also by any goose registering or unregistering
     * or being renamed.
     */
    protected void unregisterIdleGeeseAndUpdate() {
        List<String> idleGeeseNames = new ArrayList<String>();
        for (String gooseName : gooseMap.keySet()) {
            try {
                gooseMap.get(gooseName).getName();
            } catch (RemoteException e) {
                System.out.println("Removing idle goose '" + gooseName + "'");
                idleGeeseNames.add(gooseName);
            }
        }
        for (String idleGooseName : idleGeeseNames) {
            unregister(idleGooseName, false);
        }
        updateGeese();
    }

    public String register(Goose goose) throws RemoteException {
        String uniqueName = NameUniquifier.makeUnique(goose.getName(),
                gooseMap.keySet().toArray(new String[0]));
        goose.setName(uniqueName);
        addNewGoose(uniqueName, goose);
        ui.refresh();
        unregisterIdleGeeseAndUpdate();
        return uniqueName;
    }

    public String register(JSONGoose goose) {
        return "TODO";
    }

    private void updateGeese() {
        String[] keys = gooseMap.keySet().toArray(new String[0]);
        Arrays.sort(keys); // why does this need to be sorted?
        for (String gooseName : keys) {
            Goose goose = (Goose) gooseMap.get(gooseName);
            try {
                goose.update(keys);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public String register(DeafGoose deafGoose) throws RemoteException {
        deafGeese.add(deafGoose);
        return "";
    }

    /**
     * Unregisters a goose
     * @param gooseName the name of the goose to unregister
     */
    public void unregister(String gooseName) {
        unregister(gooseName, true);
    }

    public void unregister(String gooseName, boolean doUpdate) {
        System.out.println("boss: received unregister request for " + gooseName);
        try {
            if (gooseMap.containsKey(gooseName)) {
                gooseMap.remove(gooseName);
            }
            if (gooseListeningMap.containsKey(gooseName))
                gooseListeningMap.remove(gooseName);
            ui.gooseUnregistered(gooseName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (doUpdate) unregisterIdleGeeseAndUpdate();
    }

    public Goose[] getGeese() {
        return gooseMap.values().toArray(new Goose[0]);
    }

    public void broadcastNamelist(String sourceGoose, String targetGoose,
                                  Namelist nameList) {
        long startTime = System.currentTimeMillis();
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
                System.err.println("error in select request to " + gooseName + ": " +
                        ex0.getMessage());
                ex0.printStackTrace();
            }
        }
        ui.refresh(true);
        long duration = System.currentTimeMillis() - startTime;
    }

    public void broadcastMatrix(String sourceGoose, String targetGoose,
                                DataMatrix matrix) {
        long startTime = System.currentTimeMillis();
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
                System.err.println("error in handleMatrix request to " + gooseName + ": " +
                        ex0.getMessage());
                ex0.printStackTrace();
            }
        }
        ui.refresh(true);
        long duration = System.currentTimeMillis() - startTime;
    }

    public void broadcastTuple(String sourceGoose, String targetGoose,
                               GaggleTuple gaggleTuple) {
        long startTime = System.currentTimeMillis();
        ui.refresh();
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
                System.err.println("error in broadcastTuple to " + gooseName + ": " +
                        ex0.getMessage());
                ex0.printStackTrace();
            }
        }
    }

    public void broadcastCluster(String sourceGoose, String targetGoose,
                                 Cluster cluster) {
        long startTime = System.currentTimeMillis();
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
                System.err.println("error in broadcastCluster () to " + gooseName + ": " +
                        ex0.getMessage());
                ex0.printStackTrace();
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        ui.refresh();
        System.out.println("GuiBoss.broadcastCluster  " + cluster.getName() + ", " +
                "rows: " + cluster.getRowNames().length +
                "columns: " + cluster.getColumnNames().length +
                ": " + duration + " msecs");
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
        ui.refresh();
    }

    public void broadcastJson(String source, String target, String json) {
        throw new UnsupportedOperationException("TODO");
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
                System.err.println("error in hide request to " + targetGoose + ": " +
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
                System.err.println("error in show request to " + gooseName + ": " +
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
            System.err.println("error in terminate request to " + gooseName + ": " + ex1.getMessage());
        }
    }

}
