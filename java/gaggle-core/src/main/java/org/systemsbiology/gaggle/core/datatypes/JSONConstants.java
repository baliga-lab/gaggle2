package org.systemsbiology.gaggle.core.datatypes;

/**
 * Constants used by JSONReader and JSONWriter.
 * @author Wei-ju Wu
 */
public interface JSONConstants {

    // common
    String KEY_GAGGLE_DATA     = "gaggle-data";
    String KEY_METADATA        = "metadata";
    String KEY_NAME            = "name";
    String KEY_SPECIES         = "species";
    String KEY_WORKFLOW_NODES  = "workflownodes";
    String KEY_WORKFLOW_EDGES  = "workflowedges";


    // data types
    String KEY_TYPE         = "type";
    String KEY_SUBTYPE      = "subtype";

    // bicluster specific
    String KEY_GENES        = "genes";
    String KEY_CONDITIONS   = "conditions";

    // table/matrix specific
    String KEY_ROW_NAMES    = "row names";
    String KEY_COLUMNS      = "columns";
    String KEY_COLUMN_NAMES = "column-names";
    String KEY_VALUES       = "values";

    // network specific
    String KEY_ATTRIBUTES   = "attributes";
    String KEY_EDGES        = "edges";
    String KEY_DIRECTED     = "directed";
    String KEY_INTERACTION  = "interaction";
    String KEY_NODES        = "nodes";
    String KEY_NODE         = "node";
    String KEY_SOURCE       = "source";
    String KEY_TARGET       = "target";

    String TYPE_BICLUSTER   = "bicluster";
    String TYPE_MATRIX       = "matrix";
    String TYPE_NAMELIST     = "namelist";
    String TYPE_NETWORK      = "network";
    String TYPE_TABLE        = "table";
    String TYPE_TUPLE        = "tuple";
    String TYPE_WORKFLOW     = "workflow";
    String TYPE_ANY          = "Any Data";

    String WORKFLOW_NAME = "name";
    String WORKFLOW_COMPONENT_SERVICEURI = "serviceuri";
    String WORKFLOW_COMPONENT_DATAURI = "datauri";
    String WORKFLOW_COMPONENT_SUBACTION = "subaction";
    String WORKFLOW_COMPONENT_ID = "componentid";
    String WORKDLOW_COMPONENT_ARGUMENTS = "arguments";
    String WORKFLOW_ID = "workflowid";
    String WORKFLOW_RECORDING_FILE = "workflowrecordingfile";
    String WORKFLOW_EDGE_DATATYPE = "datatype";
    String WORKFLOW_EDGE_PARALLELTYPE = "paralleltype";
    String WORKFLOW_EDGE_SOURCEID = "sourcenodeid";
    String WORKFLOW_EDGE_TARGETID = "targetnodeid";
    String WORKFLOW_EDGE_STARTNODE = "startnode";
    String WORKFLOW_EMPTY_ARGUMENT = "NONE";
    String WORKFLOW_RESET = "reset";
    String WORKFLOW_ORGANISMINFO = "organism";
    String WORKFLOW_USERID = "userid";
}
