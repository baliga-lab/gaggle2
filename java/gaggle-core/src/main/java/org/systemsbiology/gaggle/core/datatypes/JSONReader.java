package org.systemsbiology.gaggle.core.datatypes;

import net.sf.json.*;
import java.util.*;
import java.io.Serializable;
import static org.systemsbiology.gaggle.core.datatypes.JSONConstants.*;

/**
 * This class reads Gaggle data structures in JSON format and turns
 * them into Java Gaggle data objects.
 * @author Wei-ju Wu
 */
public class JSONReader {

    public GaggleData createFromJSONString(String json) {
        JSONObject obj = JSONObject.fromObject(json);
        return json2GaggleData(obj);
    }

    public Workflow createWorkflowFromJSONString(String json) throws Exception
    {
        JSONObject jsonGaggleData = JSONObject.fromObject(json);
        return new Workflow(jsonGaggleData);
    }

    private GaggleData json2GaggleData(JSONObject jsonObj) {
        if (!isGaggleData(jsonObj)) {
            throw new IllegalArgumentException("JSON object does specify a Gaggle data structure");
        } else {
            return createGaggleData(jsonObj);
        }
    }

    private GaggleData createGaggleData(JSONObject jsonGaggleData) {
        String dataType = jsonGaggleData.getString(KEY_TYPE);
        if (TYPE_NAMELIST.equals(dataType)) {
            return extractNamelist(jsonGaggleData);
        } else if (TYPE_TUPLE.equals(dataType)) {
            String subtype = jsonGaggleData.getString(KEY_SUBTYPE);
            return TYPE_BICLUSTER.equals(subtype) ?
                extractCluster(jsonGaggleData) :
                extractGaggleTuple(jsonGaggleData);
        } else if (TYPE_MATRIX.equals(dataType)) {
            return extractMatrix(jsonGaggleData);
        } else if (TYPE_TABLE.equals(dataType)) {
            return extractTable(jsonGaggleData);
        } else if (TYPE_NETWORK.equals(dataType)) {
            return extractNetwork(jsonGaggleData);
        } else {
            throw new UnsupportedOperationException("unsupported type: " + dataType);
        }
    }

    private Namelist extractNamelist(JSONObject jsonGaggleData) {
        Namelist namelist = new Namelist();
        List<String> names = new ArrayList<String>();
        namelist.setName(extractName(jsonGaggleData));
        namelist.setSpecies(extractSpecies(jsonGaggleData));
        namelist.setMetadata(extractMetadata(jsonGaggleData));
        JSONArray nameArray = jsonGaggleData.getJSONArray(KEY_GAGGLE_DATA);
        for (int i = 0; i < nameArray.size(); i++) {
            names.add(nameArray.getString(i));
        }
        namelist.setNames(names.toArray(new String[0]));
        return namelist;
    }

    private GaggleTuple extractGaggleTuple(JSONObject jsonGaggleData) {
        GaggleTuple gaggleTuple = new GaggleTuple();
        gaggleTuple.setName(extractName(jsonGaggleData));
        gaggleTuple.setSpecies(extractSpecies(jsonGaggleData));
        gaggleTuple.setMetadata(extractMetadata(jsonGaggleData));
        Tuple tuple = createTuple(jsonGaggleData.getJSONObject(KEY_GAGGLE_DATA)
                                  .getJSONObject(KEY_VALUES));
        gaggleTuple.setData(tuple);
        return gaggleTuple;
    }

    private Cluster extractCluster(JSONObject jsonGaggleData) {
        Cluster cluster = new Cluster();
        cluster.setName(extractName(jsonGaggleData));
        cluster.setSpecies(extractSpecies(jsonGaggleData));
        cluster.setMetadata(extractMetadata(jsonGaggleData));

        JSONObject data = jsonGaggleData.getJSONObject(KEY_GAGGLE_DATA);
        Namelist rowNames = (Namelist) json2GaggleData(data.getJSONObject(KEY_GENES));
        Namelist colNames = (Namelist) json2GaggleData(data.getJSONObject(KEY_CONDITIONS));
        cluster.setRowNames(rowNames.getNames());
        cluster.setColumnNames(colNames.getNames());
        return cluster;
    }

    private boolean isGaggleData(JSONObject jsonObj) {
        return (jsonObj.containsKey(KEY_GAGGLE_DATA) || jsonObj.containsKey(KEY_WORKFLOW_NODES));
    }

    private Tuple createTuple(JSONObject jsonTuple) { return createTuple(jsonTuple, null); }
    private Tuple createTuple(JSONObject jsonTuple, String exceptKey) {
        Tuple tuple = new Tuple(); // TODO: What about named tuples ?
        for (Object entry : jsonTuple.entrySet()) {
            Map.Entry<String, Object> mapEntry =
                (Map.Entry<String, Object>) entry;
            if (exceptKey == null || !exceptKey.equals(mapEntry.getKey())) {
                tuple.addSingle(createSingle(mapEntry.getKey(), mapEntry.getValue()));
            }
        }
        return tuple;
    }

    private Single createSingle(String name, Object jsonValue) {
        Single single = new Single();
        single.setName(name);
        if (jsonValue instanceof JSONObject) {
            single.setValue(json2GaggleData((JSONObject) jsonValue));
        } else {
            single.setValue((Serializable) jsonValue);
        }
        return single;
    }

    private Table extractTable(JSONObject jsonGaggleData) {
        Table table = new Table(extractName(jsonGaggleData),
                                extractSpecies(jsonGaggleData),
                                extractMetadata(jsonGaggleData),
                                extractTableColumns(jsonGaggleData));
        return table;
    }
    private Table.TableColumn[] extractTableColumns(JSONObject jsonGaggleData) {
        JSONArray jsonColumns = jsonGaggleData.getJSONObject(KEY_GAGGLE_DATA)
            .getJSONArray(KEY_COLUMNS);
        Table.TableColumn[] result = new Table.TableColumn[jsonColumns.size()];
        for (int col = 0; col < result.length; col++) {
            result[col] = extractTableColumn(jsonColumns.getJSONObject(col));
        }
        return result;
    }
    private Table.TableColumn extractTableColumn(JSONObject jsonTableColumn) {
        String jsonType = jsonTableColumn.getString(KEY_TYPE);
        JSONArray jsonValues = jsonTableColumn.getJSONArray(KEY_VALUES);
        if ("string".equals(jsonType)) {
            String[] values = new String[jsonValues.size()];
            for (int i = 0; i < jsonValues.size(); i++) values[i] = jsonValues.getString(i);
            return Table.createStringColumn(jsonTableColumn.getString(KEY_NAME),
                                            values);
        } else if ("double".equals(jsonType)) {
            double[] values = new double[jsonValues.size()];
            for (int i = 0; i < jsonValues.size(); i++) values[i] = jsonValues.getDouble(i);
            return Table.createDoubleColumn(jsonTableColumn.getString(KEY_NAME),
                                            values);
        } else if ("int".equals(jsonType)) {
            int[] values = new int[jsonValues.size()];
            for (int i = 0; i < jsonValues.size(); i++) values[i] = jsonValues.getInt(i);
            return Table.createIntColumn(jsonTableColumn.getString(KEY_NAME),
                                         values);
        } else if ("boolean".equals(jsonType)) {
            boolean[] values = new boolean[jsonValues.size()];
            for (int i = 0; i < jsonValues.size(); i++) values[i] = jsonValues.getBoolean(i);
            return Table.createBooleanColumn(jsonTableColumn.getString(KEY_NAME),
                                             values);
        }
        throw new IllegalArgumentException("unsupported type: " + jsonType);
    }

    private DataMatrix extractMatrix(JSONObject jsonGaggleData) {
        DataMatrix matrix = new DataMatrix();
        matrix.setName(extractName(jsonGaggleData));
        matrix.setSpecies(extractSpecies(jsonGaggleData));
        matrix.setMetadata(extractMetadata(jsonGaggleData));
        
        JSONObject jsonMatrix = jsonGaggleData.getJSONObject(KEY_GAGGLE_DATA);
        setRowTitles(matrix, jsonMatrix);
        setColumnsAndValues(matrix, jsonMatrix);
        return matrix;
    }

    private void setRowTitles(DataMatrix matrix, JSONObject jsonMatrix) {
        JSONArray jsonRowNames = jsonMatrix.getJSONArray(KEY_ROW_NAMES);
        String[] rowTitles = new String[jsonRowNames.size()];
        for (int i = 0; i < rowTitles.length; i++) {
            rowTitles[i] = jsonRowNames.getString(i);
        }
        matrix.setRowTitles(rowTitles);
    }
    private void setColumnsAndValues(DataMatrix matrix, JSONObject jsonMatrix) {
        JSONArray jsonColumns  = jsonMatrix.getJSONArray(KEY_COLUMNS);
        String[] columnTitles = new String[jsonColumns.size()];
        matrix.setSize(matrix.getRowTitles().length, columnTitles.length);
        for (int col = 0; col < jsonColumns.size(); col++) {
            columnTitles[col] = jsonColumns.getJSONObject(col).getString(KEY_NAME);
            JSONArray jsonValues = jsonColumns.getJSONObject(col).getJSONArray(KEY_VALUES);
            for (int row = 0; row < jsonValues.size(); row++) {
                matrix.set(row, col, jsonValues.getDouble(row));
            }
        }
        matrix.setColumnTitles(columnTitles);
    }

    private Network extractNetwork(JSONObject jsonGaggleData) {
        JSONObject jsonNetwork = jsonGaggleData.getJSONObject(KEY_GAGGLE_DATA);
        Network network = new Network();
        network.setName(extractName(jsonGaggleData));
        network.setSpecies(extractSpecies(jsonGaggleData));
        network.setMetadata(extractMetadata(jsonGaggleData));
        extractAndSetNodes(network, jsonNetwork);
        extractAndSetEdges(network, jsonNetwork);
        return network;
    }

    private void extractAndSetNodes(Network network, JSONObject jsonNetwork) {
        JSONArray jsonNodes = jsonNetwork.getJSONArray(KEY_NODES);
        for (int i = 0; i < jsonNodes.size(); i++) {
            JSONObject jsonNode = jsonNodes.getJSONObject(i);
            network.add(jsonNode.getString(KEY_NODE));
            extractAndSetNodeAttributes(network, jsonNode);
        }
    }

    private void extractAndSetNodeAttributes(Network network, JSONObject jsonNode) {
        JSONObject attribs = jsonNode.getJSONObject(KEY_ATTRIBUTES);
        String nodeName = jsonNode.getString(KEY_NODE);
        for (Object entry : attribs.entrySet()) {
            Map.Entry<String, Object> mapEntry =
                (Map.Entry<String, Object>) entry;
            Object value = mapEntry.getValue();
            validateSimpleValue(value);
            network.addNodeAttribute(nodeName, mapEntry.getKey(), value);
        }
    }

    private void validateSimpleValue(Object value) {
        if (value instanceof JSONArray || value instanceof JSONObject) {
            throw new UnsupportedOperationException("only simple values supported for node attributes");
        }
    }

    private void extractAndSetEdges(Network network, JSONObject jsonNetwork) {
        JSONArray jsonEdges = jsonNetwork.getJSONArray(KEY_EDGES);
        for (int i = 0; i < jsonEdges.size(); i++) {
            JSONObject jsonEdge = jsonEdges.getJSONObject(i);
            boolean directed = false;
            if (jsonEdge.containsKey(KEY_DIRECTED)) directed = jsonEdge.getBoolean(KEY_DIRECTED);
            Interaction interaction = new Interaction(jsonEdge.getString(KEY_SOURCE),
                                                      jsonEdge.getString(KEY_TARGET),
                                                      jsonEdge.getString(KEY_INTERACTION),
                                                      directed);
            network.add(interaction);
            extractAndSetEdgeAttributes(network, interaction, jsonEdge);
        }
    }
    
    private void extractAndSetEdgeAttributes(Network network,
                                             Interaction interaction,
                                             JSONObject jsonEdge) {
        JSONObject attribs = jsonEdge.getJSONObject(KEY_ATTRIBUTES);
        String edgeName = interaction.toString();

        for (Object entry : attribs.entrySet()) {
            Map.Entry<String, Object> mapEntry =
                (Map.Entry<String, Object>) entry;
            Object value = mapEntry.getValue();
            validateSimpleValue(value);
            network.addEdgeAttribute(edgeName, mapEntry.getKey(), value);
        }
    }

    private String extractName(JSONObject jsonGaggleData) {
        return jsonGaggleData.getString(KEY_NAME);
    }

    private Tuple extractMetadata(JSONObject jsonGaggleData) {
        return createTuple(jsonGaggleData.getJSONObject(KEY_METADATA), "species");
    }

    private String extractSpecies(JSONObject jsonGaggleData) {
        if (hasSpecies(jsonGaggleData)) {   
            return jsonGaggleData.getJSONObject(KEY_METADATA).getString(KEY_SPECIES);
        }
        return null;
    }
    private boolean hasMetadata(JSONObject jsonGaggleData) {
        return jsonGaggleData.containsKey(KEY_METADATA);
    }
    private JSONObject getJSONGaggleMetadata(JSONObject jsonGaggleData) {
        return jsonGaggleData.getJSONObject(KEY_METADATA);
    }

    private boolean hasSpecies(JSONObject jsonGaggleData) {
        return hasMetadata(jsonGaggleData) &&
            getJSONGaggleMetadata(jsonGaggleData).containsKey(KEY_SPECIES);
    }
}