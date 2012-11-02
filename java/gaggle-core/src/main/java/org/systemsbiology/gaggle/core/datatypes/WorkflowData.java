package org.systemsbiology.gaggle.core.datatypes;

/**
 * Created with IntelliJ IDEA.
 * User: Ning Jiang
 * Date: 10/15/12
 * Time: 10:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowData implements GaggleData {
    private Object data;

    public String getName() { return "WorkflowData"; }
    public String getSpecies() { return ""; }
    public Tuple getMetadata() { return null; }
    public Object getData() { return data; }

    public WorkflowData(Object data)
    {
        this.data = data;
    }
}
