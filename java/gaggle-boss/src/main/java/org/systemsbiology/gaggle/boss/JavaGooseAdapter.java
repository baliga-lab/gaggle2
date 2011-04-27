package org.systemsbiology.gaggle.boss;

import java.io.*;
import java.rmi.*;

import org.systemsbiology.gaggle.core.*;
import org.systemsbiology.gaggle.core.datatypes.*;


public class JavaGooseAdapter implements SuperGoose {
    private Goose javaGoose;
    public JavaGooseAdapter(Goose javaGoose) {
        this.javaGoose = javaGoose;
    }
    public void doExit() throws RemoteException {
        javaGoose.doExit();
    }
    public void doShow() throws RemoteException {
        javaGoose.doShow();
    }
    public void doHide() throws RemoteException {
        javaGoose.doHide();
    }
    public void doBroadcastList() throws RemoteException {
        javaGoose.doBroadcastList();
    }
    public void setName(String newName) throws RemoteException {
        javaGoose.setName(newName);
    }
    public String getName() throws RemoteException {
        return javaGoose.getName();
    }
    public void update(String[] gooseNames) throws RemoteException {
        javaGoose.update(gooseNames);
    }
    public void handleNetwork(String source,
                              Network network) throws RemoteException {
        javaGoose.handleNetwork(source, network);
    }
    public void handleCluster(String source,
                              Cluster cluster) throws RemoteException {
        javaGoose.handleCluster(source, cluster);
    }
    public void handleTuple(String source,
                            GaggleTuple gaggleTuple) throws RemoteException {
        javaGoose.handleTuple(source, gaggleTuple);
    }
    public void handleMatrix(String source,
                             DataMatrix dataMatrix) throws RemoteException {
        javaGoose.handleMatrix(source, dataMatrix);
    }
    public void handleNameList(String source,
                               Namelist namelist) throws RemoteException {
        javaGoose.handleNameList(source, namelist);
    }
    public void handleTable(String source, Table table) throws RemoteException {
        if (javaGoose instanceof Goose2) {
            ((Goose2) javaGoose).handleTable(source, table);
        }
    }
    public void handleJSON(String source,
                           String json) throws RemoteException {
        GaggleData gaggleData = (new JSONReader()).createFromJSONString(json);
        if (gaggleData instanceof Network) handleNetwork(source, (Network) gaggleData);
        else if (gaggleData instanceof Cluster) handleCluster(source, (Cluster) gaggleData);
        else if (gaggleData instanceof GaggleTuple) handleTuple(source, (GaggleTuple) gaggleData);
        else if (gaggleData instanceof DataMatrix) handleMatrix(source, (DataMatrix) gaggleData);
        else if (gaggleData instanceof Namelist) handleNameList(source, (Namelist) gaggleData);
        else if (gaggleData instanceof Table) handleTable(source, (Table) gaggleData);
    }
}
