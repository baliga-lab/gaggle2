package org.systemsbiology.gaggle.core.datatypes;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A wrapper of the WorkflowAction to be processed. The class returns the information of data passed
 * to the goose to be processed. It also includes helper functions to store data for the geese in the next steps
 * of the workflow.</p>
 */
public class WorkflowGaggleData
{
    String species = "Unknown";
    String[] nameList;
    String size;
    String type;
    Tuple metadata;
    String requestID;
    String subAction;
    WorkflowAction workflowAction;
    boolean fetched = false;
    private WorkflowAction request;
    private WorkflowAction response;
    private ArrayList<WorkflowComponent> targets;
    private ArrayList<GaggleData> data;

    public WorkflowAction getWorkflowResponse() { return response; }

    public String getSpecies() { return species; }
    public String[] getNameList() { return nameList; }
    public String getSize() { return size; }
    public String getType() { return type; }
    public String getRequestID() { return requestID; }
    public String getSubAction() { return subAction; }
    public WorkflowAction getWorkflowAction() { return request; }
    public boolean isFetched() { return fetched; }
    public void setFetched(boolean fetched) { this.fetched = fetched; }

    public WorkflowGaggleData(String requestID, WorkflowAction workflowAction)
    {
        this.requestID = requestID;
        this.request = workflowAction;
        this.response = new WorkflowAction(request.getWorkflowID(),
                request.getSessionID(),
                request.getComponentID(),
                WorkflowAction.ActionType.Response,
                request.getSource(),
                null,
                request.getOption() | WorkflowAction.Options.SuccessMessage.getValue(),
                null
        );
        this.targets = new ArrayList<WorkflowComponent>();
        this.data = new ArrayList<GaggleData>();

        if (workflowAction != null)
        {
            System.out.println("=====> Initializing Workflow data " + requestID);
            try
            {
                // get species info
                if (workflowAction.getSource().getParams().containsKey(JSONConstants.WORKFLOW_ORGANISMINFO))
                {
                    species = (String)workflowAction.getSource().getParams().get(JSONConstants.WORKFLOW_ORGANISMINFO);
                    System.out.println("Workflow passed species info " + species);
                }

                if (workflowAction.getSource().getParams().containsKey(WorkflowComponent.ParamNames.Data.getValue()))
                {
                    ArrayList<Object> datalist = (ArrayList<Object>)workflowAction.getSource().getParams().get(WorkflowComponent.ParamNames.Data.getValue());
                    if (datalist != null && datalist.size() > 0)
                    {
                        String datauri = "";
                        Object data = datalist.get(0);
                        if (data instanceof GaggleData)
                            System.out.println(((GaggleData)data).getName());
                        else if (data instanceof  String)
                        {
                            for (int i = 0; i < datalist.size(); i++) {
                                datauri += ((String)datalist.get(i) + ";");
                                System.out.println((String)datalist.get(i));
                            }
                        }
                        System.out.println("JSON param: " + workflowAction.getSource().getJSONParams());

                        this.subAction = "";
                        if (workflowAction.getSource().getParams().containsKey(WorkflowComponent.ParamNames.SubTarget.getValue()))
                        {
                            this.subAction = (String)workflowAction.getSource().getParams().get(WorkflowComponent.ParamNames.SubTarget.getValue());
                            System.out.println("Subaction: " + this.subAction);
                        }

                        //this.actionType = "WorkflowAction";
                        //this.sessionID = workflowAction.getSessionID().toString();
                        //this.workflowAction = workflowAction;

                        // This has to be done the latest because hasNewDataSignal.increment() is called
                        // in all the handle[GaggleData] functions
                        //boolean dataProcessed = true;
                        if (data != null)
                        {
                            if (data instanceof WorkflowData)
                            {
                                this.type = "WorkflowData";
                                this.nameList = new String[1];
                                this.nameList[0] = (String)(((WorkflowData)data).getData());
                                System.out.println("Workflow data: " + this.nameList[0]);
                            }
                            else if (data instanceof Network)
                                this.handleNetwork(workflowAction.getSource().getName(), (Network) data);
                            else if (data instanceof Cluster)
                                this.handleCluster(workflowAction.getSource().getName(), (Cluster) data);
                            else if (data instanceof DataMatrix)
                                this.handleMatrix(workflowAction.getSource().getName(), (DataMatrix)data);
                            else if (data instanceof Namelist)
                                this.handleNameList(workflowAction.getSource().getName(), (Namelist)data);
                            else if (data instanceof GaggleTuple)
                                this.handleTuple(workflowAction.getSource().getName(), (GaggleTuple)data);
                            else if (data instanceof String) // this is a uri
                            {
                                this.type = "WorkflowData";
                                this.nameList = new String[1];
                                this.nameList[0] = (String)datauri;
                                System.out.println("URI data: " + this.nameList[0]);
                            }

                            // TODO support other data types
                            //else
                            //    dataProcessed = false;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                System.out.println("Failed to handle workflow action: " + e.getMessage());
            }
        }
    }

    public void addSessionData(int targetIndex, GaggleData gdata)
    {
        WorkflowComponent[] reqTargets = this.request.getTargets();
        if (reqTargets != null)
        {
            if (targetIndex < reqTargets.length)
            {
                System.out.println("Data added for request " + this.requestID);
                this.targets.add(reqTargets[targetIndex]);
                // gdata could be null. Fortunately ArrayList allows null elements
                this.data.add(gdata);
            }
            else
            {
                System.out.println("FireGoose: index out of range of all the targets!");
            }
        }
    }

    public boolean finalizeWorkflowAction()
    {
        if (this.targets.size() > 0)
        {
            System.out.println("Finalize targets for " + this.request.getSessionID());
            WorkflowComponent[] targetarray = new WorkflowComponent[this.targets.size()];
            this.targets.toArray(targetarray);
            this.response.setTargets(targetarray);

            System.out.println("Finalize data for " + this.request.getSessionID());
            GaggleData[] dataarray = new GaggleData[this.data.size()];
            this.data.toArray(dataarray);
            this.response.setData(dataarray);
            return true;
        }
        else
        {
            System.out.println("No target for " + this.request.getSessionID());
            return false;
        }
    }

    public int dataSubmitted()
    {
        return this.targets.size();
    }

    protected void handleNameList(String sourceGooseName, Namelist namelist) throws RemoteException {
        if (namelist.getSpecies() != null && namelist.getSpecies().length() > 0)
        {
            this.species = namelist.getSpecies();
        }
        this.nameList = namelist.getNames();
        this.type = "NameList";
        this.size = String.valueOf(nameList.length);
        System.out.println("Extracted namelist: " + type + "(" + size + ")");
    }

    protected void handleMatrix(String sourceGooseName, DataMatrix simpleDataMatrix) throws RemoteException {
        System.out.println("incoming broadcast: DataMatrix");
        this.type = "DataMatrix";
        if (simpleDataMatrix.getSpecies() != null && simpleDataMatrix.getSpecies().length() > 0)
            this.species = simpleDataMatrix.getSpecies();
        this.nameList = simpleDataMatrix.getRowTitles();    //TODO: is this correct?
        this.size = String.valueOf(simpleDataMatrix.getRowCount());
    }


    protected void handleTuple(String sourceGooseName, GaggleTuple gaggleTuple) throws RemoteException {
        System.out.println("incoming broadcast: gaggleTuple");
        this.type = "Map";    // TODO: is this correct?
        if (gaggleTuple.getSpecies() != null && gaggleTuple.getSpecies().length() > 0)
            this.species = gaggleTuple.getSpecies();
        Tuple data = gaggleTuple.getData();
        if (data != null)
        {
            List<Single> singlelist = data.getSingleList();
            if (singlelist != null && singlelist.size() > 0)
            {
                this.nameList = new String[singlelist.size()];
                int i = 0;
                for (Single s : singlelist)
                {
                    System.out.println("Single name: " + s.getName());
                    this.nameList[i] = new String(s.getName());
                    i++;
                }
            }
        }
        this.size = String.valueOf(gaggleTuple.getData().getSingleList().size());
    }

    protected void handleCluster(String sourceGooseName, Cluster cluster) throws RemoteException {
        // we handle clusters by translating them to namelists
        if (cluster.getSpecies() != null && cluster.getSpecies().length() > 0)
            this.species = cluster.getSpecies();
        this.nameList = cluster.getColumnNames();
        this.type = "NameList";
        this.size = String.valueOf(nameList.length);
        System.out.println("Extracted cluster translated to " + type + "(" + size + ")");
    }

    protected void handleNetwork(String sourceGooseName, Network network) throws RemoteException {
        System.out.println("incoming broadcast: network");
        if (network.getSpecies() != null && network.getSpecies().length() > 0)
            this.species = network.getSpecies();
        this.nameList = network.getNodes();
        this.type = "Network";
        this.size = String.valueOf(nameList.length);
    }
}

