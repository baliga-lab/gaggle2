package org.systemsbiology.gaggle;

import org.junit.Test;
import static org.junit.Assert.*;
import org.systemsbiology.gaggle.core.datatypes.Namelist;
import org.systemsbiology.gaggle.core.datatypes.Tuple;

public class NamelistTest {
    @Test public void testCreateEmpty() {
        Namelist list = new Namelist();
        assertNull(list.getName());
        assertNull(list.getSpecies());
        assertNull(list.getNames());
        assertNull(list.getMetadata());
    }
    @Test public void testCreate3Args() {
        Namelist list = new Namelist("name", "species", new String[] { "name1", "name2" });
        assertEquals("name", list.getName());
        assertEquals("species", list.getSpecies());
        assertEquals(2, list.getNames().length);
        assertNull(list.getMetadata());
    }
    @Test public void testCreate2Args() {
        Namelist list = new Namelist("species", new String[] { "name1", "name2" });
        assertNull(list.getName());
        assertEquals("species", list.getSpecies());
        assertEquals(2, list.getNames().length);
        assertNull(list.getMetadata());
    }
    @Test public void testSetters() {
        Namelist list = new Namelist();
        list.setName("name");
        assertEquals("name", list.getName());

        list.setSpecies("species");
        assertEquals("species", list.getSpecies());
        
        list.setNames(new String[] { "name1", "name2" });
        assertEquals(2, list.getNames().length);

        Tuple tuple = new Tuple();
        list.setMetadata(tuple);
        assertTrue(tuple == list.getMetadata());
    }
}