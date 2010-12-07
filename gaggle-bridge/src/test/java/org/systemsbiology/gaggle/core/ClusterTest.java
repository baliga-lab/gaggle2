package org.systemsbiology.gaggle;

import org.junit.Test;
import static org.junit.Assert.*;
import org.systemsbiology.gaggle.core.datatypes.Cluster;
import org.systemsbiology.gaggle.core.datatypes.Tuple;

public class ClusterTest {
    @Test public void testCreateEmpty() {
        Cluster cluster = new Cluster();
        assertNull(cluster.getName());
        assertNull(cluster.getSpecies());
        assertNull(cluster.getRowNames());
        assertNull(cluster.getColumnNames());
        assertNull(cluster.getMetadata());
    }
    @Test public void testCreate3Args() {
        String[] rowNames = new String[] { "row1" };
        String[] colNames = new String[] { "col1" };
        Cluster cluster = new Cluster("species", rowNames, colNames);
        assertNull(cluster.getName());
        assertEquals("species", cluster.getSpecies());
        assertEquals(rowNames, cluster.getRowNames());
        assertEquals(colNames, cluster.getColumnNames());
        assertNull(cluster.getMetadata());
    }
    @Test public void testCreate4Args() {
        String[] rowNames = new String[] { "row1" };
        String[] colNames = new String[] { "col1" };
        Cluster cluster = new Cluster("name", "species", rowNames, colNames);
        assertEquals("name", cluster.getName());
        assertEquals("species", cluster.getSpecies());
        assertEquals(rowNames, cluster.getRowNames());
        assertEquals(colNames, cluster.getColumnNames());
        assertNull(cluster.getMetadata());
    }

    @Test public void testSetters() {
        String[] rowNames = new String[] { "row1" };
        String[] colNames = new String[] { "col1" };
        Tuple metadata = new Tuple();
        Cluster cluster = new Cluster();
        cluster.setName("name");
        cluster.setSpecies("species");
        cluster.setRowNames(rowNames);
        cluster.setColumnNames(colNames);
        cluster.setMetadata(metadata);
        assertEquals("name", cluster.getName());
        assertEquals("species", cluster.getSpecies());
        assertEquals(rowNames, cluster.getRowNames());
        assertEquals(colNames, cluster.getColumnNames());
        assertEquals(metadata, cluster.getMetadata());
    }
}