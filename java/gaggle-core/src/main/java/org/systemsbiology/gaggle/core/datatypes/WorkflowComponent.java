package org.systemsbiology.gaggle.core.datatypes;

import net.sf.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * <p>Each WorkflowComponent corresponds to a goose.
 * The class stores all the information of a WorkflowComponent including the execution path, command line arguments,
 * subactions, arguments, parameters etc.<p/>
 */
public class WorkflowComponent implements Serializable {

    public enum Options
    {
        None(0x00000),
        OpenInNewWindow(0x00001);

        private final int id;
        Options(int id) { this.id = id; }
        public int getValue() { return id; }
    }

    private String componentWorkflowNodeID;
    private String componentID;
    private String name;  // corresponds to the short name of a goose
    private String gooseName; // The name registered to the Boss
    private String version;
    private String commandUri;
    private String arguments;
    private String workflowIndex;
    private HashMap<String, Object> params;
    private String jsonParams;
    private int options;
    // The name of an already opened goose, this is used when the user chooses to pass data to an opened goose.
    private String existingGooseName;



    //private ProcessingState state;

    /**
     *  Parameter names
     *  SubTarget is for goose that have subactions. For example, Firegoose can broadcast data to web handlers
     *  such as KEGG, DAVID, EMBL String, etc.
     *  EdgeType indidates the data type of an edge in the workflow.  Data type can be Namelist, Matrix, Cluster, etc.
     *  Data is the actual data passed to a goose.
     */
    public enum ParamNames
    {
        SubTarget("SubTarget"),
        EdgeType("EdgeType"),
        Data("Data");

        private final String id;
        ParamNames(String id) { this.id = id; }
        public String getValue() { return id; }
    }

    /**
     *
     * @param id ID of the goose in the workflow
     * @param workflownodeid ID of the goose stored on the DB (this is used to generate the workflow report)
     * @param name Short name of the goose (e.g. Cytoscape, Firegoose, MeV)
     * @param gooseName Full name of the goose (e.g., Cytoscape, Firegoose, Multiple Array Viewer)
     * @param version Version of the goose
     * @param cmduri  Command uri to start a goose (e.g. C:\Program Files (x86)\Mozilla Firefox\firefox.exe)
     * @param arguments Arguments passed to start the goose
     * @param params Parameters passed to the goose (See the ParamNames enum)
     */
    public WorkflowComponent(String id, String workflownodeid, String workflowindex, String name,
                             String gooseName, String version, String cmduri, String arguments,
                             HashMap params, int options, String existingGooseName)
    {
        this.componentWorkflowNodeID = workflownodeid;
        this.workflowIndex = workflowindex;
        this.componentID = id;
        this.name = name;
        this.gooseName = gooseName;
        this.version = version;
        this.commandUri = cmduri;
        this.arguments = arguments;
        if (params != null)
            this.params = new HashMap(params);
        else
            this.params = new HashMap();
        this.options = options;
        this.existingGooseName = existingGooseName;

        //this.convertParamsToJSON();
        //this.state = ProcessingState.Initial;
    }

    /**
     *
     * @param id ID of the goose in the workflow
     * @param name Short name of the goose (e.g. Cytoscape, Firegoose, MeV)
     * @param gooseName Full name of the goose (e.g., Cytoscape, Firegoose, Multiple Array Viewer)
     * @param version Version of the goose
     * @param cmduri  Command uri to start a goose (e.g. C:\Program Files (x86)\Mozilla Firefox\firefox.exe)
     * @param arguments Arguments passed to start the goose
     * @param params Parameters passed to the goose (See the ParamNames enum)
     */
    public WorkflowComponent(String id, String name, String gooseName, String version, String cmduri, String arguments, HashMap params)
    {
        this.componentWorkflowNodeID = "";
        this.componentID = id;
        this.name = name;
        this.gooseName = gooseName;
        this.version = version;
        this.commandUri = cmduri;
        this.arguments = arguments;
        this.options = options;
        if (params != null)
            this.params = new HashMap(params);
        else
            this.params = new HashMap();
        this.options = Options.None.getValue();
        //this.convertParamsToJSON();
        //this.state = ProcessingState.Initial;
    }

    /**
     * Copy constructor
     * @param source
     */
    public WorkflowComponent(WorkflowComponent source)
    {
        if (source != null)
        {
            try
            {
                System.out.println("Clone a component " + source.getComponentID());
                this.componentWorkflowNodeID = source.getComponentWorkflowNodeID();
                this.componentID = source.getComponentID();
                this.workflowIndex = source.getWorkflowIndex();
                this.commandUri = source.getCommandUri();
                this.arguments = source.getArguments();
                this.name = source.getName();
                this.gooseName = source.getGooseName();
                this.version = source.getVersion();
                this.params = new HashMap<String, Object>(source.getParams());
                this.options = source.getOptions();
                //this.convertParamsToJSON();
            }
            catch (Exception e)
            {
                System.out.println("Failed to clone a component: " + e.getMessage());
            }
        }
    }

    public String getComponentWorkflowNodeID() { return componentWorkflowNodeID; }
    public String getComponentID() { return componentID; }
    public String getWorkflowIndex() { return workflowIndex; }
    public String getName() { return name; }
    public String getGooseName()
    {
        // Some geese are scripts (e.g., R scripts), and needs a host goose (e.g. R) to open.
        // So we return the name of the host goose.
        if (existingGooseName != null && existingGooseName.length() > 0)
            return existingGooseName;
        return gooseName;
    }
    public String getVersion() { return version; }
    public String getCommandUri()
    {
        System.out.println("Getting command uri for " + gooseName);
        if (gooseName.equals("Generic"))
        {
            // For Generic goose, we return the data uri as command uri
            // See WorkflowManager.startGoose for details
            ArrayList<Object> datalist = (ArrayList)params.get(WorkflowComponent.ParamNames.Data.getValue());
            if (datalist != null && datalist.size() > 0)
            {
                String result = "";
                for (int i = 0; i < datalist.size(); i++)
                {
                    result += ((String)datalist.get(i) + ";;");
                    System.out.println("Data uri: " + result);
                }
                return result;
            }

        }
        return commandUri;
    }

    public String getArguments() { return arguments; }
    //public ProcessingState getState() { return state; }
    //public void setState(ProcessingState s) { this.state = s; }
    public HashMap getParams() { return params; }
    public String getJSONParams() { return jsonParams; }
    public int getOptions() { return options; }
    public String getExistingGooseName() { return existingGooseName; }

    public void setParams(HashMap para)
    {
        if (para != null)
        {
            this.params = new HashMap(para);
            //this.convertParamsToJSON();
        }
    }

    public void addParam(String key, Object data)
    {
        if (key != null && key.length() > 0 && data != null)
        {
            if (key.equals(ParamNames.Data.getValue()))
            {
                ArrayList<Object> datalist = (ArrayList)this.params.get(key);
                if (datalist == null)
                    datalist = new ArrayList<Object>();
                datalist.add(data);
                this.params.put(key, datalist);
            }
            else
                this.params.put(key, data);
            //this.convertParamsToJSON();
        }
    }

    // We have to override both equals and hashCode!!
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof WorkflowComponent))
            return false;
        if (this.componentID.equalsIgnoreCase(((WorkflowComponent)obj).getComponentID()))
            return true;
        return false;
    }

    public int hashCode()
    {
        int hash = 5;
        hash = 89 * hash + (this.componentID != null ? this.componentID.hashCode() : 0);
        return hash;
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
