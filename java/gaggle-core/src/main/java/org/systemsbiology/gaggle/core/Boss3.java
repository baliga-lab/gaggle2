package org.systemsbiology.gaggle.core;

import net.sf.json.JSONObject;
import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.WorkflowAction;

import java.rmi.RemoteException;
import java.util.HashMap;
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
     * @return a JSON string that includes goose information (e.g. full execution path etc)
     */
    public String submitWorkflow(Goose3 proxyGoose, String jsonWorkflow) throws RemoteException;

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

    /**
     * Pause recording a workflow
     * @param rid
     * @throws RemoteException
     * Returns the currently recorded workflow
     */
    public String pauseRecordingWorkflow(UUID rid) throws RemoteException;

    /**
     * Resume recording a workflow
     * @param rid
     * @throws RemoteException
     */
    public void resumeRecordingWorkflow(UUID rid) throws RemoteException;

    /**
     * Record a broadcast action. The API can be used to update source goose and target goose parameters
     * as well as edge parameters
     * @param sourceGoose
     * @param targetGoose
     * @param data
     * @param edgeIndex The index of the edge to be updated, -1 if it's a new edge
     * @param sourceParams
     * @param targetParams
     * @param edgeParams
     */
    public void recordAction(String sourceGoose, String targetGoose, Object data,
                             int edgeIndex,
                             HashMap<String, String> sourceParams,
                             HashMap<String, String> targetParams,
                             HashMap<String, String> edgeParams
    ) throws RemoteException;
}
