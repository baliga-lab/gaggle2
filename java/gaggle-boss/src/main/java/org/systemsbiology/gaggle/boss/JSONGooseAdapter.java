package org.systemsbiology.gaggle.boss;

import java.io.*;
import java.rmi.*;

import org.systemsbiology.gaggle.core.*;
import org.systemsbiology.gaggle.core.datatypes.*;

/**
 * This class translates messages
 */
public class JSONGooseAdapter implements SuperGoose {
    private JSONGoose jsonGoose;
    public JSONGooseAdapter(JSONGoose jsonGoose) {
        this.jsonGoose = jsonGoose;
    }
    public JSONGoose getWrappedGoose() { return jsonGoose; }
    public void doExit() { }
    public void doShow() { }
    public void doHide() { }
    public void doBroadcastList() { }
    public GaggleGooseInfo getGooseInfo() { return null; }
    public void setName(String newName) throws RemoteException {
        jsonGoose.setName(newName);
    }
    public String getName() throws RemoteException {
        return jsonGoose.getName();
    }
    public void update(String[] gooseNames) throws RemoteException {
        jsonGoose.update(gooseNames);
    }
    public void handleNetwork(String source,
                              Network network) throws RemoteException {
        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(network);
        jsonGoose.handleJSON(source, stringWriter.toString());
    }
    public void handleCluster(String source,
                              Cluster cluster) throws RemoteException {
        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(cluster);
        jsonGoose.handleJSON(source, stringWriter.toString());
    }
    public void handleTuple(String source,
                            GaggleTuple gaggleTuple) throws RemoteException {
        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(gaggleTuple);
        jsonGoose.handleJSON(source, stringWriter.toString());
    }
    public void handleMatrix(String source,
                             DataMatrix dataMatrix) throws RemoteException {
        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(dataMatrix);
        jsonGoose.handleJSON(source, stringWriter.toString());
    }
    public void handleNameList(String source,
                               Namelist namelist) throws RemoteException {
        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(namelist);
        jsonGoose.handleJSON(source, stringWriter.toString());
    }
    public void handleTable(String source,
                            Table table) throws RemoteException {
        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(table);
        jsonGoose.handleJSON(source, stringWriter.toString());
    }
    public void handleJSON(String source,
                           String json) throws RemoteException {
        jsonGoose.handleJSON(source, json);
    }

    public void handleWorkflowAction(WorkflowAction action) throws RemoteException
    {
    }

    public void handleWorkflowInformation(String type, String info) throws RemoteException
    {
    }

}
