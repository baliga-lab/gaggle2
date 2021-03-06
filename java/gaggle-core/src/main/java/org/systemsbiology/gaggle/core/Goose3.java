package org.systemsbiology.gaggle.core;

import org.systemsbiology.gaggle.core.datatypes.GaggleGooseInfo;
import org.systemsbiology.gaggle.core.datatypes.WorkflowAction;

import java.rmi.RemoteException;

/**
 * <p>The Goose3 interface defines APIs for handling workflow related activities. A goose that supports workflow should
 * perform the following tasks.
 * </p>
 * <p>First, it should implement the handleWorkflowAction API. Boss passes a WorkflowAction to a goose using this API. Details
 * of the semantics of WorkflowAPI can be found in {@link WorkflowAction}.</p>
 * <p>In handleWorkflowAction, the goose can leverage the APIs provided by {@link GooseWorkflowManager}.
 * First, the goose should store the WorkflowAction using
 * {@link GooseWorkflowManager#addSession(org.systemsbiology.gaggle.core.datatypes.WorkflowAction)}.
 * It can also implement UI components to display the next geese in the workflow and allow users to pick data for them.
 * Data picked for each subsequent goose should be submitted to the GooseWorkflowManager by
 * calling {@link GooseWorkflowManager#addSessionTargetData(String, int, org.systemsbiology.gaggle.core.datatypes.GaggleData)}.
 * Once all the data is selected, the goose should call {@link GooseWorkflowManager#CompleteWorkflowAction(Boss, String)}
 * to submit the data to Boss.</p>
 */
public interface Goose3 extends Goose2 {
    /**
     * Handles a workflowAction passed from the Boss to the goose. The workflowAction contains data
     * passed from a source goose to the target goose, which in turn processes the data
     * (e.g., process a network, tuple, etc).
     * @param action WorkflowAction passed
     * @throws java.rmi.RemoteException on remote error
     */
    public void handleWorkflowAction(WorkflowAction action) throws RemoteException;


    /**
     * Handles information passed from Boss. This is mainly used by the proxy java applet
     * to handle information generated when the boss is processing the workflow.
     * @param type type of the information passed from boss to the goose (can be error, warning, etc)
     * @param info information passed from boss to the goose
     * @throws java.rmi.RemoteException on remote error
     */
    public void handleWorkflowInformation(String type, String info) throws RemoteException;


    /**
     * Get information about a goose.
     * @return A (@link GaggleGooseInfo(GaggleGooseInfo)) structure. It contains information
     * such as goose component ID, goose workflow ID etc.
     * @throws RemoteException
     */
    public GaggleGooseInfo getGooseInfo() throws RemoteException;

    //public void setGooseInfo(GaggleGooseInfo gooseInfo) throws RemoteException;

    /**
     * This API is called by the Boss during state saving. The goose should save its current state
     * to one or multiple files prefixed with the given filePrefix string and save the files in
     * the destination directory.
     *
     * @param directory The destination directory to save the state files.
     * @param filePrefix  The prefix of the name of the state file.
     * @throws RemoteException
     */
    public void saveState(String directory, String filePrefix) throws RemoteException;

    /**
     * Load the state according given the state file.
     *
     * @param location  The full path name of the state file to be loaded.
     * @throws RemoteException
     */
    public void loadState(String location) throws RemoteException;
}
