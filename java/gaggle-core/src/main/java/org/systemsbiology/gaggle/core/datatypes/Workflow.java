package org.systemsbiology.gaggle.core.datatypes;

import net.sf.ezmorph.test.ArrayAssertions;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.commons.collections.iterators.ObjectArrayIterator;
import org.apache.commons.collections.map.HashedMap;

import java.io.Serializable;
import java.util.*;

import static org.systemsbiology.gaggle.core.datatypes.JSONConstants.KEY_GAGGLE_DATA;

/**
 * Created with IntelliJ IDEA.
 * User: Ning Jiang
 * Date: 10/12/12
 * Time: 9:39 AM
 * A workflow is a graph of workflow components and edges
 * Each key is the ID of a parent component
 * Each element has two lists of workflow components that are children of the parent component.
 * One list for parallel components, where data is passed from the source node to all of them at once.
 * No user intervention is required once the user triggers the "Next" operation from the source component.
 * The other list is for sequential components, where data is passed from the source node to each of the
 * component one by one. User intervention is required on the source node to trigger processing of each
 * component. This allows the user to make changes on the source node before passing data to following
 * components.
 *
 *
 * The first element of the parallel list is the source component
 * Input data is stored in param[0] of the parent component.
 * For its children, the edge type (type of data passed from source to a child)
 * is stored in their param[0] fields.
 */
public class Workflow implements Serializable, GaggleData {

    protected HashMap<String, ArrayList<ArrayList<WorkflowComponent>>> workflowMap = null;
    // There can be multiple starting nodes (nodes with 0 in-degree, assuming there is no cycle!)
    protected String[] startNodeIDs;

    public Workflow(JSONObject jsonWorkflow)
    {
        System.out.println();
        System.out.println("Instantiating workflow object...");
        workflowMap = new HashMap<String, ArrayList<ArrayList<WorkflowComponent>>>();

        // Parse the json object into the hashmap
        // For now we hard code it
        try
        {
            JSONObject workflowJSONObj = jsonWorkflow.getJSONObject(KEY_WORKFLOW);
            int i = 0;
            while (true)
            {
                String sourceidkey = "sourceid_" + i.toString();

                if (workflowMap.containsKey(sourceidkey))
                {
                    // Finished parsing
                    break;
                }

                if (!workflowMap.containsKey(sourceid))
                {

                }
            }
        }
        catch (Exception e)
        {

        }


        HashMap paras = new HashMap();
        //paras[0] = "http://baliga.systemsbiology.net/cmonkey/enigma/cmonkey_4.8.2_dvu_3491x739_11_Mar_02_17:37:51/htmls/cluster0001.html";
        paras.put(WorkflowComponent.ParamNames.Data.getValue(), "file://C/Users/Ning Jiang/Downloads/inf.cys");
        WorkflowComponent c1 = new WorkflowComponent("wf_1_component_1",
                "Cytoscape",
                "2.8.1",
                "C:\\\\Program Files\\\\Cytoscape_v2.8.3\\\\Cytoscape.exe",
                paras);

        HashMap paras1  = new HashMap();
        paras1.put(WorkflowComponent.ParamNames.EdgeType.getValue(), WorkflowAction.DataType.DataMatrix);
        WorkflowComponent c2 = new WorkflowComponent("wf_1_component_2",
                "Firegoose",
                "2.0",
                "C:\\Program Files (x86)\\\\Mozilla Firefox\\\\firefox.exe",
                paras1);

        HashMap paras2  = new HashMap();
        paras2.put(WorkflowComponent.ParamNames.EdgeType.getValue(), WorkflowAction.DataType.DataMatrix);
        paras2.put(WorkflowComponent.ParamNames.SubTarget.getValue(), "KEGG Pathway");
        WorkflowComponent c3 = new WorkflowComponent("wf_1_component_3",
                "Firegoose",
                "2.0",
                "C:\\Program Files (x86)\\\\Mozilla Firefox\\\\firefox.exe",
                paras2);

        HashMap paras3  = new HashMap();
        paras3.put(WorkflowComponent.ParamNames.EdgeType.getValue(), WorkflowAction.DataType.DataMatrix);
        paras3.put(WorkflowComponent.ParamNames.SubTarget.getValue(), "DAVID");
        WorkflowComponent c4 = new WorkflowComponent("wf_1_component_4",
                "Firegoose",
                "2.0",
                "C:\\Program Files (x86)\\\\Mozilla Firefox\\\\firefox.exe",
                paras3);

        ArrayList<ArrayList<WorkflowComponent>> componentarrays1 = new ArrayList<ArrayList<WorkflowComponent>>();
        ArrayList<WorkflowComponent> parallelarray = new ArrayList<WorkflowComponent>(); // parallel children
        ArrayList<WorkflowComponent> sequentialarray = new ArrayList<WorkflowComponent>();
        parallelarray.add(c1);
        parallelarray.add(c2);
        componentarrays1.add(parallelarray);
        componentarrays1.add(sequentialarray);

        ArrayList<ArrayList<WorkflowComponent>> componentarrays2 = new ArrayList<ArrayList<WorkflowComponent>>();
        parallelarray = new ArrayList<WorkflowComponent>(); // parallel children
        sequentialarray = new ArrayList<WorkflowComponent>();
        parallelarray.add(c2);
        parallelarray.add(c3);
        parallelarray.add(c4);
        componentarrays2.add(parallelarray);
        componentarrays2.add(sequentialarray);

        ArrayList<ArrayList<WorkflowComponent>> componentarrays3 = new ArrayList<ArrayList<WorkflowComponent>>();
        parallelarray = new ArrayList<WorkflowComponent>(); // parallel children
        sequentialarray = new ArrayList<WorkflowComponent>();
        parallelarray.add(c3);
        componentarrays3.add(parallelarray);
        componentarrays3.add(sequentialarray);

        ArrayList<ArrayList<WorkflowComponent>> componentarrays4 = new ArrayList<ArrayList<WorkflowComponent>>();
        parallelarray = new ArrayList<WorkflowComponent>(); // parallel children
        sequentialarray = new ArrayList<WorkflowComponent>();
        parallelarray.add(c4);
        componentarrays4.add(parallelarray);
        componentarrays4.add(sequentialarray);

        this.startNodeIDs = new String[]{"wf_1_component_1"};
        workflowMap.put("wf_1_component_1", componentarrays1);
        workflowMap.put("wf_1_component_2", componentarrays2);
        workflowMap.put("wf_1_component_3", componentarrays3);
        workflowMap.put("wf_1_component_4", componentarrays4);
    }

    public String getName() { return "Workflow"; }
    public String getSpecies() { return ""; }
    public Tuple getMetadata() { return null; }
    public String[] getStartNodeIDs() { return startNodeIDs; }
    public HashMap<String, ArrayList<ArrayList<WorkflowComponent>>> getWorkflow() { return workflowMap; }
}
