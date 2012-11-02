package org.systemsbiology.gaggle.core;

import org.systemsbiology.gaggle.core.datatypes.Table;
import org.systemsbiology.gaggle.core.datatypes.WorkflowAction;

import java.rmi.RemoteException;

/**
 * Created with IntelliJ IDEA.
 * User: Ning Jiang
 * Date: 10/12/12
 * Time: 3:52 PM
 * Goose3 handles workflow related operations.
 *
 */
public interface Goose3 extends Goose2 {
    /**
     * Handles a workflow action. This is mainly used by an actual goose
     * to perform an action (e.g., process a network, tuple, etc).
     * The proxy applet ignores it in most of cases.
     * @param action WorkflowAction
     * @throws java.rmi.RemoteException on remote error
     */
    public void handleWorkflowAction(WorkflowAction action) throws RemoteException;


    /**
     * Handles information passed from boss. This is mainly used by the proxy java applet
     * to handle information generated when the boss is processing the workflow.
     * @param type type of the information passed from boss to the goose (can be error, warning, etc)
     * @param info information passed from boss to the goose
     * @throws java.rmi.RemoteException on remote error
     */
    public void handleWorkflowInformation(String type, String info) throws RemoteException;
}
