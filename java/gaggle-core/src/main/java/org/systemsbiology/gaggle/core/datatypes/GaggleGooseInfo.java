package org.systemsbiology.gaggle.core.datatypes;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Ning Jiang
 * Date: 4/10/13
 * Time: 11:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class GaggleGooseInfo implements Serializable {
    WorkflowComponent workflowComponent;

    public GaggleGooseInfo(WorkflowComponent component)
    {
        this.workflowComponent = new WorkflowComponent(component);
    }

    public String getComponentWorkflowNodeID() { return workflowComponent.getComponentWorkflowNodeID(); }
    public String getWorkflowComponentID() { return workflowComponent.getComponentID(); }
    public String getWorkflowIndex() { return workflowComponent.getWorkflowIndex(); }
}
