package org.systemsbiology.gaggle;

import org.junit.Test;
import static org.junit.Assert.*;
import org.systemsbiology.gaggle.core.datatypes.Network;
import org.systemsbiology.gaggle.core.datatypes.Interaction;
import org.systemsbiology.gaggle.core.datatypes.Tuple;

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
    }
}