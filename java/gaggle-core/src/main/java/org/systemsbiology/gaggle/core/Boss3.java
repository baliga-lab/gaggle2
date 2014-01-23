package org.systemsbiology.gaggle.core;

import org.systemsbiology.gaggle.core.datatypes.WorkflowAction;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.UUID;

/**
 * <p>The Boss3 interface defines APIs to handle workflow related actions. A Boss implements the Boss3
 * interface should receive and parse a JSON workflow string and generate the execution plan. It should automatically
 * start geese according to the execution plan and coordinate data transmission amongst the geese.</p>
 * <p>APIs are also defined for recording workflow from users' routine usage of various geese.
 * This is a more natural way for user to form workflows.</p>
 * <p>Another important task of Boss is to coordinate the generation of workflow reports. A workflow report is a summary
 * of an execution of a workflow. Basically, each goose of the workflow takes a snapshot of the data it has processed
 * and passes it to Boss. Boss subsequently passes it to the server, which stores the information in DB for later
 * retrieval.</p>
 */
public interface Boss3 extends Boss2 {


    /**
     * <p>Submit a workflow to the boss. The boss will parse the workflow
     * and generate the execution plan. A thread will be spawned for
     * each workflow.</p>
     *
     * <p>A workflow is a graph of workflow components and edges
     * Each key is the ID of a parent component
     * Each element has two lists of workflow components that are children of the parent component.
     * One list for parallel components, where different types of data (specified by the edge data type)
     * is passed from the source node to all of them at once. No user intervention is required once
     * the user triggers the "Next" operation from the source component.
     * The other list is for sequential components, where data is passed from the source node to each of the
     * component one by one. User intervention is required on the source node to trigger processing of each
     * component. This allows the user to make changes on the source node before passing data to following
     * components.</p>
     *
     * @param jsonWorkflow A workflow wrapped in JSON string. Here is a sample workflow json string:
     *         <p>
     *         {"type":"workflow","workflownodes":{"wfcid0_component_7":
     *         {"id":"wfcid0_component_7","wfnodeid":"344","name":"MeV","goosename":"Multiple Array
     *         Viewer","serviceuri":"C:\\MeV_4_8_1\\TMEV.bat","arguments":"-
     *         gaggle","subaction":"","datauri":"","componentid":"7"},"wfcid1_component_4":
     *         {"id":"wfcid1_component_4","wfnodeid":"343","name":"Cytoscape","goosename":"Cytoscape","serviceuri":
     *         "C:\\Program Files\\Cytoscape_v2.8.3\\Cytoscape.exe","arguments":"","subaction":"","datauri":"",
     *         "componentid":"4"}},
     *         "workflowedges":  {"sourceid_0":"wfcid0_component_7","targetid_0":"wfcid1_component_4",
     *         "datatype_0":"Data","datatypeid_0":"7","isparallel_0":"1"},
     *         "workflowid":"139","name":"MevCyto","desc":"From Mev to Cytoscape","userid":"1","startNode":""}
     *         </p>
     *         <p>
     *             The workflow JSON string consists of three sections. The first section includes information
     *             about the workflow such as its ID, name, and description. User can also specify a startnode
     *             of the workflow. The second section contains all the
     *             nodes in a workflow graph. This section is marked by the key "workflownodes". Finally, a section
     *             is dedicated to represent edges of the workflow graph. The section is marked with the
     *             "workflowedges" key. Note that all edges are directed in the graph.
     *         </p>
     * @throws java.rmi.RemoteException if RMI communication fails
     * @return <p>a JSON string that includes goose information (e.g. full execution path etc). The Boss detects the
     * execution path of each goose and pass them back to the web page to update the workflow. This saves the user
     * effort of manually input the execution path of each goose.</p>
     */
    public String submitWorkflow(Goose3 proxyGoose, String jsonWorkflow) throws RemoteException;

    /**
     * <p>Tell the boss to handle a workflow action.
     * The workflowAction can be either a Request or a Response.</p>
     *
     * <p>If it is a Request, and its option field is WorkflowReportData,
     * it contains data from the goose for generating the workflow report. Boss
     * pass the data to the server for storage.</p>
     *
     * <p>If it is a Response, it contains acknowledgement data passed from
     * the source goose to the target geese. The boss should store the
     * acknowledgement and inform each of the target goose to perform subsequent
     * actions of the workflow. Details can be found in {@link WorkflowAction}.</p>
     *
     * @param action A workflow action needs to be carried out.
     * @throws java.rmi.RemoteException if RMI communication fails
     */
    public void handleWorkflowAction(WorkflowAction action) throws RemoteException;

    /**
     * <p>Tell the boss to start recording a workflow. Each boss can only process one recording session.
     * Other requests will be rejected. Once recording starts, all the broadcast between geese are
     * captured and stored on the Boss. User can call {@link #terminateRecordingWorkflow(java.util.UUID)}
     * to terminate recording. Or, user can call {@link #pauseRecordingWorkflow(java.util.UUID)} to pause,
     * perform some activities, and call {@link #resumeRecordingWorkflow(java.util.UUID)} to resume recording.</p>

     * @throws RemoteException
     * @return a UUID if the recording is started successfully
     * null if the request is rejected.
     *
     */
    public UUID startRecordingWorkflow() throws RemoteException;

    /**
     * Tell the boss to stop recording a workflow
     * @param rid The UUID generated by startRecordingWorkflow.
     * @throws RemoteException
     * return a JSON string of the recorded workflow
     */
    public String terminateRecordingWorkflow(UUID rid) throws RemoteException;

    /**
     * Pause recording a workflow
     * @param rid The UUID generated by startRecordingWorkflow.
     * @throws RemoteException
     * Returns the currently recorded workflow
     */
    public String pauseRecordingWorkflow(UUID rid) throws RemoteException;

    /**
     * Resume recording a workflow
     * @param rid The UUID generated by startRecordingWorkflow.
     * @throws RemoteException
     */
    public void resumeRecordingWorkflow(UUID rid) throws RemoteException;

    /**
     * <p>Record a broadcast action. startRecordingWorkflow should be called before calling this API.
     * The API can be used to update source goose and target goose parameters
     * as well as edge parameters. </p>
     * @param sourceGoose the source goose of the broadcast action
     * @param targetGoose the target goose of the broadcast action
     * @param data the data broadcast by the source goose to the target goose
     * @param edgeIndex The index of the edge to be updated, -1 if it's a new edge
     * @param sourceParams The parameters for the source goose (e.g., serviceuri, subaction, etc). The definition of
     *                     parameters can be found in {@link org.systemsbiology.gaggle.core.datatypes.JSONConstants}.
                           Constants prefixed with "WORKFLOW_" are supported.
     * @param targetParams The parameters for the target goose (e.g., serviceuri, subaction, etc)
     * @param edgeParams Parameters for an edge between source and target geese. Supported parameters
     *                   include edge data type (Namelist, Tuple, Matrix, etc) and edge type (parallel or
     *                   sequential).
     */
    public void recordAction(String sourceGoose, String targetGoose, Object data,
                             int edgeIndex,
                             HashMap<String, String> sourceParams,
                             HashMap<String, String> targetParams,
                             HashMap<String, String> edgeParams
    ) throws RemoteException;


    /**
     * Save the current state. The Boss will contact all the currently opened geese.
     * As a result, all the tabs opened in Firefox and all the sessions of currently opened geese
     * will be saved to the server. User can reload the session from any machine as long as there
     * is internet connection.
     * @param proxyGoose  The goose to which the Boss passes back history and debug information.
     * @param userid  The ID of the user who will be able to save and reload the state.
     * @param name    The name of the saved session.
     * @param desc    The description of the saved session.
     * @param filePrefix  A String that will be used as the prefix of all the session files generated by the geese.
     *
     * Save session is an asynchronous call. The result is a JSON string that will be passed back to proxyGoose.
     * The resulting JSON object looks like the following:
     * @throws RemoteException
     */
    public void saveState(Goose3 proxyGoose, String userid, String name, String desc, String filePrefix) throws RemoteException;

    /**
     * Load a saved state. The Boss will download the state files of the target state from the server, automatically
     * start all the involved geese, and pass the state files to each of them. The geese will re-instate all the
     * files and reload the state.
     *
     * @param stateid   The ID of the saved state.
     * @param fileids   The IDs of the saved files.
     * @throws RemoteException
     */
    public void loadState(String stateid, String[] fileids) throws RemoteException;
}
