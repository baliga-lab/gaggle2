package org.systemsbiology.gaggle.core.datatypes;

import javax.xml.transform.Source;
import java.io.*;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Ning Jiang
 * Date: 10/8/12
 * Time: 3:49 PM
 * A workflow action consists of a source component, a target component, and data passed by the source component
 * to the target. It also contains information of how to invoke source and target components.
 */
public class WorkflowAction implements Serializable {
    private String sessionID;
    private ActionType actionType; // This is the type of the action (i.e., request, response, etc)
    private WorkflowComponent source;
    private WorkflowComponent[] targets;
    private int option; //This is the data type to be broadcast from source to target
    private GaggleData[] data; // This is the data to be handled by source

    public enum ActionType
    {
        Request,
        Response
    }

    public enum DataType
    {
        DataMatrix,
        Namelist,
        Network,
        Cluster,
        Map,
        WorkflowData,
        Generic
    }

    public enum Options
    {
        Parallel(0x0001),
        Sequential(0x0010),
        SuccessMessage(0x0100),
        ErrorMessage(0x1000);

        private final int id;
        Options(int id) { this.id = id; }
        public int getValue() { return id; }
    }

    public WorkflowAction(String sessionID,
                          ActionType actionType,
                          WorkflowComponent source,
                          WorkflowComponent[] targets,
                          int option,
                          GaggleData[] data)
    {
        this.sessionID = sessionID;
        this.source = source;
        this.targets = targets;
        this.actionType = actionType;
        this.option = option;
        //this.nextStepDataTypes = dataTypes;
        this.data = data;
    }

    public String getSessionID() { return sessionID; }
    public ActionType getActionType() { return actionType; }
    public WorkflowComponent getSource() { return source; }
    public WorkflowComponent[] getTargets() { return targets; }
    public void setTargets(WorkflowComponent[] targets) { this.targets = targets; }

    //public DataType[] getDataType() { return nextStepDataTypes; }
    public int getOption() { return option; }
    public GaggleData[] getData() { return data; }
    public void setData(GaggleData[] gdata) { this.data = gdata; }
}
