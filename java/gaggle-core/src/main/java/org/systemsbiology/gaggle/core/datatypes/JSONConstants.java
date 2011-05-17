package org.systemsbiology.gaggle.core.datatypes;

/**
 * Constants used by JSONReader and JSONWriter.
 * @author Wei-ju Wu
 */
public interface JSONConstants {

    // common
    String KEY_GAGGLE_DATA  = "gaggle-data";
    String KEY_METADATA     = "metadata";
    String KEY_NAME         = "name";
    String KEY_SPECIES      = "species";

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
}
