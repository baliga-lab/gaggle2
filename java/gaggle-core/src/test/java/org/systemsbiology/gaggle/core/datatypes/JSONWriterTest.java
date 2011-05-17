package org.systemsbiology.gaggle.core.datatypes;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.StringWriter;
import java.util.*;

/**
 * The test for JSONWriter relies on JSONReader to validate the results.
 * Not stricly independent unit testing, but helps to ignore formatting
 * issues.
 */
public class JSONWriterTest {
    private static final double EPS = 0.001;
    @Test public void testWriteNamelist() {
        Namelist namelist = new Namelist("nlname", "species",
                                         new String[] {"n1", "n2"} );
        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(namelist);
        Namelist namelist2 = (Namelist)
            new JSONReader().createFromJSONString(stringWriter.toString());
        assertEquals(namelist.getName(), namelist2.getName());
    }
    @Test public void testWriteNamelistWithMetadata() {
        Namelist namelist = new Namelist("nlname", "species",
                                         new String[] {"n1", "n2"} );
        Tuple metadata = new Tuple();
        metadata.addSingle(new Single("foo", "bar"));
        namelist.setMetadata(metadata);

        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(namelist);
        Namelist namelist2 = (Namelist)
            new JSONReader().createFromJSONString(stringWriter.toString());
        assertEquals(namelist.getName(), namelist2.getName());
        assertTrue(tupleContainsKey(namelist2.getMetadata(), "foo"));
    }
    private boolean tupleContainsKey(Tuple tuple, String key) {
        for (Single single : tuple.getSingleList()) {
            if (key.equals(single.getName())) return true;
        }
        return false;
    }

    @Test public void testWriteGaggleTuple() {
        GaggleTuple gaggleTuple = new GaggleTuple();
        gaggleTuple.setName("gtname");
        gaggleTuple.setSpecies("species");
        List<Single> singleList = new ArrayList<Single>();
        singleList.add(new Single("s1", "val1"));
        singleList.add(new Single("s2", 1));
        singleList.add(new Single("s3", true));
        Tuple data = new Tuple(null, singleList);
        gaggleTuple.setData(data);

        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(gaggleTuple);
        GaggleTuple gaggleTuple2 = (GaggleTuple)
            new JSONReader().createFromJSONString(stringWriter.toString());
        assertEquals(gaggleTuple.getName(), gaggleTuple2.getName());
    }

    @Test public void testWriteCluster() {
        Cluster cluster = new Cluster("cluster", "species",
                                      new String[] { "r1", "r2" },
                                      new String[] { "c1", "c2" });
        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(cluster);

        Cluster cluster2 = (Cluster)
            new JSONReader().createFromJSONString(stringWriter.toString());
        assertEquals(cluster.getName(), cluster2.getName());
        assertEquals(cluster.getSpecies(), cluster2.getSpecies());
        assertEquals(2, cluster2.getColumnNames().length);
        assertEquals("c1", cluster2.getColumnNames()[0]);
        assertEquals("c2", cluster2.getColumnNames()[1]);
        assertEquals(2, cluster2.getRowNames().length);
        assertEquals("r1", cluster2.getRowNames()[0]);
        assertEquals("r2", cluster2.getRowNames()[1]);
    }

    @Test public void testWriteDataMatrix() {
        DataMatrix matrix = new DataMatrix();
        matrix.setName("matrix");
        matrix.setSpecies("species");
        matrix.setRowTitles(new String[] { "r1", "r2" });
        matrix.setColumnTitles(new String[] { "c1", "c2" });
        double[][] values = { {1.10, 2.10}, {3.10, 4.10}};
        matrix.set(values);

        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(matrix);

        DataMatrix matrix2 = (DataMatrix)
            new JSONReader().createFromJSONString(stringWriter.toString());
        assertEquals(matrix.getName(), matrix2.getName());
    }

    @Test public void testWriteTable() {
        Table table = new Table("table", "species", null,
                                new Table.TableColumn[] {
                                    Table.createIntColumn("intcol", new int[] { 1, 2, 3})
                                });
        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(table);

        Table table2 = (Table)
            new JSONReader().createFromJSONString(stringWriter.toString());
        assertEquals(table.getName(), table2.getName());
    }

    @Test public void testWriteNetwork() {
        Network network = new Network();
        network.setName("network");
        network.setSpecies("species");
        Interaction i1 = new Interaction("n1", "n2", "i1", false);
        Interaction i2 = new Interaction("n1", "n3", "i2", true);
        network.add(i1);
        network.add(i2);
        network.addNodeAttribute("n1", "foo", 1);
        network.addEdgeAttribute(i1.toString(), "bar", "baz");

        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        writer.write(network);
        Network network2 = (Network)
            new JSONReader().createFromJSONString(stringWriter.toString());
        assertEquals(network.getName(), network2.getName());
        assertEquals(network.getSpecies(), network2.getSpecies());
        assertEquals(2, network.getInteractions().length);
        assertEquals(3, network.nodeCount());
        assertEquals("n1", network2.getNodes()[0]);
        assertEquals("n2", network2.getNodes()[2]);
        assertEquals("n3", network2.getNodes()[1]);

        assertEquals("n1", network2.getInteractions()[0].getSource());
        assertEquals("n2", network2.getInteractions()[0].getTarget());
        assertEquals("i1", network2.getInteractions()[0].getType());
        assertEquals(false, network2.getInteractions()[0].isDirected());

        assertEquals("n1", network2.getInteractions()[1].getSource());
        assertEquals("n3", network2.getInteractions()[1].getTarget());
        assertEquals("i2", network2.getInteractions()[1].getType());
        assertEquals(true, network2.getInteractions()[1].isDirected());
    }
}
