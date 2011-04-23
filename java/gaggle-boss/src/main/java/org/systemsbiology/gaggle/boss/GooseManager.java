package org.systemsbiology.gaggle.boss;

import java.rmi.*;
import java.util.*;
import java.util.logging.*;

import org.systemsbiology.gaggle.core.*;

/**
 * A class for Goose management.
 */
public class GooseManager {
    private static Logger Log = Logger.getLogger("GooseManager");
    private Map<String, Goose> gooseMap = new HashMap<String, Goose>();
    private BossUI ui;

    public GooseManager(BossUI ui) {
        this.ui = ui;
    }

    public Goose getGoose(String name) { return gooseMap.get(name); }
    public String[] getGooseNames() {
        return gooseMap.keySet().toArray(new String[0]);
    }

    public String register(Goose goose) throws RemoteException {
        String uniqueName = uniqueNameBasedOn(goose.getName());
        Log.info("register(), uniqueName: " + uniqueName);
        goose.setName(uniqueName);
        addNewGoose(uniqueName, goose);
        unregisterIdleGeeseAndUpdate();
        return uniqueName;
    }

    private void addNewGoose(String name, Goose goose) {
        gooseMap.put(name, goose);
        ui.gooseAdded(name);
    }

    private void removeGoose(String gooseName) {
        if (gooseMap.containsKey(gooseName)) {
            gooseMap.remove(gooseName);
        }
    }

    public void unregister(String gooseName) {
        unregister(gooseName, true);
    }

    private void unregister(String gooseName, boolean doUpdate) {
        Log.info("boss: received unregister request for " + gooseName);
        removeGoose(gooseName);
        ui.gooseUnregistered(gooseName);
        if (doUpdate) unregisterIdleGeeseAndUpdate();
    }

    /**
     * Renames the goose, bypassing user insterface considerations.
     */
    public String renameGooseDirectly(String oldName,
                                      String proposedName)
        throws RemoteException {
        String uniqueName = uniqueNameBasedOn(proposedName);

        if (gooseMap.containsKey(oldName)) {
            Goose goose = gooseMap.get(oldName);
            gooseMap.remove(oldName);
            gooseMap.put(uniqueName, goose);
            goose.setName(uniqueName);
            unregisterIdleGeeseAndUpdate();
            return uniqueName;
        }
        return null;
    }

    private String uniqueNameBasedOn(String name) {
        return NameUniquifier.makeUnique(name,
                                         gooseMap.keySet().toArray(new String[0]));
    }

    /**
     * Check to see if we can communicate with all currently registered geese
     * and unregister any that do not respond, then update all geese with the
     * newly derived list of active geese. This is currently triggered by
     * the refresh button, and also by any goose registering or unregistering
     * or being renamed.
     */
    public void unregisterIdleGeeseAndUpdate() {
        for (String gooseName : disconnectedGooseNames()) {
            unregister(gooseName, false);
        }
        updateGeese();
    }

    private List<String> disconnectedGooseNames() {
        List<String> result = new ArrayList<String>();
        for (String gooseName : gooseMap.keySet()) {
            try {
                gooseMap.get(gooseName).getName();
            } catch (RemoteException e) {
                Log.info("Removing idle goose '" + gooseName + "'");
                result.add(gooseName);
            }
        }
        return result;
    }

    private void updateGeese() {
        String[] keys = gooseMap.keySet().toArray(new String[0]);
        Arrays.sort(keys); // why does this need to be sorted?
        for (String gooseName : keys) {
            Goose goose = gooseMap.get(gooseName);
            try {
                goose.update(keys);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public String renameGoose(String oldName, String proposedName) {
        try {
            Log.info("renameGoose()");
            String uniqueName = renameGooseDirectly(oldName, proposedName);
            ui.gooseRenamed(oldName, uniqueName);
            Log.info("goose renamed");
            return uniqueName;
        } catch (RemoteException ex) {
            String msg = "Failed to contact goose to rename: " + oldName + " -> " +
                proposedName;
            ui.displayErrorMessage(msg);
        }
        return null;
    }
}
