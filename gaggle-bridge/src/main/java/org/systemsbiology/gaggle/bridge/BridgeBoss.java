package org.systemsbiology.gaggle.bridge;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import netscape.javascript.*;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;

import java.util.Map;
import java.util.HashMap;

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
        goose.update(getGooseNames());
        return goose.getName();
    }
    public void unregister(String gooseName) {
        System.out.printf("unregister() goose '%s'\n", gooseName);
        jsUnregister(gooseName);
    }
    
    // application control
    public void show(String gooseName) {
        System.out.printf("show goose '%s'\n", gooseName);
    }
    public void hide(String gooseName) {
        System.out.printf("hide goose '%s'\n", gooseName);
    }
    public void terminate(String gooseName) {
        System.out.printf("terminate goose '%s'\n", gooseName);
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
