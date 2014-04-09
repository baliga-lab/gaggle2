package org.systemsbiology.gaggle.boss;

import org.systemsbiology.gaggle.core.Goose3;
import org.systemsbiology.gaggle.core.JSONGoose;
import org.systemsbiology.gaggle.core.datatypes.*;

import java.io.StringWriter;
import java.rmi.RemoteException;

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

    // Escape a json string
    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char         c = 0;
        int          i;
        int          len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String       t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    //                if (b == '<') {
                    sb.append('\\');
                    //                }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
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

    public void saveState(String directory, String filePrefix) throws RemoteException
    {
        if (jsonGoose instanceof Goose3)
        {
            ((Goose3)jsonGoose).saveState(directory, filePrefix);
        }
    }

    public void loadState(String location) throws RemoteException
    {
        if (jsonGoose instanceof Goose3)
        {
            ((Goose3)jsonGoose).loadState(location);
        }
    }


}
