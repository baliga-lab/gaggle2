package org.systemsbiology.gaggle;

import org.junit.Test;
import static org.junit.Assert.*;
import org.systemsbiology.gaggle.core.datatypes.Single;

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
        assertEquals("name of Single should be 'name'", "name", single.getName());
        single.setValue("value");
        assertEquals("value of Single should be 'value'", "value", single.getValue());
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
