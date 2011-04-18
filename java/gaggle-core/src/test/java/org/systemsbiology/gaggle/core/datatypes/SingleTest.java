package org.systemsbiology.gaggle.core.datatypes;

import org.junit.Test;
import static org.junit.Assert.*;

public class SingleTest {
    @Test public void testCreateEmpty() {
        Single single = new Single();
        assertNull("name of empty Single should be null", single.getName());
        assertNull("value of empty Single should be null", single.getValue());        
        assertEquals("(null)", single.toString());
    }
    
    @Test public void testCreateOneArg() {
        Single single = new Single("value");
        assertNull("name of Single should be null", single.getName());
        assertEquals("value of Single should be 'value'", "value", single.getValue());        
        assertEquals("(value)", single.toString());
    }
    @Test public void testCreateTwoArgs() {
        Single single = new Single("name", "value");
        assertEquals("name of Single should be 'name'", "name", single.getName());
        assertEquals("value of Single should be 'value'", "value", single.getValue());
        assertEquals("(name = value)", single.toString());
    }
    @Test public void testSettersNoErrors() {
        Single single = new Single();
        single.setName("name");
        assertEquals("name", single.getName());
        single.setValue("value");
        assertEquals("value", single.getValue());
        single.setValue(1);
        assertEquals(1, single.getValue());
        single.setValue(1l);
        assertEquals(1l, single.getValue());
        single.setValue(1.0f);
        assertEquals(1.0f, single.getValue());
        single.setValue(1.0);
        assertEquals(1.0, single.getValue());
        single.setValue(true);
        assertEquals(true, single.getValue());
        Tuple tuple = new Tuple();
        single.setValue(tuple);
        assertEquals(tuple, single.getValue());
        GaggleTuple gaggleTuple = new GaggleTuple();
        single.setValue(gaggleTuple);
        assertEquals(gaggleTuple, single.getValue());
    }
    @Test public void testSettersInvalidType() {
        Single single = new Single();
        try {
            single.setValue(new java.math.BigDecimal(12));
            fail("setting value with invalid type should lead to exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("Value must be a String, " +
                         "Integer, Long, Float, " +
                         "Double, Boolean, or " +
                         "Tuple.", ex.getMessage());
        }
    }
}
