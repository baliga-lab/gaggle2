package org.systemsbiology.gaggle;

import org.junit.Test;
import static org.junit.Assert.*;
import org.systemsbiology.gaggle.core.datatypes.Interaction;

public class InteractionTest {
    @Test public void testCreate() {
        Interaction i1 = new Interaction("source1", "target1", "itype1", true);
        assertEquals("source1", i1.getSource());
        assertEquals("target1", i1.getTarget());
        assertEquals("itype1", i1.getType());
        assertTrue(i1.isDirected());
        assertEquals("source1 (itype1) target1", i1.toString());

        Interaction i2 = new Interaction("source2", "target2", "itype2");
        assertEquals("source2", i2.getSource());
        assertEquals("target2", i2.getTarget());
        assertEquals("itype2", i2.getType());
        assertFalse(i2.isDirected());
        assertEquals("source2 (itype2) target2", i2.toString());
    }

    @Test public void testEquals() {
        Interaction i1 = new Interaction("source1", "target1", "itype1", true);
        Interaction i2 = new Interaction("source2", "target2", "itype2");
        Interaction i3 = new Interaction("source1", "target1", "itype1");
        assertTrue(i1.equals(i1));
        assertTrue(i1.equals(i3));
        assertFalse(i1.equals(i2));
    }
}