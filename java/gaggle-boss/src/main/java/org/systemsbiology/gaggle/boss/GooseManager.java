package org.systemsbiology.gaggle.boss;

import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.JSONGoose;

import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;

/**
 * A class for Goose management.
 */
public class GooseManager {
    private static Logger Log = Logger.getLogger("GooseManager");
    private Map<String, SuperGoose> gooseMap = new HashMap<String, SuperGoose>();
    private Map<String, String> gooseWorkflow
            = Collections.synchronizedMap(new HashMap<String, String>()); // This needs to be thread safe
    private BossUI ui;
    private Object gooseUpdateObj = new Object();

    public GooseManager(BossUI ui) {
        this.ui = ui;
    }

    public SuperGoose getGoose(String name) {
        Log.info("Searching goose " + name);
        return gooseMap.get(name);
    }

    public SuperGoose getGooseWith(String query)
    {
        Log.info("Search for goose with " + query);
        if (query != null && query.length() > 0)
        {
            for (String key : gooseMap.keySet())
            {
                if (key.contains(query))
                    return gooseMap.get(key);
            }
        }
        return null;
    }

    public String[] getGooseNames() {
        return gooseMap.keySet().toArray(new String[0]);
    }

    public String register(JSONGoose goose) throws RemoteException {
        return register(new JSONGooseAdapter(goose));
    }

    public String register(Goose goose) throws RemoteException {
        return register(new JavaGooseAdapter(goose));
    }

    private String register(SuperGoose goose) throws RemoteException {
        String uniqueName = uniqueNameBasedOn(goose.getName());
        Log.info("register(), uniqueName: " + uniqueName);
        goose.setName(uniqueName);
        addNewGoose(uniqueName, goose);
        unregisterIdleGeeseAndUpdate();
        return uniqueName;
    }

    private void addNewGoose(String name, SuperGoose goose) {
        gooseMap.put(name, goose);
        ui.gooseAdded(name);
    }

    private void removeGoose(String gooseName) {
        if (gooseMap.containsKey(gooseName)) {
            gooseMap.remove(gooseName);
        }

        if (gooseWorkflow.containsKey(gooseName))
            gooseWorkflow.remove(gooseName);
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
            SuperGoose goose = gooseMap.get(oldName);
            gooseMap.remove(oldName);
            gooseMap.put(uniqueName, goose);
            goose.setName(uniqueName);
            unregisterIdleGeeseAndUpdate();

            gooseWorkflow.remove(oldName);
            MarkIfGooseNotBusy(uniqueName);

            return uniqueName;
        }
        return null;
    }

    public boolean MarkIfGooseNotBusy(String gooseName)
    {
        if (!gooseWorkflow.containsKey(gooseName))
        {
            gooseWorkflow.put(gooseName, gooseName);
            return true;
        }
        return false;
    }

    public void MarkGooseAvailable(String gooseName)
    {
        if (gooseWorkflow.containsKey(gooseName))
        {
            gooseWorkflow.remove(gooseName);
        }
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
        synchronized (gooseUpdateObj)
        {
            for (String gooseName : disconnectedGooseNames()) {
                unregister(gooseName, false);
            }
            updateGeese();
        }
    }

    private List<String> disconnectedGooseNames() {
        List<String> result = new ArrayList<String>();
        Log.info("Get disconnectd goose names...");
        for (String gooseName : gooseMap.keySet()) {
            try {
                String name = gooseMap.get(gooseName).getName();
                Log.info("Obtained goose name: " + name);
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
