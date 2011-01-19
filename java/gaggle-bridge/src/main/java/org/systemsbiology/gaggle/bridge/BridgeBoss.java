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

// we need to include these for compatibility
import org.systemsbiology.gaggle.geese.DeafGoose;
import org.systemsbiology.gaggle.util.NewNameHelper;

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
    private boolean isMaster;

    public BridgeBoss(JSObject doc) throws RemoteException {
        super();
        this.document = doc;
        this.isMaster = true;
    }
    public boolean isMaster() { return isMaster; }
    public void setIsMaster(boolean flag) { isMaster = flag; }

    // **********************************************************************
    // **** Functions that call the Javascript side via LiveConnect
    // **********************************************************************
    private Object callJsBoss(String methodCall) {
        return document.eval(String.format(JS_BOSS + ".%s;", methodCall));
    }
    private void jsUnregister(String gooseUID) {
        callJsBoss(String.format("unregister('%s')", gooseUID));
    }
    private String jsRegisterGoose(Goose goose) {
        JSObject boss = (JSObject) document.eval("gaggle.boss");
        return boss.call("registerWithProxy", new Object[] { goose }).toString();
    }
    private String[] jsGetGooseNames() {
        JSObject boss = (JSObject) document.eval("gaggle.boss");
        return (String[]) boss.call("getGooseNames", new Object[0]);
    }
    private void jsBroadcastNamelist(String source, String target, Namelist namelist) {
        JSObject boss = (JSObject) document.eval("gaggle.boss");
        boss.call("broadcastNamelist", new Object[] { source, target, namelist });
    }
    private void jsBroadcastNetwork(String source, String target, Network network) {
        JSObject boss = (JSObject) document.eval("gaggle.boss");
        boss.call("broadcastNetwork", new Object[] { source, target, network });
    }
    private void jsBroadcastCluster(String source, String target, Cluster cluster) {
        JSObject boss = (JSObject) document.eval("gaggle.boss");
        boss.call("broadcastCluster", new Object[] { source, target, cluster });
    }
    private void jsBroadcastTuple(String source, String target, GaggleTuple tuple) {
        JSObject boss = (JSObject) document.eval("gaggle.boss");
        boss.call("broadcastTuple", new Object[] { source, target, tuple });
    }
    private void jsBroadcastMatrix(String source, String target, DataMatrix matrix) {
        JSObject boss = (JSObject) document.eval("gaggle.boss");
        boss.call("broadcastMatrix", new Object[] { source, target, matrix });
    }

    // **********************************************************************
    // **** Public interface implementation
    // **********************************************************************
    // registration
    public String[] getGooseNames() {
        System.out.println("getGooseNames()");
        return jsGetGooseNames();
    }
    public String renameGoose(String oldName, String newName) {
        System.out.printf("renameGoose from '%s' to '%s'\n", oldName, newName);
        return newName;
    }
    public String register(Goose goose) throws RemoteException {
        return jsRegisterGoose(goose);
    }
    public void unregister(String gooseUID) {
        System.out.printf("unregister() goose '%s'\n", gooseUID);
        jsUnregister(gooseUID);
        // TODO: this might be a good time to check for unavailable RMI geese
    }
    
    // Application control: These methods really only apply to
    // the desktop applications and we won't delegate to JsBoss here.
    public void show(String gooseUID) throws RemoteException {
        System.out.printf("show goose '%s'\n", gooseUID);
    }
    public void hide(String gooseUID) throws RemoteException {
        System.out.printf("hide goose '%s'\n", gooseUID);
    }
    public void terminate(String gooseUID) throws RemoteException {
        System.out.printf("terminate goose '%s'\n", gooseUID);
        /*
        try {
            if (geese.containsKey(gooseUID)) geese.get(gooseUID).doExit();
        } catch (java.rmi.UnmarshalException ignore) {
            // an exception that can occasionally happen and is non-critical
            System.out.println("UnmarshalException caught on terminate goose (ignored)");
            }*/
    }

    // broadcasting
    public void broadcastNetwork(String source, String target, Network network) {
        System.out.printf("broadcastNetwork() from '%s' to '%s'\n", source, target);
        jsBroadcastNetwork(source, target, network);
        System.out.println("broadcast success !!");
    }
    public void broadcastCluster(String source, String target, Cluster cluster) {
        System.out.printf("broadcastCluster() from '%s' to '%s'\n", source, target);
        jsBroadcastCluster(source, target, cluster);
        System.out.println("broadcast success !!");
    }
    public void broadcastTuple(String source, String target, GaggleTuple tuple) {
        System.out.printf("broadcastTuple() from '%s' to '%s'\n", source, target);
        jsBroadcastTuple(source, target, tuple);
        System.out.println("broadcast success !!");
    }
    public void broadcastMatrix(String source, String target, DataMatrix matrix) {
        System.out.printf("broadcastMatrix() from '%s' to '%s'\n", source, target);
        jsBroadcastMatrix(source, target, matrix);
        System.out.println("broadcast success !!");
    }
    public void broadcastNamelist(String source, String target, Namelist namelist) {
        System.out.printf("broadcastNamelist() from '%s' to '%s'\n", source, target);
        jsBroadcastNamelist(source, target, namelist);
        System.out.println("broadcast success !!");
    }

    // These methods are solely provided for compatibility reasons. Do not use.
    public NewNameHelper getNameHelper() {
        throw new UnsupportedOperationException("Not supported");
    }

    public String register(DeafGoose deafGoose) {
        throw new UnsupportedOperationException("Not supported");
    }
}
