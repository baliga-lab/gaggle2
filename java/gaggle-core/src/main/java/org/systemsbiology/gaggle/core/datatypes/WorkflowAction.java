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
    private String workflowID;
    private String sessionID;
    private String componentID;
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
        Parallel(0x00001),
        Sequential(0x00010),
        SuccessMessage(0x00100),
        ErrorMessage(0x01000),
        WorkflowReportData(0x10000);

        private final int id;
        Options(int id) { this.id = id; }
        public int getValue() { return id; }
    }

    public WorkflowAction(String workflowID,
                          String sessionID,
                          String componentID,
                          ActionType actionType,
                          WorkflowComponent source,
                          WorkflowComponent[] targets,
                          int option,
                          GaggleData[] data)
    {
        this.workflowID = workflowID;
        this.sessionID = sessionID;
        this.componentID = componentID;
        this.source = source;
        this.targets = targets;
        this.actionType = actionType;
        this.option = option;
        //this.nextStepDataTypes = dataTypes;
        this.data = data;
    }

    public String getWorkflowID() { return workflowID; }
    public String getSessionID() { return sessionID; }
    public String getComponentID() { return componentID; }
    public ActionType getActionType() { return actionType; }
    public WorkflowComponent getSource() { return source; }
    public WorkflowComponent[] getTargets() { return targets; }
    public void setTargets(WorkflowComponent[] targets) { this.targets = targets; }

    //public DataType[] getDataType() { return nextStepDataTypes; }
    public int getOption() { return option; }
    public GaggleData[] getData() { return data; }
    public void setData(GaggleData[] gdata) { this.data = gdata; }
}
