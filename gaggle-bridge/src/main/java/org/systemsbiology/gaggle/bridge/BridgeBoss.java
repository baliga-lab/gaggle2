/*
 * BridgeBoss.java
 * Copyright (C) 2010 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.bridge;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import netscape.javascript.*;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;

import java.util.Map;
import java.util.HashMap;

/**
 * BridgeBoss is a remote service that keeps track of the Java Desktop side of Gaggle.
 * It pretends to be a Boss, but delegates broadcasting and registration to a Javascript
 * implementation which is contained in the same web page as the applet this boss is started
 * from.
 * The Javascript boss has to be available under JS_BOSS, so LiveConnect can call methods
 * on it.
 * BridgeBoss keeps a map of all geese that were registered through RMI and only knows about
 * these in order to detect unavailable services and update the list in JsBoss.
 */
public class BridgeBoss extends UnicastRemoteObject implements Boss {

    private static final String JS_BOSS = "gaggle.boss";

    private JSObject document;
    private Map<String, Goose> geese = new HashMap<String, Goose>();

    public BridgeBoss(JSObject doc) throws RemoteException {
        super();
        this.document = doc;
    }

    // **********************************************************************
    // **** Functions that call the Javascript side via LiveConnect
    // **********************************************************************
    private String callJsBoss(String methodCall) {
        return document.eval(String.format(JS_BOSS + ".%s;", methodCall)).toString();
    }
    private String jsCreateProxyFor(String gooseBaseName) {
        return callJsBoss(String.format("createProxy('%s')", gooseBaseName));
    }
    private void jsUnregister(String gooseUID) {
        callJsBoss(String.format("unregister('%s')", gooseUID));
    }

    // **********************************************************************
    // **** LiveConnect interface
    // **********************************************************************
    public void updateGoose(String gooseUID, String[] currentNames) {
        if (geese.containsKey(gooseUID)) {
            try {
                geese.get(gooseUID).update(currentNames);
            } catch (RemoteException ex) {
                System.out.printf("BridgeBoss.updateGoose(): goose '%s' seems to be dead.\n",
                                  gooseUID);
                ex.printStackTrace();
            }
        } else {
            System.out.printf("BridgeBoss.updateGoose(): goose '%s' not found.\n", gooseUID);
        }
    }

    // **********************************************************************
    // **** Public interface implementation
    // **********************************************************************
    // registration
    public String[] getGooseNames() {
        System.out.println("getGooseNames()");
        return geese.keySet().toArray(new String[0]);
    }
    public String renameGoose(String oldName, String newName) {
        System.out.printf("renameGoose from '%s' to '%s'\n", oldName, newName);
        return newName;
    }
    public String register(Goose goose) throws RemoteException {
        // 1. call register(goose) on JS side, returning an unique identifier
        String gooseBaseName = goose.getName();
        String uniqueName = jsCreateProxyFor(gooseBaseName);
        // 2. put the goose into the map, using that identifier
        System.out.printf("register() goose '%s' with unique name '%s'\n", gooseBaseName, uniqueName);
        geese.put(uniqueName, goose);
        // update the list of current connected geese and inform them about the
        // updated list
        //goose.update(getGooseNames());
        return goose.getName();
    }
    public void unregister(String gooseUID) {
        System.out.printf("unregister() goose '%s'\n", gooseUID);
        geese.remove(gooseUID);
        jsUnregister(gooseUID);
        // TODO: this might be a good time to check for unavailable RMI geese
    }
    
    // Application control: These methods really only apply to
    // the desktop applications and we won't delegate to JsBoss here.
    public void show(String gooseUID) throws RemoteException {
        System.out.printf("show goose '%s'\n", gooseUID);
        if (geese.containsKey(gooseUID)) geese.get(gooseUID).doShow();
    }
    public void hide(String gooseUID) throws RemoteException {
        System.out.printf("hide goose '%s'\n", gooseUID);
        if (geese.containsKey(gooseUID)) geese.get(gooseUID).doHide();
    }
    public void terminate(String gooseUID) throws RemoteException {
        System.out.printf("terminate goose '%s'\n", gooseUID);
        try {
            if (geese.containsKey(gooseUID)) geese.get(gooseUID).doExit();
        } catch (java.rmi.UnmarshalException ignore) {
            // an exception that can occasionally happen and is non-critical
            System.out.println("UnmarshalException caught on terminate goose (ignored)");
        }
    }

    // broadcasting
    public void broadcastNetwork(String source, String target, Network network) {
        System.out.printf("broadcastNetwork() from '%s' to '%s'\n", source, target);
    }
    public void broadcastCluster(String source, String target, Cluster cluster) {
        System.out.printf("broadcastCluster() from '%s' to '%s'\n", source, target);
    }
    public void broadcastTuple(String source, String target, GaggleTuple tuple) {
        System.out.printf("broadcastTuple() from '%s' to '%s'\n", source, target);
    }
    public void broadcastMatrix(String source, String target, DataMatrix matrix) {
        System.out.printf("broadcastMatrix() from '%s' to '%s'\n", source, target);
    }
    public void broadcastNamelist(String source, String target, Namelist namelist) {
        System.out.printf("broadcastNamelist() from '%s' to '%s'\n", source, target);
    }
}
