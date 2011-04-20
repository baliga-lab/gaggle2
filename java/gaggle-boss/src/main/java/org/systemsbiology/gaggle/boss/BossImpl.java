package org.systemsbiology.gaggle.boss;

import java.util.*;

import java.rmi.server.*;
import java.rmi.registry.*;
import java.rmi.Naming;
import java.rmi.RemoteException;

import org.systemsbiology.gaggle.core.*;
import org.systemsbiology.gaggle.geese.DeafGoose;
import org.systemsbiology.gaggle.core.datatypes.*;

public class BossImpl extends UnicastRemoteObject implements Boss2 {
    public static final String SERVICE_NAME = "gaggle";
    private Map<String, Goose> gooseMap = new HashMap<String, Goose>();
    private Map gooseListeningMap = new HashMap();

    private List<DeafGoose> deafGeese = new ArrayList<DeafGoose>();

    public BossImpl() { }
    public void bind() {
        LocateRegistry.createRegistry(1099);
        Naming.rebind(SERVICE_NAME, this);
    }
    public void unbind() {
        Naming.unbind(SERVICE_NAME);
    }

    public Goose getGoose(String name) {
        return gooseMap.get(name);
    }

    public String[] getGooseNames() {
        return gooseMap.keySet().toArray(new String[0]);
    }

    public Map<String, Goose> getGooseMap() { return gooseMap; }

    public String actuallyRenameGoose(String oldName,
                                      String proposedName) throws RemoteException {
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
           uniqueName = actuallyRenameGoose(oldName, proposedName);
        } catch (RemoteException ex) {
            String msg = "Failed to contact goose to rename: " + oldName + " -> " +
                proposedName;
            JOptionPane.showMessageDialog(frame, msg);
            return null;
        }

        String[] appNames = GuiBoss.this.gooseTableModel.getAppNames();
        for (int i = 0; i < appNames.length; i++) {
            if (appNames[i].equals(oldName)) {
                GuiBoss.this.gooseTableModel.setAppNameAtRow(uniqueName, i);
                GuiBoss.this.gooseTableModel.fireTableDataChanged();
            }
        }

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
        refresh();
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
            gooseTableModel.removeGoose(gooseName);
            setTableColumnWidths();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (doUpdate) unregisterIdleGeeseAndUpdate();
    }

    public Goose[] getGeese() {
        return gooseMap.values().toArray(new Goose[0]);
    }

    public void broadcastNamelist(String sourceGoose, String targetGoose,
                                  Namelist nameList) throws RemoteException {
        long startTime = System.currentTimeMillis();
        broadcastToPlugins(nameList.getNames());

        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
            targetGoose.equalsIgnoreCase("all")) {
            gooseNames = getListeningGeese();
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
                System.err.println("error in select request to " + goose.getName() + ": " +
                        ex0.getMessage());
                ex0.printStackTrace();
            }
        }
        refresh(true);
        long duration = System.currentTimeMillis() - startTime;
    }

    public void broadcastMatrix(String sourceGoose, String targetGoose,
                                DataMatrix matrix) throws RemoteException {
        long startTime = System.currentTimeMillis();
        broadcastToPlugins(matrix.getRowTitles());

        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
            targetGoose.equalsIgnoreCase("all")) {
            gooseNames = getListeningGeese();
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
        refresh(true);
        long duration = System.currentTimeMillis() - startTime;
    }

    public void broadcastTuple(String sourceGoose, String targetGoose, GaggleTuple gaggleTuple) {
        long startTime = System.currentTimeMillis();
        refresh();
        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
            targetGoose.equalsIgnoreCase("all")) {
            gooseNames = getListeningGeese();
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
        broadcastToPlugins(cluster.getRowNames());

        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
            targetGoose.equalsIgnoreCase("all")) {
            gooseNames = getListeningGeese();
        } else {
            gooseNames = new String[]{targetGoose};
        }
        for (int i = 0; i < gooseNames.length; i++) {
            String gooseName = gooseNames[i];
            if (gooseName.equals(sourceGoose)) continue;
            if (!isListening(gooseName)) continue;
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
        refresh();
        System.out.println("GuiBoss.broadcastCluster  " + cluster.getName() + ", " +
                "rows: " + cluster.getRowNames().length +
                "columns: " + cluster.getColumnNames().length +
                ": " + duration + " msecs");
    }

    public void broadcastNetwork(String sourceGoose, String targetGoose, Network network) {
        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
            targetGoose.equalsIgnoreCase("all")) {
            gooseNames = getListeningGeese();
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
        refresh();
    }

    public void hide(String targetGoose) {
        String[] gooseNames;
        if (targetGoose == null) {
            gooseNames = getListeningGeese();
        } else if (targetGoose.equalsIgnoreCase("boss")) {
            frame.setVisible(false);
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
