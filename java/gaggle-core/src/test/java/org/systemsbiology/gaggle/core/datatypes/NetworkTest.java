package org.systemsbiology.gaggle.core.datatypes;

import org.junit.Test;
import static org.junit.Assert.*;

public class NetworkTest {
    @Test public void testCreateEmpty() {
        Network network = new Network();
        assertNull(network.getName());
        assertEquals("unknown", network.getSpecies());
        assertNull(network.getMetadata());

        assertEquals(0, network.nodeCount());
        assertEquals(0, network.getNodes().length);
        assertEquals(0, network.getOrphanNodes().length);
        assertEquals(0, network.getOrphanNodeCount());
        assertEquals(0, network.getConnectedNodes().size());
        assertEquals(0, network.edgeCount());

        assertEquals(0, network.getNodeAttributeNames().length);
        assertEquals(0, network.getEdgeAttributeNames().length);

        assertEquals("", network.toString());
    }
    @Test public void testSimpleSetters() {
        Network network = new Network();
        Tuple metadata = new Tuple();
        network.setName("name");
        network.setSpecies("species");
        network.setMetadata(metadata);

        assertEquals("name", network.getName());
        assertEquals("species", network.getSpecies());
        assertEquals(metadata, network.getMetadata());
    }

    @Test public void testAddNode() {
        Network network = new Network();
        network.add("node");
        assertEquals(1, network.nodeCount());
        assertEquals(1, network.getNodes().length);
        assertEquals("node", network.getNodes()[0]);
        assertEquals(0, network.edgeCount());
        assertEquals(1, network.getOrphanNodeCount());
        assertEquals(1, network.getOrphanNodes().length);
        assertEquals("node", network.getOrphanNodes()[0]);
        assertEquals(0, network.getConnectedNodes().size());
        assertEquals("", network.toString());

        assertEquals(0, network.getNodeAttributeNames().length);
        assertEquals(0, network.getEdgeAttributeNames().length);
    }

    @Test public void testAddSimpleInteractionWithOrphan() {
        Network network = new Network();
        Interaction interaction = new Interaction("node1", "node2", "itype");
        network.add("node1");
        network.add("node2");
        network.add("orphan");
        network.add(new Interaction[] { interaction });

        assertEquals(3, network.nodeCount());
        assertEquals(3, network.getNodes().length);
        assertEquals(1, network.edgeCount());
        assertEquals(1, network.getOrphanNodeCount());
        assertEquals(1, network.getOrphanNodes().length);
        assertEquals("orphan", network.getOrphanNodes()[0]);
        assertEquals(2, network.getConnectedNodes().size());
        assertEquals(String.format("%s\n", interaction), network.toString());

        assertEquals(0, network.getNodeAttributeNames().length);
        assertEquals(0, network.getEdgeAttributeNames().length);
    }

    @Test public void testAddEdgeAttributes() {
        // note that edge attributes are independent of actually existing edges
        Network network = new Network();

        java.util.Vector<String> vectorVal = new java.util.Vector<String>();
        network.addEdgeAttribute("n1 (i) n2", "validAttrInt", 1);
        network.addEdgeAttribute("n1 (i) n2", "validAttrDouble", 1.0);
        network.addEdgeAttribute("n1 (i) n2", "validAttrString", "str1");
        network.addEdgeAttribute("n1 (i) n3", "validAttrString", "str2");
        // why a Vector ???
        network.addEdgeAttribute("n1 (i) n2", "validAttrVector", vectorVal);

        assertEquals(4, network.getEdgeAttributeNames().length);
        assertEquals(1, network.getEdgeAttributes("validAttrInt").size());
        assertEquals(1, network.getEdgeAttributes("validAttrInt").get("n1 (i) n2"));

        assertEquals(1, network.getEdgeAttributes("validAttrDouble").size());
        assertEquals(1.0, network.getEdgeAttributes("validAttrDouble").get("n1 (i) n2"));

        assertEquals(2, network.getEdgeAttributes("validAttrString").size());
        assertEquals("str1", network.getEdgeAttributes("validAttrString").get("n1 (i) n2"));
        assertEquals("str2", network.getEdgeAttributes("validAttrString").get("n1 (i) n3"));

        assertEquals(1, network.getEdgeAttributes("validAttrVector").size());
        assertEquals(vectorVal, network.getEdgeAttributes("validAttrVector").get("n1 (i) n2"));
    }

    @Test public void addIllegalEdgeAttributeType() {
        Network network = new Network();
        try {
            network.addEdgeAttribute("n1 (i) n2", "invalid", new StringBuilder());
            fail("adding invalid attribute value type should result in an exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("Value must be a String, Double, or Integer.", ex.getMessage());
        }
    }

    @Test public void addIllegalNodeAttributeType() {
        Network network = new Network();
        try {
            network.addNodeAttribute("n1", "invalid", new StringBuilder());
            fail("adding invalid attribute value type should result in an exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("Value must be a String, Double, or Integer.", ex.getMessage());
        }
    }

    @Test public void testAddNodeAttributes() {
        // note that node attributes are independent of actually existing nodes
        Network network = new Network();

        java.util.Vector<String> vectorVal = new java.util.Vector<String>();
        network.addNodeAttribute("n1", "validAttrInt", 1);
        network.addNodeAttribute("n1", "validAttrDouble", 1.0);
        network.addNodeAttribute("n1", "validAttrString", "str1");
        network.addNodeAttribute("n2", "validAttrString", "str2");
        // why a Vector ???
        network.addNodeAttribute("n1", "validAttrVector", vectorVal);

        assertEquals(4, network.getNodeAttributeNames().length);
        assertEquals(1, network.getNodeAttributes("validAttrInt").size());
        assertEquals(1, network.getNodeAttributes("validAttrInt").get("n1"));

        assertEquals(1, network.getNodeAttributes("validAttrDouble").size());
        assertEquals(1.0, network.getNodeAttributes("validAttrDouble").get("n1"));

        assertEquals(2, network.getNodeAttributes("validAttrString").size());
        assertEquals("str1", network.getNodeAttributes("validAttrString").get("n1"));
        assertEquals("str2", network.getNodeAttributes("validAttrString").get("n2"));

        assertEquals(1, network.getNodeAttributes("validAttrVector").size());
        assertEquals(vectorVal, network.getNodeAttributes("validAttrVector").get("n1"));
    }
}
