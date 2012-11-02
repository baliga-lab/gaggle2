package org.systemsbiology.gaggle.core.datatypes;

import net.sf.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Ning Jiang
 * Date: 10/8/12
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowComponent implements Serializable {
    private String componentID;
    private String name;  // corresponds to the name of a goose
    private String version;
    private String commandUri;
    private HashMap<String, Object> params;
    private String jsonParams;

    //private ProcessingState state;

    public enum ParamNames
    {
        SubTarget("SubTarget"),
        EdgeType("EdgeType"),
        Data("Data");

        private final String id;
        ParamNames(String id) { this.id = id; }
        public String getValue() { return id; }
    }

    public WorkflowComponent(String id, String name, String version, String cmduri, HashMap params)
    {
        this.componentID = id;
        this.name = name;
        this.version = version;
        this.commandUri = cmduri;
        if (params != null)
            this.params = new HashMap(params);
        else
            this.params = new HashMap();
        this.convertParamsToJSON();
        //this.state = ProcessingState.Initial;
    }

    public WorkflowComponent(WorkflowComponent source)
    {
        if (source != null)
        {
            this.componentID = source.getComponentID();
            this.commandUri = source.getCommandUri();
            this.name = source.getName();
            this.version = source.getVersion();
            this.params = new HashMap<String, Object>(source.getParams());
            this.convertParamsToJSON();
        }
    }

    public String getComponentID() { return componentID; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getCommandUri() { return commandUri; }
    //public ProcessingState getState() { return state; }
    //public void setState(ProcessingState s) { this.state = s; }
    public HashMap getParams() { return params; }
    public String getJSONParams() { return jsonParams; }
    public void setParams(HashMap para)
    {
        if (para != null)
        {
            this.params = new HashMap(para);
            this.convertParamsToJSON();
        }
    }

    public void addParam(String key, Object data)
    {
        if (key != null && key.length() > 0 && data != null)
        {
            this.params.put(key, data);
            this.convertParamsToJSON();
        }
    }

    private void convertParamsToJSON()
    {
        if (this.params != null)
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll(this.params);
            this.jsonParams = jsonObject.toString();
        }
    }
}