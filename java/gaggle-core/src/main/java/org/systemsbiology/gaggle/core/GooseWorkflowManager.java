package org.systemsbiology.gaggle.core;

import org.systemsbiology.gaggle.core.datatypes.*;

import java.rmi.RemoteException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Ning Jiang
 * Date: 11/13/12
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */

public class GooseWorkflowManager
{
    //HashMap<String, WorkflowStagingData> workflowStagingDataMap = new HashMap<String, WorkflowStagingData>();
    Map<String, WorkflowGaggleData> processingQueue = Collections.synchronizedMap(new HashMap<String, WorkflowGaggleData>());

    public GooseWorkflowManager()
    {

    }

    public String getSpecies(String requestID)
    {
        if (this.processingQueue.containsKey(requestID))
        {
            return this.processingQueue.get(requestID).getSpecies();
        }
        return null;
    }

    public String[] getNameList(String requestID)
    {
        if (this.processingQueue.containsKey(requestID))
            return this.processingQueue.get(requestID).getNameList();
        return null;
    }

    public String getSize(String requestID)
    {
        if (this.processingQueue.containsKey(requestID))
            return this.processingQueue.get(requestID).getSize();
        return null;
    }

    public String getType(String requestID)
    {
        if (this.processingQueue.containsKey(requestID))
        {
            System.out.println("Workflow data type: " + this.processingQueue.get(requestID).getType());
            return this.processingQueue.get(requestID).getType();
        }
        return null;
    }

    public String getSubAction(String requestID)
    {
        if (this.processingQueue.containsKey(requestID))
            return this.processingQueue.get(requestID).getSubAction();
        return null;
    }

    public String addSession(WorkflowAction request)
    {
        if (request != null)
        {
            System.out.println("Storing workflow request to the processing queue");
            UUID requestID = UUID.randomUUID();
            WorkflowGaggleData wfgd = new WorkflowGaggleData(requestID.toString(), request);
            this.processingQueue.put(requestID.toString(), wfgd);
            return requestID.toString();
        }
        return null;
    }

    public WorkflowAction getWorkflowAction(String requestID)
    {
        if (this.processingQueue.containsKey(requestID))
            return this.processingQueue.get(requestID).getWorkflowAction();
        return null;
    }

    public String getCurrentRequest()
    {
        for (String key : this.processingQueue.keySet())
        {
            WorkflowGaggleData workflowGaggleData = this.processingQueue.get(key);
            if (!workflowGaggleData.isFetched())
            {
                workflowGaggleData.setFetched(true);
                return workflowGaggleData.getRequestID();
            }
        }
        return null;

        /*if (!this.processingQueue.isEmpty())
      {
          System.out.println("Getting " + this.processingQueue.size() + " workflow requests...");
          System.out.println((String)((this.processingQueue.keySet().toArray())[0]));

          return (String)((this.processingQueue.keySet().toArray())[0]);
      }
      return null; */
    }

    public void removeRequest(String requestID)
    {
        if (requestID != null)
        {
            System.out.println("Removing workflow request " + requestID);
            this.processingQueue.remove(requestID);
        }
    }

    public void addSessionTargetData(String requestID, int targetIndex, GaggleData data)
    {
        if (this.processingQueue.containsKey(requestID))
        {
            System.out.println("Adding data for " + requestID);
            WorkflowGaggleData stagingData = this.processingQueue.get(requestID);
            if (stagingData != null)
            {
                stagingData.addSessionData(targetIndex, data);
            }
        }
    }

    public boolean finalizeSessionAction(String requestID)
    {
        if (this.processingQueue.containsKey(requestID))
        {
            WorkflowGaggleData stagingData = this.processingQueue.get(requestID);
            System.out.println("Finalizing response for " + requestID);
            return stagingData.finalizeWorkflowAction();
        }
        else
            System.out.println("No data exists for " + requestID + ". Finalize session failed!!");
        return false;
    }

    public WorkflowAction getSessionResponse(String requestID)
    {
        if (this.processingQueue.containsKey(requestID))
        {
            System.out.println("Response data for " + requestID);
            return this.processingQueue.get(requestID).getWorkflowResponse();
        }
        return null;
    }

    public void RemoveSessionData(String requestID)
    {
        if (this.processingQueue.containsKey(requestID))
        {
            this.processingQueue.remove(requestID);
            System.out.println("Session data removed for " + requestID);
        }
    }

    // All the data are ready, we submit the response to the boss
    public boolean CompleteWorkflowAction(Boss boss, String requestID)
    {
        boolean succeeded = false;
        System.out.println("About to complete workflow action for " + requestID);
        if (this.finalizeSessionAction(requestID))
        {
            WorkflowAction response = this.getSessionResponse(requestID);
            if (response != null)
            {
                if (boss instanceof Boss3)
                {
                    try
                    {
                        System.out.println("About to send workflow response to boss...");
                        ((Boss3)boss).handleWorkflowAction(response);
                        System.out.println("Data Sent!");
                        succeeded = true;
                    }
                    catch (Exception e)
                    {
                        System.out.println("Failed to submit workflow response to boss: " + e.getMessage());
                    }
                }
                else
                    System.out.println("Boss does not support Workflow!");
            }
        }

        if (succeeded)
            this.RemoveSessionData(requestID);

        return succeeded;
    }
}
