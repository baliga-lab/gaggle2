package org.systemsbiology.gaggle.core.datatypes;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.List;

public class JSONReaderTest {
    private static final double EPS = 0.001;

    @Test public void testNonGaggleJson() {
        String json = "{ \"foo\": \"bar\"}";
        try {
            new JSONReader().createFromJSONString(json);
            fail("Providing a non-Gaggle JSON object should throw an exception");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
    }

    @Test public void testNamelist() {
        String json = "{" +
            "\"name\":\"RNA polymerase genes\"," +
            "\"version\":\"0.1\"," +
            "\"type\":\"namelist\"," +
            "\"metadata\":{" +
            "  \"species\":\"Halobacterium salinarium NRC-1\"" +
            "}," +
            "\"gaggle-data\":[\"VNG2662G\",\"VNG2664G\"]" +
            "}";
        Namelist namelist = (Namelist) (new JSONReader().createFromJSONString(json));
        assertEquals("RNA polymerase genes", namelist.getName());
        assertEquals("Halobacterium salinarium NRC-1", namelist.getSpecies());
        assertEquals(2, namelist.getNames().length);
        assertEquals("VNG2662G", namelist.getNames()[0]);
        assertEquals("VNG2664G", namelist.getNames()[1]);
        /*
        Tuple metadata = namelist.getMetadata();
        List<Single> singleList = metadata.getSingleList();
        assertEquals(2, singleList.size());
        assertEquals("md1", singleList.get(0).getName());
        assertEquals("md2", singleList.get(1).getName());
        */
    }

    @Test public void testTuple() {
        String json = "{" +
            "\"name\":\"RNA polymerase expression\"," +
            "\"version\":\"0.1\"," +
            "\"type\":\"tuple\"," +
            "\"subtype\":\"cytoscape node attributes\"," +
            "\"metadata\":{" +
            "  \"species\":\"Halobacterium salinarium NRC-1\"" +
            "}," +
            "\"gaggle-data\":{" +
            "\"attribute-name\":\"gene expression\"," +
            "\"values\":{" +
            "  \"astring\":\"value\"," +
            "  \"adouble\":1.23," +
            "  \"anint\":1," +
            "  \"abool\":true," +
            "  \"anamelist\":{\"name\":\"nl\",\"type\":\"namelist\",\"gaggle-data\":[\"name1\"]}," +
            "}}}";
        GaggleTuple gaggleTuple =
            (GaggleTuple) (new JSONReader().createFromJSONString(json));
        assertEquals("RNA polymerase expression", gaggleTuple.getName());
        assertEquals("Halobacterium salinarium NRC-1", gaggleTuple.getSpecies());
        Tuple t = gaggleTuple.getData();
        assertEquals("value", getNamedSingle(t, "astring").getValue());
        assertEquals(new Double(1.23), getNamedSingle(t, "adouble").getValue());
        assertEquals(new Integer(1), getNamedSingle(t, "anint").getValue());
        assertEquals(Boolean.TRUE, getNamedSingle(t, "abool").getValue());
        Namelist namelist = (Namelist) getNamedSingle(t, "anamelist").getValue();
        assertEquals("nl", namelist.getName());
        assertEquals(1, namelist.getNames().length);
        assertEquals("name1", namelist.getNames()[0]);
    }
    @Test public void testCluster() {
        String json = "{" +
            "\"name\":\"RNA polymerase bicluster\"," +
            "\"version\":\"0.1\"," +
            "\"type\":\"tuple\"," +
            "\"subtype\":\"bicluster\"," +
            "\"metadata\":{" +
            "  \"species\":\"Halobacterium salinarum NRC-1\"" +
            "}," +
            "\"gaggle-data\":{" +
            "\"genes\":{" +
            "\"name\":\"RNA polymerase genes\"," +
            "\"type\":\"namelist\"," +
            "\"gaggle-data\":[\"VNG2662G\",\"VNG2664G\"]" +
            "}," +
            "\"conditions\":{" +
            "\"name\":\"special conditions\"," +
            "\"type\":\"namelist\"," +
            "\"gaggle-data\":[\"condition1\",\"condition2\"]" +
            "}}}";

        Cluster cluster =
            (Cluster) (new JSONReader().createFromJSONString(json));
        assertEquals("RNA polymerase bicluster", cluster.getName());
        assertEquals("Halobacterium salinarum NRC-1", cluster.getSpecies());
        assertEquals(2, cluster.getRowNames().length);
        assertEquals("VNG2662G", cluster.getRowNames()[0]);
        assertEquals("VNG2664G", cluster.getRowNames()[1]);
        assertEquals(2, cluster.getColumnNames().length);
        assertEquals("condition1", cluster.getColumnNames()[0]);
        assertEquals("condition2", cluster.getColumnNames()[1]);
    }

    @Test public void testMatrix() {
        String json = "{" +
            "\"name\":\"Expression RNA polymerase genes under 3 conditions\"," +
            "\"version\":\"0.1\"," +
            "\"type\":\"matrix\"," +
            "\"metadata\":{" +
            "\"species\":\"Halobacterium salinarum NRC-1\"" +
            "}," +
            "\"gaggle-data\":{" +
            "\"row names\":[\"VNG2662G\", \"VNG2664G\"]," +
            "\"columns\":[{\"name\":\"condition1\",\"values\":[1.2, 3.4]}," +
            "{\"name\":\"condition2\",\"values\":[2.3, 4.5]},]}}";

        DataMatrix matrix =
            (DataMatrix) (new JSONReader().createFromJSONString(json));
        assertEquals("Expression RNA polymerase genes under 3 conditions", matrix.getName());
        assertEquals("Halobacterium salinarum NRC-1", matrix.getSpecies());
        assertEquals(2, matrix.getRowTitles().length);
        assertEquals("VNG2662G", matrix.getRowTitles()[0]);
        assertEquals("VNG2664G", matrix.getRowTitles()[1]);
        assertEquals(2, matrix.getColumnTitles().length);
        assertEquals("condition1", matrix.getColumnTitles()[0]);
        assertEquals("condition2", matrix.getColumnTitles()[1]);
        assertEquals(2, matrix.getRowCount());
        assertEquals(2, matrix.getColumnCount());
        assertEquals(1.2, matrix.get(0, 0), EPS);
        assertEquals(2.3, matrix.get(0, 1), EPS);
        assertEquals(3.4, matrix.get(1, 0), EPS);
        assertEquals(4.5, matrix.get(1, 1), EPS);
    }

    @Test public void testTable() {
        String json = "{" +
            "\"name\":\"table-name\"," +
            "\"version\":\"0.1\"," +
            "\"type\":\"table\"," +
            "\"metadata\":{" +
            "  \"species\": \"Halo world\"" +
            "}," +
            "\"gaggle-data\": {" +
            "  \"columns\": [" +
            "      { \"name\": \"col1\", \"type\": \"double\",  \"values\": [1.2, 3.4] }," +
            "      { \"name\": \"col2\", \"type\": \"int\",     \"values\": [2, 4] }," +
            "      { \"name\": \"col3\", \"type\": \"boolean\", \"values\": [true, false] }," +
            "      { \"name\": \"col4\", \"type\": \"string\",  \"values\": [\"foo\", \"bar\"] }" +
            "  ]}" +
            "}";
        
        Table table =
            (Table) (new JSONReader().createFromJSONString(json));
        assertEquals("table-name", table.getName());
        assertEquals("Halo world", table.getSpecies());
        assertEquals(4, table.getColumnCount());
        assertEquals(2, table.getRowCount());
        assertEquals("col1", table.getColumnName(0));
        assertEquals("col2", table.getColumnName(1));
        assertEquals("col3", table.getColumnName(2));
        assertEquals("col4", table.getColumnName(3));
        assertEquals(double.class, table.getColumnClass(0));
        assertEquals(int.class, table.getColumnClass(1));
        assertEquals(boolean.class, table.getColumnClass(2));
        assertEquals(String.class, table.getColumnClass(3));
        assertEquals(1.2, table.doubleValueAt(0, 0), 0.001);
        assertEquals(2, table.intValueAt(0, 1));
        assertEquals(true, table.booleanValueAt(0, 2));
        assertEquals("foo", table.stringValueAt(0, 3));
    }

    @Test public void testNetwork() {
        String json = "{" +
            "\"name\": \"network-name\"," +
            "\"version\": \"0.1\"," +
            "\"type\": \"network\"," +
            "\"metadata\": {" +
            "  \"species\": \"Halo world\"" +
            "}," +
            "\"gaggle-data\": {" +
            "  \"nodes\": [" +
            "      { \"node\": \"n1\", \"attributes\": {\"foo\":\"bar1\", \"dval1\": 2.5} }," +
            "      { \"node\": \"n2\", \"attributes\": {\"foo\":\"bar2\", \"dval2\": 3.5} }," +
            "      { \"node\": \"n3\", \"attributes\": {\"foo\":\"bar3\", \"dval1\": 5.5} }" +
            "  ]," +
            "  \"edges\": [" +
            "      { \"source\": \"n1\", \"target\": \"n2\", \"interaction\": \"i1\", \"directed\": true," +
            "        \"attributes\": { \"a1\": 1.3, \"a2\": \"bla\" } }," +
            "      { \"source\": \"n2\", \"target\": \"n3\", \"interaction\": \"i2\", \"directed\": true," +
            "        \"attributes\": { \"a2\": \"blu\" } }" +
            "  ]" +
            "}" +
            "}";
        
        Network network =
            (Network) (new JSONReader().createFromJSONString(json));
        assertEquals("network-name", network.getName());
        assertEquals("Halo world", network.getSpecies());
        assertEquals(3, network.nodeCount());
        String[] nodes = network.getNodes();
        assertTrue(contains(nodes, "n1", "n2", "n3"));
        assertEquals(3, network.getNodeAttributeNames().length);

        // node attributes
        Map<String, Object> nodeFooAttrs = network.getNodeAttributes("foo");
        Map<String, Object> nodeDval1Attrs = network.getNodeAttributes("dval1");
        Map<String, Object> nodeDval2Attrs = network.getNodeAttributes("dval2");
        assertEquals(3, nodeFooAttrs.size());
        assertEquals(2, nodeDval1Attrs.size());
        assertEquals(1, nodeDval2Attrs.size());

        // edges
        assertEquals(2, network.edgeCount());
        Map<String, Object> edgeA1Attributes = network.getEdgeAttributes("a1");
        Map<String, Object> edgeA2Attributes = network.getEdgeAttributes("a2");
        assertEquals(1, edgeA1Attributes.size());
        assertEquals(2, edgeA2Attributes.size());
    }

    private boolean contains(String[] arr, String... strings) {
        for (String str : strings) if (!contains(arr, str)) return false;
        return true;
    }
    private boolean contains(String[] arr, String str) {
        for (String elem : arr) if (str.equals(elem)) return true;
        return false;
    }

    private Single getNamedSingle(Tuple tuple, String name) {
        for (Single single : tuple.getSingleList()) {
            if (name.equals(single.getName())) return single;
        }
        return null;
    }
}
