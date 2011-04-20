package org.systemsbiology.gaggle.core.datatypes;

import net.sf.json.*;
import java.util.*;
import java.io.Writer;
import java.io.Serializable;
import static org.systemsbiology.gaggle.core.datatypes.JSONConstants.*;

/**
 * This class writes Gaggle data structures to a stream in JSON format.
 * @author Wei-ju Wu
 */
public class JSONWriter {
    private Writer writer;

    public JSONWriter(Writer writer) { this.writer = writer; }
    public void write(Namelist namelist) {
        JSONObject jsonGaggleData = writeCommon(namelist);
        JSONArray names = new JSONArray();
        for (String name : namelist.getNames()) names.add(name);
        writeToWriter(jsonGaggleData.element("namelist", names));
        
    }
    public void write(GaggleTuple gaggleTuple) {
        JSONObject jsonGaggleData = writeCommon(gaggleTuple);
        List<Single> singleList = gaggleTuple.getData().getSingleList();
        JSONObject jsonTuple = new JSONObject();
        for (Single single : singleList) {
            jsonTuple = jsonTuple.element(single.getName(),
                                          single.getValue());
        }
        writeToWriter(jsonGaggleData.element(KEY_TUPLE, jsonTuple));
    }

    public void write(Cluster cluster) {
        JSONObject jsonGaggleData = writeCommon(cluster);
        JSONArray rowNames = new JSONArray();
        JSONArray columnNames = new JSONArray();
        for (String name : cluster.getRowNames()) rowNames.add(name);
        for (String name : cluster.getColumnNames()) columnNames.add(name);
        JSONObject jsonCluster = new JSONObject().element(KEY_TYPE,
                                                          TYPE_BICLUSTER);
        jsonCluster = jsonCluster.element(KEY_ROW_NAMES, rowNames);
        jsonCluster = jsonCluster.element(KEY_COLUMN_NAMES, columnNames);
        writeToWriter(jsonGaggleData.element(KEY_TUPLE, jsonCluster));
    }

    public void write(DataMatrix matrix) {
        JSONObject jsonGaggleData = writeCommon(matrix);
        JSONArray rowNames = new JSONArray();
        for (String name : matrix.getRowTitles()) rowNames.add(name);
        JSONArray columns  = new JSONArray();
        for (int col = 0; col < matrix.getColumnCount(); col++) {
            JSONArray columnValues = new JSONArray();
            for (double value : matrix.getColumn(col)) columnValues.add(value);
            columns.add(new JSONObject()
                        .element(KEY_NAME, matrix.getColumnTitles()[col])
                        .element(KEY_VALUES, columnValues));
        }
        JSONObject jsonMatrix = new JSONObject()
            .element(KEY_ROW_NAMES, rowNames)
            .element(KEY_COLUMNS, columns);
        writeToWriter(jsonGaggleData.element(KEY_MATRIX, jsonMatrix));
    }

    public void write(Table table) {
        JSONObject jsonGaggleData = writeCommon(table);
        JSONArray jsonColumns = new JSONArray();
        for (int col = 0; col < table.getColumnCount(); col++) {
            jsonColumns.add(new JSONObject()
                            .element(KEY_NAME, table.getColumnName(col))
                            .element(KEY_TYPE, typeLabel(table.getColumnClass(col)))
                            .element(KEY_VALUES, columnValues(table, col)));
        }
        JSONObject jsonTable = new JSONObject().element(KEY_COLUMNS, jsonColumns);
        writeToWriter(jsonGaggleData.element(KEY_TABLE, jsonTable));
    }

    private String typeLabel(Class aClass) {
        if (aClass == int.class) return "int";
        else if (aClass == double.class) return "float";
        else if (aClass == boolean.class) return "boolean";
        else if (aClass == String.class) return "string";
        return "???";
    }

    private JSONArray columnValues(Table table, int column) {
        JSONArray values = new JSONArray();
        Class columnClass = table.getColumnClass(column);
        for (int row = 0; row < table.getRowCount(); row++) {
            if (columnClass == int.class) {
                values.add(table.intValueAt(row, column));
            } else if (columnClass == double.class) {
                values.add(table.doubleValueAt(row, column));
            } else if (columnClass == boolean.class) {
                values.add(table.booleanValueAt(row, column));
            } else if (columnClass == String.class) {
                values.add(table.stringValueAt(row, column));
            } else {
                throw new UnsupportedOperationException("Unsupported column class: " + columnClass);
            }
        }
        return values;        
    }

    public void write(Network network) {
        JSONObject jsonGaggleData = writeCommon(network);
        writeToWriter(jsonGaggleData
                      .element(KEY_NETWORK, new JSONObject()
                               .element(KEY_NODES, networkNodes2JSON(network))
                               .element(KEY_EDGES, networkEdges2JSON(network))));
    }

    private JSONArray networkNodes2JSON(final Network network) {
        JSONArray jsonNodes = new JSONArray();
        String[] nodes = network.getNodes();
        String[] attributeNames = network.getNodeAttributeNames();

        for (int i = 0; i < network.nodeCount(); i++) {
            JSONObject attributes = getAttributes(nodes[i], attributeNames,
                                                  new AttributeMapGetter() {
                                                      public Map<String, Object> getAttributeMap(String attr) {
                                                          return network.getNodeAttributes(attr);
                                                      }
                                                  });
            JSONObject jsonNode = new JSONObject()
                .element(KEY_NODE, nodes[i])
                .element(KEY_ATTRIBUTES, attributes);
            jsonNodes.add(jsonNode);
        }
        return jsonNodes;
    }

    interface AttributeMapGetter {
        Map<String, Object> getAttributeMap(String attr);
    }

    private JSONObject getAttributes(String key, String[] attributeNames, AttributeMapGetter getter) {
        JSONObject attributes = new JSONObject();
        for (String attr : attributeNames) {
            Map<String, Object> attributeMap = getter.getAttributeMap(attr);
            if (attributeMap.containsKey(key)) {
                attributes = attributes.element(attr, attributeMap.get(key));
            }
        }
        return attributes;
    }

    private JSONArray networkEdges2JSON(final Network network) {
        JSONArray jsonEdges = new JSONArray();
        Interaction[] interactions = network.getInteractions();
        String[] attributeNames = network.getEdgeAttributeNames();

        for (int i = 0; i < interactions.length; i++) {
            JSONObject attributes = getAttributes(interactions[i].toString(), attributeNames,
                                                  new AttributeMapGetter() {
                                                      public Map<String, Object> getAttributeMap(String attr) {
                                                          return network.getEdgeAttributes(attr);
                                                      }
                                                  });

            jsonEdges.add(new JSONObject()
                .element(KEY_SOURCE, interactions[i].getSource())
                .element(KEY_TARGET, interactions[i].getTarget())
                .element(KEY_INTERACTION, interactions[i].getType())
                .element(KEY_DIRECTED, interactions[i].isDirected())
                          .element(KEY_ATTRIBUTES, attributes));
        }
        return jsonEdges;
    }

    private JSONObject writeCommon(GaggleData gaggleData) {
        JSONObject metadata = new JSONObject()
            .element(KEY_SPECIES, gaggleData.getSpecies());
        if (gaggleData.getMetadata() != null) {
            for (Single single : gaggleData.getMetadata().getSingleList()) {
                metadata = metadata.element(single.getName(), jsonify(single.getValue()));
            }
        }
        return new JSONObject()
            .element(KEY_NAME, gaggleData.getName())
            .element(KEY_METADATA, metadata);
    }

    private Object jsonify(Object value) {
        return value;
    }

    private void writeToWriter(JSONObject jsonGaggleData) {
        new JSONObject()
            .element(KEY_GAGGLE_DATA, jsonGaggleData)
            .write(writer);
    }
}