package org.systemsbiology.gaggle.core.datatypes;

import java.util.List;
import java.util.ArrayList;

import org.junit.Test;
import static org.junit.Assert.*;

public class TupleTest {
    @Test public void testCreate() {
        Tuple tuple = new Tuple();
        assertNull(tuple.getName());
        assertEquals(0, tuple.getSingleList().size());
        assertEquals("empty tuple with name (no name)", tuple.toString());

        Tuple namedTuple = new Tuple("name");
        assertEquals("name", namedTuple.getName());
        assertEquals(0, namedTuple.getSingleList().size());
        assertEquals("empty tuple with name name", namedTuple.toString());

        List<Single> initList = new ArrayList<Single>();
        Tuple initTuple = new Tuple("name", initList);
        assertEquals("name", initTuple.getName());
        assertTrue(initList == initTuple.getSingleList());
        assertEquals("empty tuple with name name", initTuple.toString());
    }

    @Test public void testSetters() {
        Tuple tuple = new Tuple();
        tuple.setName("name");
        assertEquals("name", tuple.getName());
        List<Single> initList = new ArrayList<Single>();
        Single single = new Single("single", 4711);
        initList.add(single);
        tuple.setSingleList(initList);
        assertEquals(initList, tuple.getSingleList());
        assertEquals(single, tuple.getSingleAt(0));
        assertEquals("name: (single = 4711) ", tuple.toString());
    }
    @Test public void testAddSingle() {
        Tuple tuple = new Tuple();
        Single single = new Single("single", 4711);
        tuple.addSingle(single);
        assertEquals(1, tuple.getSingleList().size());
        assertEquals(single, tuple.getSingleAt(0));
        assertEquals("(no name): (single = 4711) ", tuple.toString());
    }
}
