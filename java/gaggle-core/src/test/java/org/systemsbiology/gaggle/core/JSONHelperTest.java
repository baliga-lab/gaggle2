package org.systemsbiology.gaggle.core;

import org.junit.Test;
import static org.junit.Assert.*;
import org.systemsbiology.gaggle.core.datatypes.*;

public class JSONHelperTest {
    private static final double EPS = 0.001;

    @Test public void testNonGaggleJson() {
        String json = "{ \"foo\": \"bar\"}";
        try {
            new JSONHelper().createFromJsonString(json);
            fail("Providing a non-Gaggle JSON object should throw an exception");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
    }

    @Test public void testNamelist() {
        String json = "{\"gaggle-data\": {" +
            "\"name\": \"nl-name\"," +
            "\"metadata\": {" +
            "  \"species\": \"Halo world\"" +
            "}," +
            "\"namelist\": [\"name1\", \"name2\"]" +
            "}}";
        Namelist namelist = (Namelist) (new JSONHelper().createFromJsonString(json));
        assertEquals("nl-name", namelist.getName());
        assertEquals("Halo world", namelist.getSpecies());
        assertEquals(2, namelist.getNames().length);
        assertEquals("name1", namelist.getNames()[0]);
        assertEquals("name2", namelist.getNames()[1]);
    }

    @Test public void testTuple() {
        String json = "{\"gaggle-data\": {" +
            "\"name\": \"tuple-name\"," +
            "\"metadata\": {" +
            "  \"species\": \"Halo world\"" +
            "}," +
            "\"tuple\": {" +
            "  \"astring\": \"value\"," +
            "  \"adouble\": 1.23," +
            "  \"anint\": 1," +
            "  \"abool\": true," +
            "  \"anamelist\": { \"gaggle-data\": { \"name\": \"nl\", \"namelist\": [\"name1\"] }}" +
            "}" +
            "}}";
        GaggleTuple gaggleTuple =
            (GaggleTuple) (new JSONHelper().createFromJsonString(json));        
        assertEquals("tuple-name", gaggleTuple.getName());
        assertEquals("Halo world", gaggleTuple.getSpecies());
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
        String json = "{\"gaggle-data\": {" +
            "\"name\": \"cluster-name\"," +
            "\"metadata\": {" +
            "  \"species\": \"Halo world\"" +
            "}," +
            "\"tuple\": {" +
            "  \"type\": \"bicluster\"," +
            "  \"row-names\": { \"gaggle-data\": { \"name\": \"nl1\", \"namelist\": [\"r1\", \"r2\"] }}," +
            "  \"column-names\": { \"gaggle-data\": { \"name\": \"nl2\", \"namelist\": [\"c1\", \"c2\"] }}," +
            "}" +
            "}}";
        Cluster cluster =
            (Cluster) (new JSONHelper().createFromJsonString(json));
        assertEquals("cluster-name", cluster.getName());
        assertEquals("Halo world", cluster.getSpecies());
        assertEquals(2, cluster.getRowNames().length);
        assertEquals("r1", cluster.getRowNames()[0]);
        assertEquals("r2", cluster.getRowNames()[1]);
        assertEquals(2, cluster.getColumnNames().length);
        assertEquals("c1", cluster.getColumnNames()[0]);
        assertEquals("c2", cluster.getColumnNames()[1]);
    }

    @Test public void testMatrix() {
        String json = "{\"gaggle-data\": {" +
            "\"name\": \"matrix-name\"," +
            "\"metadata\": {" +
            "  \"species\": \"Halo world\"" +
            "}," +
            "\"matrix\": {" +
            "  \"row-names\":  [\"r1\", \"r2\"]," +
            "  \"columns\": [" +
            "      { \"name\": \"col1\", \"values\": [1.2, 3.4] }," +
            "      { \"name\": \"col2\", \"values\": [2.3, 4.5] }" +
            "  ]}" +
            "}}";
        DataMatrix matrix =
            (DataMatrix) (new JSONHelper().createFromJsonString(json));
        assertEquals("matrix-name", matrix.getName());
        assertEquals("Halo world", matrix.getSpecies());
        assertEquals(2, matrix.getRowTitles().length);
        assertEquals("r1", matrix.getRowTitles()[0]);
        assertEquals("r2", matrix.getRowTitles()[1]);
        assertEquals(2, matrix.getColumnTitles().length);
        assertEquals("col1", matrix.getColumnTitles()[0]);
        assertEquals("col2", matrix.getColumnTitles()[1]);
        assertEquals(2, matrix.getRowCount());
        assertEquals(2, matrix.getColumnCount());
        assertEquals(1.2, matrix.get(0, 0), EPS);
        assertEquals(2.3, matrix.get(0, 1), EPS);
        assertEquals(3.4, matrix.get(1, 0), EPS);
        assertEquals(4.5, matrix.get(1, 1), EPS);
    }

    private Single getNamedSingle(Tuple tuple, String name) {
        for (Single single : tuple.getSingleList()) {
            if (name.equals(single.getName())) return single;
        }
        return null;
    }
}