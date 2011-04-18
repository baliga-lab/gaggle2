package org.systemsbiology.gaggle.core.datatypes;

import org.junit.Test;
import static org.junit.Assert.*;

public class GaggleTupleTest {
    @Test public void testCreateEmpty() {
        GaggleTuple tuple = new GaggleTuple();
        assertNull(tuple.getName());
        assertNull(tuple.getSpecies());
        assertNotNull(tuple.getData());
        assertNotNull(tuple.getMetadata());
    }
    @Test public void testSetters() {
        GaggleTuple tuple = new GaggleTuple();
        tuple.setName("name");
        tuple.setSpecies("species");
        Tuple data = new Tuple();
        Tuple metadata = new Tuple();
        tuple.setData(data);
        tuple.setMetadata(metadata);

        assertEquals("name", tuple.getName());
        assertEquals("species", tuple.getSpecies());
        assertTrue(data == tuple.getData());
        assertTrue(metadata == tuple.getMetadata());
    }
}
