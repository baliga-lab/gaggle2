package org.systemsbiology.gaggle.core;

import net.sf.json.JSONObject;
import org.systemsbiology.gaggle.core.datatypes.WorkflowAction;

import java.rmi.RemoteException;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Ning Jiang
 * Date: 10/8/12
 * Time: 3:34 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Boss3 extends Boss2 {


    /**
     * Submit a workflow to the boss. The boss will parse the workflow
     * and generate the execution plan. A thread will be spawned for
     * each workflow.
     * @param jsonWorkflow A workflow wrapped in JSON string.
     * @throws java.rmi.RemoteException if RMI communication fails
     */
    public void submitWorkflow(Goose3 proxyGoose, String jsonWorkflow) throws RemoteException;

    /**
     * Tell the boss to handle a workflow action.
     * This requires the source component to perform the action
     * and then pass the data to the target component.
     * If either the source or the target component is not started,
     * we need to try starting it.
     * @param action A workflow action needs to be carried out.
     * @throws java.rmi.RemoteException if RMI communication fails
     */
    public void handleWorkflowAction(WorkflowAction action) throws RemoteException;

    /**
     * Tell the boss to start recording a workflow
     * @throws RemoteException
     * Each boss can only process one recording session.
     * Other requests will be rejected.
     * returns a UUID if the recording is started successfully
     * null if the request is rejected.
     */
    public UUID startRecordingWorkflow() throws RemoteException;

    /**
     * Tell the boss to stop recording a workflow
     * @throws RemoteException
     * return a JSON string of the recorded workflow
     */
    public String terminateRecordingWorkflow(UUID rid) throws RemoteException;
}
