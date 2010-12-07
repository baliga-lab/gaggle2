package org.systemsbiology.gaggle;

import org.junit.Test;
import static org.junit.Assert.*;
import org.systemsbiology.gaggle.core.datatypes.Network;
import org.systemsbiology.gaggle.core.datatypes.Interaction;

public class NetworkTest {
    @Test public void testCreateEmpty() {
        Network network = new Network();
        assertNull(network.getName());
        assertEquals(0, network.nodeCount());
        assertEquals(0, network.getNodes().length);
        assertEquals(0, network.getConnectedNodes().size());
    }
}