package org.systemsbiology.gaggle.core;

import org.systemsbiology.gaggle.core.datatypes.GaggleGooseInfo;
import org.systemsbiology.gaggle.core.datatypes.Table;
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


    public GaggleGooseInfo getGooseInfo() throws RemoteException;
}
